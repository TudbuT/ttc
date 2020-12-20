package tudbut.mod.client.yac.mods;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.gui.GuiYAC;
import tudbut.mod.client.yac.utils.ChatUtils;
import tudbut.mod.client.yac.utils.Module;
import tudbut.mod.client.yac.utils.ThreadManager;

public class ClickGUI extends Module {
    
    static ClickGUI instance;
    
    public ClickGUI() {
        instance = this;
    }
    
    public static ClickGUI getInstance() {
        return instance;
    }
    
    @Override
    public void onEnable() {
        ThreadManager.run(() -> {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            ChatUtils.print("Showing ClickGUI");
            Yac.mc.displayGuiScreen(new GuiYAC(Yac.mc.currentScreen));
        });
    }
    
    @Override
    public void onDisable() {
        if (Yac.mc.currentScreen != null && Yac.mc.currentScreen.getClass() == GuiYAC.class)
            Yac.mc.displayGuiScreen(null);
    }
    
    @Override
    public void onTick() {
    }
    
    @Override
    public void onEveryTick() {
        if (Keyboard.isKeyDown(Keyboard.KEY_COMMA) && Yac.mc.currentScreen == null) {
            if (!enabled) {
                enabled = true;
                onEnable();
            }
        }
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public String saveConfig() {
        boolean b = enabled;
        enabled = false;
        
        return super.saveConfig() + ((enabled = b) ? "" : "");
    }
}
