package tudbut.mod.client.ttc.mods;

import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.Module;

public class TPAParty extends Module {
    
    static TPAParty instance;
    
    public static TPAParty getInstance() {
        return instance;
    }
    
    public TPAParty() {
        instance = this;
    }
    
    @Override
    public void onTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public boolean onServerChat(String s, String formatted) {
        if(s.contains("has requested to teleport to you.") && !s.startsWith("<")) {
            TTC.player.sendChatMessage("/tpaccept");
        }
        return false;
    }
}
