package tudbut.mod.client.ttc.mods;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.InventoryUtils;
import tudbut.mod.client.ttc.utils.Module;

public class AutoTotem extends Module {
    
    static AutoTotem instance;
    
    public static AutoTotem getInstance() {
        return instance;
    }
    
    public AutoTotem() {
        instance = this;
    }
    
    public int min_count = 0;
    public int orig_min_count = 0;
    private boolean isRestockingAfterRespawn = false;
    
    {
        subButtons.add(new GuiTTC.Button("Count: " + orig_min_count, text -> {
            if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                orig_min_count = min_count = orig_min_count - 1;
            else
                orig_min_count = min_count = orig_min_count + 1;
            if(orig_min_count > 12)
                orig_min_count = min_count = 0;
            if(orig_min_count < 0)
                orig_min_count = min_count = 12;
            text.set("Count: " + orig_min_count);
        }));
        subButtons.add(new GuiTTC.Button(0, 0, "Actual count: " + min_count, text -> {
        
        }, null));
    }
    
    public void updateButtons() {
        subButtons.get(0).text.set("Count: " + orig_min_count);
        subButtons.get(1).text.set("Actual count: " + min_count);
    }
    
    @Override
    public void onTick() {
        
        EntityPlayerSP player = TTC.player;
        
        updateTotCount();
    
        if(min_count < 0) {
            min_count = 0;
        }
        
        updateButtons();
        
        if(TTC.mc.currentScreen == null)
            isRestockingAfterRespawn = false;
        
        if((isRestockingAfterRespawn || isRestockingAfterRespawn()) && TTC.mc.currentScreen != null) {
            System.out.println(isRestockingAfterRespawn);
            return;
        }
        
        ItemStack stack = player.getHeldItemOffhand();
        if(stack.getCount() <= min_count) {
    
            Integer slot = InventoryUtils.getSlotWithItem(
                    player.inventoryContainer,
                    Items.TOTEM_OF_UNDYING,
                    new int[] {InventoryUtils.OFFHAND_SLOT},
                    min_count + 1,
                    64
            );
            if(slot == null)
                return;
            InventoryUtils.inventorySwap(slot, InventoryUtils.OFFHAND_SLOT);
        }
    }
    
    public boolean isRestockingAfterRespawn() {
        EntityPlayerSP player = TTC.player;
        
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
                orig_min_count = min_count = Integer.parseInt(s.substring("count ".length()));
                ChatUtils.print("Set!");
            } catch (Exception e) {
                ChatUtils.print("ERROR: NaN");
            }
        updateButtons();
    }
    
    @Override
    public void onEnable() {
        TTC.modules[2].enabled = false;
    }
    
    @Override
    public void loadConfig() {
        orig_min_count = min_count = Integer.parseInt(cfg.get("count"));
        updateButtons();
    }
    
    @Override
    public void updateConfig() {
        cfg.put("count", String.valueOf(orig_min_count));
    }
    
    public void updateTotCount() {
        EntityPlayerSP player = TTC.player;
        
        
        if(
                InventoryUtils.getSlotWithItem(
                        player.inventoryContainer,
                        Items.TOTEM_OF_UNDYING,
                        new int[] {InventoryUtils.OFFHAND_SLOT},
                        orig_min_count + 1,
                        64
                ) != null
        ) {
            min_count = orig_min_count;
        }
        Integer i = InventoryUtils.getSlotWithItem(
                player.inventoryContainer,
                Items.TOTEM_OF_UNDYING,
                new int[] {InventoryUtils.OFFHAND_SLOT},
                min_count + 1,
                64
        );
        while(i == null) {
            if(min_count < 0) {
                min_count = 0;
                break;
            }
            min_count--;
            i = InventoryUtils.getSlotWithItem(
                    player.inventoryContainer,
                    Items.TOTEM_OF_UNDYING,
                    new int[] {InventoryUtils.OFFHAND_SLOT},
                    min_count + 1,
                    64
            );
        }
        if(min_count < 0) {
            min_count = 0;
        }
    }
}
