package com.mosadie.islandmenu.client.mixin;

import com.mosadie.islandmenu.client.IslandMenuClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
public class LevelLoadingScreenMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void islandMenu$extractMcciJoinLogo(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickProgress, CallbackInfo ci) {
        IslandMenuClient.extractMcciJoinLogo((Screen)(Object)this, graphics);
    }
}
