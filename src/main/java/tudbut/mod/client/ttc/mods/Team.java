package tudbut.mod.client.ttc.mods;

import net.minecraft.client.network.NetworkPlayerInfo;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.ThreadManager;
import tudbut.mod.client.ttc.utils.Utils;

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
        subButtons.add(new GuiTTC.Button("Accept /tpa: " + tpa, text -> {
            tpa = !tpa;
            text.set("Accept /tpa: " + tpa);
        }));
        subButtons.add(new GuiTTC.Button("Accept /tpahere: " + tpaHere, text -> {
            tpaHere = !tpaHere;
            text.set("Accept /tpahere: " + tpaHere);
        }));
        subButtons.add(new GuiTTC.Button("Send /tpahere (/tpahere)", text -> {
            onChat("", new String[] {
                    "tpahere"
            });
        }));
        subButtons.add(new GuiTTC.Button("Send /tpahere (/tpa)", text -> {
            onChat("", new String[] {
                    "here"
            });
        }));
        subButtons.add(new GuiTTC.Button("Send DM", text -> {
            ChatUtils.print("§c§lUse " + TTC.prefix + "team dm <message>");
        }));
        subButtons.add(new GuiTTC.Button("Show list", text -> {
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
                TTC.player.sendChatMessage("/tell " + args[1] + " TTC[2]");
                break;
            case "remove":
                names.remove(args[1]);
                ChatUtils.print("Done!");
                TTC.player.sendChatMessage("/tell " + args[1] + " TTC[3]");
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
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        if(names.contains(info.getGameProfile().getName())) {
                            try {
                                TTC.mc.player.sendChatMessage("/tpahere " + info.getGameProfile().getName());
                                ChatUtils.print("Sent to " + info.getGameProfile().getName());
                                Thread.sleep(TPATools.getInstance().delay);
                            }
                            catch (Throwable ignore) { }
                        }
                    }
                    ChatUtils.print("Done!");
                });
                break;
            case "here":
                ChatUtils.print("Sending...");
                ThreadManager.run(() -> {
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        if(names.contains(info.getGameProfile().getName())) {
                            try {
                                TTC.mc.player.sendChatMessage("/tell " + info.getGameProfile().getName() + " TTC[0]");
                                ChatUtils.print("Sent to " + info.getGameProfile().getName());
                                Thread.sleep(TPATools.getInstance().delay);
                            }
                            catch (Throwable ignore) { }
                        }
                    }
                    ChatUtils.print("Done!");
                });
                break;
            case "go":
                ThreadManager.run(() -> {
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        if(info.getGameProfile().getName().equals(args[1])) {
                            try {
                                TTC.mc.player.sendChatMessage("/tell " + info.getGameProfile().getName() + " TTC[1]");
                            }
                            catch (Throwable ignore) { }
                        }
                    }
                    ChatUtils.print("Sent!");
                });
                break;
            case "dm":
                ChatUtils.print("Sending...");
                ThreadManager.run(() -> {
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        if(names.contains(info.getGameProfile().getName())) {
                            try {
                                TTC.mc.player.sendChatMessage("/tell " + info.getGameProfile().getName() + " " + s.substring("dm ".length()));
                                ChatUtils.print("Sent to " + info.getGameProfile().getName());
                                Thread.sleep(TPATools.getInstance().delay);
                            }
                            catch (Throwable ignore) { }
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
        names.remove(TTC.player.getName());
        names.add(TTC.player.getName());
    }
    
    @Override
    public boolean onServerChat(String s, String formatted) {
        if(tpa && s.contains("has requested to teleport to you.") && names.stream().anyMatch(name -> s.startsWith(name + " ") || s.startsWith("~" + name + " "))) {
            TTC.player.sendChatMessage("/tpaccept");
        }
        if(tpaHere && s.contains("has requested that you teleport to them.") && names.stream().anyMatch(name -> s.startsWith(name + " ") || s.startsWith("~" + name + " "))) {
            TTC.player.sendChatMessage("/tpaccept");
        }
        
        try {
            String name = names.stream().filter(
                    theName ->
                            s.startsWith(theName + " whispers:") ||
                            s.startsWith("~" + theName + " whispers:") ||
                            s.startsWith(theName + " whispers to you:") ||
                            s.startsWith("~" + theName + " whispers to you:") ||
                            s.startsWith("From " + theName + ":") ||
                            s.startsWith("From ~" + theName + ":")
            ).iterator().next();
            if(name != null) {
                String msg = s.split(": ")[1];
                if (msg.startsWith("TTC")) {
                    if(msg.substring("TTC".length()).equals("[0]") && tpaHere) {
                        TTC.player.sendChatMessage("/tpa " + name);
                        ChatUtils.print("Sent TPA to " + name + ".");
                    }
                    if(msg.substring("TTC".length()).equals("[1]") && tpa) {
                        TTC.player.sendChatMessage("/tpahere " + name);
                        ChatUtils.print("Sent TPAHere to " + name + ".");
                    }
                    return true;
                }
                
                ChatUtils.print("§b§lDM from Team member: " + s.substring(s.indexOf(":") + 1));
                return true;
            }
            for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                String theName = info.getGameProfile().getName();
                if (s.startsWith(theName + " whispers:") ||
                    s.startsWith("~" + theName + " whispers:") ||
                    s.startsWith(theName + " whispers to you:") ||
                    s.startsWith("~" + theName + " whispers to you:") ||
                    s.startsWith("From " + theName + ":") ||
                    s.startsWith("From ~" + theName + ":")) {
                    try {
                        String msg = s.split(": ")[1];
                        if (msg.startsWith("TTC")) {
                            if(msg.substring("TTC".length()).equals("[2]")) {
                                ChatUtils.print("§c§lYou have been added to the Team of " + theName + "! \n" +
                                                "§cRun ,team add " + theName + " to add them as well!");
                            }
                            if(msg.substring("TTC".length()).equals("[3]")) {
                                ChatUtils.print("§c§lYou have been removed from the Team of " + theName + "! \n" +
                                                "§cRun ,team add " + theName + " to remove them as well!");
                            }
                            return true;
                        }
                    }
                    catch (Throwable ignore) { }
                }
            }
        } catch (Exception ignore) { }
        return false;
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
