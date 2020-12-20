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
                ChatUtils.print("Trapping " + s + "...");
                ThreadManager.run(() -> trap(player));
                return;
            }
        }
        ChatUtils.print("Player not found!");
    }
    
    public void trap(EntityPlayer player) {
        BlockPos[] positions = new BlockPos[18];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = player.getPosition().add(0, 0, 0);
        }
        
        positions[0].add(+1, -1, +0);
        positions[1].add(+0, -1, +0);
        positions[2].add(-1, -1, +0);
        positions[3].add(+0, -1, +1);
        positions[4].add(+0, -1, -1);
        
        positions[5].add(+1, +0, +0);
        positions[6].add(-1, +0, +0);
        positions[7].add(+0, +0, +1);
        positions[8].add(+0, +0, -1);
        
        positions[9].add(+1, +1, +0);
        positions[10].add(-1, +1, +0);
        positions[11].add(+0, +1, +1);
        positions[12].add(+0, +1, -1);
        
        positions[13].add(+1, +2, +0);
        positions[14].add(+0, +2, +0);
        positions[15].add(-1, +2, +0);
        positions[16].add(+0, +2, +1);
        positions[17].add(+0, +2, -1);
        
        ChatUtils.print("Constructed positions. Placing blocks...");
        
        for (int i = 0; i < positions.length; i++) {
            try {
                int fI = i;
                ThreadManager.run(() -> BlockUtils.placeBlock(positions[fI], false));
                Thread.sleep(1000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        ChatUtils.print("Blocks placed.");
    }
    
    public void trapOld(EntityPlayer player) {
        BlockPos[] positions = new BlockPos[
                3 * 3 +
                3 * 3 +
                3 * 3 +
                3 * 3 +
                3 * 3 +
                3 * 3 +
                8
                ];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = player.getPosition().add(0, 0, 0);
        }
        
        positions[0].add(+1, -1, +1); // bottom
        positions[1].add(+1, -1, +0);
        positions[2].add(+1, -1, -1);
        
        positions[3].add(+0, -1, +1);
        positions[4].add(+0, -1, +0);
        positions[5].add(+0, -1, -1);
        
        positions[6].add(-1, -1, +1);
        positions[7].add(-1, -1, +0);
        positions[8].add(-1, -1, -1);
        
        
        positions[9].add(+2, -1, +0);
        positions[10].add(-2, -1, +0);
        positions[11].add(+0, -1, +2);
        positions[12].add(+0, -1, -2);
        
        
        positions[13].add(+2, +0, +1); // right
        positions[14].add(+2, +0, +0);
        positions[15].add(+2, +0, -1);
        
        positions[16].add(+2, +1, +1);
        positions[17].add(+2, +1, +0);
        positions[18].add(+2, +1, -1);
        
        positions[19].add(+2, +2, +1);
        positions[20].add(+2, +2, +0);
        positions[21].add(+2, +2, -1);
        
        
        positions[22].add(+2, +3, +0);
        
        
        positions[23].add(+1, +3, +1); // top
        positions[24].add(+1, +3, +0);
        positions[25].add(+1, +3, -1);
        
        positions[26].add(+0, +3, +1);
        positions[26].add(+0, +3, +0);
        positions[27].add(+0, +3, -1);
        
        positions[29].add(-1, +3, +1);
        positions[30].add(-1, +3, +0);
        positions[31].add(-1, +3, -1);
        
        
        positions[32].add(-2, +3, +0);
        positions[33].add(+0, +3, +2);
        positions[34].add(+0, +3, -2);
        
        
        positions[45].add(-2, +0, +1); // left
        positions[36].add(-2, +0, +0);
        positions[37].add(-2, +0, -1);
        
        positions[38].add(-2, +1, +1);
        positions[39].add(-2, +1, +0);
        positions[40].add(-2, +1, -1);
        
        positions[41].add(-2, +2, +1);
        positions[42].add(-2, +2, +0);
        positions[43].add(-2, +2, -1);
        
        
        positions[44].add(+0, +1, -2); // front
        positions[45].add(+0, +0, -2);
        positions[46].add(+0, -1, -2);
        
        positions[47].add(+1, +1, -2);
        positions[48].add(+1, +0, -2);
        positions[49].add(+1, -1, -2);
        
        positions[50].add(+2, +1, -2);
        positions[51].add(+2, +0, -2);
        positions[52].add(+2, -1, -2);
        
        
        positions[53].add(+0, +1, -2); // back
        positions[54].add(+0, +0, -2);
        positions[55].add(+0, -1, -2);
        
        positions[56].add(+1, +1, -2);
        positions[57].add(+1, +0, -2);
        positions[58].add(+1, -1, -2);
        
        positions[59].add(+2, +1, -2);
        positions[60].add(+2, +0, -2);
        positions[61].add(+2, -1, -2);
        
        ChatUtils.print("Constructed positions. Placing blocks...");
        
        for (int i = 0; i < positions.length; i++) {
            BlockUtils.placeBlockScaffold(positions[i]);
            try {
                Thread.sleep(200);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        ChatUtils.print("Blocks placed.");
    }
}
