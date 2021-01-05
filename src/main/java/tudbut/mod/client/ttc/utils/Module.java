package tudbut.mod.client.ttc.utils;

import tudbut.mod.client.ttc.gui.GuiTTC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Module {
    // Collection of event listeners and config loader/saver
    
    // Stuff for the construction of the module
    private static int cIndex = 0;
    // Module config
    public Map<String, String> cfg = new HashMap<>();
    public int index;
    
    public boolean enabled = defaultEnabled();
    public Integer clickGuiX;
    public Integer clickGuiY;
    public ArrayList<GuiTTC.Button> subButtons = new ArrayList<>();
    
    public Module() {
        index = cIndex;
        cIndex++;
    }
    
    // Defaults to override
    public boolean defaultEnabled() {
        return false;
    }
    
    public boolean displayOnClickGUI() {
        return true;
    }
    
    // Event listeners
    public abstract void onTick();
    
    public void onEveryTick() { }
    
    public abstract void onChat(String s, String[] args);
    
    public void onEveryChat(String s, String[] args) { }
    
    public void onEnable() { }
    
    public void onDisable() { }
    
    public boolean onServerChat(String s, String formatted) {
        return false;
    }
    
    // Loads the config from a file, use loadConfig without arguments when overriding
    public void loadConfig(Map<String, String> map) {
        cfg = map;
        enabled = Boolean.parseBoolean(cfg.get("enabled"));
        clickGuiX = null;
        clickGuiY = null;
        if (cfg.containsKey("cgx") && cfg.containsKey("cgy")) {
            clickGuiX = Integer.parseInt(cfg.get("cgx"));
            clickGuiY = Integer.parseInt(cfg.get("cgy"));
            System.out.println(clickGuiX);
            System.out.println(clickGuiY);
        }
        
        loadConfig();
        
        if (enabled)
            onEnable();
        else
            onDisable();
    }
    
    // Saves the settings to the cfg map and returns it as string,
    // used in the TTC class for saving the config as file, not intended for overriding,
    // but can give advantages to do so
    public String saveConfig() {
        updateConfig();
        
        cfg.put("enabled", String.valueOf(enabled));
        if (clickGuiX != null && clickGuiY != null) {
            cfg.put("cgx", clickGuiX + "");
            cfg.put("cgy", clickGuiY + "");
        }
        
        return Utils.mapToString(cfg);
    }
    
    // Intended for overriding
    public void loadConfig() { }
    
    public void updateConfig() { }
    
    // Return the module name
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
