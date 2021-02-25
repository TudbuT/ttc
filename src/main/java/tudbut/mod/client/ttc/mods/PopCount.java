package tudbut.mod.client.ttc.mods;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.Module;

import java.util.HashMap;
import java.util.Map;

public class PopCount extends Module {
    Map<EntityPlayer, Counter> counters = new HashMap<>();
    
    public void updateBinds() {
        subButtons.clear();
        subButtons.add(new GuiTTC.Button("Reset", text -> {
            counters = new HashMap<>();
        }));
    }
    
    @Override
    public void onTick() {
        Map<EntityPlayer, Counter> counters = this.counters;
        EntityPlayer[] players = counters.keySet().toArray(new EntityPlayer[0]);
        EntityPlayer[] visiblePlayers = TTC.world.playerEntities.toArray(new EntityPlayer[0]);
    
        for (int i = 0; i < visiblePlayers.length; i++) {
            if(!counters.containsKey(visiblePlayers[i])) {
                counters.put(visiblePlayers[i], new Counter(visiblePlayers[i]));
            }
        }
        
        for (int i = 0; i < players.length; i++) {
            Counter counter = counters.get(players[i]);
            counter.reload();
        }
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    public class Counter {
    
        private final EntityPlayer player;
        private int totCountLast = -1;
        private int switches = 0;
        private int pops = 0;
    
        public Counter(EntityPlayer player) {
            this.player = player;
        }
        
        public void reload() {
            if(player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING || player.getHeldItemOffhand().getItem() == Items.AIR) {
                if(totCountLast == -1) {
                    totCountLast = player.getHeldItemOffhand().getCount();
                }
                reload0();
            }
        }
        
        private void reload0() {
            int totCount = player.getHeldItemOffhand().getCount();
            if(totCount > totCountLast && totCount != 1) {
                switches++;
                ChatUtils.printChatAndHotbar("§a§l" + player.getName() + " switched (now " + switches + " switches)");
            }
            if(totCount < totCountLast) {
                pops += totCountLast - totCount; // Dont just add, add the diff so its not lag-dependent
                ChatUtils.printChatAndHotbar("§a§l" + player.getName() + " popped " + (totCountLast - totCount) + " (now " + pops + " pops)");
            }
            totCountLast = totCount;
        }
    
        public int getSwitches() {
            return switches;
        }
    
        public int getPops() {
            return pops;
        }
    }
    
    @Override
    public void loadConfig() {
        updateBinds();
    }
}
