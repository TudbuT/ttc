package tudbut.mod.client.ttc.mods;

import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.Bus;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.TTCIC;
import tudbut.mod.client.ttc.utils.ThreadManager;
import tudbut.tools.Queue;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class AltControl extends Module {
    
    Bus connection;
    private boolean started = false;
    private boolean isMain = false;
    
    {
        subButtons.add(new GuiTTC.Button("Start", text -> {
            started = !started;
            text.set(started ? "Stop" : "Start");
        }));
    }
    
    @Override
    public void onEnable() {
        connection = getMain();
    }
    
    @Override
    public void onTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    public Bus getMain() {
        try {
            return new Bus(new Socket("localhost", 55478));
        }
        catch (IOException e) {
            isMain = true;
            return new Bus(null, null) {
                private final ArrayList<Bus> sockets = new ArrayList<>();
                private final Queue<String> queue = new Queue<>();
                
                {
                    try {
                        ServerSocket server = new ServerSocket(55478);
                        ThreadManager.run(() -> {
                            while (server.isBound()) {
                                while (!started) ;
                                try {
                                    Socket socket = server.accept();
                                    Bus bus = new Bus(socket);
                                    bus.write(TTCIC.PacketsSC.INIT.run(""));
                                    TTCIC.PacketCS cs = TTCIC.deserializeCS(bus.readS());
                                    if(cs.packetType() == TTCIC.PacketsCS.INIT && cs.data().equals("")) {
                                        sockets.add(new Bus(socket));
                                    }
                                    else {
                                        bus.write(TTCIC.PacketsSC.CLOSE.run(""));
                                    }
                                }
                                catch (IOException | TTCIC.NotTTCICException ignored) {
                                }
                            }
                        });
                        for (int i = 0; i < sockets.size(); i++) {
                            Bus bus = sockets.get(i);
                            ThreadManager.run(() -> {
                                while (server.isBound()) {
                                    while (!started) ;
                                    try {
                                        queue.add(bus.readS());
                                    }
                                    catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                    catch (IOException ignored) {
                    }
                }
                
                @Override
                public void write(String data) throws IOException {
                    for (int i = 0; i < sockets.size(); i++) {
                        Bus bus = sockets.get(i);
                        bus.write(data);
                    }
                }
    
                @Override
                public String readS() {
                    return queue.next();
                }
            };
        }
    }
}
