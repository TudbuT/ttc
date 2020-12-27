package tudbut.mod.client.ttc.mods;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;

public class PlayerLog extends Module {
    NetworkPlayerInfo[] playersLastTick;
    EntityPlayer[] visiblePlayersLastTick;
    
    @Override
    public boolean defaultEnabled() {
        return true;
    }
    
    @Override
    public void onTick() {
        if(TTC.mc.getConnection() == null)
            return;
        if(playersLastTick == null) {
            playersLastTick = TTC.mc.getConnection().getPlayerInfoMap().toArray(new NetworkPlayerInfo[0]);
        }
        EntityPlayer[] visiblePlayersThisTick = TTC.mc.world.playerEntities.toArray(new EntityPlayer[0]);
        NetworkPlayerInfo[] playersThisTick = TTC.mc.getConnection().getPlayerInfoMap().toArray(new NetworkPlayerInfo[0]);
        
        try {
    
            if (playersThisTick.length < playersLastTick.length) {
                for (int i = 0; i < playersLastTick.length; i++) {
                    try {
                        boolean b = true;
                        String name = playersLastTick[i].getGameProfile().getName();
                        for (int j = 0; j < playersThisTick.length; j++) {
                            if(playersThisTick[j].getGameProfile().getName().equals(name))
                                b = false;
                        }
                        if(b) {
                            ChatUtils.print(name + " left!");
                            for (int j = 0; j < visiblePlayersLastTick.length; j++) {
                                if (visiblePlayersLastTick[j].getGameProfile().getName().equals(name)) {
                                    Vec3d vec = visiblePlayersLastTick[j].getPositionVector();
                                    ChatUtils.print(
                                            "§c§l§c§lThe player §r" +
                                            visiblePlayersLastTick[j].getName() +
                                            "§c§l left at " +
                                            ((int) (vec.x * 100)) / 100 + " " +
                                            ((int) (vec.y * 100)) / 100 + " " +
                                            ((int) (vec.z * 100)) / 100 + " " +
                                            "!"
                                    );
                                }
                            }
                        }
                    } catch (Exception ignore) { }
                }
            }
    
            if (playersThisTick.length > playersLastTick.length) {
                for (int i = 0; i < playersThisTick.length; i++) {
                    try {
                        boolean b = true;
                        String name = playersThisTick[i].getGameProfile().getName();
                        for (int j = 0; j < playersLastTick.length; j++) {
                            if(playersLastTick[j].getGameProfile().getName().equals(name))
                                b = false;
                        }
                        if(b) {
                            ChatUtils.print(name + " joined!");
                        }
                    } catch (Exception ignore) { }
                }
            }
        } catch (Exception ignore) { }
        
        playersLastTick = playersThisTick;
        visiblePlayersLastTick = visiblePlayersThisTick;
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
}
