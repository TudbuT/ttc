package tudbut.mod.client.yac.mods;

import net.minecraft.client.network.NetworkPlayerInfo;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.gui.GuiYAC;
import tudbut.mod.client.yac.utils.ChatUtils;
import tudbut.mod.client.yac.utils.Module;
import tudbut.mod.client.yac.utils.ThreadManager;

import java.util.Objects;

public class TPATools extends Module {
    static TPATools instance;
    private int delay = 1000;
    private boolean stop = false;
    
    {
        subButtons.add(new GuiYAC.Button("Send /tpa to everyone", text -> {
            onChat("tpa", null);
        }));
        subButtons.add(new GuiYAC.Button("Send /tpahere to everyone", text -> {
            onChat("tpahere", null);
        }));
        subButtons.add(new GuiYAC.Button("Delay: " + delay, text -> {
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
        subButtons.add(new GuiYAC.Button("Stop", text -> {
            stop = true;
        
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
    public void onTick() {
    
    }
    
    @Override
    public void onEveryTick() { }
    
    @Override
    public void onChat(String s, String[] args) {
        if (s.equalsIgnoreCase("delay")) {
            delay = Integer.parseInt(args[1]);
            ChatUtils.print("Set!");
        }
        
        if (s.equalsIgnoreCase("tpa")) {
            ChatUtils.print("Sending...");
            ThreadManager.run(() -> {
                for (NetworkPlayerInfo info : Objects.requireNonNull(Yac.mc.getConnection()).getPlayerInfoMap()) {
                    if(stop)
                        return;
                    try {
                        Yac.mc.player.sendChatMessage("/tpa " + info.getGameProfile().getName());
                        ChatUtils.print("Sent to " + info.getGameProfile().getName());
                    }
                    catch (Throwable e) { }
                    try {
                        Thread.sleep(delay);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ChatUtils.print("Done!");
            });
        }
        if (s.equalsIgnoreCase("tpahere")) {
            ChatUtils.print("Sending...");
            ThreadManager.run(() -> {
                for (NetworkPlayerInfo info : Objects.requireNonNull(Yac.mc.getConnection()).getPlayerInfoMap()) {
                    if(stop)
                        return;
                    try {
                        Yac.mc.player.sendChatMessage("/tpahere " + info.getGameProfile().getName());
                        ChatUtils.print("Sent to " + info.getGameProfile().getName());
                    }
                    catch (Throwable e) { }
                    try {
                        Thread.sleep(delay);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ChatUtils.print("Done!");
            });
        }
        updateButtons();
    }
    
    public void loadConfig() {
        delay = Integer.parseInt(cfg.getOrDefault("delay", "1000"));
        updateButtons();
    }
    
    @Override
    public void updateConfig() {
        cfg.put("delay", "" + delay);
    }
}
