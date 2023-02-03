package org.hockey.hockeyware.client.mixin.mixins;

import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.entity.item.EntityItem;
import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.events.render.RenderItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderEntityItem.class)
public class MixinRenderEntityItem {

    @Inject(method = "doRender", at = @At("HEAD"), cancellable = true)
    public void onDoRenderHead(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        RenderItemEvent renderItemEvent = new RenderItemEvent(entity, x, y, z, entityYaw, partialTicks);
        HockeyWare.EVENT_BUS.post(renderItemEvent);

        if (renderItemEvent.isCanceled()) {
            info.cancel();
        }
    }
}