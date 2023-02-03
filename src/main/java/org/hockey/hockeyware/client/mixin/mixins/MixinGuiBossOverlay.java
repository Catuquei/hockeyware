package org.hockey.hockeyware.client.mixin.mixins;

import net.minecraft.client.gui.GuiBossOverlay;
import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.events.render.BossOverlayEvent;
import org.hockey.hockeyware.client.setting.ColorSetting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GuiBossOverlay.class})
public class MixinGuiBossOverlay {

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void renderBossHealth(CallbackInfo info) {
        BossOverlayEvent bossOverlayEvent = new BossOverlayEvent();
        HockeyWare.EVENT_BUS.post(bossOverlayEvent);

        if (bossOverlayEvent.isCanceled()) {
            info.cancel();
        }
    }
}