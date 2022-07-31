package tudbut.mod.client.ttc.mods;

import de.tudbut.api.RequestResult;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.WebServices2;

public class Msg extends Module {
    
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onEveryChat(String s, String[] args) {
        if(args.length == 0) {
            ChatUtils.print("§aPlayers online: " + WebServices2.client.getUsersOnline().success(Integer.class).get());
            return;
        }
        String name = args[0];
        RequestResult<?> result = WebServices2.sendMessage(name, s.substring(name.length() + 1));
        System.out.println(result);
        if(result.result == RequestResult.Type.SUCCESS) {
            ChatUtils.print("§a[TTC] §r[WebServices] §aSuccessfully sent message.");
        }
        else {
            ChatUtils.print("§a[TTC] §r[WebServices] §cFailed to send message.");
        }
    }
}
