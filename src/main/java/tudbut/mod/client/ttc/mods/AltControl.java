package tudbut.mod.client.ttc.mods;

import de.tudbut.timer.AsyncTask;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.*;
import tudbut.net.ic.PBIC;
import tudbut.obj.Atomic;

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
    public int mode = -1;
    private boolean botMain = true;
    private boolean useElytra = false;
    private final Atomic<Vec3d> commonTarget = new Atomic<>();
    private EntityPlayer commonTargetPlayer = null;
    private FlightBot flightBot = null;
    
    {updateButtons();}
    
    @Override
    public void loadConfig() {
        botMain = Boolean.parseBoolean(cfg.get("botMain"));
        updateButtons();
    }
    
    @Override
    public void updateConfig() {
        cfg.put("botMain", botMain + "");
    }
    
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
        else {
            subButtons.add(new GuiTTC.Button("End connection", text ->
                    onChat("end", "end".split(" "))));
            subButtons.add(new GuiTTC.Button("List", text ->
                    onChat("list", "list".split(" "))));
        }
        if(mode == 0) {
            subButtons.add(new GuiTTC.Button("TPA alts here", text ->
                    onChat("tpa", "tpa".split(" "))));
            subButtons.add(new GuiTTC.Button("Stop alts", text ->
                    onChat("stop", "stop".split(" "))));
            subButtons.add(new GuiTTC.Button("Follow me", text ->
                    onChat("follow", "follow".split(" "))));
            subButtons.add(new GuiTTC.Button("Send client config", text ->
                    onChat("send", "send".split(" "))));
            subButtons.add(new GuiTTC.Button("Use elytra: " + useElytra, text -> {
                onChat("telytra", "telytra".split(" "));
                text.set("Use elytra: " + useElytra);
            }));
            subButtons.add(new GuiTTC.Button("Bot main: " + botMain, text -> {
                botMain = !botMain;
                text.set("Bot main: " + botMain);
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
    }
    
    @Override
    public void onTick() {
        if(useElytra) {
            if (commonTargetPlayer != null && TTC.world.getPlayerEntityByName(commonTargetPlayer.getName()) != null)
                follow();
            else {
                FlightBot.deactivate(flightBot);
                commonTargetPlayer = null;
                commonTarget.set(null);
                flightBot = null;
                if(!main.uuid.equals(TTC.player.getUniqueID()))
                    follow(main.name);
            }
        }
    }
    
    // When the client receives a packet
    public void onPacketSC(PacketSC packet) {
        if (client == null)
            throw new RuntimeException();
        try {
            ChatUtils.chatPrinterDebug().println("Received packet[" + packet.type() + "]{" + packet.content() + "}");
    
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
                    kill(packet.content());
                    break;
                case FOLLOW:
                    ChatUtils.print("Following " + packet.content());
                    follow(packet.content());
                    break;
                case STOP:
                    stop(packet.content());
                    break;
                case CONFIG:
                    TTC.cfg = Utils.stringToMap(packet.content());
                    TTC.getInstance().saveConfig();
                    TTC.getInstance().loadConfig();
                    break;
                case WALK:
                    useElytra = false;
                    FlightBot.deactivate(flightBot);
                    flightBot = null;
                    break;
                case ELYTRA:
                    useElytra = true;
                    break;
            }
        } catch (Exception e) {
            return;
        }
    }
    
    // When the server receives a packet
    public void onPacketCS(PacketCS packet, PBIC.Connection connection) throws IOException {
        ChatUtils.chatPrinterDebug().println("Received packet[" + packet.type() + "]{" + packet.content() + "}");
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
    
    public void sendPacketSC(PacketsSC type, String content) {
        if(server.connections.size() == 0)
            return;
        
        AsyncTask<Object> task = new AsyncTask<>(() -> {
            ChatUtils.chatPrinterDebug().println("Sending packet[" + type.name() + "]{" + content + "}");
            try {
                PBIC.Connection[] connections = server.connections.toArray(new PBIC.Connection[0]);
                for (int i = 0; i < connections.length; i++) {
                    try {
                        connections[i].writePacket(getPacketSC(type, content));
                    }
                    catch (Exception ignore) { }
                }
            } catch (Throwable e) {
                return e;
            }
            return new Object();
        });
        task.setTimeout(server.connections.size() * 500L);
        task.then((r) -> {
            if(r instanceof Throwable || r == null) {
                ChatUtils.print("§c§lError during communication, ending server!");
                String etype;
                if(r == null) {
                    etype = "ETimeout";
                }
                else if(r instanceof Exception) {
                    etype = "EExceptionSend {" + ((Exception) r).getMessage() + "}";
                    ((Throwable) r).printStackTrace(ChatUtils.chatPrinterDebug());
                }
                else {
                    etype = "EErrorSend {" + ((Throwable) r).getMessage() + "}";
                    ((Throwable) r).printStackTrace(ChatUtils.chatPrinterDebug());
                }
                ChatUtils.print(etype);
                onChat("end", "end".split(" "));
            }
        });
    }
    
    public void sendPacketDelayedSC(PacketsSC type, String content) {
        if(server.connections.size() == 0)
            return;
        
        AsyncTask<Object> task = new AsyncTask<>(() -> {
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
            return new Object();
        });
        task.setTimeout(server.connections.size() * 1000L);
        task.then((r) -> {
            if(r == null) {
                ChatUtils.print("§c§lError during communication, ending server!");
                onChat("end", "end".split(" "));
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
                main = new Alt();
                main.name = TTC.mc.getSession().getProfile().getName();
                main.uuid = TTC.mc.getSession().getProfile().getId();
                
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
                            onPacketSC(getPacketSC(packet));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            System.err.println("Packet: " + string);
                            try {
                                client.close();
                                mode = -1;
                                client = null;
                                alts = new ArrayList<>();
                                altsMap = new HashMap<>();
                                break;
                            }
                            catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                });
                mode = 1;
            }
            
            if(args.length >= 2) {
                if(args[0].equals("send") && s.contains(" ")) {
                    String st = s.substring(s.indexOf(" ") + 1);
                    sendPacketSC(PacketsSC.EXECUTE, st);
                }
                if(args[0].equals("kill") && s.contains(" ")) {
                    sendList();
                    String st = s.substring(s.indexOf(" ") + 1);
                    if(useElytra) {
                        sendPacketSC(PacketsSC.ELYTRA, "");
                    } else {
                        sendPacketSC(PacketsSC.WALK, "");
                    }
                    sendPacketSC(PacketsSC.KILL, st);
                    if(botMain) {
                        kill(st);
                    }
                }
                if(args[0].equals("stop") && s.contains(" ")) {
                    String st = s.substring(s.indexOf(" ") + 1);
                    sendPacketSC(PacketsSC.STOP, st);
                    ChatUtils.print("Stopping killing player " + st);
                    if(botMain) {
                        stop(st);
                    }
                }
            }
            
            if (s.equals("stop")) {
                sendPacketSC(PacketsSC.STOP, "");
                ChatUtils.print("Stopping killing/following all players");
                if(botMain) {
                    stop(null);
                }
            }
    
            if (s.equals("send")) {
                TTC.getInstance().setConfig();
                sendPacketSC(PacketsSC.CONFIG, Utils.mapToString(TTC.cfg));
                ChatUtils.print("Sending config to all alts");
            }
            
            if (s.equals("tpa")) {
                sendList();
                sendPacketDelayedSC(PacketsSC.TPA, "");
            }
    
            if (s.equals("follow")) {
                if(useElytra) {
                    sendPacketSC(PacketsSC.ELYTRA, "");
                } else {
                    sendPacketSC(PacketsSC.WALK, "");
                }
                sendPacketSC(PacketsSC.FOLLOW, main.name);
            }
            
            if(s.equals("telytra")) {
                useElytra = !useElytra;
            }
            
            if (s.equals("end")) {
                if(client != null)
                    client.close();
                client = null;
                if(server != null) {
                    server.close();
                }
                server = null;
                mode = -1;
                
                alts = new ArrayList<>();
                altsMap = new HashMap<>();
            }
            
            if(s.equals("list")) {
                StringBuilder string = new StringBuilder("List:");
                if(server != null) {
                    for (int i = 0; i < server.connections.size(); i++) {
                        PBIC.Connection connection = server.connections.get(i);
                        Alt alt = altsMap.get(connection);
                        string.append(" ").append(alt.name).append(",");
                    }
                }
                if(client != null) {
                    for (int i = 0; i < alts.size(); i++) {
                        Alt alt = alts.get(i);
                        string.append(" ").append(alt.name).append(",");
                    }
                }
                if(string.toString().contains(","))
                    string = new StringBuilder(string.substring(0, string.length() - 2));
                ChatUtils.print(string.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        updateButtons();
    }
    
    private void sendList() {
        if(server.connections.size() == 0)
            return;
        
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
    
    public void follow(String name) {
        commonTargetPlayer = TTC.world.getPlayerEntityByName(name);
        follow();
    }
    
    public void kill(String name) {
        follow(name);
        KillAura aura = KillAura.getInstance();
        aura.enabled = true;
        aura.onEnable();
        aura.targets.add(name);
    }
    
    public void stop(String name) {
        KillAura aura = KillAura.getInstance();
        commonTargetPlayer = null;
        commonTarget.set(null);
        if(useElytra) {
            FlightBot.deactivate(flightBot);
            flightBot = null;
            useElytra = false;
        } else {
            ChatUtils.simulateSend("#stop", false);
        }
        if(name == null || name.equals("")) {
            aura.targets.clear();
            aura.enabled = false;
            aura.onDisable();
        }
        else {
            aura.targets.remove(name);
            aura.targets.trimToSize();
            if (aura.targets.size() != 0) {
                ChatUtils.print("Killing player " + name);
                follow(aura.targets.get(0));
            }
        }
    }
    
    public void follow() {
        if(commonTargetPlayer == null)
            return;
        
        try {
            if (useElytra) {
                FlightBot.deactivate(flightBot);
                flightBot = FlightBot.activate(commonTarget);
                commonTarget.set(commonTargetPlayer.getPositionVector().addVector(0, 2, 0));
            } else
                ChatUtils.simulateSend("#follow player " + commonTargetPlayer.getName(), false);
        } catch (Exception e) {
            e.printStackTrace(ChatUtils.chatPrinter());
        }
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
    }
}
