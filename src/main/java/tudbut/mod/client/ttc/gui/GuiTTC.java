package tudbut.mod.client.ttc.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.mods.ClickGUI;
import tudbut.mod.client.ttc.utils.Module;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class GuiTTC extends GuiScreen {
    
    // The buttons to be rendered (sub buttons are in the button object)
    // One button per module
    private Button[] buttons;
    
    // Theme
    public enum Theme {
        TTC(0x8000ff00, 0x4000ff00),
        BARTENDER(0xff2d1537, 0xff0d0a0a),
        ETERNAL_BLUE(0xff0000ff, 0xff000080),
        DARK(0xff202020, 0xff000000),
        LIGHT(0xffcccccc, 0xff999999, 0xff000000, false),
        HACKER(0xff202020, 0xff000000, 0xff00ff00),
        BLOOD(0xffaa0000, 0xff880000, 0xff00ffff, false),
        SKY(0xff00cccc, 0xff009999, 0x000000, false),
        KAMI_BLUE(0xbb353642, 0xbb353642, 0xffbbbbbb, false),
        SCHLONGHAX(0xbb553662, 0xbb553662, 0xffbbbbbb, false),
        ORANGE(0xffcc8000, 0xff996000, 0xff404040, false),
        
        ;
        
        public final int buttonColor;
        public final int subButtonColor;
        public final int textColor;
        public final boolean shadow;
        
        Theme(int buttonColor, int subButtonColor) {
            this.buttonColor = buttonColor;
            this.subButtonColor = subButtonColor;
            this.textColor = 0xffffffff;
            this.shadow = true;
        }
        Theme(int buttonColor, int subButtonColor, int textColor) {
            this.buttonColor = buttonColor;
            this.subButtonColor = subButtonColor;
            this.textColor = textColor;
            this.shadow = true;
        }
        Theme(int buttonColor, int subButtonColor, int textColor, boolean shadow) {
            this.buttonColor = buttonColor;
            this.subButtonColor = subButtonColor;
            this.textColor = textColor;
            this.shadow = shadow;
        }
    }
    
    // The mouse X and Y
    private int cx;
    private int cy;
    private int lastScrollPos = Mouse.getEventDWheel();
    
    public GuiTTC() {
        this.mc = TTC.mc;
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
        buttons = new Button[256];
        resetButtons();
        
        // Minecraft wants this
        super.buttonList.clear();
        super.buttonList.add(new GuiButton(0, -500, -500, ""));
        super.initGui();
        lastScrollPos = Mouse.getEventDWheel();
    }
    
    // When ESC is pressed
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        ClickGUI.getInstance().enabled = false;
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
        // Call onTick on every button
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != null)
                buttons[i].onTick(this);
        }
    }
    
    // Reset the buttons array
    private void resetButtons() {
        System.out.println("Resetting buttons on ClickGUI");
        for (int i = 0, j = 0; i < TTC.modules.length; i++) {
            int x = j / 10;
            int y = j - x * 10;
            
            // Don't add the button if it isn't requested
            if (!TTC.modules[i].displayOnClickGUI())
                continue;
            
            // Create the button
            int r = i;
            Button b = new Button(
                    10 + (155 * x), 10 + (y * 25), TTC.modules[r].getClass().getSimpleName() + ": " + TTC.modules[r].enabled,
                    (text) -> {
                        if (TTC.modules[r].enabled = !TTC.modules[r].enabled)
                            TTC.modules[r].onEnable();
                        else
                            TTC.modules[r].onDisable();
                        
                    }, TTC.modules[i]
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
        for (int i = 0; i < TTC.modules.length; i++) {
            if (buttons[i] != null)
                buttons[i].text.set(TTC.modules[i].getClass().getSimpleName() + ": " + TTC.modules[i].enabled);
        }
    }
    
    // Called when the user presses a mouse button
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        // Update cx and cy
        cx = mouseX;
        cy = mouseY;
        
        // Notify buttons
        for (Button button : buttons) {
            if (button != null)
                if (button.mouseClicked(mouseX, mouseY, mouseButton))
                    return;
        }
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
        
        // Notify buttons
        for (Button button : buttons) {
            if (button != null)
                button.mouseReleased();
        }
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
        int m = -Mouse.getDWheel();
        if(m != 0) {
            for (int i = 0; i < buttons.length; i++) {
                if(buttons[i] != null) {
                    buttons[i].x += (lastScrollPos - m) / 3;
                }
            }
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    
    public static class Button {
        public int x, y;
        public AtomicReference<String> text;
        // Color for rendering
        public int color = 0x8000ff00;
        // The associated module, can be null if it is a sub button
        public Module module;
        // Called when the button is clicked
        ButtonClickEvent event;
        // If any mouse button is pressed
        private boolean mouseDown = false;
        // The mouse button that is pressed
        private int mouseDownButton = 0;
        // The sub buttons of the button, null if no module is associated to provide them
        private Button[] subButtons;
        
        private boolean display = true;
        
        // Constructor used for sub buttons
        public Button(String text, ButtonClickEvent event) {
            this(0, 0, text, event, null);
        }
        
        // Constructor used by GuiTTC to construct a button with an associated module
        // and main constructor
        public Button(int x, int y, String text, ButtonClickEvent event, Module module) {
            if (module != null) {
                if (module.clickGuiX != null && module.clickGuiY != null) {
                    x = module.clickGuiX;
                    y = module.clickGuiY;
                }
                subButtons = module.subButtons.toArray(new Button[0]);
                display = module.displayOnClickGUI();
            }
            this.x = x;
            this.y = y;
            this.text = new AtomicReference<>(text);
            this.event = event;
            this.module = module;
            if(ClickGUI.getInstance() != null)
                this.color = ClickGUI.getInstance().getTheme().buttonColor;
        }
        
        // Render the button
        public void draw(GuiTTC gui) {
            if (!display)
                return;
            
            int color = this.color;
            
            if (gui.cx >= x && gui.cy >= y && gui.cx <= x + 150 && gui.cy <= y + ySize()) {
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
            
            drawRect(x, y, x + 150, y + ySize(), color);
            gui.fontRenderer.drawString(text.get(), x + 6, y + ySize() / 2f - 8 / 2f, ClickGUI.getInstance().getTheme().textColor, ClickGUI.getInstance().getTheme().shadow);
            
            // Draw sub buttons
            if (module != null && (module.enabled ^ module.clickGuiShow)) {
                subButtons = module.getSubButtons();
                
                for (int i = 0; i < subButtons.length; i++) {
                    Button b = subButtons[i];
                    if(b != null) {
                        b.x = x;
                        b.y = y + ( ( i + 1 ) * 15 + ( 20 - 15 ) );
                        b.color = ClickGUI.getInstance().getTheme().subButtonColor;
                        b.draw(gui);
                    }
                }
            }
        }
        
        public int ySize() {
            return module == null ? 15 : 20;
        }
        
        public boolean mouseClicked(int clickX, int clickY, int button) {
            if (clickX >= x && clickY >= y) {
                if (clickX < x + 150 && clickY < y + ySize()) {
                    mouseDown = true;
                    if(ClickGUI.getInstance().flipButtons) {
                        button = (button == 0 ? 1 : (button == 1 ? 0 : button));
                    }
                    mouseDownButton = button;
                    click(button);
                    return true;
                }
            }
            if (module != null && (module.enabled ^ module.clickGuiShow)) {
                subButtons = module.getSubButtons();
                
                for (int i = 0; i < subButtons.length; i++) {
                    Button b = subButtons[i];
                    if(b != null) {
                        b.x = x;
                        b.y = y + ( ( i + 1 ) * 15 + ( 20 - 15 ) );
                        b.color = ClickGUI.getInstance().getTheme().subButtonColor;
                        if (b.mouseClicked(clickX, clickY, button))
                            return true;
                    }
                }
            }
            return false;
        }
        
        public void mouseReleased() {
            mouseDown = false;
            if (module != null && (module.enabled ^ module.clickGuiShow)) {
                subButtons = module.subButtons.toArray(new Button[0]);
                
                for (int i = 0; i < subButtons.length; i++) {
                    subButtons[i].mouseReleased();
                }
            }
            
        }
        
        // More simple onCLick, only called when the mouse is clicked while on the button
        protected void click(int button) {
            if (button == 0)
                event.run(text);
            if (button == 2 && module != null)
                module.clickGuiShow = !module.clickGuiShow;
        }
        
        protected void onTick(GuiTTC gui) {
            this.color = ClickGUI.getInstance().getTheme().buttonColor;
            if (module != null) {
                if (mouseDown && mouseDownButton == 1) {
                    x = gui.cx - 150 / 2;
                    y = gui.cy - 10;
                    x = (x / 5) * 5;
                    y = (y / 5) * 5;
                }
                module.clickGuiX = x;
                module.clickGuiY = y;
            }
        }
        
    }
    
    public interface ButtonClickEvent {
        void run(AtomicReference<String> text);
    }
}
