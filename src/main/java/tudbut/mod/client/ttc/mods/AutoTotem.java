package tudbut.mod.client.ttc.mods;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.*;

import java.util.ArrayList;

public class AutoTotem extends Module {
    
    static AutoTotem instance;
    // Actual count, set by AI
    public int min_count = 0;
    // Count, set by user
    public int orig_min_count = 0;
    // If the user seems to be restocking after respawning, if this is the case,
    // don't switch until any inventories are closed
    public boolean isRestockingAfterRespawn = false;
    // If totems should be stacked automatically
    public boolean autoStack = false;
    
    {
        subButtons.add(new GuiTTC.Button("Count: " + orig_min_count, text -> {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                orig_min_count = min_count = orig_min_count - 1;
            else
                orig_min_count = min_count = orig_min_count + 1;
            if (orig_min_count > 12)
                orig_min_count = min_count = 0;
            if (orig_min_count < 0)
                orig_min_count = min_count = 12;
            text.set("Count: " + orig_min_count);
        }));
        subButtons.add(new GuiTTC.Button("AutoStack: " + autoStack, text -> {
            autoStack = !autoStack;
            text.set("AutoStack: " + autoStack);
        }));
        subButtons.add(new GuiTTC.Button("AutoStack now", text -> {
            autoStack();
        }));
        subButtons.add(new GuiTTC.Button("Actual count: " + min_count, text -> {
        
        }));
    }
    
    public AutoTotem() {
        instance = this;
    }
    
    public static AutoTotem getInstance() {
        return instance;
    }
    
    public void updateButtons() {
        subButtons.get(0).text.set("Count: " + orig_min_count);
        subButtons.get(1).text.set("AutoStack: " + autoStack);
        subButtons.get(2).text.set("Actual count: " + min_count);
    }
    
    // Run checks and AI
    @Override
    public void onTick() {
        EntityPlayerSP player = TTC.player;
        
        if ((isRestockingAfterRespawn() || isRestockingAfterRespawn)) {
            // Don't switch yet
            return;
        }
        
        // Run AI
        updateTotCount();
        updateButtons();
        if(autoStack)
            autoStack();
        
        ItemStack stack = player.getHeldItemOffhand();
        if (stack.getCount() <= min_count) {
            // Switch!
            
            Integer slot = InventoryUtils.getSlotWithItem(
                    player.inventoryContainer,
                    Items.TOTEM_OF_UNDYING,
                    new int[]{InventoryUtils.OFFHAND_SLOT},
                    min_count + 1,
                    64
            );
            if (slot == null)
                return; // Oh no!! No totems left!
            
            // Switch a new totem stack to the offhand
            InventoryUtils.inventorySwap(slot, InventoryUtils.OFFHAND_SLOT);
        }
    }
    
    // Tests if the player is likely to be restocking after having a empty inventory,
    // does NOT check for a respawn, but very likely will only be true after a respawn!
    public boolean isRestockingAfterRespawn() {
        EntityPlayerSP player = TTC.player;
        
        // Set false if the container was closed, this will make it start switching again
        GuiScreen screen = TTC.mc.currentScreen;
        if (
                !(
                        screen instanceof GuiContainer && !(
                                screen instanceof GuiInventory ||
                                screen instanceof GuiContainerCreative
                        )
                )
        ) {
            isRestockingAfterRespawn = false;
            return false;
        }
        
        // Any slot with totems
        Integer slot0 = InventoryUtils.getSlotWithItem(
                player.inventoryContainer,
                Items.TOTEM_OF_UNDYING,
                new int[]{},
                1,
                64
        );
        // No totems, return true
        if (slot0 == null) {
            isRestockingAfterRespawn = true;
            return true;
        }
        // Any slot with totems excluding slot0
        Integer slot1 = InventoryUtils.getSlotWithItem(
                player.inventoryContainer,
                Items.TOTEM_OF_UNDYING,
                new int[]{slot0},
                1,
                64
        );
        // Only one stack of totems, return true
        if (slot1 == null) {
            isRestockingAfterRespawn = true;
            return true;
        }
        
        // There is two or more stacks, return false, seems normal
        return false;
    }
    
