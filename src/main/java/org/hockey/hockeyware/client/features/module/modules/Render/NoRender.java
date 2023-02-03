package org.hockey.hockeyware.client.features.module.modules.Render;

import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.hockey.hockeyware.client.events.PacketEvent;
import org.hockey.hockeyware.client.events.render.*;
import org.hockey.hockeyware.client.features.module.Category;
import org.hockey.hockeyware.client.features.module.Module;
import org.hockey.hockeyware.client.setting.Setting;
import org.hockey.hockeyware.client.util.player.PlayerUtil;

public class NoRender extends Module {
    public static NoRender INSTANCE;

    public NoRender() {
        super("NoRender", "Allows You To Not Render Certain Things",  Category.Render);
        INSTANCE = this;
    }



    public static Setting<Boolean> hurtCamera = new Setting<>("HurtCamera", true);


    public static final Setting<Boolean> explosions = new Setting<>("Explosions", true);

    public static Setting<Boolean> overlayFire = new Setting<>("FireOverlay", true);

    public static Setting<Boolean> overlayBoss = new Setting<>("BossOverlay", false);

    public static Setting<Boolean> overlayBlock = new Setting<>("BlockOverlay", true);

    public static Setting<Boolean> overlayLiquid = new Setting<>("LiquidOverlay", false);

    public static Setting<Boolean> armor = new Setting<>("Armor", false);

    public static Setting<Boolean> totemAnimation = new Setting<>("TotemAnimation", false);

    public static Setting<Boolean> fog = new Setting<>("Fog", false);

    public static Setting<Boolean> eating = new Setting<>("EatAnimation", false);


    public static Setting<Boolean> items = new Setting<>("Items", false);

    public static Setting<Boolean> barrier = new Setting<>("Barrier", false);

    public static Setting<Boolean> maps = new Setting<>("Maps", false);


    public static Setting<Boolean> witherSkull = new Setting<>("WitherSkulls", false);

    @SubscribeEvent
    public void onRenderItem(RenderItemEvent event) {

        // prevent dropped items from rendering
        if (items.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event) {
        if (explosions.getValue() && event.getPacket() instanceof SPacketExplosion) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent event) {

            // cancels fire hud overlay
            if (event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.FIRE) && overlayFire.getValue()) {
                event.setCanceled(true);
            }

            // cancel water hud overlay
            if (event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.WATER) && overlayLiquid.getValue()) {
                event.setCanceled(true);
            }

            // cancel water block overlay
            if (event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.BLOCK) && overlayBlock.getValue()) {
                event.setCanceled(true);
            }
        }

    @SubscribeEvent
    public void onRenderBossOverlay(BossOverlayEvent event) {

        // cancel boss hud overlay
        event.setCanceled(overlayBoss.getValue());
    }

    @SubscribeEvent
    public void onRenderMap(RenderMapEvent event) {

        // cancels maps from rendering
        if (maps.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLayerArmor(LayerArmorEvent event) {

        // cancels armor rendering
        if (armor.getValue()) {
            event.setCanceled(true);

            // removes model rendering
            switch (event.getEntityEquipmentSlot()) {
                case HEAD:
                    event.getModelBiped().bipedHead.showModel = false;
                    event.getModelBiped().bipedHeadwear.showModel = false;
                    break;
                case CHEST:
                    event.getModelBiped().bipedBody.showModel = false;
                    event.getModelBiped().bipedRightArm.showModel = false;
                    event.getModelBiped().bipedLeftArm.showModel = false;
                    break;
                case LEGS:
                    event.getModelBiped().bipedBody.showModel = false;
                    event.getModelBiped().bipedRightLeg.showModel = false;
                    event.getModelBiped().bipedLeftLeg.showModel = false;
                    break;
                case FEET:
                    event.getModelBiped().bipedRightLeg.showModel = false;
                    event.getModelBiped().bipedLeftLeg.showModel = false;
                    break;
                case MAINHAND:
                case OFFHAND:
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onRenderFog(RenderFogEvent event) {
        // prevent fog from rendering
        if (fog.getValue()) {
            event.setCanceled(true);
        }
    }{}

    @SubscribeEvent
    public void onHurtCamera(HurtCameraEvent event) {

        // cancels the hurt camera effect
        if (hurtCamera.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderWitherSkull(RenderWitherSkullEvent event) {

        // cancels wither skulls from rendering
        if (witherSkull.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderParticle(RenderParticleEvent event) {

        // cancels barrier particles from rendering
        if (barrier.getValue() && event.getParticleType().equals(EnumParticleTypes.BARRIER)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEatingRender(RenderEatingEvent event) {

        // Prevent the eating animation
        if (eating.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderItemActivation(RenderItemActivationEvent event) {

        // prevent the totem pop animation
        if (totemAnimation.getValue()) {
            event.setCanceled(true);
        }
    }
}