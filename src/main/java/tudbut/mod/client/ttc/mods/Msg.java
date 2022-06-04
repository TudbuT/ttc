package tudbut.mod.client.ttc.mods;

import de.tudbut.api.TudbuTAPI;
import tudbut.api.impl.RateLimit;
import tudbut.api.impl.TudbuTAPIV2;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.WebServices;

import java.io.IOException;

public class Msg extends Module {
    
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onEveryChat(String s, String[] args) {
        if(args.length == 0) {
            ChatUtils.print("§aPlayers online: " + String.join(" ", WebServices.usersOnline));
            return;
        }
        String name = args[0];
        TudbuTAPI
                .getUUID(name)
                .compose((resp, res, rej) -> {
                    try {
                        TudbuTAPIV2.request(TTC.player.getUniqueID(), "message", "other=" + resp, s.substring(name.length() + 1));
                    }
                    catch (IOException | RateLimit e) {
                        rej.call(e);
                    }
                })
                .then(v -> ChatUtils.print("Done!"))
                .err(e -> ChatUtils.print("Couldn't find that player! Usage: ,msg <name> <message...>"));
    }
}
