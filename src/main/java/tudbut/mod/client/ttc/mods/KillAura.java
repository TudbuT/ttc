package tudbut.mod.client.ttc.mods;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.tools.Queue;

import java.util.ArrayList;
import java.util.Date;

public class KillAura extends Module {
    int delay = 200;
    long last = 0;
    Queue<EntityPlayer> toAttack = new Queue<>();
    ArrayList<String> targets = new ArrayList<>();
    
    static KillAura instance;
    {
        instance = this;
    }
    public static KillAura getInstance() {
        return instance;
    }
    
    {
        subButtons.add(new GuiTTC.Button("Delay: " + delay, text -> {
            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                delay -= 25;
            else
                delay += 25;
            
            if(delay < 50)
                delay = 1000;
            if(delay > 1000)
                delay = 50;
            
            text.set("Delay: " + delay);
        }));
    }
    
    public void updateButtons() {
        subButtons.get(0).text.set("Delay: " + delay);
    }
    
    @Override
    public void onTick() {
        if (new Date().getTime() >= last + delay) {
            last = new Date().getTime();
            a :
            {
    
                if (TTC.world == null)
                    break a;
                
                EntityPlayer[] players = TTC.world.playerEntities.toArray(new EntityPlayer[0]);
                if(!toAttack.hasNext()) {
                    for (int i = 0; i < players.length; i++) {
                        if(
                                players[i].getDistance(TTC.player) < 6 &&
                                !Team.getInstance().names.contains(players[i].getGameProfile().getName()) &&
                                !players[i].getGameProfile().getName().equals(TTC.mc.getSession().getProfile().getName()) &&
                                !AltControl.getInstance().isAlt(players[i])
                        ) {
                            if (!targets.isEmpty()) {
                                if (targets.contains(players[i].getGameProfile().getName())) {
                                    toAttack.add(players[i]);
                                }
                            }
                            else
                                toAttack.add(players[i]);
                        }
                    }
                }
    
                if(toAttack.hasNext())
                    attackNext();
            }
        }
    }
    
    public void attackNext() {
        EntityPlayer player = toAttack.next();
    
        TTC.mc.playerController.attackEntity(TTC.player, player);
        TTC.player.swingArm(EnumHand.MAIN_HAND);
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public void updateConfig() {
        cfg.put("delay", String.valueOf(delay));
    }
    
    @Override
    public void loadConfig() {
        delay = Integer.parseInt(cfg.get("delay"));
        updateButtons();
    }
}
