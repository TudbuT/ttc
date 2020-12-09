package tudbut.mod.client.yac.utils;

import tudbut.mod.client.yac.Yac;

import java.util.HashMap;
import java.util.Map;

public abstract class Module {
    
    public Map<String, String> cfg;
    
    public boolean enabled = false;
    
    {
        cfg = new HashMap<>();
        cfg = Utils.stringToMap(saveConfig());
    }
    
    public abstract void onTick();
    
    public abstract void onChat(String s);
    
    public void loadConfig(Map<String, String> map) {
        cfg = map;
        enabled = Boolean.parseBoolean(cfg.get("enabled"));
    }
    
    public String saveConfig() {
        cfg.put("enabled", String.valueOf(enabled));
        
        return Utils.mapToString(cfg);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
