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
    // Team members
    public final ArrayList<String> names = new ArrayList<>();
    // What should be allowed to the team members?
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
            onChat("", new String[]{
                    "tpahere"
            });
        }));
        subButtons.add(new GuiTTC.Button("Send /tpahere (/tpa)", text -> {
            onChat("", new String[]{
                    "here"
            });
        }));
        subButtons.add(new GuiTTC.Button("Send DM", text -> {
            ChatUtils.print("§c§lUse " + TTC.prefix + "team dm <message>");
        }));
        subButtons.add(new GuiTTC.Button("Show list", text -> {
            onChat("", new String[]{
                    "list"
            });
        }));
    }
    public Team() {
        instance = this;
    }
    
    public static Team getInstance() {
        return instance;
    }
    
    public void updateButtons() {
        subButtons.get(0).text.set("Accept /tpa: " + tpa);
        subButtons.get(1).text.set("Accept /tpahere: " + tpaHere);
    }
    
    @Override
    public void onSubTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
        switch (args[0].toLowerCase()) {
            case "add":
                // Add a player to the team
                names.remove(args[1]);
                names.add(args[1]);
                ChatUtils.print("Done!");
                
                // If he also uses TTC, he'll be notified, if he doesn't, too bad!
                TTC.player.sendChatMessage("/tell " + args[1] + " TTC[2]");
                break;
            case "remove":
                // Remove a player from the team
                names.remove(args[1]);
                ChatUtils.print("Done!");
                
                // If he also uses TTC, he'll be notified, if he doesn't, too bad!
                TTC.player.sendChatMessage("/tell " + args[1] + " TTC[3]");
                break;
            case "settpa":
                // Enable/Disable TPA for Team members
                tpa = Boolean.parseBoolean(args[1]);
                ChatUtils.print("Done!");
                break;
            case "settpahere":
                // Enable/Disable TPAHere for Team members
                tpaHere = Boolean.parseBoolean(args[1]);
                ChatUtils.print("Done!");
                break;
            case "tpahere":
                // Send /tpahere to everyone in the team
                ChatUtils.print("Sending...");
                // This would stop the game if it wasn't in a separate thread
                ThreadManager.run(() -> {
                    // Loop through all players
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        // Is the player a team member?
                        if (names.contains(info.getGameProfile().getName())) {
                            try {
                                // Send to the player
                                TTC.mc.player.sendChatMessage("/tpahere " + info.getGameProfile().getName());
                                // Notify the user
                                ChatUtils.print("Sent to " + info.getGameProfile().getName());
                                // I hate antispam
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
                // This would stop the game if it wasn't in a separate thread
                ThreadManager.run(() -> {
                    // Loop through all players
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        // Is the player a team member?
                        if (names.contains(info.getGameProfile().getName())) {
                            try {
                                // Send to the player
                                TTC.mc.player.sendChatMessage("/tell " + info.getGameProfile().getName() + " TTC[0]");
                                // Notify the user
                                ChatUtils.print("Sent to " + info.getGameProfile().getName());
                                // I hate antispam
                                Thread.sleep(TPATools.getInstance().delay);
                            }
                            catch (Throwable ignore) { }
                        }
                    }
                    ChatUtils.print("Done!");
                });
                break;
            case "go":
                // This would stop the game if it wasn't in a separate thread
                ThreadManager.run(() -> {
                    // Loop through all players
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        // Is it the right team member
                        if (info.getGameProfile().getName().equals(args[1])) {
                            try {
                                // Send to the player
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
                // This would stop the game if it wasn't in a separate thread
                ThreadManager.run(() -> {
                    // Loop through all players
                    for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                        // Is the player a team member?
                        if (names.contains(info.getGameProfile().getName())) {
                            try {
                                // Send to the player
                                TTC.mc.player.sendChatMessage("/tell " + info.getGameProfile().getName() + " " + s.substring("dm ".length()));
                                // Notify the user
                                ChatUtils.print("Sent to " + info.getGameProfile().getName());
                                // I hate antispam
                                Thread.sleep(TPATools.getInstance().delay);
                            }
                            catch (Throwable ignore) { }
                        }
                    }
                    ChatUtils.print("Done!");
                });
                break;
            case "settings":
                // Print the member list and settings
                ChatUtils.print("TPA: " + (tpa ? "enabled" : "disabled"));
                ChatUtils.print("TPAhere: " + (tpaHere ? "enabled" : "disabled"));
            case "list":
                // Print the member list
                StringBuilder toPrint = new StringBuilder("Team members: ");
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
    public boolean onServerChat(String s, String formatted) {
        if (tpa && s.contains("has requested to teleport to you.") && names.stream().anyMatch(name -> s.startsWith(name + " ") || s.startsWith("~" + name + " "))) {
            TTC.player.sendChatMessage("/tpaccept");
        }
        if (tpaHere && s.contains("has requested that you teleport to them.") && names.stream().anyMatch(name -> s.startsWith(name + " ") || s.startsWith("~" + name + " "))) {
            TTC.player.sendChatMessage("/tpaccept");
        }
        
        try {
            // See if it is a DM from a Team member
            String name = names.stream().filter(
                    theName ->
                            s.startsWith(theName + " whispers:") ||
                            s.startsWith("~" + theName + " whispers:") ||
                            s.startsWith(theName + " whispers to you:") ||
                            s.startsWith("~" + theName + " whispers to you:") ||
                            s.startsWith("From " + theName + ":") ||
                            s.startsWith("From ~" + theName + ":")
            ).iterator().next();
            if (name != null) {
                String msg = s.split(": ")[1];
                if (msg.startsWith("TTC")) { // Control codes from team members
                    if (msg.equals("TTC[0]") && tpaHere) {
                        TTC.player.sendChatMessage("/tpa " + name);
                        ChatUtils.print("Sent TPA to " + name + ".");
                    }
                    if (msg.equals("TTC[1]") && tpa) {
                        TTC.player.sendChatMessage("/tpahere " + name);
                        ChatUtils.print("Sent TPAHere to " + name + ".");
                    }
                    if (msg.equals("TTC[3]")) {
                        ChatUtils.print("§c§lYou have been removed from the Team of " + name + "! \n" +
                                        "§cRun ,team remove " + name + " to remove them as well!");
                    }
                    // Cancel the display of the default message
                    return true;
                }
                
                ChatUtils.print("§b§lDM from team member: §r<" + name + "> " + s.substring(s.indexOf(": ") + 2));
                // Cancel the display of the default message
                return true;
            }
            // DM parsing of people outside the team
            for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                String theName = info.getGameProfile().getName();
                // Is it a DM, if yes, is this the player it came from?
                if (s.startsWith(theName + " whispers:") ||
                    s.startsWith("~" + theName + " whispers:") ||
                    s.startsWith(theName + " whispers to you:") ||
                    s.startsWith("~" + theName + " whispers to you:") ||
                    s.startsWith("From " + theName + ":") ||
                    s.startsWith("From ~" + theName + ":")) {
                    try {
                        String msg = s.split(": ")[1];
                        if (msg.startsWith("TTC")) { // Control codes from non-members
                            if (msg.equals("TTC[2]")) {
                                ChatUtils.print("§c§lYou have been added to the Team of " + theName + "! \n" +
                                                "§cRun ,team add " + theName + " to add them as well!");
                            }
                            // Cancel the display of the default message
                            return true;
                        }
                    }
                    catch (Throwable ignore) { }
                }
            }
        }
        catch (Exception ignore) { }
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
