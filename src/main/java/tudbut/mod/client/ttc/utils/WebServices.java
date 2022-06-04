package tudbut.mod.client.ttc.utils;

import net.minecraft.client.Minecraft;
import tudbut.api.impl.RateLimit;
import tudbut.api.impl.TudbuTAPIV2;
import tudbut.mod.client.ttc.TTC;
import tudbut.net.pbic2.PBIC2;
import tudbut.net.pbic2.PBIC2AEventHandler;
import tudbut.net.pbic2.PBIC2AListener;
import tudbut.obj.DoubleTypedObject;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;
import tudbut.tools.Lock;

import java.io.IOException;
import java.util.ArrayList;

public class WebServices {
    
    public static PBIC2 client;
    public static PBIC2AEventHandler handler = new PBIC2AEventHandler();
    public static Lock keepAliveLock = new Lock(true);
    private static final PBIC2AListener listener = new PBIC2AListener() {
        @Override
        public void onMessage(String s) throws IOException {
            keepAliveLock.lock(15000);
            try {
                TCN tcn = JSON.read(s);
                if(tcn.getString("id").equalsIgnoreCase("message")) {
                    queueMessage(tcn);
                }
                if(
                        tcn.getString("id").equalsIgnoreCase("save") ||
                        tcn.getString("id").equalsIgnoreCase("welcome")
                ) {
                    usersOnline = tcn.getSub("data").getArray("onlineUsernames").toArray(new String[0]);
                }
            }
            catch (JSON.JSONFormatException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }
    };
    
    public static String[] usersOnline = new String[]{};
    
    public static void handshake() throws IOException, RateLimit {
        TTC.logger.info("Starting handshake");
        
        TudbuTAPIV2.handshake(Minecraft.getMinecraft().getSession().getProfile().getId());
        if(client != null) {
            client.getSocket().close();
            handler.remove(client);
        }
        
        TTC.logger.info("Handshake passed");
        
        // Done!
    }
    
    private static void login() throws IOException, RateLimit {
        DoubleTypedObject<Boolean, String> s = TudbuTAPIV2.request(Minecraft.getMinecraft().getSession().getProfile().getId(), "track/login", "TTC " + TTC.REPO + ":" + TTC.BRANCH + "@" + TTC.VERSION);
        if(!s.o || !s.t.equals("OK")) {
            TTC.logger.info("Error during login. Redoing handshake.");
            doLogin();
            return;
        }
        client = TudbuTAPIV2.connectGateway(Minecraft.getMinecraft().getSession().getProfile().getId());
        handler.start(client, listener);
    }
    
    private static boolean play() throws IOException, RateLimit {
        DoubleTypedObject<Boolean, String> s = TudbuTAPIV2.request(Minecraft.getMinecraft().getSession().getProfile().getId(), "track/play", "");
        if(s.t.equals("DISABLE")) {
            KillSwitch.deactivate();
        }
        return s.o;
    }
    
    public static void doLogin() {
        try {
            Thread.sleep(1000);
            handshake();
            Thread.sleep(1000);
            login();
            Thread.sleep(1000);
            play();
        }
        catch (Exception e) {
            TTC.logger.info("Can't reach api.tudbut.de");
        }
    }
    
    static ArrayList<TCN> queuedMessages = new ArrayList<>();
    
    public static void trackPlay() {
        try {
            if(TTC.isIngame()) {
                sendQueuedMessages();
            }
            if(!keepAliveLock.isLocked()) {
                handler.remove(client);
                client = TudbuTAPIV2.connectGateway(Minecraft.getMinecraft().getSession().getProfile().getId());
                handler.start(client, listener);
                keepAliveLock.lock();
            }
            if(!play()) {
                TTC.logger.info("Couldn't send track/play. Redoing handshake.");
                doLogin();
            }
        }
        catch (Exception ignored) {
            doLogin();
        }
    }
    
    public static void queueMessage(TCN event) {
        queuedMessages.add(event);
        if(TTC.isIngame()) {
            sendQueuedMessages();
        }
    }
    
    public static synchronized void sendQueuedMessages() {
        for (int i = 0, queuedMessagesSize = queuedMessages.size() ; i < queuedMessagesSize ; i++) {
            TCN queuedMessage = queuedMessages.get(i);
            
            ChatUtils.print("§a[TTC] §lGOT MESSAGE");
            ChatUtils.print("§a[TTC] <" + queuedMessage.getSub("from").getSub("record").getString("name") + "> " + queuedMessage.getString("message"));
            
            queuedMessages.remove(queuedMessage);
        }
    }
}
