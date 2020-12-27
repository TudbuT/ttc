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
    public int delay = 1000;
    private boolean stop = false;
    
    static TPATools instance;
    
    public static TPATools getInstance() {
        return instance;
    }
    
    public TPATools() {
        instance = this;
    }
    
    {
        subButtons.add(new GuiTTC.Button("Send /tpa to everyone", text -> {
            onChat("tpa", null);
        }));
        subButtons.add(new GuiTTC.Button("Send /tpahere to everyone", text -> {
            onChat("tpahere", null);
        }));
        subButtons.add(new GuiTTC.Button("Delay: " + delay, text -> {
            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                delay -= 1000;
            else
                delay += 1000;
            
            if(delay > 5000)
                delay = 1000;
            if(delay < 1000)
                delay = 5000;
            text.set("Delay: " + delay);
        }));
        subButtons.add(new GuiTTC.Button("Stop", text -> {
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
        if(s.equalsIgnoreCase("delay")) {
            delay = Integer.parseInt(args[1]);
            ChatUtils.print("Set!");
        }
        
        if(s.equalsIgnoreCase("tpa")) {
            ChatUtils.print("Sending...");
            ThreadManager.run(() -> {
                for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap()) {
                    if(stop)
                        return;
                    try {
                        TTC.mc.player.sendChatMessage("/tpa " + info.getGameProfile().getName());
                        ChatUtils.print("Sent to " + info.getGameProfile().getName());
                        Thread.sleep(delay);
                    }
                    catch (Throwable ignore) { }
                }
                ChatUtils.print("Done!");
            });
        }
        if(s.equalsIgnoreCase("tpahere")) {
            ChatUtils.print("Sending...");
            ThreadManager.run(() -> {
                for (NetworkPlayerInfo info : Objects.requireNonNull(TTC.mc.getConnection()).getPlayerInfoMap()) {
                    if(stop)
                        return;
                    try {
                        TTC.mc.player.sendChatMessage("/tpahere " + info.getGameProfile().getName());
                        ChatUtils.print("Sent to " + info.getGameProfile().getName());
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
