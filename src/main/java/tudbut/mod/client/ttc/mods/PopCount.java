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
    
    private boolean countOwn;
    
    public void updateBinds() {
        subButtons.clear();
        subButtons.add(new GuiTTC.Button("Reset", text -> {
            counters = new HashMap<>();
        }));
        subButtons.add(new GuiTTC.Button("CountOwn: " + countOwn, text -> {
            countOwn = !countOwn;
            text.set("CountOwn: " + countOwn);
        }));
    }
    
    @Override
    public void onTick() {
        Map<EntityPlayer, Counter> counters = this.counters;
        EntityPlayer[] visiblePlayers = TTC.world.playerEntities.toArray(new EntityPlayer[0]);
    
        EntityPlayer[] players = counters.keySet().toArray(new EntityPlayer[0]);
        for (int i = 0; i < visiblePlayers.length; i++) {
            if(countOwn || visiblePlayers[i].getEntityId() != TTC.player.getEntityId()) {
                boolean b = false;
                for (int j = 0; j < players.length; j++) {
                    if (counters.get(players[j]).name.equals(visiblePlayers[i].getGameProfile().getName())) {
                        counters.get(players[j]).player = visiblePlayers[i];
                        b = true;
                    }
                }
                if (!b) {
                    counters.put(visiblePlayers[i], new Counter(visiblePlayers[i]));
                }
            }
        }
    
        players = counters.keySet().toArray(new EntityPlayer[0]);
        for (int i = 0; i < players.length; i++) {
            Counter counter = counters.get(players[i]);
            counter.reload();
        }
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    public static class Counter {
    
        private EntityPlayer player;
        private final String name;
        private int totCountLast = -1;
        private int switches = 0;
        private int pops = 0;
    
        public Counter(EntityPlayer player) {
            this.player = player;
            this.name = player.getGameProfile().getName();
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
