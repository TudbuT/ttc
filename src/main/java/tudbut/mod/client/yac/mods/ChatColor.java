package tudbut.mod.client.yac.mods;

import tudbut.mod.client.yac.gui.GuiYAC;
import tudbut.mod.client.yac.utils.Module;

public class ChatColor extends Module {
    
    private boolean useSpace = false;
    
    static ChatColor instance;
    
    public ChatColor() {
        instance = this;
    }
    
    public static ChatColor getInstance() {
        return instance;
    }
    
    public String get() {
        return (enabled ? (useSpace ? "> " : ">") : "");
    }
    
    public void updateButtons() {
        subButtons.clear();
        subButtons.add(new GuiYAC.Button("Add space: " + useSpace, text -> {
            useSpace = !useSpace;
            text.set("Add space: " + useSpace);
        }));
    }
    
    @Override
    public void onTick() {
    
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
}
