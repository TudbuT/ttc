package tudbut.mod.client.ttc.mods;

import tudbut.api.impl.TudbuTAPI;
import tudbut.api.impl.TudbuTAPIV2;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.WebServices;

import java.util.UUID;

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
        try {
            String name = args[0];
            UUID uuid = TudbuTAPI.getUUID(name);
            TudbuTAPIV2.request(TTC.player.getUniqueID(), "message", "other=" + uuid, s.substring(name.length() + 1));
            ChatUtils.print("Done.");
        } catch (Exception e) {
            ChatUtils.print("Couldn't find that player! Usage: ,msg <name> <message...>");
        }
    }
}
