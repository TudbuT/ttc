package tudbut.mod.client.yac.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.utils.Module;

import java.io.IOException;

public class GuiYAC extends GuiScreen {
    
    private final GuiScreen parentGuiScreen;
    
    private volatile Button[] buttons;
    
    private int cx;
    private int cy;
    
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
                buttons[i].onTick();
        }
    }
    
    private void resetButtons() {
        for (int i = 0; i < Yac.modules.length; i++) {
            int finalI = i;
            Button b = new Button(
                    10, 10 + (i * 40), Yac.modules[i].getClass().getSimpleName() + ": " + Yac.modules[i].enabled,
                    button -> {
                        if(button == 0) {
                            if(Yac.modules[finalI].enabled = !Yac.modules[finalI].enabled)
                                Yac.modules[finalI].onEnable();
                            else
                                Yac.modules[finalI].onDisable();
                        }
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
            buttons[i].text = Yac.modules[i].getClass().getSimpleName() + ": " + Yac.modules[i].enabled;
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
    protected void actionPerformed(GuiButton button) {
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
                buttons[i].draw();
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    public class Button {
        public int x, y;
        public String text;
        ButtonClickEvent event;
        private boolean mouseDown = false;
        private int mouseDownButton = 0;
        public int color = 0x8000ff00;
        public Module module;
        
        public Button(int x, int y, String text, ButtonClickEvent event, Module module) {
            if(module.clickGuiX != null && module.clickGuiY != null) {
                x = module.clickGuiX;
                y = module.clickGuiY;
            }
            this.x = x;
            this.y = y;
            this.text = text;
            this.event = event;
            this.module = module;
        }
        
        public void draw() {
            drawRect(x, y, x + 200, y + 30, color);
            drawString(fontRenderer, text, x + 10, y + 11, 0xffffffff);
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
            return false;
        }
    
        public void mouseReleased() {
            mouseDown = false;
        }
    
        protected void click(int button) {
            if(button == 0)
                event.run(button);
        }
        
        protected void onTick() {
            if(mouseDown && mouseDownButton == 1) {
                x = cx - 100;
                y = cy - 15;
            }
            module.clickGuiX = x;
            module.clickGuiY = y;
        }
        
    }
    
    public interface ButtonClickEvent {
        void run(int button);
    }
}
