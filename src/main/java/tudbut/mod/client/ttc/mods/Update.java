package tudbut.mod.client.ttc.mods;

import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.Utils;
import tudbut.net.http.HTTPRequest;
import tudbut.net.http.HTTPRequestType;
import tudbut.net.http.HTTPResponse;
import tudbut.net.http.ParsedHTTPValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class Update extends Module {
    
    public static boolean send = true;
    
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onEveryChat(String s, String[] args) {
        try {
            String version = Utils.removeNewlines(Utils.getRemote("version.txt", true));
            String v = "https://github.com/" + TTC.REPO + "/releases/tag/" + version + "/" + TTC.MODID + ".jar";
            URL url = new URL(v);
            HTTPRequest request = new HTTPRequest(HTTPRequestType.GET, "https://" + url.getHost(), 443, url.getPath());
            new Thread(() -> {
                try {
                    ChatUtils.print("Downloading newest version...");
                    HTTPResponse response = request.send();
                    ParsedHTTPValue res = response.parse();
                    byte[] bytes = res.getBodyBytes();
                    ChatUtils.print("Download finished. Saving to disk...");
                    File mods = new File("mods");
                    File old = new File(mods, "old/ttc/" + TTC.VERSION);
                    old.mkdirs();
                    //noinspection ConstantConditions
                    for (File file : mods.listFiles()) {
                        if(file.getName().startsWith("ttc") && file.getName().endsWith(".jar")) {
                            file.renameTo(new File(old, file.getName()));
                        }
                    }
                    File f = new File(mods, "ttc.jar");
                    FileOutputStream stream = new FileOutputStream(f);
                    stream.write(bytes);
                    ChatUtils.print("Finishing off...");
                    stream.close();
                    ChatUtils.print("Done! Next time you restart minecraft, your TTC will be on version " + version);
                    send = false;
                }
                catch (Exception e) {
                    ChatUtils.print("Couldn't update!");
                }
            }).start();
        } catch (Exception e) {
            ChatUtils.print("Couldn't update!");
        }
    }
}
