package tudbut.mod.client.ttc.mods;

import net.minecraft.client.network.NetworkPlayerInfo;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.ThreadManager;

import java.util.Objects;

public class TPATools extends Module {
    static TPATools instance;
    // I hate antispam
    public int delay = 1000;
    private boolean stop = false;
    
    {
        subButtons.add(new GuiTTC.Button("Send /tpa to everyone", text -> {
            onChat("tpa", null);
        }));
        subButtons.add(new GuiTTC.Button("Send /tpahere to everyone", text -> {
            onChat("tpahere", null);
        }));
        subButtons.add(new GuiTTC.Button("Delay: " + delay, text -> {
            // I hate antispam
            
            
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                delay -= 1000;
            else
                delay += 1000;
            
            if (delay > 5000)
                delay = 1000;
            if (delay < 1000)
                delay = 5000;
            text.set("Delay: " + delay);
        }));
        subButtons.add(new GuiTTC.Button("Stop", text -> {
            stop = true;
            TTC.player.sendChatMessage("/tpacancel");
            
            ThreadManager.run(() -> {
                text.set("Done");
                try {
                    Thread.sleep(2000 + delay);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stop = false;
                text.set("Stop");
            });
        }));
    }
    
    PlayerSelector.Type tpaType = new PlayerSelector.Type(player -> {
        ChatUtils.simulateSend("/tpa " + player.getGameProfile().getName(), false);
    }, "/TPA");
    PlayerSelector.Type tpaHereType = new PlayerSelector.Type(player -> {
        ChatUtils.simulateSend("/tpahere " + player.getGameProfile().getName(), false);
    }, "/TPAHERE");
    
    @Override
    public void onDisable() {
        PlayerSelector.types.remove(tpaType);
        PlayerSelector.types.remove(tpaHereType);
    }
    
    @Override
    public void onEnable() {
        PlayerSelector.types.add(tpaType);
        PlayerSelector.types.add(tpaHereType);
    }
    
    public TPATools() {
        instance = this;
    }
    
    public static TPATools getInstance() {
        return instance;
    }
    
    public void updateButtons() {
        subButtons.get(2).text.set("Delay: " + delay);
    }
    
    @Override
    public void onSubTick() {
    
    }
    
    @Override
    public void onEverySubTick() { }
    
    @Override
    public void onChat(String s, String[] args) {
        if (s.equalsIgnoreCase("delay")) {
            // I hate antispam
            delay = Integer.parseInt(args[1]);
            ChatUtils.print("Set!");
        }
        
        if (s.equalsIgnoreCase("tpa")) {
            ChatUtils.print("Sending...");
            // This would stop the game if it wasn't in a separate thread
            ThreadManager.run(() -> {
                // Loop through all players
                for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                    if (stop)
                        return;
                    try {
                        // Send /tpa <player>
                        TTC.mc.player.sendChatMessage("/tpa " + info.getGameProfile().getName());
                        // Notify the user
                        ChatUtils.print("Sent to " + info.getGameProfile().getName());
                        // I hate antispam
                        Thread.sleep(TPATools.getInstance().delay);
                    }
                    catch (Throwable ignore) { }
                }
                ChatUtils.print("Done!");
            });
        }
        if (s.equalsIgnoreCase("tpahere")) {
            ChatUtils.print("Sending...");
            // This would stop the game if it wasn't in a separate thread
            ThreadManager.run(() -> {
                // Loop through all players
                for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap().toArray(new NetworkPlayerInfo[0])) {
                    if (stop)
                        return;
                    try {
                        // Send /tpahere <player>
                        TTC.mc.player.sendChatMessage("/tpahere " + info.getGameProfile().getName());
                        // Notify the user
                        ChatUtils.print("Sent to " + info.getGameProfile().getName());
                        // I hate antispam
                        Thread.sleep(TPATools.getInstance().delay);
                    }
                    catch (Throwable ignore) { }
                }
                ChatUtils.print("Done!");
            });
        }
        updateButtons();
    }
    
    public void loadConfig() {
        delay = Integer.parseInt(cfg.get("delay"));
        updateButtons();
    }
    
    @Override
    public void updateConfig() {
        cfg.put("delay", "" + delay);
    }
}
