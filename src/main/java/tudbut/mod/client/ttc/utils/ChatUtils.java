package tudbut.mod.client.ttc.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.mods.Debug;

import java.io.OutputStream;
import java.io.PrintStream;

public class ChatUtils { // Everything here is kinda self-explanatory
    
    public static void print(String s) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(s));
    }
    
    public static void history(String s) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(s);
    }
    
    public static OutputStream chatOut() {
        return new OutputStream() {
            String s = "";
            
            @Override
            public void write(int i) {
                if ((char) i == '\n') {
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(s));
                    s = "";
                } else
                    s += (char) i;
            }
        };
    }
    
    public static PrintStream chatPrinter() {
        return new PrintStream(chatOut());
    }
    
    public static OutputStream chatOutDebug() {
        return new OutputStream() {
            String s = "";
            
            @Override
            public void write(int i) {
                
                if ((char) i == '\n') {
                    if (Debug.getInstance().enabled)
                        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(s));
                    System.out.println(s);
                    s = "";
                } else
                    s += (char) i;
            }
        };
    }
    
    public static PrintStream chatPrinterDebug() {
        return new PrintStream(chatOutDebug());
    }
}
