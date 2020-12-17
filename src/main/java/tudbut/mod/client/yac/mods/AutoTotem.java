package tudbut.mod.client.yac.mods;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.utils.ChatUtils;
import tudbut.mod.client.yac.utils.InventoryUtils;
import tudbut.mod.client.yac.utils.Module;

import java.util.Map;

public class AutoTotem extends Module {
    
    public int min_count = 0;
    
    @Override
    public void onTick() {
        EntityPlayerSP player = Yac.player;
        
        ItemStack stack = player.getHeldItemOffhand();
        if(stack.getCount() <= min_count) {
            Integer i = InventoryUtils.getSlotWithItem(
                    player.inventoryContainer,
                    Items.TOTEM_OF_UNDYING,
                    new int[] {InventoryUtils.OFFHAND_SLOT},
                    min_count + 1,
                    64
            );
            if(i == null)
                return;
            InventoryUtils.inventorySwap(i, InventoryUtils.OFFHAND_SLOT);
        }
    }
    
    @Override
    public void onChat(String s, String[] args) {
        if(s.startsWith("count "))
            try {
                min_count = Integer.parseInt(s.substring("count ".length()));
                ChatUtils.print("Set!");
            } catch (Exception e) {
                ChatUtils.print("ERROR: NaN");
            }
    }
    
    @Override
    public void loadConfig() {
        min_count = Integer.parseInt(cfg.get("count"));
    }
    
    @Override
    public String saveConfig() {
        cfg.put("count", String.valueOf(min_count));
        
        return super.saveConfig();
    }
}
