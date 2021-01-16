package tudbut.mod.client.ttc.mods;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.FlightBot;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.obj.Atomic;

public class ElytraFlight extends Module {
    boolean init;
    Vec3d dest;
    
    @Override
    public void onTick() {
        if(TTC.mc.world == null) {
            init = false;
            return;
        }
        EntityPlayerSP player = TTC.player;
        
        if(player.posY >= 270) {
            if(bot != null)
                FlightBot.updateDestination(bot, new Atomic<>(dest));
        }
        
        if(dest != null)
            if(player.getDistance(dest.x, dest.y, dest.z) < 1) {
                FlightBot.deactivate(bot);
            }
    
        boolean blockMovement = FlightBot.tickBots();
        
        if(init) {
            if(!blockMovement) {
                Vec2f movementVec = player.movementInput.getMoveVector();
    
                float f1 = MathHelper.sin(player.rotationYaw * 0.017453292F);
                float f2 = MathHelper.cos(player.rotationYaw * 0.017453292F);
                double x = movementVec.x * f2 - movementVec.y * f1;
                double y = (player.movementInput.jump ? 1 : 0) + (player.movementInput.sneak ? -1 : 0);
                double z = movementVec.y * f2 + movementVec.x * f1;
                float d = (float) Math.sqrt(x * x + y * y + z * z);
    
                if(d < 1) {
                    d = 1;
                }
                
                player.motionX = x / d;
                player.motionY = y / d;
                player.motionZ = z / d;
            }
            
            negateElytraFallMomentum(player);
        } else if(player.isElytraFlying()) {
            player.motionX = 0;
            player.motionY = 0.25;
            player.motionZ = 0;
            init = true;
    
            negateElytraFallMomentum(player);
        }
    
        if (!player.isElytraFlying()) {
            player.capabilities.isFlying = false;
            init = false;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_Z) && TTC.mc.currentScreen == null && init) {
            init = false;
            player.capabilities.isFlying = true;
        }
    }
    
    public void negateElytraFallMomentum(EntityPlayer player) {
        if (!player.isInWater()) {
            if (!player.isInLava()) {
                Vec3d vec3d = player.getLookVec();
                float f = player.rotationPitch * 0.017453292F;
                double d = vec3d.lengthVector();
                float f1 = MathHelper.cos(f);
                f1 = (float) ((double) f1 * (double) f1 * Math.min(1.0D, d / 0.4D));
                player.motionY -= -0.08D + (double) f1 * 0.06D;
            }
        }
    }
    
    @Override
    public void onDisable() {
    }
    
    FlightBot bot;
    
    @Override
    public void onChat(String s, String[] args) {
        if(TTC.mc.world == null) {
            return;
        }
        EntityPlayerSP player = TTC.player;
        
        FlightBot.deactivate(bot);
        if(args.length == 2) {
            dest = new Vec3d(Double.parseDouble(args[0]), 270, Double.parseDouble(args[1]));
            Vec3d v = new Vec3d(player.posX, 270, player.posZ);
            bot = FlightBot.activate(new Atomic<>(v));
            System.out.println("Flying...");
        }
    }
}
