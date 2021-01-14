package tudbut.mod.client.ttc.mods;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.*;
import tudbut.net.ic.PBIC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static tudbut.mod.client.ttc.utils.TTCIC.*;

public class AltControl extends Module {
    
    private static AltControl instance;
    {
        instance = this;
    }
    public static AltControl getInstance() {
        return instance;
    }
    
    private int confirmationInstance = 0;
    private int mode = -1;
    
    {updateButtons();}
    
    private void updateButtons() {
        subButtons.clear();
        
        if(mode == -1) {
            subButtons.add(new GuiTTC.Button("Main mode", text -> {
                if (mode != -1)
                    return;
        
                displayConfirmation = true;
                confirmationInstance = 0;
            }));
            subButtons.add(new GuiTTC.Button("Alt mode", text -> {
                if (mode != -1)
                    return;
        
                displayConfirmation = true;
                confirmationInstance = 1;
            }));
        }
        if(mode == 0) {
            subButtons.add(new GuiTTC.Button("TPA alts here", text -> {
                onChat("tpa", "tpa".split(" "));
            }));
            subButtons.add(new GuiTTC.Button("Stop alts", text -> {
                onChat("stop", "stop".split(" "));
            }));
            subButtons.add(new GuiTTC.Button("Follow me", text -> {
                onChat("follow", "follow".split(" "));
            }));
        }
    }
    
    
    
    
    public boolean isAlt(EntityPlayer player) {
        try {
            for (int i = 0; i < alts.size(); i++) {
                if (alts.get(i).uuid.equals(player.getGameProfile().getId())) {
                    return true;
                }
            }
            return player.getGameProfile().getId().equals(main.uuid);
        } catch (NullPointerException e) {
            for (int i = 0; i < alts.size(); i++) {
                if (alts.get(i).name.equals(player.getGameProfile().getName())) {
                    return true;
                }
            }
            return player.getGameProfile().getName().equals(main.name);
        }
    }
    
    PBIC.Server server;
    PBIC.Client client;
    
    Alt main = new Alt();
    ArrayList<Alt> alts = new ArrayList<>();
    Map<PBIC.Connection, Alt> altsMap = new HashMap<>();
    
    @Override
    public void onConfirm(boolean result) {
        if(result) {
            switch (confirmationInstance) {
                case 0:
                    onChat("server", "server".split(" "));
                    break;
                case 1:
                    onChat("client", "client".split(" "));
                    break;
            }
        }
    }
    
    @Override
    public void onEnable() {
        alts = new ArrayList<>();
        Alt alt;
        alts.add(alt = new Alt());
        alt.name = TTC.mc.getSession().getProfile().getName();
        alt.uuid = TTC.mc.getSession().getProfile().getId();
        alt.profile = TTC.mc.getSession().getProfile();
    }
    
    @Override
    public void onSubTick() {
    
    }
    
