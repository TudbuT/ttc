package tudbut.mod.client.ttc.mods;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.ThreadManager;

public class ClickGUI extends Module {
    
    static ClickGUI instance;
    
    public static ClickGUI getInstance() {
        return instance;
    }
    
    public ClickGUI() {
        instance = this;
    }
    
    {
        subButtons.add(new GuiTTC.Button("Reset layout", text -> {
            enabled = false;
            onDisable();
            for (Module module : TTC.modules) {
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
            TTC.mc.displayGuiScreen(new GuiTTC(TTC.mc.currentScreen));
        });
    }
    
    @Override
    public void onDisable() {
        if (TTC.mc.currentScreen != null && TTC.mc.currentScreen.getClass() == GuiTTC.class)
            TTC.mc.displayGuiScreen(null);
    }
    
    @Override
    public void onTick() {
    }
    
    @Override
    public void onEveryTick() {
        if(Keyboard.isKeyDown(Keyboard.KEY_COMMA) && TTC.mc.currentScreen == null) {
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
