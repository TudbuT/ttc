package tudbut.mod.client.ttc.mods;

import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.Module;

public class Prefix extends Module {
    {
        enabled = true;
    }
    
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onTick() {
    }
    
    @Override
    public void onEveryTick() {
        enabled = true;
    }
    
    @Override
    public void onChat(String s, String[] args) {
        TTC.prefix = s;
    }
}