    // When the client receives a packet
    public void onPacketSC(PacketSC packet, PBIC.Connection connection) throws IOException {
        if(client == null)
            throw new RuntimeException();
        
        KillAura aura = KillAura.getInstance();
        switch (packet.type()) {
            case INIT:
                main = new Alt();
                sendPacket(PacketsCS.NAME, TTC.mc.getSession().getProfile().getName());
                break;
            case NAME:
                main.name = packet.content();
                ChatUtils.print("Connection to main " + main.name + " established!");
                sendPacket(PacketsCS.UUID, TTC.mc.getSession().getProfile().getId().toString());
                break;
            case UUID:
                main.uuid = UUID.fromString(packet.content());
                ChatUtils.print("Got UUID from main " + main.name + ": " + packet.content());
                break;
            case TPA:
                ChatUtils.print("TPA'ing main account...");
                TTC.player.sendChatMessage("/tpa " + main.name);
                break;
            case EXECUTE:
                ChatUtils.print("Sending message received from main account...");
                ChatUtils.simulateSend(packet.content(), false);
                break;
            case LIST:
                TTC.logger.info("Received alt list from main.");
                Map<String, String> map0 = Utils.stringToMap(packet.content());
                
                alts.clear();
                int len = map0.keySet().size();
                for (int i = 0; i < len; i++) {
                    Alt alt;
                    alts.add(alt = new Alt());
                    
                    Map<String, String> map1 = Utils.stringToMap(map0.get(String.valueOf(i)));
                    alt.name = map1.get("name");
                    alt.uuid = UUID.fromString(map1.get("uuid"));
                }
                break;
            case KILL:
                ChatUtils.print("Killing player " + packet.content());
                ChatUtils.simulateSend("#follow player " + packet.content(), false);
                aura.enabled = true;
                aura.onEnable();
                aura.targets.add(packet.content());
                break;
            case FOLLOW:
                ChatUtils.print("Following main");
                ChatUtils.simulateSend("#follow player " + main.name, false);
                break;
            case STOP:
                if(packet.content().equals("")) {
                    ChatUtils.print("Stopping killing all players");
                    aura.enabled = false;
                    aura.onDisable();
                    aura.targets.clear();
                    ChatUtils.simulateSend("#stop", false);
                }
                else {
                    ChatUtils.print("Stopping killing player " + packet.content());
                    ChatUtils.simulateSend("#stop", false);
                    aura.targets.removeIf(s -> s.equals(packet.content()));
                    if(!aura.targets.isEmpty()) {
                        ChatUtils.print("Killing player " + aura.targets.get(0));
                        ChatUtils.simulateSend("#follow player " + aura.targets.get(0), false);
                    }
                }
                break;
        }
    }
    
    // When the server receives a packet
    public void onPacketCS(PacketCS packet, PBIC.Connection connection) throws IOException {
        switch (packet.type()) {
            case NAME:
                altsMap.get(connection).name = packet.content();
                ChatUtils.print("Connection to alt " + packet.content() + " established!");
                connection.writePacket(getPacketSC(PacketsSC.NAME, TTC.mc.getSession().getProfile().getName()));
                break;
            case UUID:
                altsMap.get(connection).uuid = UUID.fromString(packet.content());
                ChatUtils.print("Got UUID from alt " + altsMap.get(connection).name + ": " + packet.content());
                connection.writePacket(getPacketSC(PacketsSC.UUID, TTC.mc.getSession().getProfile().getId().toString()));
                
                sendList();
                
                break;
        }
    }
    
    public void sendPacketSC(PacketsSC type, String content) throws IOException {
        ChatUtils.chatPrinterDebug().println("Sending packet[" + type.name() + "]{" + content + "}");
        PBIC.Connection[] connections = server.connections.toArray(new PBIC.Connection[0]);
        for (int i = 0; i < connections.length; i++) {
            try {
                connections[i].writePacket(getPacketSC(type, content));
            } catch (Exception ignore) { }
        }
    }
    
