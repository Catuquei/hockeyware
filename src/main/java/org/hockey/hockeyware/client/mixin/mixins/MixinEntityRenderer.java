package org.hockey.hockeyware.client.mixin.mixins;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.events.render.HurtCameraEvent;
import org.hockey.hockeyware.client.events.render.RenderFogEvent;
import org.hockey.hockeyware.client.events.render.RenderItemActivationEvent;
import org.hockey.hockeyware.client.features.module.modules.Render.Weather;
import org.hockey.hockeyware.client.mixin.mixins.accessor.IEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer implements IEntityRenderer {
    @Shadow
    private ItemStack itemActivationItem;

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderRainSnow(F)V"))
    public void weatherHook(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (HockeyWare.INSTANCE.moduleManager.getModuleByClass(Weather.class).isToggled(true)) {
            ((Weather) HockeyWare.INSTANCE.moduleManager.getModuleByClass(Weather.class)).render(partialTicks);
        }
    }

    @Inject(method = "renderItemActivation", at = @At("HEAD"), cancellable = true)
    public void onRenderItemActivation(CallbackInfo info) {
        RenderItemActivationEvent renderItemActivationEvent = new RenderItemActivationEvent();
        HockeyWare.EVENT_BUS.post(renderItemActivationEvent);

        if (itemActivationItem != null && itemActivationItem.getItem().equals(Items.TOTEM_OF_UNDYING)) {
            if (renderItemActivationEvent.isCanceled()) {
                info.cancel();
            }
        }
    }

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    public void onSetupFog(int startCoords, float partialTicks, CallbackInfo info) {
        RenderFogEvent renderFogEvent = new RenderFogEvent();
        HockeyWare.EVENT_BUS.post(renderFogEvent);

        if (renderFogEvent.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float ticks, CallbackInfo info) {
        HurtCameraEvent hurtCameraEvent = new HurtCameraEvent();
        HockeyWare.EVENT_BUS.post(hurtCameraEvent);

        if (hurtCameraEvent.isCanceled()) {
            info.cancel();
        }
    }
}