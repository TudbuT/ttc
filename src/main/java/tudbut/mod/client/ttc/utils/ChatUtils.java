package tudbut.mod.client.ttc.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.mods.Debug;
// How hard can it be?
import java.io.OutputStream;
import java.io.PrintStream;

public class ChatUtils { // Everything here is kinda self-explanatory
    
    public static void print(String s) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(s));
    }
    
    public static void printChatAndHotbar(String s) {
        print(s);
        printHotbar(s);
    }
    
    public static void printChatAndTitle(String s, int ms) {
        print(s);
        printTitle(s, "", ms);
    }
    
    @SuppressWarnings("ConstantConditions")
    public static void printTitle(String title, String subTitle, int ms) {
        Minecraft.getMinecraft().ingameGUI.displayTitle("§c" + title, null, 2, ms / (1000 / 20), 2);
        Minecraft.getMinecraft().ingameGUI.displayTitle(null, "§b" + subTitle, 2, ms / (1000 / 20), 2);
    }
    
    public static void printHotbar(String s) {
        Minecraft.getMinecraft().ingameGUI.setOverlayMessage(new TextComponentString(s), true);
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
    
    public static void simulateSend(String msg, boolean addToHistory) {
        msg = net.minecraftforge.event.ForgeEventFactory.onClientSendMessage(msg);
        if (msg.isEmpty()) return;
        if (addToHistory) {
            TTC.mc.ingameGUI.getChatGUI().addToSentMessages(msg);
        }
        if (net.minecraftforge.client.ClientCommandHandler.instance.executeCommand(TTC.mc.player, msg) != 0)
            return;
        TTC.mc.player.sendChatMessage(msg);
    }
}
