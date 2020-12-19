package tudbut.mod.client.yac.utils;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tudbut.mod.client.yac.Yac;
import tudbut.mod.client.yac.gui.GuiYAC;

public class InventoryUtils {
    
    public static final int OFFHAND_SLOT = 45;
    
    public static Integer getSlotWithItem(Container inv, Item item, int amount) {
        return getSlotWithItem(inv, item, new int[0], amount, amount);
    }
    
    public static Integer getSlotWithItem(Container inv, Item item, int[] not, int amountMin, int amountMax) {
        for (int i = 0; i < inv.getInventory().size(); i++) {
            a:
            {
                for (int j = 0; j < not.length; j++) {
                    if(i == not[j])
                        break a;
                }
                
                ItemStack stack = inv.getSlot(i).getStack();
                if (stack.getItem().equals(item) && stack.getCount() >= amountMin && stack.getCount() <= amountMax)
                    return i;
            }
        }
        return null;
    }
    
    private static boolean swapping = false;
    
    public static void swap(int slot, int hotbarSlot) {
        Yac.mc.playerController.windowClick(Yac.mc.player.inventoryContainer.windowId, slot, hotbarSlot, ClickType.SWAP, Yac.mc.player);
    }
    
    public static void inventorySwap(int from, int to) {
        if(swapping)
            return;
        
        swapping = true;
        ThreadManager.run(() -> {
            try {
                GuiScreen screen = Yac.mc.currentScreen;
                if(screen != null && screen.getClass() != GuiChat.class && screen.getClass() != GuiIngameMenu.class && screen.getClass() != GuiYAC.class) {
                    Thread.sleep(200);
                    Yac.player.closeScreen();
                    Thread.sleep(300);
                }
                
                Yac.mc.playerController.windowClick(Yac.mc.player.inventoryContainer.windowId, from, 8, ClickType.SWAP, Yac.mc.player);
                Thread.sleep(100);
                Yac.mc.playerController.windowClick(Yac.mc.player.inventoryContainer.windowId, to, 8, ClickType.SWAP, Yac.mc.player);
                Thread.sleep(100);
                Yac.mc.playerController.windowClick(Yac.mc.player.inventoryContainer.windowId, from, 8, ClickType.SWAP, Yac.mc.player);
                Thread.sleep(100);
                Yac.mc.displayGuiScreen(screen);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            swapping = false;
        });
    }
}
