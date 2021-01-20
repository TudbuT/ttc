package tudbut.mod.client.ttc.mods;

import tudbut.mod.client.ttc.gui.GuiTTCIngame;
import tudbut.mod.client.ttc.utils.Module;

public class HUD extends Module {
    
    static HUD instance;
    
    public HUD() {
        instance = this;
    }
    
    public static HUD getInstance() {
        return instance;
    }
    
    public void renderHUD() {
        if(enabled)
            GuiTTCIngame.draw();
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
}
