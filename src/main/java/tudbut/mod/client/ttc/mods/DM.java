package tudbut.mod.client.ttc.mods;

import net.minecraft.client.network.NetworkPlayerInfo;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.ThreadManager;

import java.util.Objects;

public class DM extends Module {
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
        ThreadManager.run(() -> {
            for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap()) {
                try {
                    TTC.mc.player.sendChatMessage("/tell " + info.getGameProfile().getName() + " " + s);
                    ChatUtils.print("Sent to " + info.getGameProfile().getName());
                    Thread.sleep(TPATools.getInstance().delay);
                }
                catch (Throwable ignore) { }
            }
            ChatUtils.print("Done!");
        });
    }
}
