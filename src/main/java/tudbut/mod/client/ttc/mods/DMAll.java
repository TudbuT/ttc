package tudbut.mod.client.ttc.mods;

import net.minecraft.client.network.NetworkPlayerInfo;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.ThreadManager;

import java.util.Objects;

public class DMAll extends Module {
    {
        enabled = true;
    }
    
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onTick() {
    }
    
    @Override
    public void onEveryTick() {
        enabled = true;
    }
    
    @Override
    public void onChat(String s, String[] args) {
        ChatUtils.print("Sending...");
        
        // This would stop the game if it wasn't in a separate thread
        ThreadManager.run(() -> {
            // Loop through all players
            for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                try {
                    // Send a DM to the player
                    TTC.mc.player.sendChatMessage("/tell " + info.getGameProfile().getName() + " " + s);
                    // Notify the user
                    ChatUtils.print("Sent to " + info.getGameProfile().getName());
                    // I hate antispam
                    Thread.sleep(TPATools.getInstance().delay);
                }
                catch (Throwable ignore) { }
            }
            ChatUtils.print("Done!");
        });
    }
}
