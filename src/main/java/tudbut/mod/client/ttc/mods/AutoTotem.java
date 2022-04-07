package tudbut.mod.client.ttc.mods;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import tudbut.debug.DebugProfiler;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.gui.GuiTTCIngame;
import tudbut.mod.client.ttc.utils.*;
import tudbut.tools.Lock;
import tudbut.tools.ThreadPool;

import java.util.ArrayList;

public class AutoTotem extends Module {
    
    static AutoTotem instance;
    
    public static DebugProfiler profiler = new DebugProfiler("AutoTotem", "idle");
    
    // Actual count, set by AI
    public int minCount = 0;
    // Count, set by user
    public int origMinCount = 0;
    // If the user seems to be restocking after respawning, if this is the case,
    // don't switch until any inventories are closed
    public boolean isRestockingAfterRespawn = false;
    // If totems should be stacked automatically
    public boolean autoStack = false;
    // If the AutoStack should always run, regardless of the count
    private boolean autoStackIgnoreCount = false;
    
    public int delay = 0;
    
    Lock swapLock = new Lock();
    ThreadPool swapThread = new ThreadPool(1, "Swap thread", true);
    
    private boolean noTotems = true;
    public int fullCount;
    
    public AutoTotem() {
        instance = this;
    }
    
    public static AutoTotem getInstance() {
        return instance;
    }
    
    
    public void renderTotems() {
        if(fullCount != 0) {
            ScaledResolution res = new ScaledResolution(TTC.mc);
            int y = res.getScaledHeight() - 16 * 2 - 3 - 8;
            int x;
            if (TTC.player.getPrimaryHand() == EnumHandSide.RIGHT)
                x = res.getScaledWidth() / 2 - 91 - 26;
            else
                x = res.getScaledWidth() / 2 + 91 + 10;
            
            GuiTTCIngame.drawOffhandSlot(x - 3, y - 3);
            GuiTTCIngame.drawItem(x, y, 1, TTC.player, new ItemStack(Items.TOTEM_OF_UNDYING, fullCount));
        }
    }
    
    public int getColor() {
        if(fullCount < (origMinCount + 1) * 5) {
            return 0xffff0000;
        }
        
        if(fullCount < (origMinCount + 1) * 20) {
            return 0xffffff00;
        }
        
        return 0xff00ff00;
    }
    
    public int getTotemCount() {
        return InventoryUtils.getItemAmount(TTC.player.inventoryContainer, Items.TOTEM_OF_UNDYING);
    }
    
    public void updateBinds() {
        subButtons.clear();
        subButtons.add(Setting.createInt(0, 12, 1, "Count: $val", this, "origMinCount"));
        subButtons.add(Setting.createBoolean("AutoStack (WIP): $val", this, "autoStack"));
        subButtons.add(Setting.createInt(0, 5000, 50, "Delay: $val", this, "delay"));
        subButtons.add(new GuiTTC.Button("AutoStack now", text -> {
            autoStackIgnoreCount = true;
            autoStack();
            autoStackIgnoreCount = false;
        }));
        subButtons.add(new GuiTTC.Button("Actual count: " + minCount, text -> {
        
        }));
    }
    