    public void sendPacketDelayedSC(PacketsSC type, String content) {
        ThreadManager.run(() -> {
            ChatUtils.chatPrinterDebug().println("Sending packet[" + type.name() + "]{" + content + "}");
            PBIC.Connection[] connections = server.connections.toArray(new PBIC.Connection[0]);
            for (int i = 0; i < connections.length; i++) {
                try {
                    connections[i].writePacket(getPacketSC(type, content));
                }
                catch (IOException ignore) { }
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void sendPacket(PacketsCS type, String content) throws IOException {
        ChatUtils.chatPrinterDebug().println("Sending packet[" + type.name() + "]{" + content + "}");
        if(client == null)
            throw new RuntimeException();
        client.connection.writePacket(getPacketCS(type, content));
    }
    
    @Override
    public void onChat(String s, String[] args) {
        try {
            if (s.equals("server") && server == null) {
                server = new PBIC.Server(50278);
                server.start();
                server.onJoin.add(() -> {
                    PBIC.Connection theConnection = server.lastConnection;
                    try {
                        theConnection.writePacket(getPacketSC(PacketsSC.INIT, ""));
                        altsMap.put(theConnection, new Alt());
                        
                        while (true) {
                            String string = "UNKNOWN";
                            try {
                                PBIC.Packet packet = theConnection.readPacket();
                                string = packet.getContent();
                                onPacketCS(getPacketCS(packet), theConnection);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                System.err.println("Packet: " + string);
                            }
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                
                mode = 0;
                
                ChatUtils.print("§aServer started");
            }
            if (s.equals("client") && client == null) {
                client = new PBIC.Client("127.0.0.1", 50278);
                ThreadManager.run("TTCIC client receive thread", () -> {
                    while (true) {
                        String string = "UNKNOWN";
                        try {
                            PBIC.Packet packet = client.connection.readPacket();
                            string = packet.getContent();
                            onPacketSC(getPacketSC(packet), client.connection);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            System.err.println("Packet: " + string);
                        }
                    }
                });
                mode = 1;
            }
            
            if(args.length >= 2) {
                KillAura aura = KillAura.getInstance();
                if(args[0].equals("send") && s.contains(" ")) {
                    String st = s.substring(s.indexOf(" ") + 1);
                    sendPacketSC(PacketsSC.EXECUTE, st);
                }
                if(args[0].equals("kill") && s.contains(" ")) {
                    sendList();
                    String st = s.substring(s.indexOf(" ") + 1);
                    sendPacketSC(PacketsSC.KILL, st);
                    ChatUtils.simulateSend("#follow player " + st, false);
                    aura.enabled = true;
                    aura.onEnable();
                    aura.targets.add(st);
                }
                if(args[0].equals("stop") && s.contains(" ")) {
                    String st = s.substring(s.indexOf(" ") + 1);
                    sendPacketSC(PacketsSC.STOP, st);
                    ChatUtils.print("Stopping killing player " + st);
                    ChatUtils.simulateSend("#stop", false);
                    aura.targets.remove(st);
                    if(!aura.targets.isEmpty()) {
                        ChatUtils.print("Killing player " + st);
                        ChatUtils.simulateSend("#follow player " + aura.targets.get(0), false);
                    }
                }
            }
            
            if (s.equals("stop")) {
                KillAura aura = KillAura.getInstance();
                sendPacketSC(PacketsSC.STOP, "");
                ChatUtils.print("Stopping killing/following all players");
                aura.enabled = false;
                aura.onDisable();
                aura.targets.clear();
                ChatUtils.simulateSend("#stop", false);
            }
            
            if (s.equals("tpa")) {
                sendList();
                sendPacketDelayedSC(PacketsSC.TPA, "");
            }
    
            if (s.equals("follow")) {
                sendPacketDelayedSC(PacketsSC.FOLLOW, "");
            }
            
            if (s.equals("end")) {
                if(client != null)
                    client.connection.getBus().close();
                client = null;
                if(server != null) {
                    server.busses.forEach(bus -> {
                        try {
                            bus.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    server.busses.clear();
                    server.connections.clear();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        updateButtons();
    }
    
    private void sendList() throws IOException {
        Map<String, String> map0 = new HashMap<>();
        PBIC.Connection[] keys = altsMap.keySet().toArray(new PBIC.Connection[0]);
        alts.clear();
        for (int i = 0; i < keys.length; i++) {
            Alt alt = altsMap.get(keys[i]);
            alts.add(alt);
            
            Map<String, String> map1 = new HashMap<>();
            map1.put("name", alt.name);
            map1.put("uuid", alt.uuid.toString());
            
            map0.put(String.valueOf(i), Utils.mapToString(map1));
        }
        sendPacketSC(PacketsSC.LIST, Utils.mapToString(map0));
    }
    
    @Override
    public boolean onServerChat(String s, String formatted) {
        if (
                s.contains("has requested to teleport to you.") &&
                alts.stream().anyMatch(alt -> s.startsWith(alt.name + " ") || s.startsWith("~" + alt.name + " "))
        ) {
            TTC.player.sendChatMessage("/tpaccept");
        }
        return false;
    }
    
    public static class Alt {
        public String name;
        public UUID uuid;
        public GameProfile profile;
    }
}
