package tudbut.mod.client.ttc.utils;

import net.minecraft.client.gui.Gui;
import tudbut.obj.Vector2i;
// Almost But Not Entirely Unlike Good Code.
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;

public class FontRenderer extends Gui {
    
    Font myFont;
    FontRenderContext context;
    
    public FontRenderer(int size) {
        this.myFont = new Font("serif", Font.PLAIN, size);
        this.context = new FontRenderContext(null, false, false);
    }
    
    public int getTextWidth(String text) {
        int r = 0;
        
        for (int i = 0; i < text.split("\n").length; ++i) {
            r += (int) this.myFont.getStringBounds(text.split("\n")[i], this.context).getWidth() + 4;
        }
        
        return r;
    }
    
    public int getTextHeight(String text) {
        int r = 0;
        
        for (int i = 0; i < text.split("\n").length; ++i) {
            r += (int) this.myFont.getStringBounds(text.split("\n")[i], this.context).getHeight() + 4;
        }
        
        return r;
    }
    
    public void drawText(String text, int color, int x, int y) {
        //Renderer.draw(x, y, renderText(text, color));
    }
    
    public Image renderText(String text, int color) {
        BufferedImage image = new BufferedImage(this.getTextWidth(text), this.getTextHeight(text), 2);
        Graphics graphics = image.getGraphics();
        graphics.setColor(new Color(color));
        graphics.setFont(this.myFont);
        
        for (int i = 0; i < text.split("\n").length; ++i) {
            graphics.drawString(text.split("\n")[i], 0, (this.myFont.getSize() + 4) * (i + 1));
        }
        
        return image;
    }
    
    public Vector2i getCoordsForCentered(Vector2i coords, String text) {
        return new Vector2i(coords.getX() - this.getTextWidth(text) / 2, coords.getY() - this.getTextHeight(text) / 2);
    }
    
}
