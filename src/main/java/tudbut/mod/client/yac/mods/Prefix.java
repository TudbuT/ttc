package tudbut.mod.client.yac.mods;

import tudbut.mod.client.yac.YAC;
import tudbut.mod.client.yac.utils.Module;

public class Prefix extends Module {
    {
        enabled = true;
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
        YAC.prefix = s;
    }
}
