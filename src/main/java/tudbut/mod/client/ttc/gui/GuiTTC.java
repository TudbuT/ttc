package tudbut.mod.client.ttc.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.mods.ClickGUI;
import tudbut.mod.client.ttc.utils.Module;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class GuiTTC extends GuiScreen {
    
    // The buttons to be rendered (sub buttons are in the button object)
    // One button per module
    private Button[] buttons;
    
    // The mouse X and Y
    private int cx;
    private int cy;
    
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
        
        // Minecraft wants this
        super.buttonList.clear();
        super.buttonList.add(new GuiButton(0, -500, -500, ""));
        super.initGui();
    }
    
    // When ESC is pressed
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        ClickGUI.getInstance().enabled = false;
        // Minecraft wants this
        mc.mouseHelper.grabMouseCursor();
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
            int x = j / 6;
            int y = j - x * 6;
            
            // Don't add the button if it isn't requested
            if (!TTC.modules[i].displayOnClickGUI())
                continue;
            
            // Create the button
            int r = i;
            Button b = new Button(
                    10 + (210 * x), 10 + (y * 40), TTC.modules[r].getClass().getSimpleName() + ": " + TTC.modules[r].enabled,
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
        }
        
        // Render the button
        public void draw(GuiTTC gui) {
            if (!display)
                return;
            
            drawRect(x, y, x + 200, y + 30, color);
            gui.drawString(gui.fontRenderer, text.get(), x + 10, y + 11, 0xffffffff);
            
            // Draw sub buttons
            if (module != null && module.enabled) {
                subButtons = module.subButtons.toArray(new Button[0]);
                
                for (int i = 0; i < subButtons.length; i++) {
                    Button b = subButtons[i];
                    b.x = x;
                    b.y = y + ((i + 1) * 30);
                    b.color = 0x4000ff00;
                    b.draw(gui);
                }
            }
        }
        
        public boolean mouseClicked(int clickX, int clickY, int button) {
            if (clickX >= x && clickY >= y) {
                if (clickX <= x + 200 && clickY <= y + 30) {
                    mouseDown = true;
                    mouseDownButton = button;
                    click(button);
                    return true;
                }
            }
            if (module != null && module.enabled) {
                subButtons = module.subButtons.toArray(new Button[0]);
                
                for (int i = 0; i < subButtons.length; i++) {
                    if (subButtons[i].mouseClicked(clickX, clickY, button))
                        return true;
                }
            }
            return false;
        }
        
        public void mouseReleased() {
            mouseDown = false;
            if (module != null && module.enabled) {
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
        }
        
        protected void onTick(GuiTTC gui) {
            if (module != null) {
                if (mouseDown && mouseDownButton == 1) {
                    x = gui.cx - 100;
                    y = gui.cy - 15;
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
