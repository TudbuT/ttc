package tudbut.mod.client.ttc.mods;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.BlockUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.ThreadManager;

public class Trap extends Module {
    
    private boolean ticked = false;
    
    @Override
    public void onEnable() {
        ThreadManager.run(() -> {
            while (enabled) {
                try {
                    if (TTC.mc.world != null) {
                        for (EntityPlayer player : TTC.mc.world.playerEntities) {
                            if (!Team.getInstance().names.contains(player.getName())) {
                                trap(player);
                            }
                        }
                    }
                } catch (Exception e) {
            
                }
            }
        });
    }
    
    @Override
    public void onTick() {
        ticked = true;
    }
    
    public void waitForTick() throws InterruptedException {
        while (!ticked);
        Thread.sleep(1);
        ticked = false;
    }
    
    @Override
    public void onChat(String s, String[] args) {
    }
    
    public void add(BlockPos[] positions, int i, int x, int y, int z) {
        positions[i] = positions[i].add(x,y,z);
    }
    
    public void trap(EntityPlayer player) {
        BlockPos[] positions = new BlockPos[18];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = new BlockPos(player.getPositionVector());
        }
        
        add(positions,  0, +1, -1, +0);
        add(positions,  1, +0, -1, +0);
        add(positions,  2, -1, -1, +0);
        add(positions,  3, +0, -1, +1);
        add(positions,  4, +0, -1, -1);
        
        add(positions,  5, +1, +0, +0);
        add(positions,  6, -1, +0, +0);
        add(positions,  7, +0, +0, +1);
        add(positions,  8, +0, +0, -1);
        
        add(positions,  9, +1, +1, +0);
        add(positions, 10, -1, +1, +0);
        add(positions, 11, +0, +1, +1);
        add(positions, 12, +0, +1, -1);
        
        add(positions, 13, +1, +2, +0);
        add(positions, 14, +0, +2, +0);
        add(positions, 15, -1, +2, +0);
        add(positions, 16, +0, +2, +1);
        add(positions, 17, +0, +2, -1);
        
        for (int i = 0; i < positions.length; i++) {
            try {
                waitForTick();
                BlockUtils.placeBlock(positions[i], false);
                System.out.println(positions[i]);
                Thread.sleep(50);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
