package tudbut.mod.client.ttc.utils;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tudbut.mod.client.ttc.TTC;

public class InventoryUtils {
    
    public static final int OFFHAND_SLOT = 45;
    private static boolean swapping = false;
    
    public static Integer getSlotWithItem(Container inv, Item item, int amount) {
        return getSlotWithItem(inv, item, Utils.range(0, 8), amount, amount);
    }
    
    public static Integer getSlotWithItem(Container inv, Item item, int[] not, int amountMin, int amountMax) {
        for (int i = 1; i < inv.getInventory().size(); i++) {
            a:
            {
                for (int j = 0; j < not.length; j++) {
                    if (i == not[j])
                        break a;
                }
                
                ItemStack stack = inv.getSlot(i).getStack();
                if (stack.getItem().equals(item) && stack.getCount() >= amountMin && stack.getCount() <= amountMax)
                    return i;
            }
        }
        return null;
    }
    
    // Drop contents of a slot
    public static void drop(int slot) {
        clickSlot(slot, ClickType.THROW, 1);
    }
    
    // Virtually clicks a slot
    public static void clickSlot(int slot, ClickType type, int key) {
        TTC.mc.playerController.windowClick(TTC.mc.player.inventoryContainer.windowId, slot, key, type, TTC.mc.player);
    }
    
    // This only swaps between a slot and a hotbar slot!
    public static void swap(int slot, int hotbarSlot) {
        clickSlot(slot, ClickType.SWAP, hotbarSlot);
    }
    
    // Swap two items in inventory
    public static void inventorySwap(int slot0, int slot1) {
        // Swapping is fast, but not always fast enough! It may not be run in separate threads!
        while (swapping) ; // Sorry for the delay!
        
        // Make other threads wait
        swapping = true;
        
        // "slot1" must not be set to 8, it will not be able to switch!
        if (slot1 == 8) {
            // Exchange values of slot0 and slot1
            int i = slot0;
            slot1 = slot0;
            slot0 = i;
        }
        
        // Check for a GUIScreen that would block switching
        GuiScreen screen = TTC.mc.currentScreen;
        boolean doResetScreen = false;
        if (
                screen instanceof GuiContainer && !(
                        screen instanceof GuiInventory ||
                        screen instanceof GuiContainerCreative
                )
        ) {
            // If the current GUIScreen blocks switching, close it
            TTC.player.closeScreen();
            doResetScreen = true;
        }
        
        swap(slot0, 8);
        swap(slot1, 8);
        swap(slot0, 8);
        
        // Reset GUIScreen if needed
        if (doResetScreen)
            TTC.mc.displayGuiScreen(screen);
        
        // Enable the next swapping operation
        swapping = false;
    }
}
