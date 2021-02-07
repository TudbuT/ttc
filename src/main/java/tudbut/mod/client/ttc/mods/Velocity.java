package tudbut.mod.client.ttc.mods;

import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.Module;

public class Velocity extends Module {
    
    
    @SubscribeEvent
    public void onKnockBackDeal(LivingKnockBackEvent event) {
        if(enabled)
            if(event.getEntityLiving().getUniqueID().equals(TTC.player.getUniqueID()))
                event.setCanceled(true);
    }
    
    @Override
    public void onEveryTick() {
        if(enabled)
            TTC.mc.player.entityCollisionReduction = 1;
        else
            TTC.mc.player.entityCollisionReduction = 0;
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
}
