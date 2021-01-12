package tudbut.mod.client.ttc.mods;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.PlayerCapabilities;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.Module;

public class CreativeFlight extends Module {
    boolean init;
    @Override
    public void onTick() {
        if(TTC.mc.world == null) {
            init = false;
            return;
        }
        EntityPlayerSP player = TTC.player;
        PlayerCapabilities capabilities = player.capabilities;
        if(init) {
            capabilities.isFlying = true;
        } else if(player.isElytraFlying()) {
            player.motionX = 0;
            player.motionY = 0.5;
            player.motionZ = 0;
            init = true;
        }
        
        if(Keyboard.isKeyDown(Keyboard.KEY_Z) || player.onGround) {
            onDisable();
        }
    }
    
    @Override
    public void onDisable() {
        EntityPlayerSP player = TTC.player;
        PlayerCapabilities capabilities = player.capabilities;
        capabilities.isFlying = false;
        init = false;
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
}
