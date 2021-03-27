package tudbut.mod.client.ttc.mods;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;

import java.util.ArrayList;

import static tudbut.mod.client.ttc.utils.Tesselator.*;

public class PlayerLog extends Module {
    NetworkPlayerInfo[] playersLastTick;
    EntityPlayer[] visiblePlayersLastTick;
    ArrayList<AxisAlignedBB> logouts = new ArrayList<>();
    
    {
        subButtons.add(new GuiTTC.Button("Reset logout spots", text -> {
            logouts.clear();
        }));
    }
    
    @Override
    public boolean defaultEnabled() {
        return true;
    }
    
    @Override
    public void onSubTick() {
        // Is online?
        if (TTC.mc.getConnection() == null)
            return;
        
        if (playersLastTick == null) {
            playersLastTick = TTC.mc.getConnection().getPlayerInfoMap().toArray(new NetworkPlayerInfo[0]);
        }
        EntityPlayer[] visiblePlayersThisTick = TTC.mc.world.playerEntities.toArray(new EntityPlayer[0]);
        NetworkPlayerInfo[] playersThisTick = TTC.mc.getConnection().getPlayerInfoMap().toArray(new NetworkPlayerInfo[0]);
        
        try {
            // Did a player leave?
            if (playersThisTick.length < playersLastTick.length) {
                // What player left?
                for (int i = 0; i < playersLastTick.length; i++) {
                    try {
                        boolean b = true;
                        String name = playersLastTick[i].getGameProfile().getName();
                        for (int j = 0; j < playersThisTick.length; j++) {
                            if (playersThisTick[j].getGameProfile().getName().equals(name))
                                b = false;
                        }
                        if (b) {
                            // This player left, its data is still in the data from last tick
                            ChatUtils.print(name + " left!");
                            for (int j = 0; j < visiblePlayersLastTick.length; j++) {
                                if (visiblePlayersLastTick[j].getGameProfile().getName().equals(name)) {
                                    Vec3d vec = visiblePlayersLastTick[j].getPositionVector();
                                    ChatUtils.print(
                                            "§c§l§c§lThe player §r" +
                                            visiblePlayersLastTick[j].getName() +
                                            "§c§l left at " +
                                            // Round to two decimal places
                                            Math.round(vec.x * 100d) / 100d + " " +
                                            Math.round(vec.y * 100d) / 100d + " " +
                                            Math.round(vec.z * 100d) / 100d + " " +
                                            "!"
                                    );
                                    logouts.add(visiblePlayersLastTick[j].getEntityBoundingBox().offset(0,0,0));
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            // Did a player join?
            if (playersThisTick.length > playersLastTick.length) {
                // What player joined?
                for (int i = 0; i < playersThisTick.length; i++) {
                    try {
                        boolean b = true;
                        String name = playersThisTick[i].getGameProfile().getName();
                        for (int j = 0; j < playersLastTick.length; j++) {
                            if (playersLastTick[j].getGameProfile().getName().equals(name))
                                b = false;
                        }
                        if (b) {
                            // This player joined
                            ChatUtils.print(name + " joined!");
                        }
                    }
                    catch (Exception ignore) { }
                }
            }
        }
        catch (Exception ignore) { }
        
        // Refresh
        playersLastTick = playersThisTick;
        visiblePlayersLastTick = visiblePlayersThisTick;
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    Vec3d pos = new Vec3d(0,0,0);
    
    @SubscribeEvent
    public void onRenderWorld(Event event) {
        
        if(event instanceof RenderWorldLastEvent)
            if(this.enabled && TTC.isIngame()) {
                Entity e = TTC.mc.getRenderViewEntity();
                assert e != null;
                pos = e.getPositionEyes(((RenderWorldLastEvent) event).getPartialTicks()).addVector(0, -e.getEyeHeight(), 0);
                
                for (int i = 0; i < logouts.size(); i++) {
                    drawAroundBox(logouts.get(i), 0x8000ff00);
                }
            }
    }
    
    public void drawAroundBox(AxisAlignedBB box, int color) {
        try {
            
            ready();
            translate(-this.pos.x, -this.pos.y, -this.pos.z);
            color(color);
            depth(false);
            begin(GL11.GL_QUADS);
            
            double entityHalfed = (box.maxX - box.minX) / 2;
            double entityHeight = (box.maxY - box.minY);
            Vec3d pos = new Vec3d(box.maxX - entityHalfed, box.minY, box.maxZ - entityHalfed);
            
            // bottom
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            
            next();
            
            // top
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            
            next();
            
            // z -
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            
            next();
            
            // z +
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            
            next();
            
            // x -
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            
            next();
            
            // y +
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            
            end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
