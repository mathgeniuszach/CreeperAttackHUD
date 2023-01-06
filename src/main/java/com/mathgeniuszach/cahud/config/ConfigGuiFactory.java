package com.mathgeniuszach.cahud.config;

import java.util.Set;

import com.mathgeniuszach.cahud.CreeperAttackHUD;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConfigGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return GuiGeneralConfig.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
    
    @SideOnly(Side.CLIENT)
	public static class GuiGeneralConfig extends GuiConfig {
		public GuiGeneralConfig(GuiScreen parent) {
			super(parent, CreeperAttackHUD.CONFIG.getClientGuiElements(), "cahud", false, false, GuiConfig.getAbridgedConfigPath(CreeperAttackHUD.CONFIG.getFileName()));
		}
	}
}
