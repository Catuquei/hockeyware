package org.hockey.hockeyware.client.features.module.modules.Combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.events.PacketEvent;
import org.hockey.hockeyware.client.events.player.RotationUpdateEvent;
import org.hockey.hockeyware.client.events.render.RenderRotationsEvent;
import org.hockey.hockeyware.client.features.module.Category;
import org.hockey.hockeyware.client.features.module.Module;
import org.hockey.hockeyware.client.manager.InventoryManager;
import org.hockey.hockeyware.client.manager.TickManager;
import org.hockey.hockeyware.client.mixin.mixins.accessor.IEntityLivingBase;
import org.hockey.hockeyware.client.mixin.mixins.accessor.IEntityPlayerSP;
import org.hockey.hockeyware.client.setting.Setting;
import org.hockey.hockeyware.client.util.Timer;
import org.hockey.hockeyware.client.util.math.AngleUtil;
import org.hockey.hockeyware.client.util.player.*;
import org.hockey.hockeyware.client.util.player.Rotation.Rotate;
import org.hockey.hockeyware.client.util.world.EntityUtil;

import java.util.ArrayList;
import java.util.TreeMap;

public class Aura extends Module {
    public static Aura INSTANCE;

    public Aura() {
        super("Aura", "Allows You To Automatically Attack Entities Near You", Category.Combat);
        INSTANCE = this;
    }

    public static Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.PACKET);

    public static Setting<Boolean> yawStep = new Setting<>("YawStep", true, () -> !rotate.getValue().equals(Rotate.NONE));

    public static Setting<Double> yawStepThreshold = new Setting<>("YawStepThreshold", 180.0, 20.0, 180.0, () -> !rotate.getValue().equals(Rotate.NONE) && yawStep.getValue());

    public static Setting<Boolean> stopSprint = new Setting<>("StopSprint", false);

    public static Setting<Boolean> swing = new Setting<>("Swing", true);

    public static Setting<Raytrace> raytrace = new Setting<>("Raytrace", Raytrace.None);

    public static Setting<Boolean> attackDelay = new Setting<>("AttackDelayOverride", false);

    public static Setting<Double> attackSpeed = new Setting<>("AttackSpeed", 20.0, 1.0, 20.0, () -> !attackDelay.getValue());

    public static Setting<Double> switchDelay = new Setting<>("SwitchDelay", 0.0, 0.0, 10.0);

    public static Setting<TickManager.TPS> tps = new Setting<>("TPS", TickManager.TPS.None);

    public static Setting<Integer> range = new Setting<>("Range", 5, 1, 6);

    public static Setting<Double> wallsRange = new Setting<>("WallsRange", 0.0, 3.5, 6.0, () -> !raytrace.getValue().equals(Raytrace.None));

    public static Setting<Weapon> weapon = new Setting<>("Weapon", Weapon.Sword);

    public static Setting<Boolean> weaponOnly = new Setting<>("OnlyWeapon", true);

    public static Setting<InventoryManager.Switch> autoSwitch = new Setting<>("Switch", InventoryManager.Switch.Normal);

    public static Setting<Boolean> autoBlock = new Setting<>("AutoBlock", false);


    public static Setting<Target> target = new Setting<>("Target", Target.Closest);

    public static Setting<Boolean> targetPlayers = new Setting<>("TargetPlayers", true);

    public static Setting<Boolean> targetPassives = new Setting<>("TargetPassives", false);

    public static Setting<Boolean> targetNeutrals = new Setting<>("TargetNeutrals", false);

    public static Setting<Boolean> targetHostiles = new Setting<>("TargetHostiles", false);


    public static Setting<Boolean> render = new Setting<>("Render", true);


    // target that we are attacking
    private Entity attackTarget;


    // timer for attack delays
    private final Timer attackTimer = new Timer();

    // switch timers
    private final Timer switchTimer = new Timer();
    private final Timer autoSwitchTimer = new Timer();

    // vector that holds the angle we are looking at
    private Vec3d angleVector;

    // rotation angels
    private Rotation rotateAngles;

    // ticks to pause the process
    private int rotateTicks;

    @Override
    public void onUpdate() {

        // should not function when the ca is active
        if (AutoCrystal.INSTANCE.isActive()) {
            return;
        }

        // search ideal targets
        attackTarget = getTarget();

        // we are cleared to process our calculations
        if (rotateTicks <= 0) {

            // we found a target to attack
            if (attackTarget != null) {

                // we have waited the proper time ???
                boolean delayed;

                // should delay attack
                if (!attackDelay.getValue()) {

                    // ticks to adjust (based on server's TPS)
                    float adjustTicks = 20 - HockeyWare.INSTANCE.getTickManager.getTPS(tps.getValue());

                    // cooldown between attacks
                    float cooldown = mc.player.getCooledAttackStrength(adjustTicks);

                    // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                    long swapDelay = switchDelay.getValue().longValue() * 25L;

                    // we have waited the proper time ???
                    delayed = cooldown >= 1 && switchTimer.passedTime(swapDelay, Timer.Format.MILLISECONDS);
                }

                // custom delays (based on millis instead of vanilla attack delay)
                else {

                    // calculate if we have passed delays
                    // attack delay based on attack speeds
                    long attackDelay = (long) ((attackSpeed.getMax().doubleValue() - attackSpeed.getValue()) * 50);

                    // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                    long swapDelay = switchDelay.getValue().longValue() * 25L;

                    // custom delay
                    delayed = attackTimer.passedTime(attackDelay, Timer.Format.MILLISECONDS) && switchTimer.passedTime(swapDelay, Timer.Format.MILLISECONDS);
                }

                // check if we have passed the place time
                if (delayed) {

                    // face the target
                    angleVector = attackTarget.getPositionVector();

                    // attack the target
                    if (attackTarget(attackTarget)) {

                        // clear
                        attackTimer.resetTime();
                    }
                }
            }
        }

        else {
            rotateTicks--;
        }
    }

