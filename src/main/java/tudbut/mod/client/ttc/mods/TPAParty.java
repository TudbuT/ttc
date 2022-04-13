package tudbut.mod.client.ttc.mods;

import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.Setting;

public class TPAParty extends Module {
    
    static TPAParty instance;
    public boolean disableOnDeath = true;



    public TPAParty() {
        instance = this;
    }
    
    public static TPAParty getInstance() {
        return instance;
    }

    public void updateButtons() {
        subButtons.clear();
        subButtons.add(Setting.createBoolean("DeathDisable: $val", this, "disableOnDeath"));
    }

    @Override
    public void onSubTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public boolean onServerChat(String s, String formatted) {
        if (s.contains("/tpaccept") && !s.startsWith("<")) {
            // Accept TPA requests
            TTC.player.sendChatMessage("/tpaccept");
        }
        return false;
    }
    
    @Override
    public int danger() {
        return 4;
    }

    @Override
    public void updateConfig() {
        cfg.put("dod", disableOnDeath + "");
    }

    @Override
    public void loadConfig() {
        disableOnDeath = "true".equals(cfg.get("dod"));
    }
}
