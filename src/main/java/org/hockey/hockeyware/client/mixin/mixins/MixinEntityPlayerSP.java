package org.hockey.hockeyware.client.mixin.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.events.player.*;
import org.hockey.hockeyware.client.manager.RotationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.hockey.hockeyware.client.features.Globals.mc;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {

    @Shadow
    private double lastReportedPosX;
    @Shadow
    private double lastReportedPosY;
    @Shadow
    private double lastReportedPosZ;
    @Shadow
    private float lastReportedYaw;
    @Shadow
    private float lastReportedPitch;
    @Shadow
    private boolean serverSprintState;
    @Shadow
    private boolean serverSneakState;
    @Shadow
    private boolean prevOnGround;
    @Shadow
    private int positionUpdateTicks;
    @Shadow
    private boolean autoJumpEnabled;

    private boolean updateLock;

    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onUpdateMovingPlayer(CallbackInfo info) {

        // pre
        RotationUpdateEvent rotationUpdateEvent = new RotationUpdateEvent();
        HockeyWare.EVENT_BUS.post(rotationUpdateEvent);

        if (rotationUpdateEvent.isCanceled()) {

            // post
            MotionUpdateEvent motionUpdateEvent = new MotionUpdateEvent();
            HockeyWare.EVENT_BUS.post(motionUpdateEvent);

            // prevent vanilla packets from sending
            info.cancel();

            if (motionUpdateEvent.isCanceled()) {
                positionUpdateTicks++;

                boolean sprintUpdate = isSprinting();
                if (sprintUpdate != serverSprintState) {
                    if (sprintUpdate) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SPRINTING));
                    }

                    else {
                        mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SPRINTING));
                    }

                    serverSprintState = sprintUpdate;
                }

                boolean sneakUpdate = isSneaking();
                if (sneakUpdate != serverSneakState) {
                    if (sneakUpdate) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING));
                    }

                    else {
                        mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING));
                    }

                    serverSneakState = sneakUpdate;
                }

                if (isCurrentViewEntity()) {
                    boolean movementUpdate = StrictMath.pow(motionUpdateEvent.getX() - lastReportedPosX, 2) + StrictMath.pow(motionUpdateEvent.getY() - lastReportedPosY, 2) + StrictMath.pow(motionUpdateEvent.getZ() - lastReportedPosZ, 2) > 9.0E-4D || positionUpdateTicks >= 20;
                    boolean rotationUpdate = motionUpdateEvent.getYaw() - lastReportedYaw != 0.0D || motionUpdateEvent.getPitch() - lastReportedPitch != 0.0D;

                    if (isRiding()) {
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(motionX, -999.0D, motionZ, motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), motionUpdateEvent.getOnGround()));
                        movementUpdate = false;
                    }

                    else if (movementUpdate && rotationUpdate) {
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(motionUpdateEvent.getX(), motionUpdateEvent.getY(), motionUpdateEvent.getZ(), motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), motionUpdateEvent.getOnGround()));
                    }

                    else if (movementUpdate) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(motionUpdateEvent.getX(), motionUpdateEvent.getY(), motionUpdateEvent.getZ(), motionUpdateEvent.getOnGround()));
                    }

                    else if (rotationUpdate) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(motionUpdateEvent.getYaw(), motionUpdateEvent.getPitch(), motionUpdateEvent.getOnGround()));
                    }

                    else if (prevOnGround != motionUpdateEvent.getOnGround()) {
                        mc.player.connection.sendPacket(new CPacketPlayer(motionUpdateEvent.getOnGround()));
                    }

                    if (movementUpdate) {
                        lastReportedPosX = motionUpdateEvent.getX();
                        lastReportedPosY = motionUpdateEvent.getY();
                        lastReportedPosZ = motionUpdateEvent.getZ();
                        positionUpdateTicks = 0;
                    }

                    if (rotationUpdate) {
                        lastReportedYaw = motionUpdateEvent.getYaw();
                        lastReportedPitch = motionUpdateEvent.getPitch();
                    }

                    prevOnGround = motionUpdateEvent.getOnGround();
                    autoJumpEnabled = mc.gameSettings.autoJump;
                }
            }
        }
    }

    @Inject(method = {"pushOutOfBlocks"}, at = {@At(value = "HEAD")}, cancellable = true)
    private void pushOutOfBlocksHook(double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
        PushEvent event = new PushEvent(1);
        MinecraftForge.EVENT_BUS.post((Event) event);
        if (event.isCanceled()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "net/minecraft/client/entity/EntityPlayerSP.onUpdateWalkingPlayer()V", ordinal = 0, shift = At.Shift.AFTER))
    public void onUpdateMovingPlayerPost(CallbackInfo info) {

        // event is locked
        if (updateLock) {
            return;
        }

        UpdateWalkingPlayerEvent updateWalkingPlayerEvent = new UpdateWalkingPlayerEvent();
        HockeyWare.EVENT_BUS.post(updateWalkingPlayerEvent);

        // rots
        float yaw = HockeyWare.INSTANCE.rotationManager.getRotation().isValid() ? HockeyWare.INSTANCE.rotationManager.getRotation().getYaw() : mc.player.rotationYaw;
        float pitch = HockeyWare.INSTANCE.rotationManager.getRotation().isValid() ? HockeyWare.INSTANCE.rotationManager.getRotation().getPitch() : mc.player.rotationPitch;

        if (updateWalkingPlayerEvent.isCanceled()) {

            // idk
            if (updateWalkingPlayerEvent.getIterations() > 0) {

                // run
                for (int i = 0; i < updateWalkingPlayerEvent.getIterations(); i++) {

                    // lock
                    updateLock = true;

                    onUpdate();

                    // unlock
                    updateLock = false;

                    boolean sprintUpdate = isSprinting();
                    if (sprintUpdate != serverSprintState) {
                        if (sprintUpdate) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SPRINTING));
                        }

                        else {
                            mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SPRINTING));
                        }

                        serverSprintState = sprintUpdate;
                    }

                    boolean sneakUpdate = isSneaking();
                    if (sneakUpdate != serverSneakState) {
                        if (sneakUpdate) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING));
                        }

                        else {
                            mc.player.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING));
                        }

                        serverSneakState = sneakUpdate;
                    }

                    if (isCurrentViewEntity()) {
                        boolean movementUpdate = StrictMath.pow(mc.player.posX - lastReportedPosX, 2) + StrictMath.pow(mc.player.posY - lastReportedPosY, 2) + StrictMath.pow(mc.player.posZ - lastReportedPosZ, 2) > 9.0E-4D || positionUpdateTicks >= 20;
                        boolean rotationUpdate = yaw - lastReportedYaw != 0.0D || pitch - lastReportedPitch != 0.0D;

                        if (isRiding()) {
                            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(motionX, -999.0D, motionZ, yaw, pitch, mc.player.onGround));
                            movementUpdate = false;
                        }

                        else if (movementUpdate && rotationUpdate) {
                            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY, mc.player.posZ, yaw, pitch, mc.player.onGround));
                        }

                        else if (movementUpdate) {
                            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.onGround));
                        }

                        else if (rotationUpdate) {
                            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, mc.player.onGround));
                        }

                        else if (prevOnGround != mc.player.onGround) {
                            mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
                        }

                        if (movementUpdate) {
                            lastReportedPosX = mc.player.posX;
                            lastReportedPosY = mc.player.posY;
                            lastReportedPosZ = mc.player.posZ;
                            positionUpdateTicks = 0;
                        }

                        if (rotationUpdate) {
                            lastReportedYaw = yaw;
                            lastReportedPitch = pitch;
                        }

                        prevOnGround = mc.player.onGround;
                        autoJumpEnabled = mc.gameSettings.autoJump;
                    }
                }
            }
        }
    }

    @Shadow
    protected abstract boolean isCurrentViewEntity();

    @Shadow public MovementInput movementInput;

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void move(AbstractClientPlayer player, MoverType type, double x, double y, double z) {
        MotionEvent motionEvent = new MotionEvent(type, x, y, z);
        HockeyWare.EVENT_BUS.post(motionEvent);

        if (motionEvent.isCanceled()) {
            super.move(motionEvent.getType(), motionEvent.getX(), motionEvent.getY(), motionEvent.getZ());
        }

        else {
            super.move(type, x, y, z);
        }
    }

    @Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V"), cancellable = true)
    private void onMoveStateUpdate(CallbackInfo ci) {
        PlayerUpdateMoveEvent event = new PlayerUpdateMoveEvent(movementInput);
        HockeyWare.EVENT_BUS.post(event);
        if (event.isCanceled()) ci.cancel();
    }
}