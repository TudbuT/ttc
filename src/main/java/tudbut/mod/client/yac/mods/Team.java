package tudbut.mod.client.yac.mods;

import net.minecraft.client.network.NetworkPlayerInfo;
import tudbut.mod.client.yac.YAC;
import tudbut.mod.client.yac.gui.GuiYAC;
import tudbut.mod.client.yac.utils.ChatUtils;
import tudbut.mod.client.yac.utils.Module;
import tudbut.mod.client.yac.utils.ThreadManager;
import tudbut.mod.client.yac.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Team extends Module {
    
    static Team instance;
    
    public static Team getInstance() {
        return instance;
    }
    
    public Team() {
        instance = this;
    }
    
    public final ArrayList<String> names = new ArrayList<>();
    private boolean tpa = true;
    private boolean tpaHere = false;
    
    {
        subButtons.add(new GuiYAC.Button("Accept /tpa: " + tpa, text -> {
            tpa = !tpa;
            text.set("Accept /tpa: " + tpa);
        }));
        subButtons.add(new GuiYAC.Button("Accept /tpahere: " + tpaHere, text -> {
            tpaHere = !tpaHere;
            text.set("Accept /tpahere: " + tpaHere);
        }));
        subButtons.add(new GuiYAC.Button("Send /tpahere", text -> {
            onChat("", new String[] {
                    "tpahere"
            });
        }));
        subButtons.add(new GuiYAC.Button("Show list", text -> {
            onChat("", new String[] {
                    "list"
            });
        }));
    }
    
    public void updateButtons() {
        subButtons.get(0).text.set("Accept /tpa: " + tpa);
        subButtons.get(1).text.set("Accept /tpahere: " + tpaHere);
    }
    
    @Override
    public void onTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
        switch (args[0].toLowerCase()) {
            case "add":
                names.remove(args[1]);
                names.add(args[1]);
                ChatUtils.print("Done!");
                break;
            case "remove":
                names.remove(args[1]);
                ChatUtils.print("Done!");
                break;
            case "settpa":
                tpa = Boolean.parseBoolean(args[1]);
                ChatUtils.print("Done!");
                break;
            case "settpahere":
                tpaHere = Boolean.parseBoolean(args[1]);
                ChatUtils.print("Done!");
                break;
            case "tpahere":
                ChatUtils.print("Sending...");
                ThreadManager.run(() -> {
                    for (NetworkPlayerInfo info : Objects.requireNonNull(YAC.mc.getConnection()).getPlayerInfoMap()) {
                        if(names.contains(info.getGameProfile().getName())) {
                            try {
                                YAC.mc.player.sendChatMessage("/tpahere " + info.getGameProfile().getName());
                                ChatUtils.print("Sent to " + info.getGameProfile().getName());
                            }
                            catch (Throwable e) { }
                            try {
                                Thread.sleep(1000);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    ChatUtils.print("Done!");
                });
                break;
            case "settings":
                ChatUtils.print("TPA: " + (tpa ? "enabled" : "disabled"));
                ChatUtils.print("TPAhere: " + (tpaHere ? "enabled" : "disabled"));
            case "list":
                StringBuilder toPrint = new StringBuilder("Team members: ");
                for (String name : names) {
                    toPrint.append(name).append(", ");
                }
                if(names.size() >= 1)
                    toPrint.delete(toPrint.length() - 2, toPrint.length() - 1);
                ChatUtils.print(toPrint.toString());
                break;
        }
        updateButtons();
        names.remove(YAC.player.getName());
        names.add(YAC.player.getName());
    }
    
    @Override
    public void onServerChat(String s, String formatted) {
        if(tpa && s.contains("has requested to teleport to you.") && names.stream().anyMatch(name -> s.startsWith(name + " ") || s.startsWith("~" + name + " "))) {
            YAC.player.sendChatMessage("/tpaccept");
        }
        if(tpaHere && s.contains("has requested that you teleport to them.") && names.stream().anyMatch(name -> s.startsWith(name + " ") || s.startsWith("~" + name + " "))) {
            YAC.player.sendChatMessage("/tpaccept");
        }
    }
    
    @Override
    public void loadConfig() {
        Map<String, String> map = Utils.stringToMap(cfg.get("team"));
        for (String key : map.keySet()) {
            names.add(Integer.parseInt(key), map.get(key));
        }
        tpa = Boolean.parseBoolean(cfg.getOrDefault("tpa", "true"));
        tpaHere = Boolean.parseBoolean(cfg.getOrDefault("tpahere", "true"));
        updateButtons();
    }
    
    @Override
    public void updateConfig() {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            map.put(String.valueOf(i), names.get(i));
        }
        cfg.put("team", Utils.mapToString(map));
        cfg.put("tpa", "" + tpa);
        cfg.put("tpahere", "" + tpaHere);
    }
}
