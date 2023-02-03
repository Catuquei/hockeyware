package org.hockey.hockeyware.client.features.module.modules.Movement;

import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.hockey.hockeyware.client.events.PacketEvent;
import org.hockey.hockeyware.client.events.network.DisconnectEvent;
import org.hockey.hockeyware.client.events.player.MotionEvent;
import org.hockey.hockeyware.client.features.module.Category;
import org.hockey.hockeyware.client.features.module.Module;
import org.hockey.hockeyware.client.mixin.mixins.accessor.INetworkManager;
import org.hockey.hockeyware.client.mixin.mixins.accessor.ISPacketPlayerPosLook;
import org.hockey.hockeyware.client.setting.Setting;
import org.hockey.hockeyware.client.util.player.MotionUtil;

import java.util.HashMap;
import java.util.Map;

public class PacketFly extends Module {
    public static PacketFly INSTANCE;

    // 0.001 less than the server is checking for
    private static final double CONCEAL = 0.0624;

    // funny number shit
    private static final double MOVE_FACTOR = 1.0 / StrictMath.sqrt(2.0);

    public PacketFly() {
        super("PacketFly", "Allows You To Fly With Packets", Category.Movement);
        INSTANCE = this;
    }

    public static Setting<Mode> mode = new Setting<>("Mode", Mode.FACTOR);

    public static Setting<Double> factor = new Setting<>("Factor", 1.2, 0.1, 5.0);

    public static Setting<Bounds> bounds = new Setting<>("Bounds", Bounds.DOWN);

    public static Setting<Phase> phase = new Setting<>("Phase", Phase.NCP);

    public static Setting<Boolean> conceal = new Setting<>("Conceal", false);

    public static Setting<Boolean> antiKick = new Setting<>("AntiKick", true);

    // a map of predictions
    private final Map<Integer, Vec3d> predictions = new HashMap<>();

    // the current teleport id to predict off of
    private int tpId = 0;

    // the time to slow down to prevent NCP kicks
    private int lagTime = 0;

    @Override
    public void onDisable() {
        super.onDisable();

        predictions.clear();
        tpId = 0;
        lagTime = 0;

        // do not no-clip
        mc.player.noClip = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMove(MotionEvent event) {

        // get our factor floored
        int loops = (int) Math.floor(factor.getValue());

        // if we are on mode FACTOR
        if (mode.getValue().equals(Mode.FACTOR)) {

            // add a factor based on the local players ticks existed
            if (mc.player.ticksExisted % 10.0 < 10.0 * (factor.getValue() - Math.floor(factor.getValue()))) {
                loops++;
            }
        }

        else {

            // else, we only loop once
            loops = 1;
        }

        // our move speed must be at conceal if we have recently flagged, are phasing, or the option has been set
        // else, we just use a base NCP speed without potion factoring
        double moveSpeed = (conceal.getValue() || --lagTime > 0 || isPhased()) ? CONCEAL : 0.2873;

        // our y velocity, defaults to 0.0
        double motionY = 0.0;

        // if we should anti-kick, this boolean comes into handy later
        boolean doAntiKick = false;

        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            motionY = CONCEAL;

            if (MotionUtil.isMoving()) {
                //loops = 1;

                moveSpeed *= MOVE_FACTOR;
                motionY *= MOVE_FACTOR;
            }
        }

        else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            motionY = -CONCEAL;

            if (MotionUtil.isMoving()) {
                //loops = 1;

                moveSpeed *= MOVE_FACTOR;
                motionY *= MOVE_FACTOR;
            }
        }

        else {
            doAntiKick = antiKick.getValue()
                    && mc.player.ticksExisted % 40 == 0
                    && !isPhased()
                    && !mc.world.collidesWithAnyBlock(mc.player.getEntityBoundingBox())
                    && !MotionUtil.isMoving();

            if (doAntiKick) {
                // send one loop
                loops = 1;

                // only go down -0.04. realistically, this could be lower because minecraft only
                // checks for -0.03125 in NetworkPlayServerHandler (or whatever the class is called)
                motionY = -0.04;
            }
        }

        // send our packets
        send(loops, moveSpeed, motionY, doAntiKick);

        // update this event motion values as we have modified them client-sided in the above method
        event.setX(mc.player.motionX);
        event.setY(mc.player.motionY);
        event.setZ(mc.player.motionZ);

        // set our no-clip if needed
        if (!phase.getValue().equals(Phase.NONE)) {
            mc.player.noClip = true;
        }