    // AI, finds out the amount to switch at, looks for lowest amount of totems in inventory
    public void updateTotCount() {
        EntityPlayerSP player = TTC.player;
        
        // Is the player-set count usable?
        if (
                InventoryUtils.getSlotWithItem(
                        player.inventoryContainer,
                        Items.TOTEM_OF_UNDYING,
                        new int[]{InventoryUtils.OFFHAND_SLOT},
                        orig_min_count + 1,
                        64
                ) != null
        ) {
            min_count = orig_min_count;
            return;
        }
        
        // Look for a stack of the AI-set count
        Integer i = InventoryUtils.getSlotWithItem(
                player.inventoryContainer,
                Items.TOTEM_OF_UNDYING,
                new int[]{InventoryUtils.OFFHAND_SLOT},
                min_count + 1,
                64
        );
        // If it doesnt exist, step down the count until a stack exist or the count hits 0
        while (i == null) {
            // Step down
            min_count--;
            // Check
            i = InventoryUtils.getSlotWithItem(
                    player.inventoryContainer,
                    Items.TOTEM_OF_UNDYING,
                    new int[]{InventoryUtils.OFFHAND_SLOT},
                    min_count + 1,
                    64
            );
            
            if (min_count < 0) {
                // No stacks left
                min_count = 0;
                return; // Sorry
            }
        }
        
        // Found!
    }
    
    public void autoStack() {
        
        if(min_count == 0)
            return;
        
        EntityPlayerSP player = TTC.player;
        ArrayList<Integer> slotsWithTotems = new ArrayList<>();
        // The minimal amount that is required to stack totems
        int min = 2;
        // Only restack when totems are likely not a normal stack
        int max = 24;
        // TMP variable
        Integer slot;
    
        do {
    
            ArrayList<Integer> dropped = new ArrayList<>();
            // Drop unusable stacks
            if (slotsWithTotems.size() != 0) {
                slot = InventoryUtils.getSlotWithItem(
                        player.inventoryContainer,
                        Items.TOTEM_OF_UNDYING,
                        new int[0],
                        0,
                        min - 1
                );
        
        
                while (slot != null) {
            
                    // Drop stack contents of the slot
                    InventoryUtils.drop(slot);
                    System.out.println("Dropped item in " + slot);
                    dropped.add(slot);
            
                    // Next
                    slot = InventoryUtils.getSlotWithItem(
                            player.inventoryContainer,
                            Items.TOTEM_OF_UNDYING,
                            Utils.objectArrayToNativeArray(dropped.toArray(new Integer[0])),
                            0,
                            min - 1
                    );
                }
            }
    
            if(orig_min_count == min_count)
                return;
            
            
            slotsWithTotems.clear();
            // Get slots with totems
            slot = InventoryUtils.getSlotWithItem(
                    player.inventoryContainer,
                    Items.TOTEM_OF_UNDYING,
                    new int[0],
                    min,
                    max
            );
            while (slot != null && slotsWithTotems.size() < 2) {
                slotsWithTotems.add(slot);
                slot = InventoryUtils.getSlotWithItem(
                        player.inventoryContainer,
                        Items.TOTEM_OF_UNDYING,
                        Utils.objectArrayToNativeArray(slotsWithTotems.toArray(new Integer[0])),
                        min,
                        max
                );
            }
    
            // The slots found
            int[] slots = Utils.objectArrayToNativeArray(slotsWithTotems.toArray(new Integer[0]));
    
            // Combine totems
            if (slots.length >= 2) {
                // Get empty slot
                slot = InventoryUtils.getSlotWithItem(player.inventoryContainer, Items.AIR, 0);
                if (slot == null) {
                    InventoryUtils.drop(slots[0]);
                    continue;
                }
                System.out.println("Combining " + slots[0] + " and " + slots[1] + " to " + slot);
                // Pick first stack
                InventoryUtils.clickSlot(slots[0], ClickType.PICKUP, 0);
                // Pick second stack
                InventoryUtils.clickSlot(slots[1], ClickType.PICKUP, 0);
                // Put result in empty slot
                InventoryUtils.clickSlot(slot, ClickType.PICKUP, 0);
                // Drop junk
                InventoryUtils.drop(slots[1]);
            }
        } while (slotsWithTotems.size() >= 2);
    }
    
    @Override
    public void onChat(String s, String[] args) {
        if (s.startsWith("count "))
            try {
                orig_min_count = min_count = Integer.parseInt(s.substring("count ".length()));
                ChatUtils.print("Set!");
            }
            catch (Exception e) {
                ChatUtils.print("ERROR: NaN");
            }
        updateButtons();
    }
    
    @Override
    public void loadConfig() {
        orig_min_count = min_count = Integer.parseInt(cfg.get("count"));
        autoStack = Boolean.parseBoolean(cfg.get("autoStack"));
        updateButtons();
    }
    
    @Override
    public void updateConfig() {
        cfg.put("count", String.valueOf(orig_min_count));
        cfg.put("autoStack", String.valueOf(autoStack));
    }
}
