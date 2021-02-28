package tudbut.mod.client.ttc.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Mouse;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.mods.AltControl;
import tudbut.mod.client.ttc.mods.ClickGUI;
import tudbut.mod.client.ttc.utils.Module;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class GuiPlayerSelect extends GuiScreen {
    
    private GuiPlayerSelect.Button[] buttons;
    private EntityPlayer[] players;
    ButtonClickEvent event;
    
    // The mouse X and Y
    private int cx;
    private int cy;
    
    public GuiPlayerSelect(EntityPlayer[] players, ButtonClickEvent onClick) {
        this.mc = TTC.mc;
        event = onClick;
        this.players = players;
    }
    
    // Minecraft wants this
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    // The initiator, this can, for some reason, not be in the constructor
    public void initGui() {
        // Minecraft is stupid.
        mc.mouseHelper.ungrabMouseCursor();
        while (Mouse.isGrabbed())
            mc.mouseHelper.ungrabMouseCursor();
        
        // Creates buttons
        buttons = new GuiPlayerSelect.Button[256];
        resetButtons();
        
        // Minecraft wants this
        super.buttonList.clear();
        super.buttonList.add(new GuiButton(0, -500, -500, ""));
        super.initGui();
    }
    
    // When ESC is pressed
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }
    
    // Called every tick, idk why its called update tho
    @Override
    public void updateScreen() {
        // Minecraft is stupid and sometimes forgets to call initScreen, so this is needed
        while (buttons == null) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (buttons == null)
                resetButtons();
        }
    
        for (int i = 0; i < buttons.length; i++) {
            if(buttons[i] != null)
                buttons[i].onTick();
        }
    }
    
    // Reset the buttons array
    private void resetButtons() {
        System.out.println("Resetting buttons on PlayerSelectGUI");
        for (int i = 0, j = 0; i < players.length; i++) {
            int x = j / 8;
            int y = j - x * 8;
            
            // Create the button
            int r = i;
            GuiPlayerSelect.Button b = new GuiPlayerSelect.Button(
                    10 + (160 * x), 10 + (y * 30), players[r].getName(),
                    (text) -> {
                        EntityPlayer player = players[r];
                        if(event.run(player)) {
                            close();
                        }
                    }
            );
            buttons[i] = b;
            
            j++;
        }
    }
    
    // Reset text on the buttons
    private void updateButtons() {
        while (buttons == null) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (buttons == null)
                resetButtons();
        }
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != null)
                buttons[i].text.set(players[i].getName());
        }
    }
    
    // Called when the user presses a mouse button
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        // Update cx and cy
        cx = mouseX;
        cy = mouseY;
        
        // Notify buttons
        for (GuiPlayerSelect.Button button : buttons) {
            if (button != null)
                if (button.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return;
                }
        }
    }
    
    public void close() {
        onGuiClosed();
        TTC.mc.displayGuiScreen(null);
    }
    
    
    
    // Update cx and cy
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        cx = mouseX;
        cy = mouseY;
    }
    
    // Called when the user releases a mouse button
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        
        // Update cx and cy
        cx = mouseX;
        cy = mouseY;
    }
    
    // Render the screen
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateButtons();
        
        this.drawDefaultBackground();
        
        cx = mouseX;
        cy = mouseY;
        
        // Ask the buttons to render themselves
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != null)
                buttons[i].draw(this);
        }
        
        // TMP fix for a strange bug that causes the mouse to be hidden
        if (ClickGUI.getInstance().mouseFix) {
            drawRect(mouseX - 2, mouseY - 2, mouseX + 2, mouseY + 2, 0xffffffff);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    
    public static class Button {
        public int x, y;
        public AtomicReference<String> text;
        // Color for rendering
        public int color = 0x8000ff00;
        // Called when the button is clicked
        GuiTTC.ButtonClickEvent event;
        
        // Constructor used by GuiTTC to construct a button with an associated module
        // and main constructor
        public Button(int x, int y, String text, GuiTTC.ButtonClickEvent event) {
            this.x = x;
            this.y = y;
            this.text = new AtomicReference<>(text);
            this.event = event;
            this.color = ClickGUI.getInstance().getTheme().buttonColor;
        }
        
        // Render the button
        public void draw(GuiPlayerSelect gui) {
            int color = this.color;
            
            if (gui.cx >= x && gui.cy >= y && gui.cx <= x + 150 && gui.cy <= y + 20) {
                Color c = new Color(color, true);
                int r, g, b, a;
                r = c.getRed();
                g = c.getGreen();
                b = c.getBlue();
                a = c.getAlpha();
                r += 0x20;
                g += 0x20;
                b += 0x20;
                a += 0x20;
                color = new Color(Math.min(r, 0xff),Math.min(g, 0xff),Math.min(b, 0xff),Math.min(a, 0xff)).getRGB();
            }
            
            drawRect(x, y, x + 150, y + 20, color);
            gui.fontRenderer.drawString(text.get(), x + 6, y + 6, ClickGUI.getInstance().getTheme().textColor, ClickGUI.getInstance().getTheme().shadow);
        }
        
        public boolean mouseClicked(int clickX, int clickY, int button) {
            if (clickX >= x && clickY >= y) {
                if (clickX <= x + 150 && clickY <= y + 20) {
                    click(button);
                    return true;
                }
            }
            return false;
        }
        
        // More simple onCLick, only called when the mouse is clicked while on the button
        protected void click(int button) {
            if (button == 0)
                event.run(text);
        }
    
        public void onTick() {
            this.color = ClickGUI.getInstance().getTheme().buttonColor;
        }
    }
    
    public interface ButtonClickEvent {
        boolean run(EntityPlayer player);
    }
}
