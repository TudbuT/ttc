package tudbut.mod.client.ttc.mods;

import net.minecraft.client.gui.ScaledResolution;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTCIngame;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.obj.Vector2i;

public class HUD extends Module {
    
    static HUD instance;
    
    public HUD() {
        instance = this;
    }
    
    public static HUD getInstance() {
        return instance;
    }
    
    public void renderHUD() {
        if(enabled) {
            GuiTTCIngame.draw();
            if(AutoTotem.getInstance().enabled)
                AutoTotem.instance.renderTotems();
        }
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    public static class HUDElement {
    
        
        
        
    }
    
    /*
    public enum Anchor {
        TL,
        TR,
        BL,
        BR,
        
        ;
        
        public Vector2i getCoords(Vector2i offset) {
            ScaledResolution sr = new ScaledResolution(TTC.mc);
            Vector2i screenSize = new Vector2i(sr.getScaledWidth(), sr.getScaledHeight());
            
            switch (this) {
                case TL:
                    return new Vector2i(0,0).add(offset);
                case TR:
                    return new Vector2i(sr.getScaledWidth() / 2, 0).add(offset);
                case BL:
                    return new Vector2i(0, screenSize.getY() / 2).add(offset);
                case BR:
                    return new Vector2i(screenSize.getX() / 2, screenSize.getY() / 2).add(offset);
            }
            
            return new Vector2i(0,0);
        }
    }*/
}
