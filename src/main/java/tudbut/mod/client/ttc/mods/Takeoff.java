package tudbut.mod.client.ttc.mods;

import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.FlightBot;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.obj.Atomic;

public class Takeoff extends Module {
    
    boolean isTakingOff = false;
    
    @Override
    public boolean defaultEnabled() {
        return true;
    }
    
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public void onTick() {
        if(!FlightBot.isFlying() && isTakingOff && TTC.player.isElytraFlying()) {
            FlightBot.deactivate();
            isTakingOff = false;
        }
    }
    
    @Override
    public void onChat(String s, String[] args) {
        isTakingOff = true;
        FlightBot.activate(new Atomic<>(TTC.player.getPositionVector().addVector(0, 4, 0)));
    }
}
