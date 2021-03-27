package tudbut.mod.client.ttc.utils;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
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
    public boolean clickGuiShow = false;
    public Integer clickGuiX;
    public Integer clickGuiY;
    public KeyBind key = new KeyBind(null, () -> {
        enabled = !enabled;
        if(enabled)
            onEnable();
        else
            onDisable();
    });
    public ArrayList<GuiTTC.Button> subButtons = new ArrayList<>();
    public Map<String, KeyBind> customKeyBinds = new HashMap<>();
    private GuiTTC.Button[] confirmationButtons = new GuiTTC.Button[3];
    
    {
        confirmationButtons[0] = new GuiTTC.Button("Are you sure?", text -> {});
        confirmationButtons[1] = new GuiTTC.Button("Yes", text -> {
            displayConfirmation = false;
            onConfirm(true);
        });
        confirmationButtons[2] = new GuiTTC.Button("No", text -> {
            displayConfirmation = false;
            onConfirm(false);
        });
    }
    private GuiTTC.Button keyButton = Setting.createKey("KeyBind: $val", key);
    
    
    public Module() {
        index = cIndex;
        cIndex++;
    }
    
    protected boolean displayConfirmation = false;
    
    public final GuiTTC.Button[] getSubButtons() {
        if(displayConfirmation)
            return confirmationButtons;
        ArrayList<GuiTTC.Button> buttons = (ArrayList<GuiTTC.Button>) subButtons.clone();
        buttons.add(keyButton);
        return buttons.toArray(new GuiTTC.Button[0]);
    }
    
    // Defaults to override
    public boolean defaultEnabled() {
        return false;
    }
    
    public boolean doStoreEnabled() {
        return true;
    }
    
    public boolean displayOnClickGUI() {
        return true;
    }
    
    // Event listeners
    public void onSubTick() { }
    
    public void onEverySubTick()                     { }
    
    public void init()                               { }
    
    public void onTick()                             { }
    
    public void onEveryTick()                        { }
    
    public void onConfirm(boolean result)            { }
    
    public void onChat(String s, String[] args)      { }
    
    public void onEveryChat(String s, String[] args) { }
    
    public void onEnable()                           { }
    
    public void onDisable() { }
    
    public boolean onServerChat(String s, String formatted) {
        return false;
    }
    
    // Loads the config from a file, use loadConfig without arguments when overriding
    public void loadConfig(Map<String, String> map) {
        cfg = map;
        
        if(doStoreEnabled())
            enabled = Boolean.parseBoolean(cfg.get("enabled"));
        
        if(cfg.containsKey("clickGuiShow"))
            clickGuiShow = Boolean.parseBoolean(cfg.get("clickGuiShow"));
        
        clickGuiX = null;
        clickGuiY = null;
        if (cfg.containsKey("cgx") && cfg.containsKey("cgy")) {
            clickGuiX = Integer.parseInt(cfg.get("cgx"));
            clickGuiY = Integer.parseInt(cfg.get("cgy"));
        }
        
        if(cfg.containsKey("key")) {
            key.key = Integer.parseInt(cfg.get("key"));
        }
    
        if (cfg.containsKey("ckb")) {
            Map<String, String> ckb = Utils.stringToMap(map.get("ckb"));
            
            for (String kb : ckb.keySet()) {
                if(customKeyBinds.containsKey(kb))
                    customKeyBinds.get(kb).key = Integer.parseInt(ckb.get(kb));
            }
        }
        keyButton = Setting.createKey("KeyBind: $val", key);
        
        loadConfig();
        
        if (enabled)
            onEnable();
        else
            onDisable();
    }
    
    public int danger() {
        return 0;
    }
    
    // Saves the settings to the cfg map and returns it as string,
    // used in the TTC class for saving the config as file, not intended for overriding,
    // but can give advantages to do so
    public String saveConfig() {
        updateConfig();
        
        if(doStoreEnabled())
            cfg.put("enabled", String.valueOf(enabled));
        cfg.put("clickGuiShow", String.valueOf(clickGuiShow));
        if (clickGuiX != null && clickGuiY != null) {
            cfg.put("cgx", clickGuiX + "");
            cfg.put("cgy", clickGuiY + "");
        }
        
        if(key.key != null)
            cfg.put("key", key.key + "");
        else
            cfg.remove("key");
        
        Map<String, String> ckb = new HashMap<>();
        for (String kb : customKeyBinds.keySet()) {
            if(customKeyBinds.get(kb).key != null)
                ckb.put(kb, String.valueOf(customKeyBinds.get(kb).key));
        }
        cfg.put("ckb", Utils.mapToString(ckb));
        
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
    
    public static class KeyBind {
        public Integer key;
        public boolean down = false;
        public Runnable onPress;
        
        public KeyBind(Integer key, Runnable onPress) {
            this.key = key;
            this.onPress = onPress;
        }
        
        public void onTick() {
            if(key != null && TTC.mc.currentScreen == null) {
                if (Keyboard.isKeyDown(key)) {
                    if(!down) {
                        down = true;
                        if(onPress != null)
                            onPress.run();
                    }
                }
                else
                    down = false;
            }
            else
                down = false;
        }
    }
}
