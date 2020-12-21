package tudbut.mod.client.yac.mods;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.utils.BlockUtils;
import tudbut.mod.client.yac.utils.ChatUtils;
import tudbut.mod.client.yac.utils.Module;
import tudbut.mod.client.yac.utils.ThreadManager;

public class Trap extends Module {
    
    {
        enabled = true;
    }
    
    @Override
    public void onTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
        ChatUtils.print("Finding player");
        for (EntityPlayer player : Yac.mc.world.playerEntities) {
            if (player.getName().equalsIgnoreCase(s)) {
                ThreadManager.run(() -> trap(player));
                return;
            }
        }
        ChatUtils.print("Player not found!");
    }
    
    public void add(BlockPos[] positions, int i, int x, int y, int z) {
        positions[i] = positions[i].add(x,y,z);
    }
    
    public void trap(EntityPlayer player) {
        ChatUtils.print("Trapping " + player.getName() + "...");
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
        
        ChatUtils.print("Constructed positions. Placing blocks...");
        
        for (int i = 0; i < positions.length; i++) {
            try {
                BlockUtils.placeBlock(positions[i], false);
                System.out.println(positions[i]);
                Thread.sleep(50);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        ChatUtils.print("Blocks placed.");
    }
}
