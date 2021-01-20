package tudbut.mod.client.ttc.mods;

import net.minecraft.entity.player.EntityPlayer;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.Module;

public class Freecam extends Module {
    
    @Override
    public boolean displayOnClickGUI() {
        return false;
    }
    
    @Override
    public boolean doStoreEnabled() {
        return false;
    }
    
    public void onEnable() {
        TTC.mc.setRenderViewEntity(new EntityPlayer(TTC.mc.world, TTC.mc.getSession().getProfile()) {
            @Override
            protected void entityInit() {
                super.entityInit();
                //setHealth(20);
                setPositionAndRotation(TTC.player.posX, TTC.player.posY, TTC.player.posZ, TTC.player.cameraYaw, TTC.player.cameraPitch);
            }
    
            @Override
            public void onLivingUpdate() {
                super.onLivingUpdate();
            }
    
            @Override
            public boolean isSpectator() {
                return true;
            }
    
            @Override
            public boolean isCreative() {
                return false;
            }
        });
    }
    
    @Override
    public int danger() {
        return 1;
    }
    
    @Override
    public void onDisable() {
        TTC.mc.setRenderViewEntity(TTC.mc.player);
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
}
