package tudbut.mod.client.ttc.mods;

import de.tudbut.type.Vector2d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiPlayerSelect;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.BlockUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.Setting;
import tudbut.mod.client.ttc.utils.Utils;
import tudbut.obj.Vector2i;
import tudbut.tools.Queue;

import java.util.ArrayList;
import java.util.Date;

public class KillAura extends Module {
    int delay = 200;
    long last = 0;
    int attack = 0;
    boolean rotate = true;
    Queue<Entity> toAttack = new Queue<>();
    ArrayList<String> targets = new ArrayList<>();
    
    {
        customKeyBinds.put("select", new KeyBind(null, () -> {
            targets.clear();
            TTC.mc.displayGuiScreen(
                    new GuiPlayerSelect(
                            TTC.world.playerEntities.stream().filter(
                                    player -> !player.getName().equals(TTC.player.getName())
                            ).toArray(EntityPlayer[]::new),
                            player -> {
                                targets.remove(player.getName());
                                targets.add(player.getName());
                                
                                return false;
                            }
                    )
            );
        }));
    }
    
    @Override
    public void init() {
        PlayerSelector.types.add(new PlayerSelector.Type(player -> {
            targets.clear();
            targets.add(player.getGameProfile().getName());
        }, "Set KillAura target"));
    }
    
    static KillAura instance;
    {
        instance = this;
    }
    public static KillAura getInstance() {
        return instance;
    }
    
    public void updateButtons() {
        subButtons.clear();
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
        subButtons.add(new GuiTTC.Button("Attack " + (attack == 0 ? "all" : (attack == 1 ? "players" : "targets")), text -> {
            attack++;
            if(attack > 2)
                attack = 0;
        
            text.set("Attack " + (attack == 0 ? "all" : (attack == 1 ? "players" : "targets")));
        }));
        subButtons.add(Setting.createBoolean("Rotate: $val", this, "rotate"));
    }
    
    @Override
    public void onTick() {
        if (new Date().getTime() >= last + delay) {
            last = new Date().getTime();
            a :
            {
    
                if (TTC.world == null)
                    break a;
                
                if(!toAttack.hasNext()) {
                    EntityPlayer[] players = TTC.world.playerEntities.toArray(new EntityPlayer[0]);
                    for (int i = 0; i < players.length; i++) {
                        if(
                                players[i].getDistance(TTC.player) < 6 &&
                                !Team.getInstance().names.contains(players[i].getGameProfile().getName()) &&
                                !Friend.getInstance().names.contains(players[i].getGameProfile().getName()) &&
                                !players[i].getGameProfile().getName().equals(TTC.mc.getSession().getProfile().getName()) &&
                                !AltControl.getInstance().isAlt(players[i])
                        ) {
                            if (!targets.isEmpty() || attack == 2) {
                                if (targets.contains(players[i].getGameProfile().getName())) {
                                    toAttack.add(players[i]);
                                }
                            }
                            else
                                toAttack.add(players[i]);
                        }
                    }
                }
                if(!toAttack.hasNext() && attack == 0) {
                    Entity[] entities = Utils.getEntities(EntityLivingBase.class, EntityLivingBase::isEntityAlive);
                    for (int i = 0; i < entities.length; i++) {
                        if(
                                entities[i].getDistance(TTC.player) < 6 &&
                                !(entities[i] instanceof EntityPlayer)
                        ) {
                            toAttack.add(entities[i]);
                        }
                    }
                }
    
                if(toAttack.hasNext())
                    attackNext();
            }
        }
    }
    
    public void attackNext() {
        Entity entity = toAttack.next();
    
        Vector2d rot = new Vector2d(TTC.player.rotationYaw, TTC.player.rotationPitch);
        if(rotate)
            BlockUtils.lookAt(entity.getPositionVector().addVector(0, (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) / 2, 0));
        TTC.mc.playerController.attackEntity(TTC.player, entity);
        TTC.player.swingArm(EnumHand.MAIN_HAND);
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public void updateConfig() {
        cfg.put("delay", String.valueOf(delay));
        cfg.put("attack", String.valueOf(attack));
        cfg.put("rotate", String.valueOf(rotate));
    }
    
    @Override
    public void loadConfig() {
        delay = Integer.parseInt(cfg.get("delay"));
        attack = Integer.parseInt(cfg.get("attack"));
        rotate = Boolean.parseBoolean(cfg.get("rotate"));
        updateButtons();
    }
    
    @Override
    public int danger() {
        return 3;
    }
}
