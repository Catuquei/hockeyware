package org.hockey.hockeyware.client.util.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.events.player.MoveEvent;
import org.hockey.hockeyware.client.util.Globals;
import org.hockey.hockeyware.client.util.world.BlockUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class PlayerUtil implements Globals {
    public static UUID getUUIDFromName(final String name) {
        try {
            final lookUpUUID process = new lookUpUUID(name);
            final Thread thread = new Thread(process);
            thread.start();
            thread.join();
            return process.getUUID();
        } catch (Exception e) {
            return null;
        }
    }

    public static double getDistance(EntityPlayer player, BlockPos pos) {
        return player.getDistance(pos.getX(), pos.getY(), pos.getZ());
    }

    public static double getDistanceSq(EntityPlayer player, BlockPos pos) {
        return player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
    }

    public static float getHealth(Entity entity) {
        // player
        if (entity instanceof EntityPlayer) {
            return ((EntityPlayer) entity).getHealth() + ((EntityPlayer) entity).getAbsorptionAmount();
        }

        // living
        else if (entity instanceof EntityLivingBase) {
            return ((EntityLivingBase) entity).getHealth() + ((EntityLivingBase) entity).getAbsorptionAmount();
        }

        return 0;
    }

    public static double getDistanceToCenter(EntityPlayer player, BlockPos in) {

        double dX = in.getX() + 0.5 - player.posX;
        double dY = in.getY() + 0.5 - player.posY;
        double dZ = in.getZ() + 0.5 - player.posZ;

        return StrictMath.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
    }

    public static double[] forward(final double speed) {
        float forward = mc.player.movementInput.moveForward;
        float side = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;
        return new double[]{posX, posZ};
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = .2873;
        if (mc.player != null && mc.player.isPotionActive(Objects.requireNonNull(Potion.getPotionById(1)))) {
            final int amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(Objects.requireNonNull(Potion.getPotionById(1)))).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }

    public static void setVanilaSpeed(MoveEvent event, double speed) {
        float moveForward = mc.player.movementInput.moveForward;
        float moveStrafe = mc.player.movementInput.moveStrafe;
        float rotationYaw = mc.player.rotationYaw;

        if (moveForward == 0.0f && moveStrafe == 0.0f) {
            event.setX(0.0);
            event.setZ(0.0);

            return;
        } else if (moveForward != 0.0f) {
            if (moveStrafe >= 1.0f) {
                rotationYaw += moveForward > 0.0f ? -45.0f : 45.0f;
                moveStrafe = 0.0f;
            } else if (moveStrafe <= -1.0f) {
                rotationYaw += moveForward > 0.0f ? 45.0f : -45.0f;
                moveStrafe = 0.0f;
            }

            if (moveForward > 0.0f)
                moveForward = 1.0f;
            else if (moveForward < 0.0f)
                moveForward = -1.0f;
        }

        double motionX = Math.cos(Math.toRadians(rotationYaw + 90.0f));
        double motionZ = Math.sin(Math.toRadians(rotationYaw + 90.0f));

        double newX = moveForward * speed * motionX + moveStrafe * speed * motionZ;
        double newZ = moveForward * speed * motionZ - moveStrafe * speed * motionX;

        event.setX(newX);
        event.setZ(newZ);
    }

    public static boolean isOnStairs() {
        if (getNextPos() == null) return false;
        BlockPos air1 = new BlockPos(getNextPos().getX(), getNextPos().getY() + 1, getNextPos().getZ());
        BlockPos air2 = new BlockPos(getNextPos().getX(), getNextPos().getY() + 2, getNextPos().getZ());
        return mc.player.moveForward != 0 && getBlockHeight() <= 0 && mc.player != null && mc.world != null && BlockUtil.getBlock(getNextPos()).equals(Blocks.AIR) && BlockUtil.getBlock(air1).equals(Blocks.AIR) && BlockUtil.getBlock(air2).equals(Blocks.AIR);
    }

    public static boolean isInBurrow(EntityPlayer player) {
        return BlockUtil.getBlock(new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ))).equals(Blocks.OBSIDIAN) || BlockUtil.getBlock(new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ))).equals(Blocks.BEDROCK) || BlockUtil.getBlock(new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ))).equals(Blocks.WEB);
    }

    public static BlockPos nextPos;

    public static BlockPos getNextPos() {
        if (mc.player == null || mc.world == null) return null;
        String axisDirection = null;
        if (mc.player.getAdjustedHorizontalFacing().getAxisDirection().toString().equals("Towards positive")) {
            axisDirection = "+";
        } else if (mc.player.getAdjustedHorizontalFacing().getAxisDirection().toString().equals("Towards negative")) {
            axisDirection = "-";
        }
        String axisName = mc.player.getHorizontalFacing().getAxis().getName();

        if (axisName.equals("x") && Objects.equals(axisDirection, "+")) {
            //+x
            nextPos = new BlockPos(mc.player.posX + 1, mc.player.posY - 1, mc.player.posZ);
        }
        if (axisName.equals("x") && Objects.equals(axisDirection, "-")) {
            //-x
            nextPos = new BlockPos(mc.player.posX - 1, mc.player.posY - 1, mc.player.posZ);
        }
        if (axisName.equals("z") && Objects.equals(axisDirection, "+")) {
            //+z
            nextPos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ + 1);
        }
        if (axisName.equals("z") && Objects.equals(axisDirection, "-")) {
            //-z
            nextPos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ - 1);
        }
        return nextPos;
    }

    public static double getBlockHeight() {
        double max_y = -1;
        final AxisAlignedBB grow = mc.player.getEntityBoundingBox().offset(0, 0.05, 0).grow(0.05);
        if (!mc.world.getCollisionBoxes(mc.player, grow.offset(0, 2, 0)).isEmpty()) return 100;
        for (final AxisAlignedBB aabb : mc.world.getCollisionBoxes(mc.player, grow)) {
            if (aabb.maxY > max_y) {
                max_y = aabb.maxY;
            }
        }
        return max_y - mc.player.posY;
    }

    public static void setTimer(float speed) {
        mc.timer.tickLength = 50.0f / speed;
    }

    public static void resetTimer() {
        mc.timer.tickLength = 50;
    }

    public static Block isColliding(double posX, double posY, double posZ) {
        Block block = null;
        if (mc.player != null) {
            final AxisAlignedBB bb = mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(posX, posY, posZ) : mc.player.getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(posX, posY, posZ);
            int y = (int) bb.minY;
            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX) + 1; x++) {
                for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ) + 1; z++) {
                    block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                }
            }
        }
        return block;
    }

    public static boolean isInLiquid() {
        if (mc.player != null) {
            if (mc.player.fallDistance >= 3.0f) {
                return false;
            }
            boolean inLiquid = false;
            final AxisAlignedBB bb = mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().getEntityBoundingBox() : mc.player.getEntityBoundingBox();
            int y = (int) bb.minY;
            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX) + 1; x++) {
                for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ) + 1; z++) {
                    final Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (!(block instanceof BlockAir)) {
                        if (!(block instanceof BlockLiquid)) {
                            return false;
                        }
                        inLiquid = true;
                    }
                }
            }
            return inLiquid;
        }
        return false;
    }

    public static boolean isPlayerInRender(String player) {
        for (EntityPlayer player1 : mc.player.world.playerEntities) {
            if (player1.getName().equals(player)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMoving(EntityLivingBase entity) {
        return entity.moveForward != 0 || entity.moveStrafing != 0;
    }

    public static void setSpeed(final EntityLivingBase entity, final double speed) {
        double[] dir = forward(speed);
        entity.motionX = dir[0];
        entity.motionZ = dir[1];
    }

    public static String requestIDs(final String data) {
        try {
            final String query = "https://api.mojang.com/profiles/minecraft";
            final URL url = new URL(query);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            final OutputStream os = conn.getOutputStream();
            os.write(data.getBytes(StandardCharsets.UTF_8));
            os.close();
            final InputStream in = new BufferedInputStream(conn.getInputStream());
            final String res = convertStreamToString(in);
            in.close();
            conn.disconnect();
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        is.close();
        return sb.toString();
    }

    public static EntityPlayer findClosestTarget() {

        if (mc.world.playerEntities.isEmpty())
            return null;

        EntityPlayer closestTarget = null;

        for (final EntityPlayer target : mc.world.playerEntities) {
            if (target == mc.player || !target.isEntityAlive()) continue;

            if (HockeyWare.INSTANCE.friendManager.isFriendByName(target.getName())) continue;

            if (target.getHealth() <= 0.0f) continue;

            if (closestTarget != null)
                if (mc.player.getDistance(target) > mc.player.getDistance(closestTarget))
                    continue;

            closestTarget = target;
        }


        return closestTarget;
    }

    public static boolean isEating() {
        if (mc.player.isHandActive()) {
            return mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.EAT) || mc.player.getActiveItemStack().getItemUseAction().equals(EnumAction.DRINK);
        }

        return false;
    }

    public static boolean isMending() {
        return InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) && mc.gameSettings.keyBindUseItem.isKeyDown();
    }

    public static boolean isMining() {
        return InventoryUtil.isHolding(Items.DIAMOND_PICKAXE) && mc.playerController.getIsHittingBlock() && BlockUtil.canBreak(mc.objectMouseOver.getBlockPos()) && !mc.world.isAirBlock(mc.objectMouseOver.getBlockPos());
    }

    public static float getPlayerHealth(EntityPlayer player) {
        return player.getHealth() + player.getAbsorptionAmount();
    }

    public static BlockPos getPlayerPos(EntityPlayer player) {
        return new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
    }

    public static double getDistanceToPos(EntityPlayer player, BlockPos pos) {
        return player.getDistance(pos.getX(), pos.getY(), pos.getZ());
    }

    public static class lookUpUUID implements Runnable {
        private final String name;
        private volatile UUID uuid;

        public lookUpUUID(final String name) {
            this.name = name;
        }

        @Override
        public void run() {
            NetworkPlayerInfo profile;
            try {
                final ArrayList<NetworkPlayerInfo> infoMap = new ArrayList<NetworkPlayerInfo>(Objects.requireNonNull(mc.getConnection()).getPlayerInfoMap());
                profile = infoMap.stream().filter(networkPlayerInfo -> networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(this.name)).findFirst().orElse(null);
                assert profile != null;
                this.uuid = profile.getGameProfile().getId();
            } catch (Exception e2) {
                profile = null;
            }
            if (profile == null) {
                final String s = PlayerUtil.requestIDs("[\"" + this.name + "\"]");
                if (s == null || s.isEmpty()) {
                } else {
                    final JsonElement element = new JsonParser().parse(s);
                    if (element.getAsJsonArray().size() == 0) {
                    } else {
                        try {
                            final String id = element.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                            this.uuid = UUIDTypeAdapter.fromString(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        public UUID getUUID() {
            return this.uuid;
        }

        public String getName() {
            return this.name;
        }
    }
}
