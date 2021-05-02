package tudbut.mod.client.ttc.utils;

import de.tudbut.tools.Hasher;
import net.minecraft.client.Minecraft;
import tudbut.api.impl.RateLimit;
import tudbut.api.impl.TudbuTAPIV2;
import tudbut.mod.client.ttc.TTC;
import tudbut.net.http.*;
import tudbut.obj.DoubleTypedObject;
import tudbut.tools.encryption.RawKey;

import java.io.IOException;
import java.util.UUID;

public class WebServices {
    
    public static void handshake() throws IOException, RateLimit {
        TTC.logger.info("Starting handshake");
    
        TudbuTAPIV2.handshake(Minecraft.getMinecraft().getSession().getProfile().getId());
        
        TTC.logger.info("Handshake passed");
        
        // Done!
    }
    
    private static void login() throws IOException, RateLimit {
        DoubleTypedObject<Boolean, String> s = TudbuTAPIV2.request(Minecraft.getMinecraft().getSession().getProfile().getId(), "track/login", "TTC " + TTC.BRAND + "@" + TTC.VERSION);
        if(!s.o || !s.t.equals("OK")) {
            TTC.logger.info("Error during login. Redoing handshake.");
            doLogin();
        }
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
            handshake();
            login();
            play();
        }
        catch (IOException | RateLimit e) {
            TTC.logger.info("Can't reach api.tudbut.de");
        }
    }
    public static void trackPlay() {
        try {
            if(!play()) {
                TTC.logger.info("Couldn't send track/play. Redoing handshake.");
                doLogin();
            }
        }
        catch (IOException | RateLimit ignored) { }
    }
}
