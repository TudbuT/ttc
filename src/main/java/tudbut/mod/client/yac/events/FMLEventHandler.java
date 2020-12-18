package tudbut.mod.client.yac.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.utils.ChatUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class FMLEventHandler {
    
    private int chatHelper = 0;
    
    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        if(event.getOriginalMessage().startsWith(Yac.prefix)) {
            event.setCanceled(true);
            ChatUtils.print("Blocked message");
            ChatUtils.history(event.getOriginalMessage());
            String s = event.getOriginalMessage().substring(Yac.prefix.length());
            
            try {
                if (s.startsWith("t ")) {
                    for (int i = 0; i < Yac.modules.length; i++) {
                        if (Yac.modules[i].getClass().getSimpleName().equalsIgnoreCase(s.substring("t ".length()))) {
                            ChatUtils.print(String.valueOf(!Yac.modules[i].enabled));
                            
                            if(Yac.modules[i].enabled = !Yac.modules[i].enabled)
                                Yac.modules[i].onEnable();
                            else
                                Yac.modules[i].onDisable();
                        }
                    }
                }
                
                if (s.startsWith("say ")) {
                    Yac.player.sendChatMessage(s.substring("say ".length()));
                    ChatUtils.history(event.getOriginalMessage());
                }
                
                for (int i = 0; i < Yac.modules.length; i++) {
                    if(Yac.modules[i].enabled)
                        if (s.toLowerCase().startsWith(Yac.modules[i].getClass().getSimpleName().toLowerCase())) {
                            String args = s.substring(Yac.modules[i].getClass().getSimpleName().length() + 1);
                            Yac.modules[i].onChat(args, args.split(" "));
                        }
                }
            } catch (Exception e) {
                ChatUtils.print("Command failed!");
            }
            
        }
        else if(!event.getOriginalMessage().startsWith("/") && !event.getOriginalMessage().startsWith(".") && !event.getOriginalMessage().startsWith("#")) {
            event.setCanceled(true);
            Yac.player.sendChatMessage(">" + event.getMessage() + (chatHelper == 0 ? " ›YAC‹" : ""));
            chatHelper++;
            if(chatHelper == 6)
                chatHelper = 0;
            
            ChatUtils.history(event.getOriginalMessage());
        }
    }
    
    
    @SubscribeEvent
    public void onJoin(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        try {
            URL updateCheckURL = new URL("https://raw.githubusercontent.com/TudbuT/yacpub/master/version.txt");
            InputStream stream = updateCheckURL.openConnection().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            
            String s;
            StringBuilder builder = new StringBuilder();
            while ((s = reader.readLine()) != null) {
                builder.append(s);
            }
            
            s = builder.toString();
            if(!s.equals(Yac.VERSION)) {
                ChatUtils.print(
                        "§a§lA new YAC version was found! Current: " +
                        Yac.VERSION +
                        ", New: " +
                        s +
                        "! Please consider updating at " +
                        "https://github.com/TudbuT/yacpub/releases/tag/" +
                        s
                );
            }
        }
        catch (IOException e) {
            ChatUtils.print("Unable to check for a new version!");
            e.printStackTrace();
        }
    }
    
    @SubscribeEvent
    public void onServerChat(ClientChatReceivedEvent event) {
        if(event.getMessage().getUnformattedText().startsWith("BayMax") && event.getMessage().getUnformattedText().contains("Please type '")) {
            String key = event.getMessage().getUnformattedText().substring("BayMax _ Please type '".length(), "BayMax _ Please type '".length() + 4);
            Yac.player.sendChatMessage(key);
            ChatUtils.print("Auto-solved");
        }
        for (int i = 0; i < Yac.modules.length; i++) {
            if(Yac.modules[i].enabled)
                Yac.modules[i].onServerChat(event.getMessage().getUnformattedText(), event.getMessage().getFormattedText());
        }
    }
    
    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        Yac.player = Minecraft.getMinecraft().player;
    }
    
    @SubscribeEvent
    public void onTick(TickEvent event) {
        EntityPlayerSP player = Yac.player;
        if(player == null)
            return;
        for (int i = 0; i < Yac.modules.length; i++) {
            if(Yac.modules[i].enabled)
                try {
                    Yac.modules[i].onTick();
                } catch (Exception ignore) {}
            Yac.modules[i].onEveryTick();
        }
    }
}
