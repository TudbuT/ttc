package tudbut.mod.client.ttc.mods;

import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.Utils;
import tudbut.net.http.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class Update extends Module {
    
    public static boolean send = true;

    // Checks if the version on master is actually newer than the current one in case the current version is a pre-release
    public static boolean isNewer(String version) {
        String current = TTC.VERSION;
        String[] numbersCurrent = current.substring(1, current.length() - 1).split("[.]");
        String[] numbers = version.substring(1, current.length() - 1).split("[.]");
        for(int i = 0; i < numbers.length; i++) {
            if(Integer.parseInt(numbers[i]) > Integer.parseInt(numbersCurrent[i]))
                return true;
        }
        return version.charAt(version.length() - 1) > current.charAt(current.length() - 1);
    }
    
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onEveryChat(String s, String[] args) {
        try {
            String version = Utils.removeNewlines(Utils.getRemote("version.txt", true));
            String v = "https://github.com/" + TTC.REPO + "/releases/download/" + version + "/" + TTC.MODID + ".jar";
            new Thread(() -> {
                try {
                    URL url = new URL(v);
                    boolean b = false;
                    ChatUtils.print("Downloading newest version...");
                    ParsedHTTPValue res = null;
                    while (!b) {
                        HTTPRequest request = new HTTPRequest(HTTPRequestType.GET, "https://" + url.getHost(), 443, url.getPath() + "?" + url.getQuery());
                        HTTPResponse response = request.send();
                        res = response.parse();
                        if(res.getStatusCode() == HTTPResponseCode.OK.asInt) {
                            b = true;
                        }
                        else if(res.getStatusCode() != HTTPResponseCode.NotFound.asInt) {
                            ChatUtils.print("Redirected! Trying on new URL...");
                            HTTPHeader loc = null;
                            for (HTTPHeader header : res.getHeaders()) {
                                if(header.key().equalsIgnoreCase("Location")) {
                                    loc = header;
                                    break;
                                }
                            }
                            if(loc != null) {
                                url = new URL(loc.value());
                            }
                            else {
                                ChatUtils.print("Couldn't update!");
                                return;
                            }
                        }
                        else {
                            ChatUtils.print("Couldn't update! 404!");
                            send = false;
                        }
                    }
                    byte[] bytes = res.getBodyBytes();
                    ChatUtils.print("Download finished. Saving to disk...");
                    File mods = new File("mods");
                    File old = new File("mods/old/ttc/" + TTC.VERSION);
                    old.mkdirs();
                    //noinspection ConstantConditions
                    for (File file : mods.listFiles()) {
                        if(file.getName().startsWith("ttc") && file.getName().endsWith(".jar")) {
                            if(!file.renameTo(new File(old, file.getName()))) {
                                file.delete();
                            }
                        }
                    }
                    File f = new File("mods/ttc.jar");
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
