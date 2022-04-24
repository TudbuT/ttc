package tudbut.mod.client.ttc.mods;

import de.tudbut.io.StreamReader;
import de.tudbut.io.StreamWriter;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.isbpl.ISBPLModule;
import tudbut.obj.Save;
import tudbut.obj.TLMap;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;
import tudbut.tools.ConfigSaverTCN;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ISBPLModules extends Module {

    ArrayList<String> isbplModules = new ArrayList<>();

    TLMap<String, ISBPLModule> modules = new TLMap<>();

    @Override
    public void onEnable() {
        for (String module : isbplModules) {
            loadModule(module);
        }
    }

    @Override
    public void onDisable() {
        for (String module : isbplModules) {
            unloadModule(module);
        }
    }

    public Module loadModule(String s) {
        try {
            ISBPLModule module = ISBPLModule.Loader.create(s);
            if(module == null)
                return null;
            modules.set(s, module);
            try {
                try {
                    TCN tcn = JSON.read(new StreamReader(new FileInputStream("config/ttc/modules/config/" + s + ".isbplmodulecfg.json")).readAllAsString());
                    ConfigSaverTCN.loadConfig(module, tcn);
                    try {
                        if (module.enabled)
                            module.onEnable();
                    } catch (NullPointerException ignored) {
                    }
                } catch (Exception ignored) { }
                module.onConfigLoad();
            } catch (Exception e) {
                e.printStackTrace();
            }
            TTC.addModule(module);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void unloadModule(String s) {
        if(modules.get(s) != null) {
            modules.get(s).onDisable();
            modules.get(s).updateConfig();
            try {
                StreamWriter writer = new StreamWriter(new FileOutputStream("config/ttc/modules/config/" + modules.get(s).id + ".isbplmodulecfg.json"));
                writer.writeChars(JSON.writeReadable(ConfigSaverTCN.saveConfig(modules.get(s))).toCharArray());
            } catch (Exception ignored) { }
            TTC.removeModule(modules.get(s));
            modules.set(s, null);
        }
    }

    public void reloadModule(String s) {
        unloadModule(s);
        if(loadModule(s) == null) {
            ChatUtils.print("Couldn't load module " + s);
        }
    }

    @Override
    public void onChat(String s, String[] args) {
        if(new File("config/ttc/modules/config").mkdirs()) {
            ChatUtils.print("Put ISBPLModule files in your config/ttc/modules folder!");
        }
        try {
            StreamWriter writer = new StreamWriter(new FileOutputStream("config/ttc/modules/Example.ttcmodule.isbpl"));
            writer.writeChars(("" +
                    "def name \"Example\" =name\n" +
                    "" +
                    "func onEnable {\n" +
                    "    \"Example module enabled!\" jm printChat1 \"jm = The module\" #\n" +
                    "    \"net.minecraft.util.EnumHand\" JIO class MAIN_HAND mc player swingArm1\n" +
                    "}\n" +
                    "").toCharArray());
            writer.stream.close();
        } catch (IOException ignored) {
        }
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("add")) {
                isbplModules.remove(args[1]);
                isbplModules.add(args[1]);
                unloadModule(args[1]);
                if(loadModule(args[1]) != null)
                    ChatUtils.print("Loaded!");
                else
                    ChatUtils.print("Failed to load module. It seems to be faulty!");
            }
            if(args[0].equalsIgnoreCase("remove")) {
                isbplModules.remove(args[1]);
                unloadModule(args[1]);
                ChatUtils.print("Unloaded!");
            }
            if(args[0].equalsIgnoreCase("reload")) {
                reloadModule(args[1]);
                ChatUtils.print("Reloaded!");
            }
        }
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                for (String module : isbplModules) {
                    reloadModule(module);
                }
                ChatUtils.print("Reloaded!");
            }
        }
    }
    
    @Override
    public void updateConfig() {
        for (int i = 0 ; i < isbplModules.size() ; i++) {
            String isbplModule = isbplModules.get(i);
            modules.get(isbplModule).updateConfig();
            try {
                StreamWriter writer = new StreamWriter(new FileOutputStream("config/ttc/modules/config/" + modules.get(isbplModule).id + ".isbplmodulecfg.json"));
                writer.writeChars(JSON.writeReadable(ConfigSaverTCN.saveConfig(modules.get(isbplModule))).toCharArray());
            } catch (Exception ignored) { }
            cfg.put("mo" + i, isbplModule);
        }
    }
    
    @Override
    public void loadConfig() {
        for (String s : cfg.keySet()) {
            if(s.startsWith("mo")) {
                isbplModules.add(cfg.get(s));
            }
        }
    }
}
