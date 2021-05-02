package tudbut.mod.client.ttc.mods;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.FakeSkidUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.Utils;
import tudbut.obj.Save;

import java.io.IOException;

public class ClickGUI extends Module {
    
    static ClickGUI instance;
    // TMP fix for mouse not showing
    public boolean mouseFix = false;
    
    public boolean flipButtons = false;
    
    public int themeID = 0;
    
    public GuiTTC.ITheme skidTheme = FakeSkidUtils.theme;
    
    public GuiTTC.ITheme getTheme() {
        if(skidTheme != null)
            return skidTheme;
        return GuiTTC.Theme.values()[themeID];
    }
    
    private int confirmInstance = 0;
    
    {
        updateButtons();
    }
    
    public ClickGUI() {
        instance = this;
    }
    
    public static ClickGUI getInstance() {
        return instance;
    }
    
    @Override
    public void onEveryTick() {
        if(key.key == null) {
            key.key = Keyboard.KEY_COMMA;
            updateButtons();
        }
    }
    private void updateButtons() {
        subButtons.clear();
        subButtons.add(new GuiTTC.Button("Flip buttons: " + flipButtons, text -> {
            flipButtons = !flipButtons;
            text.set("Flip buttons: " + flipButtons);
        }));
        subButtons.add(new GuiTTC.Button("Theme: " + getTheme(), text -> {
            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                themeID--;
            else
                themeID++;
        
            if(themeID < 0)
                themeID = GuiTTC.Theme.values().length - 1;
            if(themeID > GuiTTC.Theme.values().length - 1)
                themeID = 0;
        
            text.set("Theme: " + getTheme());
        }));
        subButtons.add(new GuiTTC.Button("Reset layout", text -> {
            displayConfirmation = true;
            confirmInstance = 0;
        }));
        subButtons.add(new GuiTTC.Button("Mouse fix: " + mouseFix, text -> {
            mouseFix = !mouseFix;
            text.set("Mouse fix: " + mouseFix);
        }));
        subButtons.add(new GuiTTC.Button("Reset client", text -> {
            displayConfirmation = true;
            confirmInstance = 1;
        }));
    }
    
    @Override
    public void onEnable() {
        ChatUtils.print("§a[TTC] §rShowing ClickGUI");
        TTC.mc.displayGuiScreen(new GuiTTC());
    }
    
    @Override
    public void onConfirm(boolean result) {
        if (result)
            switch (confirmInstance) {
                case 0:
                    // Reset ClickGUI by closing it, resetting its values, and opening it
                    enabled = false;
                    onDisable();
                    for (Module module : TTC.modules) {
                        module.clickGuiX = null;
                        module.clickGuiY = null;
                    }
                    enabled = true;
                    onEnable();
                    break;
                case 1:
                    displayConfirmation = true;
                    confirmInstance = 2;
                    break;
                case 2:
                    enabled = false;
                    onDisable();
                    for (int i = 0; i < TTC.modules.length; i++) {
                        Class<? extends Module> clazz = TTC.modules[i].getClass();
                        try {
                            TTC.modules[i].onDisable();
                            TTC.modules[i].enabled = false;
                            TTC.modules[i] = clazz.newInstance();
                            TTC.cfg.put(clazz.getSimpleName(), TTC.modules[i].saveConfig());
                        }
                        catch (InstantiationException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    // Saving global config
                    TTC.cfg.put("prefix", ",");
    
                    // Saving file
                    try {
                        TTC.file.setContent(Utils.mapToString(cfg));
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    Minecraft.getMinecraft().shutdown();
                    break;
            }
    }
    
    @Override
    public void onDisable() {
        // Kill the GUI
        if (TTC.mc.currentScreen != null && TTC.mc.currentScreen.getClass() == GuiTTC.class)
            TTC.mc.displayGuiScreen(null);
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public void loadConfig() {
        mouseFix = Boolean.parseBoolean(cfg.get("mouseFix"));
        flipButtons = Boolean.parseBoolean(cfg.get("flipButtons"));
        themeID = Integer.parseInt(cfg.get("theme"));
        
        if(key.key == null)
            key.key = Keyboard.KEY_COMMA;
        
        updateButtons();
    }
    
    @Override
    public void updateConfig() {
        cfg.put("mouseFix", String.valueOf(mouseFix));
        cfg.put("flipButtons", flipButtons + "");
        cfg.put("theme", themeID + "");
    }
    
    @Override
    public String saveConfig() {
        boolean b = enabled;
        enabled = false;
        
        return super.saveConfig() + ((enabled = b) ? "" : "");
    }
}
