package tudbut.mod.client.ttc.mods;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.utils.FreecamPlayer;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.Tesselator;

import java.util.Objects;

import static tudbut.mod.client.ttc.utils.Tesselator.*;

public class Freecam extends Module {
    
    public static Freecam getInstance() {
        return TTC.getModule(Freecam.class);
    }
    
    GameType type;
    
    @Override
    public boolean displayOnClickGUI() {
        return true;
    }
    
    @Override
    public boolean doStoreEnabled() {
        return false;
    }
    
    public void onEnable() {
        if(TTC.isIngame() && !LSD.getInstance().enabled && TTC.mc.getRenderViewEntity() == TTC.player) {
            EntityPlayer player = new FreecamPlayer(TTC.player, TTC.world);
            TTC.world.spawnEntity(player);
            type = TTC.mc.playerController.getCurrentGameType();
            //TTC.mc.playerController.setGameType(GameType.SPECTATOR);
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
            //TTC.mc.playerController.setGameType(type);
        }
        TTC.mc.setRenderViewEntity(TTC.mc.player);
        TTC.mc.gameSettings.thirdPersonView = 0;
        TTC.mc.renderChunksMany = true;
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
    
        if(TTC.isIngame() && enabled) {
            Entity main = TTC.player;
            Entity e = TTC.mc.getRenderViewEntity();
            Vec3d p = e.getPositionEyes(event.getPartialTicks()).addVector(0, -e.getEyeHeight(), 0);
            Vec3d pos = main.getPositionVector();
            float entityHalfed = main.width / 2 + 0.01f;
            float entityHeight = main.height + 0.01f;
            
            ready();
            translate(-p.x, -p.y, -p.z);
            color(0x80ff0000);
            depth(false);
            begin(GL11.GL_QUADS);
            
            // bottom
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
    
            next();
            
            // top
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
    
            next();
    
            // z -
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
    
            next();
    
            // z +
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
    
            next();
    
            // x -
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            put(pos.x - entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            put(pos.x - entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
    
            next();
    
            // y +
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z - entityHalfed);
            put(pos.x + entityHalfed, pos.y + entityHeight, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z + entityHalfed);
            put(pos.x + entityHalfed, pos.y - 0.01, pos.z - entityHalfed);
            
            end();
        }
    }
}
