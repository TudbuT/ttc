package tudbut.mod.client.ttc.utils;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttc.TTC;
import tudbut.obj.Atomic;

import java.util.Date;

public class FlightBot {
    
    private static Atomic<Vec3d> destination;
    private static EntityPlayerSP player = TTC.player;
    private static volatile boolean lock = false;
    private static boolean flying = false;
    private static boolean active = false;
    private static long tookOff = 0;
    private static double speed = 1;
    
    public static boolean isActive() {
        return active;
    }
    
    public static boolean isFlying() {
        return flying && player.getPositionVector().distanceTo(destination.get()) > 1;
    }
    
    private FlightBot() { }
    
    public static void activate(Atomic<Vec3d> destination, double speed) {
        while (lock);
        flying = true;
        active = true;
        FlightBot.speed = speed;
        FlightBot.destination = destination;
    }
    
    public static void activate(Atomic<Vec3d> destination) {
        activate(destination, 1);
    }
    
    public static void deactivate() {
        active = false;
        speed = 1;
    }
    
    public static void updateDestination(Atomic<Vec3d> destination) {
        while (lock);
        FlightBot.destination = destination;
    }
    
    public static void setSpeed(double speed) {
        FlightBot.speed = speed;
    }
    
    private static void takeOff() {
        player = TTC.player;
    
        if (player.onGround) {
            if (!player.isElytraFlying()) {
                tookOff = 0;
                player.jump();
            }
        } else if (player.fallDistance > 0.2) {
            player.rotationPitch = -20;
            player.connection.sendPacket(new CPacketPlayer.Rotation(TTC.player.rotationYaw, -20, false));
            player.connection.sendPacket(new CPacketEntityAction(player, CPacketEntityAction.Action.START_FALL_FLYING));
            tookOff = new Date().getTime();
        }
    }
    
    public static synchronized boolean tickBot() {
        if(!active)
            return false;
        
        player = TTC.player;
        
        if(!player.isElytraFlying()) {
            takeOff();
            return false;
        }
        
        if(new Date().getTime() - tookOff < 1000 && tookOff != 0) {
            return true;
        }
        
        if(destination.get() == null) {
            return false;
        }
        
        lock = true;
        double x, y, z;
        Vec3d dest = destination.get();
        double dx = dest.x - player.posX, dy = dest.y - player.posY, dz = dest.z - player.posZ;
        
        
        double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        if(d < 1) {
            d = 1;
            flying = false;
        }
        else
            flying = true;
    
        x = dx / d;
        y = dy / d;
        z = dz / d;
        
        player.motionX = x;
        player.motionY = y;
        player.motionZ = z;
        lock = false;
        
        return true;
    }
}
