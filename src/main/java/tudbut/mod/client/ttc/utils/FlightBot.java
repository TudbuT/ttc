package tudbut.mod.client.ttc.utils;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttc.TTC;
import tudbut.obj.Atomic;

import java.util.ArrayList;
import java.util.Date;

public class FlightBot {
    
    private static final ArrayList<FlightBot> bots = new ArrayList<>();
    
    {
        bots.add(this);
    }
    
    private Atomic<Vec3d> destination;
    private EntityPlayerSP player = TTC.player;
    private boolean lock = false;
    private boolean isInTakeoff = false;
    private long tookOff = 0;
    
    public boolean isActive() {
        return bots.contains(this);
    }
    
    private FlightBot() { }
    
    public static FlightBot activate(Atomic<Vec3d> destination) {
        FlightBot bot = new FlightBot();
        bot.destination = destination;
        return bot;
    }
    
    public static void activate(FlightBot bot) {
        if(bot == null || bot.isActive())
            return;
        
        bots.add(bot);
    }
    
    public static void deactivate(FlightBot bot) {
        if(bot == null || !bot.isActive())
            return;
        
        bot.destination.set(bot.player.getPositionVector());
        bots.remove(bot);
    }
    
    public static void updateDestination(FlightBot bot, Atomic<Vec3d> destination) {
        while (bot.lock);
        bot.destination = destination;
    }
    
    public static boolean tickBots() {
        for (int i = 0; i < bots.size(); i++) {
            bots.get(i).tickBot();
        }
        return bots.size() > 0;
    }
    
    private void takeOff() {
        player = TTC.player;
        
        if(player.onGround) {
            if(isInTakeoff)
                return;
            tookOff = 0;
            isInTakeoff = true;
            player.jump();
        }
        else if(player.fallDistance > 0.1) {
            player.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING));
            isInTakeoff = false;
            tookOff = new Date().getTime();
        }
    }
    
    private synchronized void tickBot() {
        player = TTC.player;
        
        if(!player.isElytraFlying()) {
            takeOff();
            return;
        }
        
        if(new Date().getTime() - tookOff < 500 && tookOff != 0) {
            return;
        }
        
        if(destination.get() == null) {
            return;
        }
        
        lock = true;
        double x, y, z;
        Vec3d dest = destination.get();
        double dx = dest.x - player.posX, dy = dest.y - player.posY, dz = dest.z - player.posZ;
        
        
        double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        if(d < 1) {
            d = 1;
        }
        
        x = dx / d;
        y = dy / d;
        z = dz / d;
        
        player.motionX = x;
        player.motionY = y;
        player.motionZ = z;
        lock = false;
    }
}
