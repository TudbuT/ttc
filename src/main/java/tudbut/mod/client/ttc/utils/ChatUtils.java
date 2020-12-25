package tudbut.mod.client.ttc.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class ChatUtils {
    
    public static void print(String s) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(s));
    }
    
    public static void history(String s) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().addToSentMessages(s);
    }
}
