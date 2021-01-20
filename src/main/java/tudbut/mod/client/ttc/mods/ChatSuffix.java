package tudbut.mod.client.ttc.mods;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;

// Placeholder module, code is in FMLEventHandler
public class ChatSuffix extends Module {
    String suffix = "";
    int mode = 0;
    public int chance = 100;
    
    private static ChatSuffix instance;
    
    public ChatSuffix() {
        instance = this;
    }
    
    public static ChatSuffix getInstance() {
        return instance;
    }
    
    {updateButtons();}
    private void updateButtons() {
        subButtons.clear();
        subButtons.add(new GuiTTC.Button("Chance: " + chance + "%", text -> {
            
            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                chance -= 5;
            else
                chance += 5;
            
            if(chance > 100)
                chance = 0;
            if(chance < 0)
                chance = 100;
            
            text.set("Chance: " + chance + "%");
        }));
        subButtons.add(new GuiTTC.Button("Mode:" + (mode == -1 ? " CUSTOM" : get(100)), text -> {
        
            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                mode--;
            else
                mode++;
        
            if(mode > 5)
                mode = 0;
            if(mode < 0)
                mode = 5;
        
            text.set("Mode:" + get(100));
        }));
    }
    
    public String get(int chance) {
        if(!enabled)
            return "";
        
        if(Math.random() < chance / 100d) {
            if (mode == -1)
                return " " + suffix;
            else {
                switch (mode) {
                    case 0:
                        return " ›TTC‹";
                    case 1:
                        return " »TTC«";
                    case 2:
                        return " ‹TTC›";
                    case 3:
                        return " «TTC»";
                    case 4:
                        return " | TTC";
                    case 5:
                        return " → TTC";
                }
            }
        }
        return "";
    }
    
    @Override
    public void loadConfig() {
        suffix = cfg.get("suffix");
        mode = Integer.parseInt(cfg.get("mode"));
        chance = Integer.parseInt(cfg.get("chance"));
    
        updateButtons();
    }
    
    @Override
    public void updateConfig() {
        cfg.put("suffix", suffix);
        cfg.put("mode", mode + "");
        cfg.put("chance", chance + "");
    }
    
    @Override
    public void onSubTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
        suffix = s;
        mode = -1;
        ChatUtils.print("Done!");
        
        updateButtons();
    }
    
    @Override
    public int danger() {
        return 2;
    }
}
