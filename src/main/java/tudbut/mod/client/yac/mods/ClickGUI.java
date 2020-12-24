package tudbut.mod.client.yac.mods;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.yac.YAC;
import tudbut.mod.client.yac.gui.GuiYAC;
import tudbut.mod.client.yac.utils.ChatUtils;
import tudbut.mod.client.yac.utils.Module;
import tudbut.mod.client.yac.utils.ThreadManager;

public class ClickGUI extends Module {
    
    static ClickGUI instance;
    
    public static ClickGUI getInstance() {
        return instance;
    }
    
    public ClickGUI() {
        instance = this;
    }
    
    {
        subButtons.add(new GuiYAC.Button("Reset layout", text -> {
            enabled = false;
            onDisable();
            for (Module module : YAC.modules) {
                module.clickGuiX = null;
                module.clickGuiY = null;
            }
            enabled = true;
            onEnable();
        }));
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
            YAC.mc.displayGuiScreen(new GuiYAC(YAC.mc.currentScreen));
        });
    }
    
    @Override
    public void onDisable() {
        if (YAC.mc.currentScreen != null && YAC.mc.currentScreen.getClass() == GuiYAC.class)
            YAC.mc.displayGuiScreen(null);
    }
    
    @Override
    public void onTick() {
    }
    
    @Override
    public void onEveryTick() {
        if(Keyboard.isKeyDown(Keyboard.KEY_COMMA) && YAC.mc.currentScreen == null) {
            if(!enabled) {
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
