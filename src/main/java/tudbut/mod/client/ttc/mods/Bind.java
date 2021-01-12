package tudbut.mod.client.ttc.mods;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
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
    public void onTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public void onEveryChat(String s, String[] args) {
        for (int i = 0; i < TTC.modules.length; i++) {
            if (args[0].startsWith(TTC.modules[i].getClass().getSimpleName().toLowerCase())) {
                if(args.length == 2)
                    TTC.modules[i].key = Keyboard.getKeyIndex(args[1]);
                else
                    TTC.modules[i].key = null;
            }
        }
    }
}
