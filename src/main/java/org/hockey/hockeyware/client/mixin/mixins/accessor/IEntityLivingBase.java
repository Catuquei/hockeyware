package org.hockey.hockeyware.client.mixin.mixins.accessor;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(EntityLivingBase.class)
public interface IEntityLivingBase {

    @Accessor("activePotionsMap")
    Map<Potion, PotionEffect> getActivePotionMap();

    @Invoker("onNewPotionEffect")
    void hookOnNewPotionEffect(PotionEffect potionEffectIn);

    @Invoker("onChangedPotionEffect")
    void hookOnChangedPotionEffect(PotionEffect potionEffectIn, boolean in);

    @Invoker("getArmSwingAnimationEnd")
    int hookGetArmSwingAnimationEnd();
}
