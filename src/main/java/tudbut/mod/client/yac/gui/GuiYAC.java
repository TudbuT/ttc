package tudbut.mod.client.yac.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.utils.Module;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class GuiYAC extends GuiScreen {
    
    private final GuiScreen parentGuiScreen;
    
    private Button[] buttons;
    
    private int cx,
            cy;
    
    public GuiYAC(GuiScreen parentScreenIn)
    {
        this.mc = Yac.mc;
        this.parentGuiScreen = parentScreenIn;
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttons = new Button[256];
        
        resetButtons();
        //this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height - 27, I18n.format("gui.done", new Object[0])));
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Yac.modules[3].enabled = false;
    }
    
    @Override
    public void updateScreen() {
        while(buttons == null) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(buttons == null)
                resetButtons();
        }
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != null)
                buttons[i].onTick(this);
        }
    }
    
    private void resetButtons() {
        for (int i = 0; i < Yac.modules.length; i++) {
            int finalI = i;
            Button b = new Button(
                    10, 10 + (i * 40), Yac.modules[i].getClass().getSimpleName() + ": " + Yac.modules[i].enabled,
                    (text) -> {
                        if(Yac.modules[finalI].enabled = !Yac.modules[finalI].enabled)
                            Yac.modules[finalI].onEnable();
                        else
                            Yac.modules[finalI].onDisable();
                        
                    }, Yac.modules[i]
            );
            
            buttons[i] = b;
        }
    }
    
    
    private void updateButtons() {
        while(buttons == null) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(buttons == null)
                resetButtons();
        }
        for (int i = 0; i < Yac.modules.length; i++) {
            if(buttons[i] == null)
                return;
            buttons[i].text.set(Yac.modules[i].getClass().getSimpleName() + ": " + Yac.modules[i].enabled);
        }
    }
    
    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
    }
    
    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 200)
            {
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }
        }
    }
    
    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        cx = mouseX;
        cy = mouseY;
        
        for (Button button : buttons) {
            if(button != null)
                if(button.mouseClicked(mouseX, mouseY, mouseButton))
                    return;
        }
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        cx = mouseX;
        cy = mouseY;
    }
    
    /**
     * Called when a mouse button is released.  Args : mouseX, mouseY, releaseButton
     */
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        
        
        for (Button button : buttons) {
            if(button != null)
                button.mouseReleased();
        }
    }
    
    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        updateButtons();
        
        this.drawDefaultBackground();
        //this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 5, 16777215);
        
        //for (int i = 0; i < Yac.modules.length; i++) {
        //drawRect(10, 10 + (i * 40), 10 + 200, 10 + (i * 40) + 30, 0x8000ff00);
        //}
        
        for (int i = 0; i < buttons.length; i++) {
            if(buttons[i] != null)
                buttons[i].draw(this);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    public static class Button {
        public int x, y;
        public AtomicReference<String> text;
        ButtonClickEvent event;
        private boolean mouseDown = false;
        private int mouseDownButton = 0;
        public int color = 0x8000ff00;
        public Module module;
        private Button[] subButtons;
        
        public Button(int x, int y, String text, ButtonClickEvent event, Module module) {
            if(module != null) {
                if(module.clickGuiX != null && module.clickGuiY != null) {
                    x = module.clickGuiX;
                    y = module.clickGuiY;
                }
                subButtons = module.subButtons.toArray(new Button[0]);
            }
            this.x = x;
            this.y = y;
            this.text = new AtomicReference<>(text);
            this.event = event;
            this.module = module;
        }
        
        public void draw(GuiYAC gui) {
            drawRect(x, y, x + 200, y + 30, color);
            gui.drawString(gui.fontRenderer, text.get(), x + 10, y + 11, 0xffffffff);
            
            if(subButtons != null) {
                if(module != null && module.enabled) {
                    for (int i = 0; i < subButtons.length; i++) {
                        Button b = subButtons[i];
                        b.x = x;
                        b.y = y + ((i + 1) * 30);
                        b.color = 0x4000ff00;
                        b.draw(gui);
                    }
                }
            }
        }
        
        public boolean mouseClicked(int clickX, int clickY, int button) {
            if(clickX >= x && clickY >= y) {
                if(clickX <= x + 200 && clickY <= y + 30) {
                    mouseDown = true;
                    mouseDownButton = button;
                    click(button);
                    return true;
                }
            }
            if(subButtons != null) {
                if(module != null && module.enabled) {
                    for (int i = 0; i < subButtons.length; i++) {
                        subButtons[i].mouseClicked(clickX, clickY, button);
                    }
                }
            }
            return false;
        }
        
        public void mouseReleased() {
            mouseDown = false;
            if(subButtons != null) {
                if(module != null && module.enabled) {
                    for (int i = 0; i < subButtons.length; i++) {
                        subButtons[i].mouseReleased();
                    }
                }
            }
        }
        
        protected void click(int button) {
            if(button == 0)
                event.run(text);
        }
        
        protected void onTick(GuiYAC gui) {
            if (module != null) {
                if(mouseDown && mouseDownButton == 1) {
                    x = gui.cx - 100;
                    y = gui.cy - 15;
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
