package tudbut.mod.client.ttc.utils.isbpl;

import de.tudbut.io.StreamReader;
import net.minecraft.client.Minecraft;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.obj.Save;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ISBPLModule extends Module implements ISBPLScript {

    private final ISBPL context;
    public final String id;
    
    public TCN config;
    
    @Override
    public ISBPL context() {
        return context;
    }
    
    @Override
    public String toString() {
        String s = "ISBPLModule (ERROR)";
        try {
            s = context.toJavaString(run("name").pop());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return s;
    }

    public ISBPLModule(ISBPL context, String id) {
        this.context = context;
        try {
            context.natives.put("jm", stack -> stack.push(context.toISBPL(this)));
            context.natives.put("mc", stack -> stack.push(context.toISBPL(Minecraft.getMinecraft())));
            run("native mc native jm");
            run("def config jm config =config");
            run("def sb jm subButtons =sb");
            run("def Setting \"tudbut.mod.client.ttc.utils.Setting\" JIO class =Setting");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        this.id = id;

        updateBinds();
    }

    public void updateBinds() {
        try {
            if(functionExists("updateBinds"))
                run("updateBinds");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        try {
            if(functionExists("onEnable"))
                run("onEnable");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            if(functionExists("onDisable"))
                run("onDisable");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTick() {
        try {
            if(functionExists("onTick"))
                run("onTick");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEveryTick() {
        try {
            if(functionExists("onEveryTick"))
                run("onEveryTick");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSubTick() {
        try {
            if(functionExists("onSubTick"))
                run("onSubTick");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEverySubTick() {
        try {
            if(functionExists("onEverySubTick"))
                run("onEverySubTick");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onServerChat(String s, String formatted) {
        try {
            if (functionExists("onServerChat"))
                return run("onServerChat", s, formatted).pop().isTruthy();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onChat(String s, String[] args) {
        try {
            if(functionExists("onChat"))
                run("onChat", s, args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        try {
            if(functionExists("onEveryChat"))
                run("onEveryChat", s, args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void printChat(String toPrint) {
        ChatUtils.print(toPrint);
    }

    @Save
    String cfgStr = "{}";

    public void onConfigSave() {
        cfgStr = JSON.write(config);
    }

    public void onConfigLoad() {
        try {
            config = JSON.read(cfgStr);
        }
        catch (JSON.JSONFormatException e) {
            e.printStackTrace();
        }
    }

    public static class Loader {

        public static ISBPLModule create(String id) {
            try {
                ISBPL context = ISBPLScript.Loader.makeContext();
                Stack<ISBPLObject> stack = new Stack<>();
                File file = new File("config/ttc/modules/" + id + ".ttcmodule.isbpl").getAbsoluteFile();
                context.interpret(file, ISBPL.readFile(file), stack);
                return new ISBPLModule(context, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
