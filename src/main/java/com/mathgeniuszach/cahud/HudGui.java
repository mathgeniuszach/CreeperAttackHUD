package com.mathgeniuszach.cahud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.mathgeniuszach.cahud.config.ConfigData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.*;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class HudGui extends Gui {
    public static final Vec3 VILLAGER_POS = new Vec3(-2450.5, 17, 748.5);

    public enum State {
        LOBBY,
        INTERMISSION,
        WAVE,
        END
    }
    public State state = State.LOBBY;

    public long worldTick = -1;
    public long gameTime = 0;

    public int timerI = 0;
    public long timer5 = 0;
    public long timer4 = 0;
    public int timer5Cooldown = 0;
    public int golemSpawn = 20;

    public int blazes = 0;
    public int creepers = 0;
    public int zombies = 0;

    public int zombiesLeft = 0;
    public int creeperSpawn = 0;
    public int creepersLeft = 0;
    public int wave = 0;

    public List<String> renderLines;

    public String title;
    public float titleVisible = 0;
    public float titleTime;

    public HashSet<UUID> seenHashes = new HashSet<UUID>();

    public String formatGameTime() {
        long gameSeconds = gameTime / 20;
        if (gameSeconds < 3600) {
            return String.format("%d:%02d", gameSeconds / 60, gameSeconds % 60);
        } else {
            return String.format("%d:%02d:%02d", gameSeconds / 3600, (gameSeconds % 3600) / 60, gameSeconds % 60);
        }
    }

    public static int[] getMaxCreepers(int w) {
        if (w <= 8) {
            return new int[] {4, 1, 1, 1, 1};
        } else if (w <= 18) {
            return new int[] {5, 2, 1, 1, 1};
        } else if (w <= 19) {
            return new int[] {6, 2, 1, 2, 1};
        } else if (w <= 24) {
            return new int[] {7, 3, 1, 2, 1};
        } else if (w <= 28) {
            return new int[] {8, 3, 1, 3, 1};
        } else if (w <= 34) {
            return new int[] {9, 3, 2, 3, 1};
        } else if (w <= 38) {
            return new int[] {10, 3, 2, 3, 2};
        } else if (w <= 39) {
            return new int[] {11, 3, 2, 3, 3};
        } else if (w <= 44) {
            return new int[] {12, 4, 2, 3, 3};
        } else if (w <= 48) {
            return new int[] {14, 5, 2, 4, 3};
        } else if (w <= 49) {
            return new int[] {15, 5, 2, 5, 3};
        } else { // w >= 50
            return new int[] {33, 10, 5, 10, 8};
        }
    }
    public static int getGolems(int w) {
        return w > 50 ? 10 : w % 5 == 0 ? (w/5 - 1)/2 + 1 : 0;
    }
    public static int getMaxBlazes(int w) {
        return w >= 46 ? 5 : w % 5 == 0 ? 0 : w / 10;
    }
    public static int getMaxPigmen(int w) {
        return w >= 46 ? 9 : w % 5 == 0 ? 0 : w / 5;
    }
    public static int getMaxSkeletons(int w) {
        if (w >= 50) return 20;
        if (w % 5 == 0 || w < 5) return 0;

        if (w == 22) return 8; // Probably a typo in the wave code
        if (w == 49) return 19; // Probably for a more smooth transition to wave 50 from 48

        return (w / 5) * 2 + (w % 5 - 1) / 2 - 1;
    }
    public static int getMaxZombies(int w) {
        return w >= 50 ? 59 : w % 5 == 0 || w == 1 ? 0 : w+w/5;
    }

    public static int getPlayerCount() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer.sendQueue.getPlayerInfoMap().size();
    }

    public static String getMobString(int w) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("§2%dC", getMaxCreepers(w)[0]));

        int golems = getGolems(w);
        int blazes = getMaxBlazes(w);
        int pigmen = getMaxPigmen(w);
        int skeletons = getMaxSkeletons(w);
        int zombies = getMaxZombies(w);

        if (golems > 0) sb.append(String.format(" §f%dI", golems));
        if (blazes > 0) sb.append(String.format(" §6%dB", blazes));
        if (pigmen > 0) sb.append(String.format(" §e%dP", pigmen));
        if (skeletons > 0) sb.append(String.format(" §7%dS", skeletons));
        if (zombies > 0) sb.append(String.format(" §3%dZ", zombies));

        return sb.toString();
    }

    public void activateTimer5(boolean sum) {
        if (sum) timer5 += 100;
        else timer5 = 100;

        if (timer5Cooldown <= 0) {
            timer5Cooldown = 40;

            if (creeperSpawn > 0 && blazes < getMaxBlazes(wave)) blazes++;

            int[] maxCreepers = getMaxCreepers(wave);
    
            for (int i = 1; i < maxCreepers.length; i++) {
                if (maxCreepers[i] > creeperSpawn) creepers++;
            }
            creeperSpawn++;
    
            if (!ConfigData.disable5Sound) CAUtil.playSound(ConfigData.soundTimer5);
        }
    }

    public void tick(TickEvent.ClientTickEvent event) {
        // System.out.println("tick");
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        if (world == null) return;

        if (worldTick < 0) {
            worldTick = world.getTotalWorldTime();
            return;
        }
        long elapsedTicks = world.getTotalWorldTime() - worldTick;
        worldTick = world.getTotalWorldTime();

        if (state == State.INTERMISSION || state == State.WAVE) {
            timer5 -= elapsedTicks;
            timer4 -= elapsedTicks;
            if (timer5 < 0) {
                if (state == State.WAVE && creepers < getMaxCreepers(wave)[0]) {
                    activateTimer5(true);
                } else {
                    timer5 = 0;
                }
            }
            if (timer4 < -20) timer4 = -20;

            timer5Cooldown -= elapsedTicks;
            if (timer5Cooldown < 0) timer5Cooldown = 0;
        }
        

        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) return;
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return;

        if (state != State.LOBBY && state != State.END) {
            gameTime += elapsedTicks;

            String waveString = CAUtil.stripFormatting(objective.getDisplayName()).trim();
            if (waveString.contains("WAVE")) {
                int newWave = Integer.parseInt(waveString.substring(waveString.lastIndexOf(" ")+1));
                if (newWave != wave) {
                    wave = newWave;
                    if (!ConfigData.disableGolemSound && getGolems(wave) > 0) CAUtil.playSound(ConfigData.soundGolem); 
                }
            }
        }
        
        List<String> lines = new ArrayList<String>();
        for (Score score : scoreboard.getSortedScores(objective)) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(CAUtil.stripFormatting(
                ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()).replace(score.getPlayerName(), "")
            ));
        }
        Collections.reverse(lines);

        if (lines.size() < 3) return;

        String firstLine = lines.get(2).trim();
        if (state == State.LOBBY) {
            // In lobby
            if (!firstLine.contains("Players")) return;

            String players = firstLine.substring(firstLine.lastIndexOf(" ")+1);
            String[] nums = players.split("/");
            
            String secondLine = lines.get(4);
            int zeroLoc = secondLine.indexOf("0");
            String time = secondLine.substring(zeroLoc+1,zeroLoc+5);

            renderLines = new ArrayList<String>(3);
            if (ConfigData.disableLobby) return;

            renderLines.add(ConfigData.textLobbyHeader);
            renderLines.add(String.format(Integer.parseInt(nums[0]) < 5 ? ConfigData.textPlayerBad : ConfigData.textPlayerGood, players));
            renderLines.add(String.format(ConfigData.textLobbyTime, time));
        } else if (state == State.END) {
            renderLines = new ArrayList<String>();
            renderLines.add(String.format(ConfigData.textEndingHeader, ""+wave, formatGameTime()));
        } else {
            renderLines = new ArrayList<String>(3);
            if (!ConfigData.disableGameInfo) renderLines.add(String.format(ConfigData.textGameInfo, formatGameTime(), getPlayerCount()+"/14"));

            int[] maxCreepers = getMaxCreepers(wave);
            int maxBlazes = getMaxBlazes(wave);

            if (firstLine.contains("Creepers left")) {
                creepersLeft = Integer.parseInt(firstLine.substring(firstLine.lastIndexOf(":")+1).trim());
            } else {
                creepersLeft = 0;
            }

            if (state == State.INTERMISSION) {
                if (firstLine.contains("Next")) {
                    timerI = firstLine.charAt(firstLine.length()-1) - '0';
                    if (wave != 0) timerI += 1;
                    if (timerI == 0) timerI = 10;
                } else {
                    timerI = wave == 0 ? 10 : 1;
                }

                renderLines.add(String.format(ConfigData.textInterHeader, ""+timerI));
                if (!ConfigData.disableNextWave) renderLines.add(String.format(ConfigData.textInterNext, ""+(wave+1), getMobString(wave+1)));
            } else if (state == State.WAVE) {
                renderLines.add(String.format(ConfigData.textWaveHeader, ""+wave));

                if (!ConfigData.disableCreeper) renderLines.add(String.format(ConfigData.textWaveCreeper,
                    ""+creepers, ""+maxCreepers[0],
                    creeperSpawn < maxCreepers[1] ? "§a1" : "§c§m1",
                    creeperSpawn < maxCreepers[2] ? "§a2" : "§c§m2",
                    creeperSpawn < maxCreepers[3] ? "§a3" : "§c§m3",
                    creeperSpawn < maxCreepers[4] ? "§a4" : "§c§m4",
                    creepers >= maxCreepers[0] ? "Complete" : ""+(timer5/20+1)
                ));
            }

            if (!ConfigData.disableBlaze && maxBlazes > 0) renderLines.add(String.format(ConfigData.textWaveBlaze, ""+blazes, ""+maxBlazes, blazes >= maxBlazes ? "Complete" : ""+(timer5/20+1)));
            if (!ConfigData.disableZombie && wave > 1 && (wave % 5 != 0 || wave >= 50)) renderLines.add(String.format(ConfigData.textWaveZombie, ""+zombies, timer4 == -20 ? "Complete" : ""+(timer4/20+1)));
        }
    }

    public void chat(ClientChatReceivedEvent event) {
        // System.out.println("chat");
        String message = CAUtil.stripFormatting(event.message.getUnformattedText().trim());
        if (message.startsWith("The game has started!") || message.startsWith("All Creepers are dead!")) {
            state = State.INTERMISSION;

            creepersLeft = 0;

            if (blazes < getMaxBlazes(wave)) {
                activateTimer5(false);
                blazes = getMaxBlazes(wave);
            } else {
                seenHashes.clear();
            }

            timerI = 0;
            timer5 = 0;
        } else if (message.startsWith("You survived")) {
            state = State.END;
        }
    }

    public void entityJoin(EntityJoinWorldEvent event) {
        if (state == State.LOBBY) return;
        if (!(
            event.entity instanceof EntityBlaze ||
            event.entity instanceof EntityCreeper ||
            event.entity instanceof EntityZombie
        )) return;

        // Prevent entities from firing this event multiple times
        if (seenHashes.contains(event.entity.getUniqueID())) return;
        seenHashes.add(event.entity.getUniqueID());

        Minecraft mc = Minecraft.getMinecraft();
        
        // System.out.println("spawn");
        if (event.entity instanceof EntityBlaze) {
            // Blaze Spawn Locations (20.928 blocks from villager)
            // 1-2. (-2463.5, 27.0, 761.5)
            // 2-3. (-2463.5, 27.0, 735.5)
            // 3-4. (-2437.5, 27.0, 735.5)
            // 4-1. (-2437.5, 27.0, 761.5)

            // System.out.println("Blaze: " + event.entity.getPositionVector() + "; " + event.entity.getDistanceToEntity(mc.thePlayer) + " to P; " + VILLAGER_POS.distanceTo(event.entity.getPositionVector()) + " to V");

            // Ignore Blaze "spawns" just inside the render distance
            if (event.entity.getDistanceToEntity(mc.thePlayer) > 60) return;
            // Ignore blaze "spawns" too close to the villager (the blaze moved already)
            if (VILLAGER_POS.distanceTo(event.entity.getPositionVector()) < 20.5) return;

            activateTimer5(false);
        } else if (event.entity instanceof EntityCreeper) {
            // Creeper Spawn Locations (38 blocks from villager)
            // 1. (-2450.5, 17.0, 786.5)
            // 2. (-2488.5, 17.0, 748.5)
            // 3. (-2450.5, 17.0, 710.5)
            // 4. (-2412.5, 17.0, 748.5)

            // System.out.println("Creeper: " + event.entity.getPositionVector() + "; " + event.entity.getDistanceToEntity(mc.thePlayer) + " to P; " + VILLAGER_POS.distanceTo(event.entity.getPositionVector()) + " to V");

            // Ignore creeper "spawns" too far away from the player
            if (event.entity.getDistanceToEntity(mc.thePlayer) > 40) return;
            // Ignore creeper "spawns" too close to the villager (the creeper moved already)
            if (VILLAGER_POS.distanceTo(event.entity.getPositionVector()) < 37.5) return;
            // Creepers that just spawned reset the wave timer
            if (state != State.WAVE && state != State.END) {
                // New wave begins
                state = State.WAVE;

                blazes = 0;
                creepers = 0;
                zombies = 0;

                creeperSpawn = 0;
            }
            activateTimer5(false);
        } else if (event.entity instanceof EntityZombie) {
            zombies += 1;
            timer4 = 80;
        }
    }

    public void drawCenteredText(FontRenderer fr, String text, int x, int y, int scale, boolean fix) {
        GL11.glScalef(scale, scale, 1);
        int ox = x-(int)Math.ceil(fr.getStringWidth(text)/2.0);
        if (fix) ox += (text.length()-1)/2;
        fr.drawStringWithShadow(text, ox, y, 0xFFFFFFFF);
    }

    public void render(int screenWidth, int screenHeight) {
        if (ConfigData.disableHUD) return;

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        GL11.glColor4f(1, 1, 1, 1);

        if (ConfigData.rotations.length > 0 && state == State.INTERMISSION && wave >= 49) {
            GL11.glPushMatrix();

            GL11.glTranslatef(screenWidth/2+ConfigData.rotXOffset, screenHeight/2+ConfigData.rotYOffset, 0);
            drawCenteredText(fr, String.format(ConfigData.rotationText, ConfigData.rotations[(wave+1) % ConfigData.rotations.length]), 0, 0, ConfigData.rotScale, true);

            GL11.glPopMatrix();
        }

        if (renderLines != null) {
            GL11.glPushMatrix();

            for (int i = 0; i < renderLines.size(); ++i) {
                drawCenteredText(fr, renderLines.get(i), screenWidth/2+1+ConfigData.infoXOffset, fr.FONT_HEIGHT*i+ConfigData.infoYOffset, ConfigData.infoScale, false);
            }

            GL11.glPopMatrix();
        }
        GlStateManager.disableAlpha();

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}
