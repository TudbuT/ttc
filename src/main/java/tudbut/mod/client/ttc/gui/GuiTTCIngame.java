package tudbut.mod.client.ttc.gui;

import de.tudbut.type.Vector3d;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.FontRenderer;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.obj.Vector2i;

import java.awt.*;

public class GuiTTCIngame extends Gui {
    
    static FontRenderer fontRenderer = new FontRenderer(6);
    
    public static void draw() {
        new GuiTTCIngame().drawImpl();
    }
    
    public void drawImpl() {
        ScaledResolution sr = new ScaledResolution(TTC.mc);
        Vector2i screenSize = new Vector2i(sr.getScaledWidth(), sr.getScaledHeight());
    
        int y = sr.getScaledHeight() - (5 + TTC.mc.fontRenderer.FONT_HEIGHT);
        int x = screenSize.getX() - 5;
    
        if(!TTC.isIngame())
            return;
    
        y = drawPos(TTC.player, "Player", x, y);
        if(TTC.mc.getRenderViewEntity() != TTC.player)
            y = drawPos(TTC.mc.getRenderViewEntity(), "Camera", x, y);
        
        y-=10;
        
        for (int i = 0; i < TTC.modules.length; i++) {
            Module module = TTC.modules[i];
            
            if(module.enabled && module.displayOnClickGUI()) {
                int color = 0x000000;
                
                switch (module.danger()) {
                    case 0:
                        color = 0x00ff00;
                        break;
                    case 1:
                        color = 0x80ff00;
                        break;
                    case 2:
                        color = 0xffff00;
                        break;
                    case 3:
                        color = 0xff8000;
                        break;
                    case 4:
                        color = 0xff0000;
                        break;
                    case 5:
                        color = 0xff00ff;
                        break;
                }
                
                
                drawString(module.toString(), x, y, color);
                y-=10;
            }
        }
    }
    
    private void drawString(String s, int x, int y, int color) {
        drawString(
                TTC.mc.fontRenderer,
                s,
                x - TTC.mc.fontRenderer.getStringWidth(s),
                y,
                color
        );
    }
    
    private int drawPos(Entity e, String s, int x, int y) {
        Vector3d p = new Vector3d(e.posX, e.posY, e.posZ);
        
        p.setX(Math.round(p.getX() * 10d) / 10d);
        p.setY(Math.round(p.getY() * 10d) / 10d);
        p.setZ(Math.round(p.getZ() * 10d) / 10d);
    
        if(TTC.mc.world.provider.getDimension() == -1)
            drawString(
                    s + " Overworld " + Math.round(p.getX() * 8 * 10d) / 10d + " " + Math.round(p.getY() * 8 * 10d) / 10d + " " + Math.round(p.getZ() * 8 * 10d) / 10d,
                    x, y, 0xff00ff00
            );
        if(TTC.mc.world.provider.getDimension() == 0)
            drawString(
                    s + " Nether " + Math.round(p.getX() / 8 * 10d) / 10d + " " + Math.round(p.getY() / 8 * 10d) / 10d + " " + Math.round(p.getZ() / 8 * 10d) / 10d,
                    x, y, 0xff00ff00
            );
        y -= 10;
        drawString(s + " " + p.getX() + " " + p.getY() + " " + p.getZ(), x, y, 0xff00ff00);
        return y - 10;
    }
}
