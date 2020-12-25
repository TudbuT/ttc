package tudbut.mod.client.ttc.mods;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.InventoryUtils;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.ThreadManager;

public class SafeTotem extends Module {
    
    @Override
    public void onTick() {
        EntityPlayerSP player = TTC.player;
    
        ((AutoTotem) TTC.modules[0]).updateTotCount();
        
        ItemStack stack = player.getHeldItemOffhand();
        if(stack.getCount() <= ((AutoTotem) TTC.modules[0]).min_count) {
            Integer slot = InventoryUtils.getSlotWithItem(
                    player.inventoryContainer,
                    Items.TOTEM_OF_UNDYING,
                    new int[] {InventoryUtils.OFFHAND_SLOT},
                    ((AutoTotem) TTC.modules[0]).min_count + 1,
                    64
            );
            if(slot == null) {
                ((AutoTotem) TTC.modules[0]).min_count--;
                if(((AutoTotem) TTC.modules[0]).min_count >= 0)
                    onTick();
                return;
            }
            ThreadManager.run(() -> safeInventorySwap(slot, InventoryUtils.OFFHAND_SLOT));
        }
    }
    
    @Override
    public void onEnable() {
        TTC.modules[0].enabled = false;
    }
    
    private static boolean swapping = false;
    public static void safeInventorySwap(int from, int to) {
        if(swapping)
            return;
    
        swapping = true;
        ThreadManager.run(() -> {
            try {
                int bSlot = TTC.player.inventory.currentItem;
    
                GuiScreen screen = TTC.mc.currentScreen;
                if(screen != null && screen.getClass() != GuiChat.class && screen.getClass() != GuiInventory.class && screen.getClass().getComponentType() != GuiNewChat.class.getComponentType() && screen.getClass() != GuiIngameMenu.class && screen.getClass() != GuiTTC.class) {
                    Thread.sleep(200);
                    TTC.player.closeScreen();
                    Thread.sleep(300);
                }
                //Thread.sleep(10);
                InventoryUtils.swap(from, bSlot);
                //Thread.sleep(100);
                InventoryUtils.swap(to, bSlot);
                //Thread.sleep(200);
                InventoryUtils.swap(from, bSlot);
                //Thread.sleep(200);
                TTC.mc.displayGuiScreen(screen);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            swapping = false;
        });
    }
    
    @Override
    public void onChat(String s, String[] args) {
    }
}
