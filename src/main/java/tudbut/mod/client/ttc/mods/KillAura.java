package tudbut.mod.client.ttc.mods;

import org.lwjgl.input.Keyboard;
import de.tudbut.type.Vector2d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.events.FMLEventHandler;
import tudbut.mod.client.ttc.gui.GuiPlayerSelect;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.*;
import tudbut.obj.Save;
import tudbut.tools.Lock;
import tudbut.tools.Queue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class KillAura extends Module {
    @Save
    int delay = 300;
    @Save
    int randomDelay = 0;
    @Save
    int attack = 0;
    @Save
    boolean threadMode = false;
    @Save
    boolean swing = true;
    @Save
    boolean superAttack = false;
    @Save
    boolean batch = false;
    boolean cBatch = false;
    @Save
    boolean switchItem = false;
    @Save
    int iterations = 1;
    @Save
    int iterationDelay = 0;
    Lock switchTimer = new Lock();
    
    Queue<EntityLivingBase> toAttack = new Queue<>();
    ArrayList<String> targets = new ArrayList<>();
    Lock timer = new Lock();
    
    {
        customKeyBinds.put("select", new KeyBind(null, this::triggerSelect));
    }
    
    @Override
    public void init() {
        PlayerSelector.types.add(new PlayerSelector.Type(player -> {
            targets.clear();
            targets.add(player.getGameProfile().getName());
        }, "Set KillAura target"));
        updateBinds();
    }
    
    @SuppressWarnings("unused")
    public void triggerSelect() {
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
    }
    
    static KillAura instance;
    {
        instance = this;
    }
    public static KillAura getInstance() {
        return instance;
    }
    
    public void updateBinds() {
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
        subButtons.add(Setting.createInt(0, 500, 10, "RandomDelay: $val", this, "randomDelay"));
        subButtons.add(Setting.createBoolean("Swing: $val", this, "swing"));
        subButtons.add(Setting.createBoolean("Batches: $val", this, "batch"));
        subButtons.add(Setting.createBoolean("Switch: $val", this, "switchItem"));
        subButtons.add(Setting.createInt(1, 10, 1, "Iterations: $vali/a", this, "iterations"));
        subButtons.add(Setting.createInt(0, 100, 10, "IterationDelay: $val", this, "iterationDelay"));
    }
    
    @Override
    public void onTick() {
        
        {
            if (!toAttack.hasNext()) {
                EntityPlayer[] players = TTC.world.playerEntities.toArray(new EntityPlayer[0]);
                for (int i = 0 ; i < players.length ; i++) {
                    if (
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
            if (!toAttack.hasNext() && attack == 0) {
                EntityLivingBase[] entities = Utils.getEntities(EntityLivingBase.class, EntityLivingBase::isEntityAlive);
                for (int i = 0 ; i < entities.length ; i++) {
                    if (
                            entities[i].getDistance(TTC.player) < 6 &&
                            !(entities[i] instanceof EntityPlayer)
                    ) {
                        toAttack.add(entities[i]);
                    }
                }
            }
        }
        
        if(!switchTimer.isLocked()) {
            switchTimer.lock();
            
            if(switchItem && toAttack.hasNext()) {
                InventoryUtils.swap(36, 1);
            }
        }
        if (!timer.isLocked()) {
            int e = extraDelay();
            switchTimer.lock((delay + e / 3));
            timer.lock(delay + e);
            //noinspection AssignmentUsedAsCondition
            if(cBatch = !cBatch && batch) {
                timer.lock(((delay + e) * 2));
                switchTimer.lock(((delay + e) / 3 * 2));
            }
            
            if(toAttack.hasNext())
                attackNext();
        }
    }
    
    private int extraDelay() {
        return (int) (randomDelay * Math.random());
    }
    
    public void attackNext() {
        EntityLivingBase entity = toAttack.next();
        
        if(!superAttack || entity.hurtTime <= 0) {
            for (int i = 0 ; i < iterations ; i++) {
                BlockUtils.lookAt(entity.getPositionVector().addVector(0, (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) / 2d, 0));
                TTC.mc.playerController.attackEntity(TTC.player, entity);
                if (swing)
                    TTC.player.swingArm(EnumHand.MAIN_HAND);
                
                if(iterations > 1) {
                    try {
                        Thread.sleep(iterationDelay);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public int danger() {
        return 3;
    }
    
    @Override
    public void updateConfig() {
        cfg.put("delay", delay + "");
        cfg.put("randomDelay", randomDelay + "");
        cfg.put("attack", attack + "");
        cfg.put("swing", swing + "");
        cfg.put("superAttack", superAttack + "");
        cfg.put("batch", batch + "");
        cfg.put("switchItem", switchItem + "");
        cfg.put("iterations", iterations + "");
        cfg.put("iterationDelay", iterationDelay + "");
    }
    
    @Override
    public void loadConfig() {
        delay = Integer.parseInt(cfg.get("delay"));
        randomDelay = Integer.parseInt(cfg.get("randomDelay"));
        attack = Integer.parseInt(cfg.get("attack"));
        iterations = Integer.parseInt(cfg.get("iterations"));
        iterationDelay = Integer.parseInt(cfg.get("iterationDelay"));
        swing = Boolean.parseBoolean(cfg.get("swing"));
        superAttack = Boolean.parseBoolean(cfg.get("superAttack"));
        batch = Boolean.parseBoolean(cfg.get("batch"));
        switchItem = Boolean.parseBoolean(cfg.get("switchItem"));
        updateBinds();
    }
}
