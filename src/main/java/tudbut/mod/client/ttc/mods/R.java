package tudbut.mod.client.ttc.mods;

import de.tudbut.api.RequestResult;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.WebServices2;

public class R extends Module {
    
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onEveryChat(String s, String[] args) {
        ThreadManager.run(() -> {
            RequestResult<?> result = WebServices2.sendMessage(null, s);
            System.out.println(result);
            if(result.result == RequestResult.Type.SUCCESS) {
                ChatUtils.print("§a[TTC] §r[WebServices] §aSuccessfully sent message.");
            }
            else {
                ChatUtils.print("§a[TTC] §r[WebServices] §cFailed to send message.");
            }
        });
    }
}
