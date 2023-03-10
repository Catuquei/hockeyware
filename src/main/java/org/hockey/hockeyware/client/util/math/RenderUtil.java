package org.hockey.hockeyware.client.util.math;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.features.module.modules.Client.CustomFont;
import org.hockey.hockeyware.client.util.Globals;
import org.hockey.hockeyware.client.util.client.ColorUtil;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtil implements Globals {
    private static float[] rgba;

    public static void drawRect(double x, double y, double width, double height, int color) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        getUpdatedColor(color);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x + width, y, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(x, y, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(x, y + height, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(x + width, y + height, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
    }

    public static void drawString2(final double scale, final String text,
                                  final float x, final float y, final int color) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
            Minecraft.getMinecraft().fontRenderer.drawString(text, (int) x, (int) y, color);
        GlStateManager.popMatrix();
    }

    public static void drawBorderedRect(final double x, final double y, final double x1, final double y1, final double width, final int internalColor, final int borderColor) {
        enableGL2D();
        fakeGuiRect(x + width, y + width, x1 - width, y1 - width, internalColor);
        fakeGuiRect(x + width, y, x1 - width, y + width, borderColor);
        fakeGuiRect(x, y, x + width, y1, borderColor);
        fakeGuiRect(x1 - width, y, x1, y1, borderColor);
        fakeGuiRect(x + width, y1 - width, x1 - width, y1, borderColor);
        disableGL2D();
    }

    public static void createChamsPre() {
        mc.getRenderManager().setRenderShadow(false);
        mc.getRenderManager().setRenderOutlines(false);
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(1.0f, -5300000.0f);
        GlStateManager.popMatrix();
    }

    public static void createColorPre(boolean isPlayer, int red, int green, int blue, int alpha) {
        mc.getRenderManager().setRenderShadow(false);
        mc.getRenderManager().setRenderOutlines(false);
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(1.0f, -5300000.0f);
        glDisable(GL11.GL_TEXTURE_2D);
        if (!isPlayer) {
            GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
        }
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
        GlStateManager.popMatrix();
    }

    public static void createChamsPost() {
        boolean shadow = mc.getRenderManager().isRenderShadow();
        mc.getRenderManager().setRenderShadow(shadow);
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(1.0f, -5300000.0f);
        GlStateManager.popMatrix();
    }

    public static void createColorPost(boolean isPlayer) {
        boolean shadow = mc.getRenderManager().isRenderShadow();
        mc.getRenderManager().setRenderShadow(shadow);
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);
        if (!isPlayer) {
            GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
        }
        glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(1.0f, -5300000.0f);
        glEnable(GL11.GL_TEXTURE_2D);
        GlStateManager.popMatrix();
    }

    private static void fakeGuiRect(double left, double top, double right, double bottom, final int color) {
        if (left < right) {
            final double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            final double j = top;
            top = bottom;
            bottom = j;
        }
        final float f3 = (color >> 24 & 0xFF) / 255.0f;
        final float f4 = (color >> 16 & 0xFF) / 255.0f;
        final float f5 = (color >> 8 & 0xFF) / 255.0f;
        final float f6 = (color & 0xFF) / 255.0f;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f4, f5, f6, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0).endVertex();
        bufferbuilder.pos(right, bottom, 0.0).endVertex();
        bufferbuilder.pos(right, top, 0.0).endVertex();
        bufferbuilder.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static void enableGL2D() {
        glDisable(2929);
        glEnable(3042);
        glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    private static void disableGL2D() {
        glEnable(3553);
        glDisable(3042);
        glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static float drawStringWithShadow(final String text, final int x, final int y, final int color) {
        return (float) mc.fontRenderer.drawStringWithShadow(text, (float) x, (float) y, color);
    }

    public static float drawString(int i, final String text, final int x, final int y, final int color) {
        return (float) mc.fontRenderer.drawString(text, x, y, color);
    }

    public static int getStringWidth(final String str) {
        return mc.fontRenderer.getStringWidth(str);
    }

    public static int getFontHeight() {
        return mc.fontRenderer.FONT_HEIGHT;
    }

    public static void drawText(BlockPos pos, String text) {
        GlStateManager.pushMatrix();
        RenderUtil.glBillboardDistanceScaled((float) pos.getX() + 0.5f, (float) pos.getY() + 0.5f, (float) pos.getZ() + 0.5f, RenderUtil.mc.player, 1.0f);
        GlStateManager.disableDepth();
        GlStateManager.translate(-((double) mc.fontRenderer.getStringWidth(text) / 2.0), 0.0, 0.0);
        mc.fontRenderer.drawStringWithShadow(text, 0.0f, 0.0f, -5592406);
        GlStateManager.popMatrix();
    }

    public static void glBillboardDistanceScaled(float x, float y, float z, EntityPlayer player, float scale) {
        RenderUtil.glBillboard(x, y, z);
        int distance = (int) player.getDistance(x, y, z);
        float scaleDistance = (float) distance / 2.0f / (2.0f + (2.0f - scale));
        if (scaleDistance < 1.0f) {
            scaleDistance = 1.0f;
        }
        GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance);
    }

    public static void glBillboard(float x, float y, float z) {
        float scale = 0.02666667f;
        GlStateManager.translate((double) x - RenderUtil.mc.getRenderManager().renderPosX, (double) y - RenderUtil.mc.getRenderManager().renderPosY, (double) z - RenderUtil.mc.getRenderManager().renderPosZ);
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-RenderUtil.mc.player.rotationYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(RenderUtil.mc.player.rotationPitch, RenderUtil.mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
    }


    private static void getUpdatedColor(int color) {
        rgba = ColorUtil.toRGBA(color);
    }

    public static void drawLine(double startX, double startY, double endX, double endY, float width, int color) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(width);

        glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        getUpdatedColor(color);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(startX, startY, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        buffer.pos(endX, endY, 0.0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
        tessellator.draw();

        glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
    }


    public static void drawBox(AxisAlignedBB bb, float r, float g, float b, float a) {
        glPrepare();
        RenderGlobal.renderFilledBox(bb, r, g, b, a);
        RenderGlobal.drawSelectionBoundingBox(bb, r, g, b, a);
        glRelease();
    }

    public static void glRelease() {
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
    }

    public static void glPrepare() {
        GL11.glBlendFunc(770, 771);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(1.5f);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }

//    public static void glRelease() {
//        GL11.glEnable(3553);
//        GL11.glEnable(2929);
//        GL11.glDepthMask(true);
//        GL11.glDisable(3042);
//        GL11.glPopMatrix();
//    }

//    public static void glPrepare() {
//        GL11.glPushMatrix();
//        GL11.glBlendFunc(770, 771);
//        GL11.glEnable(3042);
//        GL11.glDisable(3553);
//        GL11.glDisable(2929);
//        GL11.glDepthMask(false);
//    }

//    public static void drawEsp(AxisAlignedBB box, boolean filled, boolean outline, float width, float r, float g, float b, float a) {
//        if (filled) {
//            drawFilledBox(box, r, g, b, a);
//        }
//        if (outline) {
//            drawOutlinedBox(box, width, r, g, b, a);
//        }
//    }
//    public static void drawFilledBox(AxisAlignedBB box, float r, float g, float b, float a) {
//        GlStateManager.pushMatrix();
//        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
//        GlStateManager.disableTexture2D();
//        GlStateManager.disableDepth();
//        GlStateManager.depthMask(false);
//        RenderGlobal.renderFilledBox(box, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
//        GlStateManager.depthMask(true);
//        GlStateManager.enableDepth();
//        GlStateManager.enableTexture2D();
//        GlStateManager.disableBlend();
//        GlStateManager.popMatrix();
//    }

    public static AxisAlignedBB generateBB(double x, double y, double z) {
        BlockPos blockPos = new BlockPos(x, y, z);
        final AxisAlignedBB bb = new AxisAlignedBB
                (
                        blockPos.getX() - Minecraft.getMinecraft().getRenderManager().viewerPosX,
                        blockPos.getY() - Minecraft.getMinecraft().getRenderManager().viewerPosY,
                        blockPos.getZ() - Minecraft.getMinecraft().getRenderManager().viewerPosZ,
                        blockPos.getX() + 1 - Minecraft.getMinecraft().getRenderManager().viewerPosX,
                        blockPos.getY() + (1) - Minecraft.getMinecraft().getRenderManager().viewerPosY,
                        blockPos.getZ() + 1 - Minecraft.getMinecraft().getRenderManager().viewerPosZ
                );
        return bb;
    }

    public static class TessellatorUtil extends Tessellator {
        public static TessellatorUtil INSTANCE = new TessellatorUtil();

        public TessellatorUtil() {
            super(0x200000);
        }

        public static void prepareGL() {
            GL11.glBlendFunc(770, 771);
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(1.5f);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.enableAlpha();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }


        public static void render() {
            INSTANCE.draw();
        }

        public static void releaseGL() {
            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.enableDepth();
        }
    }

}
