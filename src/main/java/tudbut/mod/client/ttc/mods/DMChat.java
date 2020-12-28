package tudbut.mod.client.ttc.mods;

import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;

import java.util.Arrays;

public class DMChat extends Module {
    public static DMChat instance;
    {
        instance = this;
    }
    
    public static DMChat getInstance() {
        return instance;
    }
    
    public String[] users = new String[0];
    
    @Override
    public void onTick() {
    }
    
    @Override
    public void onChat(String s, String[] args) {
        users = args;
    }
    
    @Override
    public void onServerChat(String s, String formatted) {
        try {
            String name = Arrays.stream(users).filter(
                    theName ->
                            s.startsWith(theName + " whispers:") ||
                            s.startsWith("~" + theName + " whispers:") ||
                            s.startsWith(theName + " whispers to you:") ||
                            s.startsWith("~" + theName + " whispers to you:") ||
                            s.startsWith("From " + theName + ":") ||
                            s.startsWith("From ~" + theName + ":")
            ).iterator().next();
            if(name != null) {
                ChatUtils.print("<" + name + "> " + s.substring(s.indexOf(": ") + 2));
            }
        } catch (Exception ignore) { }
    }
    
    @Override
    public void loadConfig() {
        users = cfg.get("users").split(" ");
    }
    
    @Override
    public String saveConfig() {
        boolean b = enabled;
        enabled = false;
        
        return super.saveConfig() + ((enabled = b) ? "" : "");
    }
    
    @Override
    public void updateConfig() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < users.length; i++) {
            s.append(users[i]);
            s.append(" ");
        }
        if(s.length() >= 1)
            s.deleteCharAt(s.length() - 1);
        cfg.put("users", s.toString());
    }
}
