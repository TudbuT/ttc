package tudbut.mod.client.ttc.utils.isbpl;

import de.tudbut.io.StreamReader;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.mods.Update;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Stack;

/**
 * @author TudbuT
 * @since 01 May 2022
 */

public class UpdateManager implements ISBPLScript {
    ISBPL context;
    Stack<ISBPLObject> stack = new Stack<>();
    
    @Override
    public ISBPL context() {
        return context;
    }
    
    public void run() {
        new File("config/ttc").mkdir();
        if (new File("config/ttc/version.txt").exists()) {
            try {
                String s = ISBPL.readFile(new File("config/ttc/version.txt"));
                if (Update.isNewer(s, TTC.VERSION)) {
                    System.out.println("Detected an update has been installed.");
                    context = ISBPLScript.Loader.makeContext();
                    new File("config/ttc/backup.cfg").delete();
                    Files.copy(Paths.get("config/ttc.cfg"), Paths.get("config/ttc/backup.cfg"));
                    URL url = new URL("https://raw.githubusercontent.com/" + TTC.REPO + "/" + TTC.BRANCH + "/ttcupdater.isbpl");
                    InputStream inputStream = url.openStream();
                    FileOutputStream fos = new FileOutputStream("config/ttc/updater.isbpl");
                    fos.write(new StreamReader(inputStream).readAllAsBytes());
                    inputStream.close();
                    fos.close();
                    System.out.println("Running migrator...");
                    File updater = new File("config/ttc/updater.isbpl");
                    context.interpret(updater, ISBPL.readFile(updater), stack);
                    if (run("migrate", s, TTC.VERSION).pop().isTruthy()) {
                        switch (JOptionPane.showConfirmDialog(null, "" +
                                                                    "TTC failed to migrate versions. (Error: " + run("error").pop() + s + TTC.VERSION + ")\n" +
                                                                    "Do you want to reset your config (A backup will can be found in .minecraft/config/ttc/backup.cfg)?\n" +
                                                                    "Yes: Reset - No: Keep unmigrated config - Cancel: Close the game\n" +
                                                                    "If you select cancel, no changes will be made and TTC will not attempt to load the config. \n" +
                                                                    "This is usually the best option as the migration script will be fixed as soon as this is reported. \n" +
                                                                    "Please report the issue to the developer.")) {
                            case JOptionPane.YES_OPTION:
                                new File("config/ttc.cfg").delete();
                                break;
                            case JOptionPane.NO_OPTION:
                                break;
                            case JOptionPane.CANCEL_OPTION:
                                throw new Error("TTC: Force exit due to cancel option on migration-failed dialog.");
                        }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream("config/ttc/version.txt");
            fos.write(TTC.VERSION.getBytes(StandardCharsets.UTF_8));
            fos.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
