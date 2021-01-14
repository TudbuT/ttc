package tudbut.mod.client.ttc.mods;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.events.ParticleLoop;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;

import java.util.Date;

public class PlayerLog extends Module {
    NetworkPlayerInfo[] playersLastTick;
    EntityPlayer[] visiblePlayersLastTick;
    
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
                                            Math.round(vec.x * 100) / 100 + " " +
                                            Math.round(vec.y * 100) / 100 + " " +
                                            Math.round(vec.z * 100) / 100 + " " +
                                            "!"
                                    );
                                    long time = new Date().getTime() + 2 * 60 * 1000;
                                    final int[] k = {-1};
                                    // Render hitbox as particles using the ParticleLoop
                                    ParticleLoop.register(new ParticleLoop.Particle() {
                                        @Override
                                        public boolean summon() {
                                            return enabled && new Date().getTime() < time;
                                        }
                                        
                                        @Override
                                        public EnumParticleTypes getType() {
                                            return EnumParticleTypes.FLAME;
                                        }
                                        
                                        // Efficiency is important!
                                        @Override
                                        public Vec3d getPosition() {
                                            k[0]++;
                                            if (k[0] > 7)
                                                k[0] = 0;
                                            switch (k[0]) {
                                                case 0:
                                                    return vec.addVector(-0.3, 0.0, -0.3);
                                                case 1:
                                                    return vec.addVector(+0.3, 0.0, -0.3);
                                                case 2:
                                                    return vec.addVector(-0.3, 0.0, +0.3);
                                                case 3:
                                                    return vec.addVector(+0.3, 0.0, +0.3);
                                                case 4:
                                                    return vec.addVector(-0.3, 1.8, -0.3);
                                                case 5:
                                                    return vec.addVector(+0.3, 1.8, -0.3);
                                                case 6:
                                                    return vec.addVector(-0.3, 1.8, +0.3);
                                                case 7:
                                                    return vec.addVector(+0.3, 1.8, +0.3);
                                            }
                                            return vec;
                                        }
                                    });
                                }
                            }
                        }
                    }
                    catch (Exception ignore) { }
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
}
