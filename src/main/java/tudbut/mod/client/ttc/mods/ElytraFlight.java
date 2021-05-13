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
import tudbut.mod.client.ttc.utils.Setting;

public class ElytraFlight extends Module {
    boolean init;
    float speed = 1;
    
    @Override
    public void init() {
        subButtons.clear();
        subButtons.add(Setting.createSecureFloat(1, 50, 1, 10, "Speed: $val", this, "speed"));
    }
    
    @Override
    public void onTick() {
        if(TTC.mc.world == null) {
            init = false;
            return;
        }
        EntityPlayerSP player = TTC.player;
    
        boolean blockMovement = FlightBot.tickBot();
        
        if(init) {
            if (TTC.player == TTC.mc.getRenderViewEntity()) {
                if (!blockMovement) {
                    Vec2f movementVec = player.movementInput.getMoveVector();
    
                    float f1 = MathHelper.sin(player.rotationYaw * 0.017453292F);
                    float f2 = MathHelper.cos(player.rotationYaw * 0.017453292F);
                    double x = movementVec.x * f2 - movementVec.y * f1;
                    double y = (player.movementInput.jump ? 1 : 0) + (player.movementInput.sneak ? -1 : 0);
                    double z = movementVec.y * f2 + movementVec.x * f1;
                    float d = (float) Math.sqrt(x * x + y * y + z * z);
    
                    if (d < 1) {
                        d = 1;
                    }
    
                    player.motionX = x / d * speed;
                    player.motionY = y / d * speed;
                    player.motionZ = z / d * speed;
                }
            }
            else {
                player.motionX = 0;
                player.motionY = 0;
                player.motionZ = 0;
            }
            negateElytraFallMomentum(player);
            
        } else if(player.isElytraFlying()) {
            player.motionX = 0;
            player.motionY = 0;
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
    
    @Override
    public void onChat(String s, String[] args) {
    }
    
    @Override
    public int danger() {
        return 2;
    }
    
    @Override
    public void updateConfig() {
        cfg.put("speed", speed + "");
    }
    
    @Override
    public void loadConfig() {
        speed = Float.parseFloat(cfg.get("speed"));
    }
}
