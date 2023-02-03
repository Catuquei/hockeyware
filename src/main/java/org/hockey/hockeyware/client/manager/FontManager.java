package org.hockey.hockeyware.client.manager;

import org.hockey.hockeyware.client.features.Globals;
import org.hockey.hockeyware.client.features.module.modules.Client.CustomFont;
import org.hockey.hockeyware.client.util.font.FontRenderer;

import java.awt.*;
import java.util.Locale;

public class FontManager implements Globals {

    public final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.ENGLISH);
    public String fontName = CustomFont.font.getName();
    public int fontSize = 35;

    private FontRenderer font = new FontRenderer(new Font(fontName, Font.PLAIN, 40));

    public void setFont() {
        this.font = new FontRenderer(new Font(fontName, Font.PLAIN, 40));
    }


    public boolean setFont(String fontName) {
        for (String font : fonts) {
            if (fontName.equalsIgnoreCase(font)) {
                this.fontName = font;
                this.setFont();
                return true;
            }
        }
        return false;
    }

    public void setFontSize(int size) {
        this.fontSize = size;
        this.setFont();
    }

    public void drawStringWithShadow(String string, float x, float y, int colour) {
        this.drawString(string, x, y, colour, true);
    }

    public float drawString(String string, float x, float y, int colour, boolean shadow) {
        return this.font.drawString(string, x, y, colour, shadow);
    }

    public int getTextHeight() {
        return ((int) this.font.getHeight());
    }

    public int getStringWidth(String string) {
        return this.font.getStringWidth(string);
    }
}

