package tudbut.mod.client.yac.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import tudbut.mod.client.yac.YAC;
import tudbut.mod.client.yac.mods.ChatColor;
import tudbut.mod.client.yac.mods.ChatSuffix;
import tudbut.mod.client.yac.mods.TPAParty;
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
        if(event.getOriginalMessage().startsWith(YAC.prefix)) {
            
            event.setCanceled(true);
            ChatUtils.print("Blocked message");
            ChatUtils.history(event.getOriginalMessage());
            String s = event.getOriginalMessage().substring(YAC.prefix.length());
            
            try {
                if (s.startsWith("t ")) {
                    for (int i = 0; i < YAC.modules.length; i++) {
                        if (YAC.modules[i].getClass().getSimpleName().equalsIgnoreCase(s.substring("t ".length()))) {
                            ChatUtils.print(String.valueOf(!YAC.modules[i].enabled));
                            
                            if(YAC.modules[i].enabled = !YAC.modules[i].enabled)
                                YAC.modules[i].onEnable();
                            else
                                YAC.modules[i].onDisable();
                        }
                    }
                }
                
                if (s.startsWith("say ")) {
                    YAC.player.sendChatMessage(s.substring("say ".length()));
                    ChatUtils.history(event.getOriginalMessage());
                }
    
                for (int i = 0; i < YAC.modules.length; i++) {
                    if(YAC.modules[i].enabled)
                        if (s.toLowerCase().startsWith(YAC.modules[i].getClass().getSimpleName().toLowerCase())) {
                            String args = s.substring(YAC.modules[i].getClass().getSimpleName().length() + 1);
                            YAC.modules[i].onChat(args, args.split(" "));
                        }
                }
            } catch (Exception e) {
                ChatUtils.print("Command failed!");
            }
    
        }
        else if(!event.getOriginalMessage().startsWith("/") && !event.getOriginalMessage().startsWith(".") && !event.getOriginalMessage().startsWith("#")) {
            event.setCanceled(true);
            YAC.player.sendChatMessage(ChatColor.getInstance().get() + event.getMessage() + (chatHelper == 0 && ChatSuffix.getInstance().enabled ? " ›YAC‹" : ""));
            chatHelper++;
            if(chatHelper == 6)
                chatHelper = 0;
            
            ChatUtils.history(event.getOriginalMessage());
        }
    }
    
    @SubscribeEvent
    public void onServerChat(ClientChatReceivedEvent event) {
        if(event.getMessage().getUnformattedText().startsWith("BayMax") && event.getMessage().getUnformattedText().contains("Please type '")) {
            String key = event.getMessage().getUnformattedText().substring("BayMax _ Please type '".length(), "BayMax _ Please type '".length() + 4);
            YAC.player.sendChatMessage(key);
            ChatUtils.print("Auto-solved");
        }
        for (int i = 0; i < YAC.modules.length; i++) {
            if(YAC.modules[i].enabled)
                YAC.modules[i].onServerChat(event.getMessage().getUnformattedText(), event.getMessage().getFormattedText());
        }
    }
    
    @SubscribeEvent
    public void onJoinServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
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
            if (!s.equals(YAC.VERSION)) {
                ChatUtils.print(
                        "§a§lA new YAC version was found! Current: " +
                        YAC.VERSION +
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
    public void onJoin(EntityJoinWorldEvent event) {
        YAC.player = Minecraft.getMinecraft().player;
    }
    
    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        try {
            if (event.getEntity().getName().equals(YAC.player.getName()) && event.getEntity() instanceof EntityPlayer) {
                TPAParty.getInstance().enabled = false;
                TPAParty.getInstance().onDisable();
                YAC.player = Minecraft.getMinecraft().player;
            }
        } catch (Exception ignore) { }
    }
    
    @SubscribeEvent
    public void onTick(TickEvent event) {
        EntityPlayerSP player = YAC.player;
        if(player == null)
            return;
        for (int i = 0; i < YAC.modules.length; i++) {
            if(YAC.modules[i].enabled)
                try {
                    YAC.modules[i].onTick();
                } catch (Exception ignore) {}
            YAC.modules[i].onEveryTick();
        }
    }
}
