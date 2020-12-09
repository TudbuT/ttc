package tudbut.mod.client.yac.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.mods.TPAParty;
import tudbut.mod.client.yac.utils.ChatUtils;
import tudbut.mod.client.yac.utils.Utils;

import java.io.IOException;

public class FMLEventHandler {
    
    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        if(event.getOriginalMessage().startsWith(",")) {
            event.setCanceled(true);
            ChatUtils.print("Blocked message");
            ChatUtils.history(event.getOriginalMessage());
            
            try {
                if (event.getOriginalMessage().startsWith(",t ")) {
                    for (int i = 0; i < Yac.modules.length; i++) {
                        if (Yac.modules[i].getClass().getSimpleName().equalsIgnoreCase(event.getOriginalMessage().substring(",t ".length()))) {
                            Yac.modules[i].enabled = !Yac.modules[i].enabled;
                            ChatUtils.print(String.valueOf(Yac.modules[i].enabled));
                        }
                    }
                }
                
                if (event.getOriginalMessage().startsWith(",say ")) {
                    Yac.player.sendChatMessage(event.getOriginalMessage().substring(",say ".length()));
                    ChatUtils.history(event.getOriginalMessage());
                }
    
                for (int i = 0; i < Yac.modules.length; i++) {
                    if (event.getOriginalMessage().substring(",".length()).toLowerCase().startsWith(Yac.modules[i].getClass().getSimpleName().toLowerCase())) {
                        Yac.modules[i].onChat(event.getOriginalMessage().substring(", ".length() + Yac.modules[i].getClass().getSimpleName().length()));
                    }
                }
            } catch (Exception e) {
                ChatUtils.print("Command failed!");
            }
    
        }
        else if(!event.getOriginalMessage().startsWith("/") && !event.getOriginalMessage().startsWith(".") && !event.getOriginalMessage().startsWith("#")) {
            event.setCanceled(true);
            Yac.player.sendChatMessage(">" + event.getMessage() + " ›YAC‹");
            
            ChatUtils.history(event.getOriginalMessage());
        }
    }
    
    @SubscribeEvent
    public void onServerChat(ClientChatReceivedEvent event) {
        if(event.getMessage().getUnformattedText().startsWith("BayMax") && event.getMessage().getUnformattedText().contains("Please type '")) {
            String key = event.getMessage().getUnformattedText().substring("BayMax _ Please type '".length(), "BayMax _ Please type '".length() + 4);
            Yac.player.sendChatMessage(key);
            ChatUtils.print("Auto-solved");
        }
        if(event.getMessage().getUnformattedText().contains("has requested to teleport to you.") && !event.getMessage().getUnformattedText().startsWith("<")) {
            if(Yac.modules[1].enabled)
                Yac.player.sendChatMessage("/tpaccept");
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
                Yac.modules[i].onTick();
        }
    }
}
