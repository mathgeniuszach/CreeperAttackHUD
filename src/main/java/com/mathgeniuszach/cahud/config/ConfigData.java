package com.mathgeniuszach.cahud.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.mathgeniuszach.cahud.CreeperAttackHUD;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;

public class ConfigData {
    public static Configuration CONFIG;

    public static final String[] CATEGORIES = {"general", "texts"};

    public ConfigData(File file) {
        MinecraftForge.EVENT_BUS.register(this);
        CONFIG = new Configuration(file);
        CONFIG.load();
        loadConfig();
    }

    public static boolean disableAll;
    public static boolean disableHUD;
    public static boolean disableLobby;
    public static boolean disableGameInfo;
    public static boolean disableBlaze;
    public static boolean disableCreeper;
    public static boolean disableZombie;
    public static boolean disableNextWave;
    public static boolean disableBigCount;

    public static boolean disable5Sound;
    public static boolean disableGolemSound;

    public static int infoXOffset;
    public static int infoYOffset;
    // public static double infoXAlign;
    // public static double infoYAlign;

    public static String soundTimer5;
    public static String soundGolem;

    public static String textLobbyHeader;
    public static String textLobbyTime;
    public static String textPlayerGood;
    public static String textPlayerBad;
    public static String textInterHeader;
    public static String textInterNext;
    public static String textGameInfo;
    public static String textWaveHeader;
    public static String textWaveCreeper;
    public static String textWaveBlaze;
    public static String textWaveZombie;
    public static String textEndingHeader;

    public static void loadConfig() {
        disableAll = getBoolean("general", "disableAll", false, "Disable the entire mod.");
        disableHUD = getBoolean("general", "disableHUD", false, "Disable all HUD information.");
        disableLobby = getBoolean("general", "disableLobby", false, "Disable HUD information in the lobby.");
        disableGameInfo = getBoolean("general", "disableGameInfo", false, "Disable game time and player count after match start.");
        disableBlaze = getBoolean("general", "disableBlaze", false, "Disable blaze information.");
        disableCreeper = getBoolean("general", "disableCreeper", false, "Disable creeper information.");
        disableZombie = getBoolean("general", "disableZombie", false, "Disable zombie information.");
        disableNextWave = getBoolean("general", "disableNextWave", false, "Disable next wave information");
        // disableBigCount = getBoolean("general", "disableBigCount", false, "Disable bigger wave countdowns.");

        disable5Sound = getBoolean("general", "disableSoundTimer5", false, "Disable the sound when creepers/blazes spawn.");
        disableGolemSound = getBoolean("general", "disableSoundGolem", false, "Disable the sound when iron golems spawn.");

        infoXOffset = getInt("general", "infoXOffset", 0, "X Offset for info");
        infoYOffset = getInt("general", "infoYOffset", 20, "Y Offset for info");

        soundTimer5 = getString("general", "soundTimer5", "mob.guardian.curse 1.0 1.2", "The sound event to play when a blaze/creepers spawn. Volume and pitch needed.");
        soundGolem = getString("general", "soundGolem", "random.anvil_land 1 2", "The sound event to play when iron golems spawn. Volume and pitch needed.");

        textLobbyHeader = getString("texts", "textLobby", "§lLOBBY", "Lobby header text.");
        textLobbyTime = getString("texts", "textLobbyTime", "§a%s", "Lobby time till start.");
        textPlayerGood = getString("texts", "textPlayerGood", "§a%s", "Sufficient player count.");
        textPlayerBad = getString("texts", "textPlayerBad", "§c%s", "Insufficient player count.");
        textInterHeader = getString("texts", "textInter", "§lINTERMISSION - %s", "Intermission header text.");
        textInterNext = getString("texts", "textInterNext", "Next: §lWAVE %s§r - %s", "Next wave text.");
        textGameInfo = getString("texts", "textGameTime", "§7%s - %s", "Game time and player count.");
        textWaveHeader = getString("texts", "textWaveHeader", "§lWAVE %s", "Wave header text.");
        textWaveCreeper = getString("texts", "textWaveCreeper", "§2Creepers: %s/%s %s%s%s%s§2 - %s", "Creeper line text.");
        textWaveBlaze = getString("texts", "textWaveBlaze", "§6Blazes: %s/%s - %s", "Blaze line text.");
        textWaveZombie = getString("texts", "textWaveZombie", "§3Zombies: %s - %s", "Zombie line text.");
        textEndingHeader = getString("texts", "textEnding", "§lEND - WAVE %s - %s", "Ending header text.");

        if (CONFIG.hasChanged()) CONFIG.save();
    }

    public static int getInt(String category, String key, int defValue, String comment) {
        Property prop = CONFIG.get(category, key, defValue);
        prop.comment = comment;
        return prop.getInt();
    }

    public static boolean getBoolean(String category, String key, boolean defValue, String comment) {
        Property prop = CONFIG.get(category, key, defValue);
        prop.comment = comment;
        return prop.getBoolean();
    }

    public static String getString(String category, String key, String defValue, String comment) {
        Property prop = CONFIG.get(category, key, defValue);
        prop.comment = comment;
        return prop.getString();
    }

    public static double getDouble(String category, String key, double defValue, String comment) {
        Property prop = CONFIG.get(category, key, defValue);
        prop.comment = comment;
        return prop.getDouble();
    }

    public static int getColor(String category, String key, String defColor, String comment) {
        Property prop = CONFIG.get(category, key, defColor);
        prop.comment = comment;

        String stringColor = prop.getString();

        long color = 0xFFFF00FF;

        if (!stringColor.isEmpty()) {
            if (stringColor.charAt(0) == '#') {
                stringColor = stringColor.substring(1);
            }

            try {
                switch (stringColor.length()) {
                    case 3:
                        color = Long.parseLong(""
                            +stringColor.charAt(0)+stringColor.charAt(0)
                            +stringColor.charAt(1)+stringColor.charAt(1)
                            +stringColor.charAt(2)+stringColor.charAt(2)
                        , 16) + 0xFF000000;
                        break;
                    case 4:
                        color = Long.parseLong(""
                            +stringColor.charAt(0)+stringColor.charAt(0)
                            +stringColor.charAt(1)+stringColor.charAt(1)
                            +stringColor.charAt(2)+stringColor.charAt(2)
                            +stringColor.charAt(3)+stringColor.charAt(3)
                            , 16);
                        break;
                    case 6:
                        color = Long.parseLong(stringColor, 16) + 0xFF000000;
                        break;
                    case 8:
                        color = Long.parseLong(stringColor, 16);
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.format("Failure to parse color \"%s\"", key);
            }
        }

        return (int)color;
    }

    @SideOnly(Side.CLIENT)
	public List<IConfigElement> getClientGuiElements() {
        List<IConfigElement> list = new ArrayList<IConfigElement>();
        for (String category : CATEGORIES) {
            list.add(new ConfigElement(ConfigData.CONFIG.getCategory(category)));
        }
        return list;
        // return new ConfigElement(ConfigData.CONFIG.getCategory("client")).getChildElements();
	}
	
	public String getFileName(){
		return CONFIG.toString();
	}

    @SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event) {
		if (event.modID.equalsIgnoreCase(CreeperAttackHUD.MODID)) {
			ConfigData.loadConfig();
		}
	}
}
