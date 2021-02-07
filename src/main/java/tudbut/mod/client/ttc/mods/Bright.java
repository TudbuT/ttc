package tudbut.mod.client.ttc.mods;

import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.Module;

public class Bright extends Module
{
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    public void onEveryTick() {
        if (enabled) {
            PotionEffect p;
            TTC.player.addPotionEffect(p = new PotionEffect(
                    MobEffects.NIGHT_VISION,
                    1000,
                    127,
                    true,
                    false
            ));
            p.setPotionDurationMax(true);
        } else
           TTC.player.removeActivePotionEffect(MobEffects.NIGHT_VISION);
    
    }
}
