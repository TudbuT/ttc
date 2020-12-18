package tudbut.mod.client.yac.utils;

import tudbut.mod.client.yac.gui.GuiYAC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Module {
    
    public Map<String, String> cfg = new HashMap<>();
    
    public boolean enabled = false;
    public Integer clickGuiX;
    public Integer clickGuiY;
    public ArrayList<GuiYAC.Button> subButtons = new ArrayList<>();
    
    public abstract void onTick();
    public void onEveryTick() {
    
    }
    
    public abstract void onChat(String s, String[] args);
    
    public void onEnable() {
    
    }
    
    public void onDisable() {
    
    }
    
    public void onServerChat(String s, String formatted) {
    
    }
    
    public void loadConfig(Map<String, String> map) {
        cfg = map;
        enabled = Boolean.parseBoolean(cfg.get("enabled"));
        clickGuiX = null;
        clickGuiY = null;
        if(cfg.containsKey("cgx") && cfg.containsKey("cgy")) {
            clickGuiX = Integer.parseInt(cfg.get("cgx"));
            clickGuiY = Integer.parseInt(cfg.get("cgy"));
        }
        
        loadConfig();
        
        if(enabled)
            onEnable();
        else
            onDisable();
    }
    
    public void loadConfig() {
    
    }
    
    public String saveConfig() {
        cfg.put("enabled", String.valueOf(enabled));
        if(clickGuiX != null && clickGuiY != null) {
            cfg.put("cgx", clickGuiX + "");
            cfg.put("cgy", clickGuiY + "");
        }
        
        return Utils.mapToString(cfg);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
