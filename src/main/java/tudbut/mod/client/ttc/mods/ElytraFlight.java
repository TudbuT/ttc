package tudbut.mod.client.ttc.mods;

import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.InventoryUtils;
import tudbut.mod.client.ttc.utils.Module;

public class ElytraFlight extends Module {
    boolean init;
    
    @Override
    public void onTick() {
        if(TTC.mc.world == null) {
            init = false;
            return;
        }
        EntityPlayerSP player = TTC.player;
        if(init) {
            Vec2f movementVec = player.movementInput.getMoveVector();
            
            float f1 = MathHelper.sin(player.rotationYaw * 0.017453292F);
            float f2 = MathHelper.cos(player.rotationYaw * 0.017453292F);
            player.motionX = movementVec.x * f2 - movementVec.y * f1;
            player.motionY = (player.movementInput.jump ? 1 : 0) + (player.movementInput.sneak ? -1 : 0);
            player.motionZ = movementVec.y * f2 + movementVec.x * f1;
            
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
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
}
