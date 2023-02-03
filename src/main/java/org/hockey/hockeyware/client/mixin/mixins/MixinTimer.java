package org.hockey.hockeyware.client.mixin.mixins;

import net.minecraft.util.Timer;
import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.events.network.UpdateTimerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Timer.class)
public class MixinTimer {

    @Shadow
    public float elapsedPartialTicks;

    @Inject(method = "updateTimer", at = @At(value = "FIELD", target = "Lnet/minecraft/util/Timer;elapsedPartialTicks:F", ordinal = 1))
    private void onUpdateTimerTicks(CallbackInfo ci) {
        UpdateTimerEvent event = new UpdateTimerEvent(1.0f);
        HockeyWare.EVENT_BUS.post(event);
        elapsedPartialTicks *= event.timerSpeed;
    }
}