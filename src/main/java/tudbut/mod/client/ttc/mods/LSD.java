package tudbut.mod.client.ttc.mods;

import net.minecraft.entity.player.EntityPlayer;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.FreecamPlayer;
import tudbut.mod.client.ttc.utils.LSDRenderer;
import tudbut.mod.client.ttc.utils.Module;

import java.lang.reflect.Field;
import java.util.Objects;

public class LSD extends Module {
    public static LSD getInstance() {
        return TTC.getModule(LSD.class);
    }
    
    int mode = 0x00;
    
    {
        try {
            subButtons.add(new GuiTTC.Button("Mode: " + getMode(mode), text -> {
                mode++;
                if(mode > 0x0a)
                    mode = 0x00;
                try {
                    text.set("Mode: " + getMode(mode));
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }));
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    private String getMode(int mode) throws IllegalAccessException {
        Class<LSDRenderer> clazz = LSDRenderer.class;
        Field[] fields = clazz.getDeclaredFields();
        
        for (int i = 0; i < fields.length; i++) {
            if(fields[i].getInt(null) == mode && !fields[i].getName().equals("mode")) {
                return fields[i].getName();
            }
        }
        
        return null;
    }
    
    @Override
    public boolean displayOnClickGUI() {
        return true;
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public boolean doStoreEnabled() {
        return false;
    }
    
    @Override
    public void onTick() {
        LSDRenderer.mode = mode;
    }
    
    public void onEnable() {
        if(TTC.isIngame() && !Freecam.getInstance().enabled) {
            EntityPlayer player = new LSDRenderer(TTC.player, TTC.world);
            TTC.world.spawnEntity(player);
            TTC.mc.renderChunksMany = true;
            //TTC.mc.skipRenderWorld = true;
            TTC.mc.setRenderViewEntity(player);
        }
        else
            enabled = false;
    }
    
    @Override
    public int danger() {
        return 1;
    }
    
    @Override
    public void onDisable() {
        if(TTC.isIngame()) {
            TTC.world.removeEntity(Objects.requireNonNull(TTC.mc.getRenderViewEntity()));
            TTC.mc.setRenderViewEntity(TTC.mc.player);
            TTC.mc.renderChunksMany = true;
        }
    }
    
}
