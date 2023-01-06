package com.mathgeniuszach.cahud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mathgeniuszach.cahud.config.ConfigData;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClientEventHandler {

    private HudGui hudGui;
    private boolean inCreeperAttack = false;

    public ClientEventHandler() {
        hudGui = new HudGui();
    }

    public void updateInCreeperAttack() {
        // System.out.println("updating status");
        Minecraft minecraft = Minecraft.getMinecraft();

        inCreeperAttack = false;

        World world = minecraft.theWorld;
        if (world == null) return;
        Scoreboard scoreboard = world.getScoreboard();
        if (scoreboard == null) return;
        ScoreObjective objective = minecraft.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
        if (objective == null) return;

        String s = CAUtil.stripFormatting(objective.getDisplayName());
        if (s.equalsIgnoreCase("CREEPER ATTACK")) {
            inCreeperAttack = true;
            return;
        }

        List<String> lines = new ArrayList<String>();
        for (Score score : scoreboard.getSortedScores(objective)) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(CAUtil.stripFormatting(
                ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()).replace(score.getPlayerName(), "")
            ));
        }
        Collections.reverse(lines);

        if (lines.size() < 4) {
            inCreeperAttack = false;
            return;
        }
        inCreeperAttack = lines.get(3).startsWith("Trader Health");
    }

    @SubscribeEvent
    public void onEvent(RenderGameOverlayEvent.Post event) {
        try {
            if (event.type != ElementType.HOTBAR) return;
            if (ConfigData.disableAll) return;
            
            if (!inCreeperAttack) {
                updateInCreeperAttack();
                return;
            }

            hudGui.render(event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onEvent(ClientChatReceivedEvent event) {
        if (!inCreeperAttack) return;
        try {
            hudGui.chat(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onEvent(TickEvent.ClientTickEvent event) {
        if (!inCreeperAttack) return;
        try {
            hudGui.tick(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onEvent(EntityJoinWorldEvent event) {
        try {
            if (event.entity instanceof EntityPlayer) return;
            if (!inCreeperAttack) return;

            hudGui.entityJoin(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onEvent(WorldEvent.Load event) {
        try {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft == null || minecraft.thePlayer == null || minecraft.theWorld == null) return;
            updateInCreeperAttack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
