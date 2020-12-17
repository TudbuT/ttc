package tudbut.mod.client.yac.mods;

import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.utils.Module;

public class TPAParty extends Module {
    
    @Override
    public void onTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public void onServerChat(String s, String formatted) {
        if(s.contains("has requested to teleport to you.") && !s.startsWith("<")) {
            Yac.player.sendChatMessage("/tpaccept");
        }
    }
}
