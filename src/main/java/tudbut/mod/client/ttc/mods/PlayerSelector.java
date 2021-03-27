package tudbut.mod.client.ttc.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.tools.ArrayTools;

import java.util.ArrayList;
import java.util.Objects;

public class PlayerSelector extends Module {
    static Minecraft mc = Minecraft.getMinecraft();
    
    static int selected = 0;
    static boolean downDown = false;
    static boolean upDown = false;
    static boolean rightDown = false;
    static boolean leftDown = false;
    static boolean enterDown = false;
    static int selectedType = -1;
    public static boolean displayInRangeOnly = true;
    static NetworkPlayerInfo[] playersLastTick = new NetworkPlayerInfo[0];
    
    public static ArrayList<Type> types = new ArrayList<>();
    
    public static class Type {
        
        public final Callback callback;
        public final String displayName;
        
        public Type(Callback callback, String displayName) {
            this.callback = callback;
            this.displayName = displayName;
        }
    }
    
    public interface Callback {
        void run(NetworkPlayerInfo player);
    }
    
    public static void render() {
        ScaledResolution resolution = new ScaledResolution(mc);
        
        NetworkPlayerInfo[] players;
        
        if(displayInRangeOnly) {
            try {
                players = ArrayTools.getFromArray(mc.world.playerEntities.toArray(new EntityPlayer[0]), player -> mc.player.connection.getPlayerInfo(player.getUniqueID()));
            }
            catch (Throwable ignored) {
                players = new NetworkPlayerInfo[0];
            }
        }
        else {
            players = mc.player.connection.getPlayerInfoMap().toArray(new NetworkPlayerInfo[0]);
        }
        
        int x = resolution.getScaledWidth() / 6;
        int y = (int) (resolution.getScaledHeight() / 2.5 - Math.min(players.length * 10, 10) / 2);
        
        for (int i = 0; i < players.length; i++) {
            NetworkPlayerInfo player = players[i];
            if(player != null) {
                boolean b = false;
                for (int j = 0 ; j < playersLastTick.length ; j++) {
                    if(playersLastTick[j] != null)
                        if (playersLastTick[j].getGameProfile().getId().equals(player.getGameProfile().getId())) {
                            b = true;
                            break;
                        }
                }
                if (!b) {
                    if (selected >= i) {
                        selected++;
                    }
                }
            }
        }
        for (int i = 0; i < playersLastTick.length; i++) {
            NetworkPlayerInfo player = playersLastTick[i];
            if(player != null) {
                boolean b = false;
                for (int j = 0 ; j < players.length ; j++) {
                    if(players[j] != null)
                        if (players[j].getGameProfile().getId().equals(player.getGameProfile().getId())) {
                            b = true;
                            break;
                        }
                }
                if (!b) {
                    if (selected == i)
                        selectedType = -1;
                    if (selected > i) {
                        selected--;
                    }
                }
            }
        }
        
        
        Type[] types = PlayerSelector.types.toArray(new Type[0]);
        
        
        if (mc.currentScreen != null)
            selectedType = -1;
        
        if (selected >= players.length)
            selected = players.length - 1;
        if (selected < 0)
            selected = 0;
        if (selectedType >= types.length)
            selectedType = types.length - 1;
        
        if (selectedType == -1) {
            for (int i = Math.max(0, selected - 5); i < Math.min(selected + 5, players.length); i++) {
                if(players[i] != null) {
                    mc.fontRenderer.drawString(
                            (
                                    ( selected == i ? "§m| §f " : "| §f " ) +
                                    (
                                            players[i].getDisplayName() != null ?
                                            Objects.requireNonNull(players[i].getDisplayName()).getUnformattedText() :
                                            ScorePlayerTeam.formatPlayerName(
                                                    players[i].getPlayerTeam(),
                                                    players[i].getGameProfile().getName()
                                            )
                                    ) +
                                    ( selected == i ? "§r§c > " : "" )
                            ),
                            x, y, selected == i ? 0xff0000 : 0x00ff00
                    );
                    y += 10;
                }
            }
        }
        else {
            for (int i = 0 ; i < types.length; i++) {
                mc.fontRenderer.drawString(
                        (
                                (selectedType == i ? "< §m| §f " : "< | §f ") +
                                types[i].displayName
                        ),
                        x, y, selectedType == i ? 0xff0000 : 0x00ff00
                );
                y += 10;
            }
            
            if (Keyboard.isKeyDown(Keyboard.KEY_RETURN) && mc.currentScreen == null) {
                if (!enterDown) {
                    types[selectedType].callback.run(players[selected]);
                    selectedType = -1;
                }
                enterDown = true;
            }
            else {
                enterDown = false;
            }
        }
        
        if (Keyboard.isKeyDown(Keyboard.KEY_UP) && mc.currentScreen == null) {
            if (!upDown)
                if (selectedType == -1)
                    selected--;
                else if (selectedType != 0)
                    selectedType--;
            upDown = true;
        }
        else
            upDown = false;
        
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && mc.currentScreen == null) {
            if (!downDown)
                if (selectedType == -1)
                    selected++;
                else
                    selectedType++;
            downDown = true;
        }
        else
            downDown = false;
        
        
        if (selected >= players.length)
            selected = players.length - 1;
        if (selected < 0)
            selected = 0;
        if (selectedType >= types.length)
            selectedType = types.length - 1;
        
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) && mc.currentScreen == null) {
            if (!rightDown)
                selectedType = 0;
            rightDown = true;
        }
        else {
            rightDown = false;
        }
        
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) && mc.currentScreen == null) {
            if (!leftDown)
                selectedType = -1;
            leftDown = true;
        }
        else {
            leftDown = false;
        }
        
        playersLastTick = players;
    }
}
