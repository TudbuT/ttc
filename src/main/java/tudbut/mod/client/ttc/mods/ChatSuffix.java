package tudbut.mod.client.ttc.mods;

import tudbut.mod.client.ttc.utils.Module;

// Placeholder module, code is in FMLEventHandler
public class ChatSuffix extends Module {
    private static ChatSuffix instance;
    
    public ChatSuffix() {
        instance = this;
    }
    
    public static ChatSuffix getInstance() {
        return instance;
    }
    
    @Override
    public boolean defaultEnabled() {
        return true;
    }
    
    @Override
    public void onTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
}
