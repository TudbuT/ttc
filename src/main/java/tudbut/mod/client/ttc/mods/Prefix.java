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
    public void onSubTick() {
    }
    
    @Override
    public void onEverySubTick() {
        enabled = true;
    }
    
    @Override
    public void onChat(String s, String[] args) {
        // Set the prefix
        TTC.prefix = s;
    }
}
