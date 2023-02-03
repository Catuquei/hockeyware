package org.hockey.hockeyware.client.mixin.mixins.accessor;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderer.class)
public interface IEntityRenderer {

    @Accessor("rendererUpdateCount")
    int getRendererUpdateCount();

    @Accessor("rainXCoords")
    float[] getRainXCoords();

    @Accessor("rainYCoords")
    float[] getRainYCoords();
}