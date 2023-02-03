package org.hockey.hockeyware.client.mixin.mixins;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.events.render.LayerArmorEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(LayerBipedArmor.class)
public class MixinLayerBipedArmor {

    @Inject(method = "setModelSlotVisible", at = @At(value = "HEAD"), cancellable = true)
    protected void setModelSlotVisible(ModelBiped model, EntityEquipmentSlot slotIn, CallbackInfo info) {
        LayerArmorEvent layerArmorEvent = new LayerArmorEvent(model, slotIn);
        HockeyWare.EVENT_BUS.post(layerArmorEvent);

        if (layerArmorEvent.isCanceled())
            info.cancel();
    }
}