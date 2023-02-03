package org.hockey.hockeyware.client.gui.component.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatAllowedCharacters;
import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.features.module.modules.Client.CustomFont;
import org.hockey.hockeyware.client.gui.Component;
import org.hockey.hockeyware.client.setting.Setting;
import org.hockey.hockeyware.client.util.math.StopWatch;
import org.hockey.hockeyware.client.util.render.Render2DUtil;
import org.hockey.hockeyware.client.util.render.RenderUtil;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;

public class StringComponent extends Component {
    private final Setting<String> stringSetting;
    public boolean isListening;
    private CurrentString currentString = new CurrentString("");
    private boolean idling;
    private final StopWatch idleTimer = new StopWatch();

    public StringComponent(Setting<String> stringSetting, float posX, float posY, float offsetX, float offsetY, float width, float height) {
        super(stringSetting.getName(), posX, posY, offsetX, offsetY, width, height);
        this.stringSetting = stringSetting;
    }

    @Override
    public void moved(float posX, float posY) {
        super.moved(posX, posY);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        final boolean hovered = RenderUtil.mouseWithinBounds(mouseX, mouseY, getFinishedX() + 5, getFinishedY() + 1, getWidth() - 10, getHeight() - 2);
        Render2DUtil.drawBorderedRect(getFinishedX() + 4.5f, getFinishedY() + 1.0f, getFinishedX() + getWidth() - 4.5f, getFinishedY() + getHeight() - 0.5f, 0.5f, hovered ? 0x66333333 : 0, 0xff000000);
        if (CustomFont.INSTANCE.isOn()) {
            if (isListening) {
                HockeyWare.INSTANCE.fontManager.drawStringWithShadow(currentString.getString() + getIdleSign(), getFinishedX() + 6.5f, getFinishedY() + getHeight() - HockeyWare.INSTANCE.fontManager.getTextHeight() - 1f, getState() ? 0xFFFFFFFF : 0xFFAAAAAA);
            } else {
                HockeyWare.INSTANCE.fontManager.drawStringWithShadow(getStringSetting().getValue(), getFinishedX() + 6.5f, getFinishedY() + getHeight() - HockeyWare.INSTANCE.fontManager.getTextHeight() - 1f, getState() ? 0xFFFFFFFF : 0xFFAAAAAA);
            }
        } else {
            if (isListening) {
                Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(currentString.getString() + getIdleSign(), getFinishedX() + 6.5f, getFinishedY() + getHeight() - Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT - 1f, getState() ? 0xFFFFFFFF : 0xFFAAAAAA);
            } else {
                Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(getStringSetting().getValue(), getFinishedX() + 6.5f, getFinishedY() + getHeight() - Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT - 1f, getState() ? 0xFFFFFFFF : 0xFFAAAAAA);
            }
        }
    }

    @Override
    public void keyTyped(char character, int keyCode) {
        super.keyTyped(character, keyCode);
        if (isListening) {
            if (keyCode == 1) {
                return;
            }
            if (keyCode == 28) {
                enterString();
                setListening(false);
            } else if (keyCode == 14) {
                setString(removeLastChar(currentString.getString()));
            } else {
                if (keyCode == Keyboard.KEY_V && (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))) {
                    try {
                        setString(currentString.getString() + Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                if (ChatAllowedCharacters.isAllowedCharacter(character)) {
                    setString(currentString.getString() + character);
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        final boolean hovered = RenderUtil.mouseWithinBounds(mouseX, mouseY, getFinishedX() + 5, getFinishedY() + 1, getWidth() - 10, getHeight() - 2);
        if (hovered && mouseButton == 0)
            toggle();
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    public String getIdleSign() {
        if (idleTimer.passed(500)) {
            idling = !idling;
            idleTimer.reset();
        }

        if (idling) {
            return "_";
        }
        return "";
    }

    private void enterString() {
        if (currentString.getString().isEmpty()) {
            getStringSetting().setValue("default");
        } else {
            getStringSetting().setValue(currentString.getString());
        }
        setString("");
    }

    public Setting<String> getStringSetting() {
        return stringSetting;
    }

    public void toggle() {
        isListening = !isListening;
    }

    public boolean getState() {
        return !isListening;
    }

    public void setListening(boolean listening) {
        isListening = listening;
    }

    public void setString(String newString) {
        this.currentString = new CurrentString(newString);
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && str.length() > 0) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    public static class CurrentString {
        private final String string;

        public CurrentString(String string) {
            this.string = string;
        }

        public String getString() {
            return this.string;
        }
    }
}
