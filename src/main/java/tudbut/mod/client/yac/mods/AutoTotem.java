package tudbut.mod.client.yac.mods;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.gui.GuiYAC;
import tudbut.mod.client.yac.utils.ChatUtils;
import tudbut.mod.client.yac.utils.InventoryUtils;
import tudbut.mod.client.yac.utils.Module;

import java.util.Map;

public class AutoTotem extends Module {
    
    public int min_count = 0;
    private boolean isRestockingAfterRespawn = false;
    
    {
        subButtons.add(new GuiYAC.Button(0, 0, "Count: " + min_count, text -> {
            min_count = min_count + 1;
            if(min_count > 12)
                min_count = 0;
            text.set("Count: " + min_count);
        }, null));
    }
    
    public void updateButtons() {
        subButtons.get(0).text.set("Count: " + min_count);
    }
    
    @Override
    public void onTick() {
        EntityPlayerSP player = Yac.player;
    
        updateButtons();
        
        if(Yac.mc.currentScreen == null)
            isRestockingAfterRespawn = false;
    
        if(isRestockingAfterRespawn() || isRestockingAfterRespawn)
            return;
    
        
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
    
    public boolean isRestockingAfterRespawn() {
        EntityPlayerSP player = Yac.player;
        
        Integer slot0 = InventoryUtils.getSlotWithItem(
                player.inventoryContainer,
                Items.TOTEM_OF_UNDYING,
                new int[] {},
                1,
                64
        );
        if(slot0 == null) {
            isRestockingAfterRespawn = true;
            return true;
        }
        Integer slot1 = InventoryUtils.getSlotWithItem(
                player.inventoryContainer,
                Items.TOTEM_OF_UNDYING,
                new int[] {slot0},
                1,
                64
        );
        if(slot1 == null) {
            isRestockingAfterRespawn = true;
            return true;
        }
        return false;
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
