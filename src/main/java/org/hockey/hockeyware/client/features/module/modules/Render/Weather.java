package org.hockey.hockeyware.client.features.module.modules.Render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.hockey.hockeyware.client.features.module.Category;
import org.hockey.hockeyware.client.features.module.Module;
import org.hockey.hockeyware.client.mixin.mixins.accessor.IEntityRenderer;
import org.hockey.hockeyware.client.setting.Setting;

import java.util.Random;

public class Weather extends Module {

    private static final Random RANDOM = new Random();
    private static final ResourceLocation RAIN_TEXTURES =
            new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation SNOW_TEXTURES =
            new ResourceLocation("textures/environment/snow.png");

    private final Setting<Boolean> snow =
            new Setting("Snow", true);
    private final Setting<Integer> height =
            new Setting<>("Height", 0, 0, 255);
    private final Setting<Float> strength =
            new Setting<>("Strength", 0.3f, 0.1f, 2.0f);

    public Weather() {
        super("Weather", "Allows You To Change The Weather Client Side", Category.Render);
    }

    public void render(float partialTicks) {
        float f = strength.getValue();

        EntityRenderer renderer = mc.entityRenderer;
        renderer.enableLightmap();
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) {
            return;
        }

        World world = mc.world;
        int i = MathHelper.floor(entity.posX);
        int j = MathHelper.floor(entity.posY);
        int k = MathHelper.floor(entity.posZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.disableCull();
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.alphaFunc(516, 0.1F);
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
        int l = MathHelper.floor(d1);
        int i1 = 5;

        if (mc.gameSettings.fancyGraphics) {
            i1 = 10;
        }

        int j1 = -1;
        float f1 = (float) ((IEntityRenderer) renderer).getRendererUpdateCount() + partialTicks;
        bufferbuilder.setTranslation(-d0, -d1, -d2);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int k1 = k - i1; k1 <= k + i1; ++k1) {
            for (int l1 = i - i1; l1 <= i + i1; ++l1) {
                int i2 = (k1 - k + 16) * 32 + l1 - i + 16;
                double d3 = (double) ((IEntityRenderer) renderer).getRainXCoords()[i2] * 0.5D;
                double d4 = (double) ((IEntityRenderer) renderer).getRainYCoords()[i2] * 0.5D;
                blockpos$mutableblockpos.setPos(l1, 0, k1);
                Biome biome = world.getBiome(blockpos$mutableblockpos);

                int j2 = /*world.getPrecipitationHeight(blockpos$mutableblockpos).getY();*/ height.getValue();
                int k2 = j - i1;
                int l2 = j + i1;

                if (k2 < j2) {
                    k2 = j2;
                }

                if (l2 < j2) {
                    l2 = j2;
                }

                int i3 = j2;

                if (j2 < l) {
                    i3 = l;
                }

                if (k2 != l2) {
                    RANDOM.setSeed((long) l1 * l1 * 3121 + l1 * 45238971L ^ (long) k1 * k1 * 418711 + k1 * 13761L);
                    blockpos$mutableblockpos.setPos(l1, k2, k1);
                    float f2 = biome.getTemperature(blockpos$mutableblockpos);

                    if (/*world.getBiomeProvider().getTemperatureAtHeight(f2, j2) >= 0.15F*/!snow.getValue()) {
                        if (j1 != 0) {
                            if (j1 >= 0) {
                                tessellator.draw();
                            }

                            j1 = 0;
                            mc.getTextureManager().bindTexture(RAIN_TEXTURES);
                            bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                        }

                        double d5 = -((double) (((IEntityRenderer) renderer).getRendererUpdateCount() + l1 * l1 * 3121 + l1 * 45238971 + k1 * k1 * 418711 + k1 * 13761 & 31) + (double) partialTicks) / 32.0D * (3.0D + RANDOM.nextDouble());
                        double d6 = (double) ((float) l1 + 0.5F) - entity.posX;
                        double d7 = (double) ((float) k1 + 0.5F) - entity.posZ;
                        float f3 = MathHelper.sqrt(d6 * d6 + d7 * d7) / (float) i1;
                        float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * f; // this controls
                        blockpos$mutableblockpos.setPos(l1, i3, k1);
                        int j3 = world.getCombinedLight(blockpos$mutableblockpos, 0);
                        int k3 = j3 >> 16 & 65535;
                        int l3 = j3 & 65535;
                        bufferbuilder.pos((double) l1 - d3 + 0.5D, l2, (double) k1 - d4 + 0.5D).tex(0.0D, (double) k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                        bufferbuilder.pos((double) l1 + d3 + 0.5D, l2, (double) k1 + d4 + 0.5D).tex(1.0D, (double) k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                        bufferbuilder.pos((double) l1 + d3 + 0.5D, k2, (double) k1 + d4 + 0.5D).tex(1.0D, (double) l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                        bufferbuilder.pos((double) l1 - d3 + 0.5D, k2, (double) k1 - d4 + 0.5D).tex(0.0D, (double) l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                    } else {
                        if (j1 != 1) {
                            if (j1 >= 0) {
                                tessellator.draw();
                            }

                            j1 = 1;
                            mc.getTextureManager().bindTexture(SNOW_TEXTURES);
                            bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                        }

                        double d8 = -((float) (((IEntityRenderer) renderer).getRendererUpdateCount() & 511) + partialTicks) / 512.0F;
                        double d9 = RANDOM.nextDouble() + (double) f1 * 0.01D * (double) ((float) RANDOM.nextGaussian());
                        double d10 = RANDOM.nextDouble() + (double) (f1 * (float) RANDOM.nextGaussian()) * 0.001D;
                        double d11 = (double) ((float) l1 + 0.5F) - entity.posX;
                        double d12 = (double) ((float) k1 + 0.5F) - entity.posZ;
                        float f6 = MathHelper.sqrt(d11 * d11 + d12 * d12) / (float) i1;
                        float f5 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * f;
                        blockpos$mutableblockpos.setPos(l1, i3, k1);
                        int i4 = (world.getCombinedLight(blockpos$mutableblockpos, 0) * 3 + 15728880) / 4;
                        int j4 = i4 >> 16 & 65535;
                        int k4 = i4 & 65535;
                        bufferbuilder.pos((double) l1 - d3 + 0.5D, l2, (double) k1 - d4 + 0.5D).tex(0.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                        bufferbuilder.pos((double) l1 + d3 + 0.5D, l2, (double) k1 + d4 + 0.5D).tex(1.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                        bufferbuilder.pos((double) l1 + d3 + 0.5D, k2, (double) k1 + d4 + 0.5D).tex(1.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                        bufferbuilder.pos((double) l1 - d3 + 0.5D, k2, (double) k1 - d4 + 0.5D).tex(0.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                    }
                }
            }
        }

        if (j1 >= 0) {
            tessellator.draw();
        }

        bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
        renderer.disableLightmap();
    }

}