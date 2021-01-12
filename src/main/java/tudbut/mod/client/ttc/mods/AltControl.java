package tudbut.mod.client.ttc.mods;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.ThreadManager;
import tudbut.mod.client.ttc.utils.Utils;
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
    public void onEnable() {
        alts = new ArrayList<>();
        Alt alt;
        alts.add(alt = new Alt());
        alt.name = TTC.mc.getSession().getProfile().getName();
        alt.uuid = TTC.mc.getSession().getProfile().getId();
        alt.profile = TTC.mc.getSession().getProfile();
    }
    
    @Override
    public void onTick() {
    
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
                
                Map<String, String> map0 = new HashMap<>();
                PBIC.Connection[] keys = altsMap.keySet().toArray(new PBIC.Connection[0]);
                for (int i = 0; i < keys.length; i++) {
                    Alt alt = altsMap.get(keys[i]);
                    
                    Map<String, String> map1 = new HashMap<>();
                    map1.put("name", alt.name);
                    map1.put("uuid", alt.uuid.toString());
                    
                    map0.put(String.valueOf(i), Utils.mapToString(map1));
                }
                sendPacketSC(PacketsSC.LIST, Utils.mapToString(map0));
                
                break;
        }
    }
    
    public void sendPacketSC(PacketsSC type, String content) throws IOException {
        PBIC.Connection[] connections = server.connections.toArray(new PBIC.Connection[0]);
        for (int i = 0; i < connections.length; i++) {
            connections[i].writePacket(getPacketSC(type, content));
        }
    }
    
    public void sendPacket(PacketsCS type, String content) throws IOException {
        if(client == null)
            throw new RuntimeException();
        client.connection.writePacket(getPacketCS(type, content));
    }
    
    @Override
    public void onChat(String s, String[] args) {
        try {
            if (s.equals("server")) {
                server = new PBIC.Server(50278);
                server.start();
                server.onJoin.add(() -> {
                    PBIC.Connection theConnection = server.lastConnection;
                    try {
                        theConnection.writePacket(getPacketSC(PacketsSC.INIT, ""));
                        altsMap.put(theConnection, new Alt());
                        
                        while (true) {
                            onPacketCS(getPacketCS(theConnection.readPacket()), theConnection);
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            if (s.equals("client")) {
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
            }
            
            if(args.length >= 2) {
                KillAura aura = KillAura.getInstance();
                if(args[0].equals("send") && s.contains(" ")) {
                    String st = s.substring(s.indexOf(" ") + 1);
                    sendPacketSC(PacketsSC.EXECUTE, st);
                }
                if(args[0].equals("kill") && s.contains(" ")) {
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
                ChatUtils.print("Stopping killing all players");
                aura.enabled = false;
                aura.onDisable();
                aura.targets.clear();
                ChatUtils.simulateSend("#stop", false);
            }
            
            if (s.equals("tpa")) {
                sendPacketSC(PacketsSC.TPA, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static class Alt {
        public String name;
        public UUID uuid;
        public GameProfile profile;
    }
}
