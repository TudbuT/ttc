package tudbut.mod.client.ttc.mods;

import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.Module;

public class ChatColor extends Module {
    static ChatColor instance;
    // Use "> " instead of ">"
    private boolean useSpace = false;
    
    {
        updateButtons();
    }
    
    public ChatColor() {
        instance = this;
    }
    
    public static ChatColor getInstance() {
        return instance;
    }
    
    // Return the correct string
    public String get() {
        return (enabled ? (useSpace ? "> " : ">") : "");
    }
    
    public void updateButtons() {
        subButtons.clear();
        subButtons.add(new GuiTTC.Button("Add space: " + useSpace, text -> {
            useSpace = !useSpace;
            text.set("Add space: " + useSpace);
        }));
    }
    
    @Override
    public void onSubTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public void updateConfig() {
        cfg.put("space", useSpace + "");
    }
    
    @Override
    public void loadConfig() {
        useSpace = Boolean.parseBoolean(cfg.get("space"));
        
        updateButtons();
    }
    
    @Override
    public int danger() {
        return 1;
    }
}
