package com.mathgeniuszach.cahud;

import com.mathgeniuszach.cahud.config.ConfigData;
import com.mathgeniuszach.cahud.proxy.CommonProxy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;

@Mod(
    modid = CreeperAttackHUD.MODID,
    version = CreeperAttackHUD.VERSION,
    guiFactory = "${group}.config.ConfigGuiFactory"
)
public class CreeperAttackHUD
{
    public static final String MODID = "${modid}";
    public static final String VERSION = "${version}";
    public static final String GROUP = "${group}";

    @Mod.Instance(CreeperAttackHUD.MODID)
    public static CreeperAttackHUD INSTANCE;

    @SidedProxy(
        clientSide = "${group}.proxy.ClientProxy",
        serverSide = "${group}.proxy.ServerProxy"
    )
    public static CommonProxy PROXY;

    public static ConfigData CONFIG;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CONFIG = new ConfigData(event.getSuggestedConfigurationFile());
        PROXY.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PROXY.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PROXY.postInit(event);
    }

}