//    @Override
//    public void onRender3D() {
//
//        if (render.getValue()) {
//
//            // render a visual around the target
//            if (isActive()) {
//
//                // circle (anim based on sin wave)
//                RenderUtil.drawCircle(new RenderBuilder()
//                        .setup()
//                        .line(1.5F)
//                        .depth(true)
//                        .blend()
//                        .texture(), InterpolationUtil.getInterpolatedPosition(attackTarget, 1), attackTarget.width, attackTarget.height * (0.5 * (Math.sin((mc.player.ticksExisted * 3.5) * (Math.PI / 180)) + 1)), ColorUtil.getPrimaryColor());
//            }
//        }
//    }

    @Override
    public void onDisable() {
        super.onDisable();

        // clear lists and reset variables
        attackTarget = null;
        angleVector = null;
        rotateAngles = null;
    }

    public boolean isActive() {
        return isOn() && attackTarget != null && (isHoldingWeapon() || !weaponOnly.getValue()) && !AutoCrystal.INSTANCE.isActive();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {

        // packet for switching held item
        if (event.getPacket() instanceof CPacketHeldItemChange) {

            // reset our switch time, we just switched
            switchTimer.resetTime();

            // pause switch if item we switched to is not a crystal
            autoSwitchTimer.resetTime();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRotationUpdate(RotationUpdateEvent event) {

        // rotate
        if (!rotate.getValue().equals(Rotate.NONE)) {

            // manipulate packets if process are trying to complete
            if (isActive()) {

                // rotate only if we have an interaction vector to rotate to
                if (angleVector != null) {

                    // cancel the existing rotations, we'll send our own
                    event.setCanceled(true);

                    // yaw and pitch to the angle vector
                    rotateAngles = AngleUtil.calculateAngles(angleVector);

                    // rotation that we have serverside
                    Rotation serverRotation = HockeyWare.INSTANCE.rotationManager.getServerRotation();

                    // wrapped yaw value
                    float yaw = MathHelper.wrapDegrees(serverRotation.getYaw());

                    // difference between current and upcoming rotation
                    float angleDifference = rotateAngles.getYaw() - yaw;

                    // should never be over 180 since the angles are at max 180 and if it's greater than 180 this means we'll be doing a less than ideal turn
                    // (i.e current = 180, required = -180 -> the turn will be 360 degrees instead of just no turn since 180 and -180 are equivalent)
                    // at worst scenario, current = 90, required = -90 creates a turn of 180 degrees, so this will be our max
                    if (Math.abs(angleDifference) > 180) {

                        // adjust yaw
                        float adjust = angleDifference > 0 ? -360 : 360;
                        angleDifference += adjust;
                    }

                    // use absolute angle diff
                    // rotating too fast
                    if (Math.abs(angleDifference) > yawStepThreshold.getValue()) {

                        // check if we need to yaw step
                        if (yawStep.getValue()) {

                            // ideal rotation direction
                            int rotationDirection = angleDifference > 0 ? 1 : -1;

                            // add max angle
                            yaw += yawStepThreshold.getValue() * rotationDirection;

                            // update rotation
                            rotateAngles = new Rotation(yaw, rotateAngles.getPitch());

                            // update player rotations
                            if (rotate.getValue().equals(Rotate.CLIENT)) {
                                mc.player.rotationYaw = rotateAngles.getYaw();
                                mc.player.rotationYawHead = rotateAngles.getYaw();
                                mc.player.rotationPitch = rotateAngles.getPitch();
                            }

                            // add our rotation to our client rotations, AutoCrystal has priority over all other rotations
                            HockeyWare.INSTANCE.rotationManager.setRotation(rotateAngles);

                            // we need to wait till we reach our rotation
                            rotateTicks++;
                        }
                    }

                    else {

                        // update player rotations
                        if (rotate.getValue().equals(Rotate.CLIENT)) {
                            mc.player.rotationYaw = rotateAngles.getYaw();
                            mc.player.rotationYawHead = rotateAngles.getYaw();
                            mc.player.rotationPitch = rotateAngles.getPitch();
                        }

                        // add our rotation to our client rotations, AutoCrystal has priority over all other rotations
                        HockeyWare.INSTANCE.rotationManager.setRotation(rotateAngles);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderRotations(RenderRotationsEvent event) {

        // packet rotations
        if (rotate.getValue().equals(Rotate.PACKET)) {

            // render angles if rotating
            if (isActive()) {

                // rotate only if we have an interaction vector to rotate to
                if (rotateAngles != null) {

                    // cancel the model rendering for rotations, we'll set it to our values
                    event.setCanceled(true);

                    // set our model angles; visual
                    event.setYaw(rotateAngles.getYaw());
                    event.setPitch(rotateAngles.getPitch());
                }
            }
        }
    }

    /**
     * Gets the attack target
     * @return The target to attack for the given tick
     */
    public Entity getTarget() {

        /*
         * Map of valid targets
         * Sorted by natural ordering of keys
         * Using tree map allows time complexity of O(logN)
         */
        TreeMap<Double, Entity> validTargets = new TreeMap<>();

        // iterate all entities in the world
        for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {

            // make sure the entity actually exists
            if (entity == null || entity.equals(mc.player) || entity.getEntityId() < 0 || EnemyUtil.isDead(entity) || HockeyWare.INSTANCE.friendManager.isFriendByName(entity.getName())) {
                continue;
            }

            // ignore crystals, they can't be targets (attack crystals is delegated to the AutoCrystal)
            if (entity instanceof EntityEnderCrystal) {
                continue;
            }

            // don't attack our riding entity
            if (entity.isBeingRidden() && entity.getPassengers().contains(mc.player)) {
                continue;
            }

            // verify that the entity is a target
            if (entity instanceof EntityPlayer && !targetPlayers.getValue() || EntityUtil.isPassiveMob(entity) && !targetPassives.getValue() || EntityUtil.isNeutralMob(entity) && !targetNeutrals.getValue() || EntityUtil.isHostileMob(entity) && !targetHostiles.getValue()) {
                continue;
            }

            // distance to target
            double entityRange = mc.player.getDistance(entity);

            // check if the target is in range
            if (entityRange > range.getValue()) {
                continue;
            }

            // check if crystal is behind a wall
            boolean isNotVisible = RaytraceUtil.isNotVisible(entity, raytrace.getValue().getModifier(entity));

            // check if entity can be attacked through wall
            if (isNotVisible) {
                if (entityRange > wallsRange.getValue()) {
                    continue;
                }
            }

            // add to map
            validTargets.put(target.getValue().getModifier(entity), entity);
        }

        // make sure we actually have some valid targets
        if (!validTargets.isEmpty()) {

            // best target in the map, in a TreeMap this is the last entry
            Entity bestTarget = validTargets.lastEntry().getValue();

            // check if the entity hasn't died since the calculation
            if (!EnemyUtil.isDead(bestTarget)){

                // mark it as our current target
                return bestTarget;
            }
        }

        // we were not able to find any attack-able targets
        return null;
    }

    /**
     * Attacks the given entity
     * @param in the given entity
     */
    public boolean attackTarget(Entity in) {

        // make sure the target actually exists
        if (in == null || in.isDead) {
            return false;
        }

        // wait for rotations
        if (!rotate.getValue().equals(Rotate.NONE) && yawStep.getValue()) {

            // check if we are facing the position
            if (!isFacing(angleVector)) {
                return false;
            }
        }

        // pause switch to account for actions
        if (PlayerUtil.isEating() || PlayerUtil.isMending() || PlayerUtil.isMining()) {
            autoSwitchTimer.resetTime();
        }

        // switch to weapon if not holding weapon
        if (!isHoldingWeapon()) {

            // wait for switch pause
            if (autoSwitchTimer.passedTime(500, Timer.Format.MILLISECONDS)) {

                // switch
                HockeyWare.INSTANCE.getInventoryManager.switchToItem(weapon.getValue().getItem(), autoSwitch.getValue());
            }
        }

        // only attack if holding weapon
        if (weaponOnly.getValue()) {

            // if we are not holding a weapon we cannot attack
            if (!isHoldingWeapon()) {
                return false;
            }
        }

        // player shield state
        boolean shieldState = false;

        // stop blocking with a shield
        if (autoBlock.getValue()) {

            // update shield state
            shieldState = mc.player.getHeldItemOffhand().getItem() instanceof ItemShield && mc.player.isActiveItemStackBlocking();

            if (shieldState) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(mc.player), EnumFacing.getFacingFromVector((float) mc.player.posX, (float) mc.player.posY, (float) mc.player.posZ)));
            }
        }

        // player sprint state
        boolean sprintState = false;

        // on strict anticheat configs, you need to stop sprinting before attacking (keeping consistent with vanilla behavior)
        if (stopSprint.getValue()) {

            // update sprint state
            sprintState = mc.player.isSprinting() || ((IEntityPlayerSP) mc.player).getServerSprintState();

            // stop sprinting when attacking an entity
            if (sprintState) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
        }

        // send attack packet
        mc.player.connection.sendPacket(new CPacketUseEntity(in));
        mc.player.resetCooldown();

        // swing the player's arm
        if (swing.getValue()) {

            // held item stack
            ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);

            // check stack
            if (!stack.isEmpty()) {
                if (!stack.getItem().onEntitySwing(mc.player, stack)) {

                    // apply swing progress
                    if (!mc.player.isSwingInProgress || mc.player.swingProgressInt >= ((IEntityLivingBase) mc.player).hookGetArmSwingAnimationEnd() / 2 || mc.player.swingProgressInt < 0) {
                        mc.player.swingProgressInt = -1;
                        mc.player.isSwingInProgress = true;
                        mc.player.swingingHand = EnumHand.MAIN_HAND;

                        // send animation packet
                        if (mc.player.world instanceof WorldServer) {
                            ((WorldServer) mc.player.world).getEntityTracker().sendToTracking(mc.player, new SPacketAnimation(mc.player, 0));
                        }
                    }
                }
            }
        }

        // swing with packets
        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

        // reset shield state
        if (shieldState && isHoldingWeapon()) {
            mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
        }

        // reset sprint state
        if (sprintState) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }

        return true;
    }

    /**
     * Checks if the player is holding the required weapon
     * @return Whether the player is holding the required weapon
     */
    public boolean isHoldingWeapon() {

        // weapon item
        Class<? extends Item> weaponItem = weapon.getValue().getItem();

        // check if player is holding weapon
        return InventoryUtil.isHolding(weaponItem);
    }

    /**
     * Checks if the player is facing a certain vector
     * @return Whether the player is facing a certain vector
     */
    public boolean isFacing(Vec3d in) {

        // yaw and pitch that we've sent to the server
        Rotation serverRotation = HockeyWare.INSTANCE.rotationManager.getServerRotation();

        // target rotation
        Rotation facingRotation = AngleUtil.calculateAngles(in);

        // rotation diffs
        float yaw = Math.abs(serverRotation.getYaw() - facingRotation.getYaw());
        float pitch = Math.abs(serverRotation.getPitch() - facingRotation.getPitch());

        // both yaw and pitch must be nearly equal to facing rotation
        return yaw <= 0.1 & pitch <= 0.1;
    }

    public enum Raytrace {

        /**
         * Attack the entity at the eyes
         */
        Eyes((in) ->
                (double) in.getEyeHeight()
        ),

        /**
         * Attack the entity at the torso
         */
        Torso((in) ->
                in.height / 2D
        ),

        /**
         * Attack the entity at the feet
         */
        Feet((in) ->
                0
        ),

        /**
         * No attacks through walls
         */
        None(null);

        // modifier for the entity height
        private final EntityModifier modifier;

        Raytrace(EntityModifier modifier) {
            this.modifier = modifier;
        }

        /**
         * Gets the modifier for the trace
         * @param in The entity
         * @return The modified raytrace offset
         */
        public double getModifier(Entity in) {
            return modifier == null ? -1000 : modifier.modify(in);
        }
    }

    public enum Target {

        /**
         * Finds the closest entity to the player
         */
        Closest((in) ->
                -mc.player.getDistance(in)
        ),

        /**
         * Finds the entity with the lowest health
         */
        LowestHealth((in) ->
                -EnemyUtil.getHealth(in)
        ),

        /**
         * Finds the entity with the lowest armor durability
         */
        LowestArmor((in) ->
                -EnemyUtil.getArmor(in)
        );

        // modifier for the heuristic
        private final EntityModifier modifier;

        Target(EntityModifier modifier) {
            this.modifier = modifier;
        }

        /**
         * Gets the modifier for the heuristic
         * @param in The entity
         * @return The modified heuristic value
         */
        public double getModifier(Entity in) {
            return modifier == null ? -1000 : modifier.modify(in);
        }
    }

    @FunctionalInterface
    public interface EntityModifier {

        /**
         * Gets the modified value based on the entity's attribute
         * @param in The entity
         * @return The modified value
         */
        double modify(Entity in);
    }

    public enum Weapon {

        /**
         * Sword is the preferred weapon
         */
        Sword(ItemSword.class),

        /**
         * Axe is the preferred weapon
         */
        Axe(ItemAxe.class),

        /**
         * Pickaxe is the preferred weapon
         */
        Pickaxe(ItemPickaxe.class);

        // weapon item
        private final Class<? extends Item> item;

        Weapon(Class<? extends Item> item) {
            this.item = item;
        }

        /**
         * Gets the preferred item
         * @return The preferred item
         */
        public Class<? extends Item> getItem() {
            return item;
        }
    }
}