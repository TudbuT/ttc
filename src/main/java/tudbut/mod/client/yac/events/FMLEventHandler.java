package tudbut.mod.client.yac.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.utils.ChatUtils;

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
                            Yac.modules[i].enabled = !Yac.modules[i].enabled;
                            ChatUtils.print(String.valueOf(Yac.modules[i].enabled));
                        }
                    }
                }
                
                if (s.startsWith("say ")) {
                    Yac.player.sendChatMessage(s.substring("say ".length()));
                    ChatUtils.history(event.getOriginalMessage());
                }
                
                for (int i = 0; i < Yac.modules.length; i++) {
                    if (s.toLowerCase().startsWith(Yac.modules[i].getClass().getSimpleName().toLowerCase())) {
                        Yac.modules[i].onChat(s.substring(Yac.modules[i].getClass().getSimpleName().length() + 1));
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
            if(chatHelper == 5)
                chatHelper = 0;
            
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
                try {
                    Yac.modules[i].onTick();
                } catch (Exception ignore) {}
        }
    }
}
