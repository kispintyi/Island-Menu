package com.mosadie.islandmenu.client.theme;

import com.mosadie.islandmenu.client.IslandMenuClient;
import com.mosadie.simplemainmenu.api.MenuTheme;
import com.mosadie.simplemainmenu.api.SplashText;
import com.mosadie.simplemainmenu.api.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class NormalTheme implements MenuTheme {
    @Override
    public String getId() {
        return "normal";
    }

    @Override
    public Identifier getPanorama() {
        return Identifier.fromNamespaceAndPath(IslandMenuClient.MOD_ID, "textures/gui/title/background/"+ getId() + "/panorama");
    }

    @Override
    public SplashText getSplashText() {
        return IslandMenuClient.getSplashText();
    }

    @Override
    public Component getQuickJoinButtonComponent() {
        return Component.translatable("island-menu.menu.join");
    }

    @Override
    public void onQuickJoinClicked() {
        Util.joinServer("MCC Island", "play.mccisland.net");
    }

    @Override
    public boolean rollOdds() {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isSingleplayerVisible() {
        return false;
    }

    @Override
    public boolean isMultiplayerVisible() {
        return false;
    }

    @Override
    public boolean isQuickJoinVisible() {
        return true;
    }

    @Override
    public boolean isModsVisible() {
        return true;
    }
}