        // we'll override the vanilla movements
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketSend(PacketEvent.PacketSendEvent event) {

        // do not allow other packets sent externally from this module to interfere
        if (event.getPacket() instanceof CPacketPlayer) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {

        // if the server is lagging us back
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();

            // get the prediction for this server lagback
            Vec3d prediction = predictions.get(packet.getTeleportId());
            if (prediction != null) {

                // these have to match PERFECTLY - all of them
                if (prediction.x == packet.getX() && prediction.y == packet.getY() && prediction.z == packet.getZ()) {

                    // if we are on FACTOR or FAST, cancel
                    if (!mode.getValue().equals(Mode.SETBACK)) {
                        event.setCanceled(true);
                    }

                    // confirm teleport, as we have canceled this teleport from being handled
                    mc.player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
                    return;
                }
            }

            // remove annoying rotation-resets
            ((ISPacketPlayerPosLook) packet).setYaw(mc.player.rotationYaw);
            ((ISPacketPlayerPosLook) packet).setPitch(mc.player.rotationPitch);

            // accept this packet teleport
            mc.player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));

            // slow down
            lagTime = 10;
            tpId = packet.getTeleportId();
        }
    }

    private void send(int factor, double moveSpeed, double motionY, boolean antiKick) {

        // if for some reason we have a 0 factor, null out velocity
        if (factor == 0) {
            mc.player.setVelocity(0, 0, 0);
            return;
        }

        // get our motion values based off our speed
        double[] strafe = MotionUtil.getMoveSpeed(moveSpeed);

        for (int i = 1; i < factor + 1; ++i) {

            // multiply our move by the current factor
            double motionX = strafe[0] * i;
            double motionZ = strafe[1] * i;

            // our y velocity (motionY)
            double velY = motionY;

            if (!antiKick) {

                // factor in the current loop factor if we are not anti-kicking
                velY *= i;
            }

            // set our client-sided velocity
            mc.player.motionX = motionX;
            mc.player.motionY = velY;
            mc.player.motionZ = motionZ;

            // our position vector
            Vec3d posVec = mc.player.getPositionVector();

            // our absolute movement vector
            Vec3d moveVec = posVec.add(motionX, velY, motionZ);

            // send our movement vector
            send(moveVec);

            // send our bounds packet to abuse the minecraft exploit (thanks mojang)
            send(bounds.getValue().modify(posVec));

            // if we are on modes FACTOR or FAST, add a prediction
            if (!mode.getValue().equals(Mode.SETBACK)) {

                // increment after putting this into its map
                predictions.put(++tpId, moveVec);

                // send our confirm teleport
                mc.player.connection.sendPacket(new CPacketConfirmTeleport(tpId));
            }
        }
    }

    /**
     * Sends a position packet based off of a vector
     * @param vec to send
     */
    private void send(Vec3d vec) {

        // send this packet silently, so we do not have to keep these in memory
        ((INetworkManager) mc.player.connection.getNetworkManager()).hookDispatchPacket(new CPacketPlayer.Position(vec.x, vec.y, vec.z, true), null);
    }

    /**
     * Tells us if the local player is phased
     * @return if the local player is phased
     */
    private boolean isPhased() {

        // simple check to see if our local player hitbox is intersecting with a block
        return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625, -0.0625, -0.0625)).isEmpty();
    }

    public enum Mode {
        /**
         * Aka Desync, cancels server teleport packets if predictions are met via their teleport ids
         */
        FACTOR,

        /**
         * Factor packet fly, with a forced 1 factor (~20km/h constantly)
         */
        FAST,

        /**
         * Does not cancel server teleport packets
         */
        SETBACK
    }

    public enum Bounds {
        UP(1337.0),
        DOWN(-1337.0),
        MIN(512.0);

        private final double yOffset;

        Bounds(double yOffset) {
            this.yOffset = yOffset;
        }

        /**
         * Adds the bounds offset to an existing position vector
         * @param in the vector to modify
         * @return the modified vector
         */
        public Vec3d modify(Vec3d in) {
            return in.add(0, yOffset, 0);
        }
    }

    public enum Phase {
        /**
         * Does not attempt to phase easily
         */
        NONE,

        /**
         * Vanilla phase, sets noClip to true and does nothing else in terms of compatibility
         */
        VANILLA,

        /**
         * Vanilla phase, but compatible with NCP
         * This mode will multiply move speeds / y velocity to not flag NCP phase checks to be able to
         * phase with ease on NCP.
         * <p>
         * Cannot promise this will work on Updated NCP, as I have not found a packetfly that works well
         * on Updated NCP.
         */
        NCP
    }
}