    // Run checks and AI
    @Override
    public void onSubTick() {
        if (TTC.isIngame()) {
            fullCount = getTotemCount();
    
            if (!swapLock.isLocked()) {
                EntityPlayerSP player = TTC.player;
        
        
                profiler.next("RestockCheck");
                if ((isRestockingAfterRespawn() || isRestockingAfterRespawn)) {
                    // Don't switch yet
                    return;
                }
        
                // Run AI
                if (noTotems) {
                    profiler.next("TotCountUpdate");
                    updateTotCount();
                }
                profiler.next("AutoStack");
                if (autoStack)
                    autoStack();
        
                profiler.next("Check");
                ItemStack stack = player.getHeldItemOffhand();
                int minCount = this.minCount;
                if(stack.getCount() <= minCount + 4) {
                    KillAura ka = KillAura.getInstance();
                    if(ka.switchItem) {
                        ka.switchItemTmp = true;
                        ka.switchItem = false;
                    }
                }
                if (stack.getCount() <= minCount) {
                    // Switch!
            
                    profiler.next("Switch.GetSlot");
                    Integer slot = InventoryUtils.getSlotWithItem(
                            player.inventoryContainer,
                            Items.TOTEM_OF_UNDYING,
                            new int[] { InventoryUtils.OFFHAND_SLOT },
                            minCount + 1,
                            64
                    );
                    if (slot == null) {
                        profiler.next("Switch.NotifyEmpty");
                        if (!noTotems)
                            Notifications.add(new Notifications.Notification("No more totems! Couldn't switch!"));
                        noTotems = true;
                        profiler.next("idle");
                        return; // Oh no!! No totems left!
                    }
                    else
                        noTotems = false;
            
                    profiler.next("Switch.Swap");
                    swapLock.lock(2000);
                    swapThread.run(() -> {
                        // Switch a new totem stack to the offhand
                        InventoryUtils.inventorySwap(slot, InventoryUtils.OFFHAND_SLOT, delay, 300, 100);
                        KillAura ka = KillAura.getInstance();
                        ka.switchItem = ka.switchItemTmp;
                        ka.switchItemTmp = false;
                        swapLock.lock(200);
                    });
            
            
                    profiler.next("Switch.Notify");
                    Notifications.add(new Notifications.Notification("Switched to next TotemStack"));
                }
            }
        }
        profiler.next("idle");
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
                        origMinCount + 1,
                        64
                ) != null
        ) {
            minCount = origMinCount;
            updateBinds();
            return;
        }
        
        // Look for a stack of the AI-set count
        Integer i = InventoryUtils.getSlotWithItem(
                player.inventoryContainer,
                Items.TOTEM_OF_UNDYING,
                new int[]{InventoryUtils.OFFHAND_SLOT},
                minCount + 1,
                64
        );
        // If it doesnt exist, step down the count until a stack exist or the count hits 0
        while (i == null) {
            // Step down
            minCount--;
            // Check
            i = InventoryUtils.getSlotWithItem(
                    player.inventoryContainer,
                    Items.TOTEM_OF_UNDYING,
                    new int[]{InventoryUtils.OFFHAND_SLOT},
                    minCount + 1,
                    64
            );
            updateBinds();
            
            if (minCount < 0) {
                // No stacks left
                minCount = 0;
                updateBinds();
                return; // Sorry
            }
        }
        
        // Found!
    }
    
    public void autoStack() {
        
        if(minCount == 0)
            return;
        
        EntityPlayerSP player = TTC.player;
        ArrayList<Integer> slots = new ArrayList<>();
        // The minimal amount that is required to stack totems
        int min = 2;
        // Only restack when totems are likely not a normal stack
        int max = 24;
        // TMP variable
        Integer slot;
        
        // Runs 50 times
        for (int i = 0; i < 50; i++) {
            
            // Drop unusable stacks
            ArrayList<Integer> dropped = new ArrayList<>();
            if (slots.size() != 0) {
                
                for (int j = 0; j < 100; j++) {
                    // Next
                    slot = InventoryUtils.getSlotWithItem(
                            player.inventoryContainer,
                            Items.TOTEM_OF_UNDYING,
                            Utils.objectArrayToNativeArray(dropped.toArray(new Integer[0])),
                            0,
                            min - 1
                    );
                    
                    if (slot == null)
                        break;
                    
                    // Drop stack contents of the slot
                    InventoryUtils.drop(slot);
                    System.out.println("Dropped item in " + slot);
                    dropped.add(slot);
                }
                
            }
            
            if(origMinCount == minCount && !autoStackIgnoreCount)
                return;
            
            
            // Get slots with totems
            slots.clear();
            for (int j = 0; j < 100; j++) {
                slot = InventoryUtils.getSlotWithItem(
                        player.inventoryContainer,
                        Items.TOTEM_OF_UNDYING,
                        Utils.objectArrayToNativeArray(slots.toArray(new Integer[0])),
                        min,
                        max
                );
                if(slot == null)
                    break;
                
                slots.add(slot);
            }
            
            // Combine totems
            while (slots.size() >= 2) {
                // Get empty slot
                slot = InventoryUtils.getSlotWithItem(player.inventoryContainer, Items.AIR, 0);
                if (slot == null) {
                    InventoryUtils.drop(slots.get(0));
                    slots.remove(0);
                    continue;
                }
                System.out.println("Combining " + slots.get(0) + " and " + slots.get(1) + " to " + slot);
                // Pick first stack
                InventoryUtils.clickSlot(slots.get(0), ClickType.PICKUP, 0);
                // Pick second stack
                InventoryUtils.clickSlot(slots.get(1), ClickType.PICKUP, 0);
                // Put result in empty slot
                InventoryUtils.clickSlot(slot, ClickType.PICKUP, 0);
                // Drop junk
                InventoryUtils.drop(slots.get(1));
                
                slots.remove(0);
                slots.remove(0);
            }
        }
    }
    
    @Override
    public void onChat(String s, String[] args) {
        if (s.startsWith("count "))
            try {
                origMinCount = minCount = Integer.parseInt(s.substring("count ".length()));
                ChatUtils.print("Set!");
            }
            catch (Exception e) {
                ChatUtils.print("ERROR: NaN");
            }
        if(s.startsWith("debug"))
            ChatUtils.print(profiler.getTempResults().toString());
        updateBinds();
    }
    
    @Override
    public void loadConfig() {
        origMinCount = minCount = Integer.parseInt(cfg.get("count"));
        autoStack = Boolean.parseBoolean(cfg.get("autoStack"));
        delay = Integer.parseInt(cfg.get("delay"));
    }
    
    @Override
    public void updateConfig() {
        cfg.put("count", String.valueOf(origMinCount));
        cfg.put("autoStack", String.valueOf(autoStack));
        cfg.put("delay", String.valueOf(delay));
    }
}
