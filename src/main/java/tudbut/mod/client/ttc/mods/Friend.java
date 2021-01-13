package tudbut.mod.client.ttc.mods;

import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Friend extends Module {
    
    static Friend instance;
    public final ArrayList<String> names = new ArrayList<>();
    
    public Friend() {
        instance = this;
    }
    
    public static Friend getInstance() {
        return instance;
    }
    
    public void updateButtons() { }
    
    @Override
    public boolean defaultEnabled() {
        return true;
    }
    
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onEveryTick() {
        enabled = true;
    }
    
    @Override
    public void onTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
        switch (args[0].toLowerCase()) {
            case "add":
                // Add a player to the team
                names.remove(args[1]);
                names.add(args[1]);
                ChatUtils.print("Done!");
                break;
            case "remove":
                // Remove a player from the team
                names.remove(args[1]);
                ChatUtils.print("Done!");
                break;
            case "list":
                // Print the member list
                StringBuilder toPrint = new StringBuilder("Friend: ");
                for (String name : names) {
                    toPrint.append(name).append(", ");
                }
                if (names.size() >= 1)
                    toPrint.delete(toPrint.length() - 2, toPrint.length() - 1);
                ChatUtils.print(toPrint.toString());
                break;
        }
        
        // Updating stuff
        updateButtons();
        names.remove(TTC.player.getName());
        names.add(TTC.player.getName());
    }
    
    @Override
    public void loadConfig() {
        Map<String, String> map = Utils.stringToMap(cfg.get("team"));
        for (String key : map.keySet()) {
            names.add(Integer.parseInt(key), map.get(key));
        }
        updateButtons();
    }
    
    @Override
    public void updateConfig() {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            map.put(String.valueOf(i), names.get(i));
        }
        cfg.put("team", Utils.mapToString(map));
    }
}
