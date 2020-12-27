package tudbut.mod.client.ttc.events;

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
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.mods.ChatColor;
import tudbut.mod.client.ttc.mods.ChatSuffix;
import tudbut.mod.client.ttc.mods.TPAParty;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.ThreadManager;
import tudbut.mod.client.ttc.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class FMLEventHandler {
    
    private int chatHelper = 0;
    
    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        if(event.getOriginalMessage().startsWith(TTC.prefix)) {
            
            event.setCanceled(true);
            ChatUtils.print("Blocked message");
            ChatUtils.history(event.getOriginalMessage());
            String s = event.getOriginalMessage().substring(TTC.prefix.length());
            
            try {
                if (s.startsWith("t ")) {
                    for (int i = 0; i < TTC.modules.length; i++) {
                        if (TTC.modules[i].getClass().getSimpleName().equalsIgnoreCase(s.substring("t ".length()))) {
                            ChatUtils.print(String.valueOf(!TTC.modules[i].enabled));
                            
                            if(TTC.modules[i].enabled = !TTC.modules[i].enabled)
                                TTC.modules[i].onEnable();
                            else
                                TTC.modules[i].onDisable();
                        }
                    }
                }
                
                if (s.startsWith("say ")) {
                    TTC.player.sendChatMessage(s.substring("say ".length()));
                    ChatUtils.history(event.getOriginalMessage());
                }
                
                if(s.equals("help")) {
                    String help = Utils.removeNewlines(Utils.getRemote("help.chat.txt", false));
                    if(help == null) {
                        ChatUtils.print("Unable retrieve help message! Check your connection!");
                    }
                    else {
                        help = help.replaceAll("%p", TTC.prefix);
                        ChatUtils.print(help);
                    }
                }
                
                for (int i = 0; i < TTC.modules.length; i++) {
                    if(TTC.modules[i].enabled)
                        if (s.toLowerCase().startsWith(TTC.modules[i].getClass().getSimpleName().toLowerCase())) {
                            String args = s.substring(TTC.modules[i].getClass().getSimpleName().length() + 1);
                            TTC.modules[i].onChat(args, args.split(" "));
                        }
                }
            } catch (Exception e) {
                ChatUtils.print("Command failed!");
            }
            
        }
        else if(!event.getOriginalMessage().startsWith("/") && !event.getOriginalMessage().startsWith(".") && !event.getOriginalMessage().startsWith("#")) {
            event.setCanceled(true);
            TTC.player.sendChatMessage(ChatColor.getInstance().get() + event.getMessage() + (chatHelper == 0 && ChatSuffix.getInstance().enabled ? " ›TTC‹" : ""));
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
            TTC.player.sendChatMessage(key);
            ChatUtils.print("Auto-solved");
        }
        for (int i = 0; i < TTC.modules.length; i++) {
            if(TTC.modules[i].enabled)
                TTC.modules[i].onServerChat(event.getMessage().getUnformattedText(), event.getMessage().getFormattedText());
        }
    }
    
    @SubscribeEvent
    public void onJoinServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ThreadManager.run(() -> {
            while (TTC.mc.world != null) {
                try {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String s = Utils.removeNewlines(Utils.getRemote("version.txt", true));
                if (s == null) {
                    ChatUtils.print("Unable to check for a new version! Check your connection!");
                } else if (!s.equals(TTC.VERSION)) {
                    ChatUtils.print(
                            "§a§lA new TTC version was found! Current: " +
                            TTC.VERSION +
                            ", New: " +
                            s +
                            "! Please consider updating at " +
                            "https://github.com/TudbuT/ttc/releases/tag/" +
                            s
                    );
                }
                try {
                    Thread.sleep(10000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        TTC.player = Minecraft.getMinecraft().player;
    }
    
    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        try {
            if (event.getEntity().getName().equals(TTC.player.getName()) && event.getEntity() instanceof EntityPlayer) {
                TPAParty.getInstance().enabled = false;
                TPAParty.getInstance().onDisable();
                TTC.player = Minecraft.getMinecraft().player;
                ChatUtils.print("§c§l§k|||§c§l You died at " + event.getEntity().getPosition());
            }
        } catch (Exception ignore) { }
    }
    
    @SubscribeEvent
    public void onTick(TickEvent event) {
        EntityPlayerSP player = TTC.player;
        if(player == null)
            return;
        for (int i = 0; i < TTC.modules.length; i++) {
            if(TTC.modules[i].enabled)
                try {
                    TTC.modules[i].onTick();
                } catch (Exception ignore) {}
            TTC.modules[i].onEveryTick();
        }
    }
}
