package com.mathgeniuszach.cahud;

import net.minecraft.client.Minecraft;

public class CAUtil {
    public static String stripFormatting(String s) {
        try {
            StringBuilder sb = new StringBuilder(s);
            for (;;) {
                int ss = sb.indexOf("§");
                if (ss < 0) return sb.toString();
                sb.delete(ss, ss+2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void playSound(String s) {
        try {
            Minecraft minecraft = Minecraft.getMinecraft();
            String[] items = s.split(" ");
            minecraft.thePlayer.playSound(items[0], Float.parseFloat(items[1]), Float.parseFloat(items[2]));
        } catch (Exception e) {
            System.err.println("Unable to play sound \"" + s + "\"");
        }
    }
}
