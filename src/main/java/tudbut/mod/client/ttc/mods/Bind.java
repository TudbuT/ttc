package tudbut.mod.client.ttc.mods;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;

public class Bind extends Module {
    @Override
    public boolean defaultEnabled() {
        return true;
    }
    
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onSubTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public void onEveryChat(String s, String[] args) {
        if(s.equals("help")) {
            
            ChatUtils.print("§a§lBinds");
            for (int i = 0; i < TTC.modules.length; i++) {
                ChatUtils.print("§aModule: " + TTC.modules[i].toString());
                if(TTC.modules[i].key.key != null)
                    ChatUtils.print("State: " + Keyboard.getKeyName(TTC.modules[i].key.key));
                for (String kb : TTC.modules[i].customKeyBinds.keySet()) {
                    if(TTC.modules[i].customKeyBinds.get(kb).key != null)
                        ChatUtils.print("Function " + kb + ": " + Keyboard.getKeyName(TTC.modules[i].customKeyBinds.get(kb).key));
                }
            }
            
            return;
        }
        
        for (int i = 0; i < TTC.modules.length; i++) {
            if (args[0].equalsIgnoreCase(TTC.modules[i].getClass().getSimpleName().toLowerCase())) {
                if(args.length == 2) {
                    int key = Keyboard.getKeyIndex(args[1].toUpperCase());
                    if(key == Keyboard.KEY_NONE) {
                        TTC.modules[i].customKeyBinds.get(args[1]).key = null;
                    }
                    else
                        TTC.modules[i].key.key = key;
                }
                else if (args.length == 3) {
                    if (TTC.modules[i].customKeyBinds.containsKey(args[1])) {
                        TTC.modules[i].customKeyBinds.get(args[1]).key = Keyboard.getKeyIndex(args[2].toUpperCase());
                    }
                }
                else
                    TTC.modules[i].key.key = null;
            }
        }
    }
}
