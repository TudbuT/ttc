package tudbut.mod.client.ttc.mods;

import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.FlightBot;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.obj.Atomic;

public class Takeoff extends Module {
    
    boolean isTakingOff = false;
    
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onEnable() {
        ChatUtils.print("Starting elytra...");
        isTakingOff = true;
        FlightBot.activate(new Atomic<>(TTC.mc.player.getPositionVector().addVector(0, 4, 0)));
        ChatUtils.print("Bot started.");
    }
    
    @Override
    public void onTick() {
        if(!FlightBot.isFlying() && isTakingOff && TTC.player.isElytraFlying()) {
            FlightBot.deactivate();
            isTakingOff = false;
            enabled = false;
            onDisable();
            ChatUtils.print("Elytra started.");
        }
    }
    
    @Override
    public void onChat(String s, String[] args) {
    }
}
