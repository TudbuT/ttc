package tudbut.mod.client.yac.mods;

import tudbut.mod.client.yac.utils.Module;

public class ChatSuffix extends Module {
    public static ChatSuffix getInstance() {
        return instance;
    }
    
    private static ChatSuffix instance;
    
    public ChatSuffix() {
        instance = this;
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
