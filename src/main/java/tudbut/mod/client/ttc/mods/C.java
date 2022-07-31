package tudbut.mod.client.ttc.mods;

import de.tudbut.api.RequestResult;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.WebServices2;

/**
 * @author TudbuT
 * @since 31 Jul 2022
 */

public class C extends Module {
    
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onEveryChat(String s, String[] args) {
        RequestResult<?> result = WebServices2.client.sendMessage(s);
        System.out.println(result);
        if(result.result == RequestResult.Type.SUCCESS) {
            ChatUtils.print("§a[TTC] §r[WebServices] §aSuccessfully sent message.");
        }
        else {
            ChatUtils.print("§a[TTC] §r[WebServices] §cFailed to send message.");
        }
    }
}
