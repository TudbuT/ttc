package tudbut.mod.client.ttc.mods;

import de.tudbut.type.Vector3d;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import scala.Int;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.*;
import tudbut.obj.Vector2i;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static tudbut.mod.client.ttc.utils.Tesselator.*;

public class SeedOverlay extends Module {
    Map<BlockPos, Integer> toRender = new HashMap<>();
    WorldGeneratorV2 generator;
    World world;
    String worldOptions;
    WorldType worldType;
    long seed = Long.MAX_VALUE;
    boolean isUpdating = false;
    boolean lock = false;
    
    static final ArrayList<Block> disableCheck = new ArrayList<>();
    
    static {
        disableCheck.add(Blocks.GLOWSTONE);
        disableCheck.add(Blocks.LOG);
        disableCheck.add(Blocks.LEAVES);
        disableCheck.add(Blocks.LOG2);
        disableCheck.add(Blocks.LEAVES2);
        disableCheck.add(Blocks.COAL_ORE);
        disableCheck.add(Blocks.IRON_ORE);
        disableCheck.add(Blocks.GOLD_ORE);
        disableCheck.add(Blocks.LAPIS_ORE);
        disableCheck.add(Blocks.EMERALD_ORE);
        disableCheck.add(Blocks.DIAMOND_ORE);
        disableCheck.add(Blocks.TALLGRASS);
        disableCheck.add(Blocks.DOUBLE_PLANT);
        disableCheck.add(Blocks.VINE);
        disableCheck.add(Blocks.YELLOW_FLOWER);
        disableCheck.add(Blocks.RED_FLOWER);
        disableCheck.add(Blocks.BROWN_MUSHROOM);
        disableCheck.add(Blocks.RED_MUSHROOM);
        disableCheck.add(Blocks.BROWN_MUSHROOM_BLOCK);
        disableCheck.add(Blocks.RED_MUSHROOM_BLOCK);
        disableCheck.add(Blocks.FIRE);
        disableCheck.add(Blocks.DEADBUSH);
    }
    
    {
        subButtons.add(new GuiTTC.Button("Update world data", text -> ThreadManager.run(() -> {
            world = TTC.world;
            worldOptions = TTC.world.getWorldInfo().getGeneratorOptions();
            worldType = TTC.world.getWorldType();
            ChatUtils.print("Downloaded data, now generating chunks!");
            if(seed != Long.MAX_VALUE) {
                ThreadManager.run(() -> {
                    if(generator != null) {
                        try {
                            generator.stopServer();
                        }
                        catch (Exception ignore) { }
                    }
                    generator = createFreshWorldCopy(world, seed);
                    world = generator.getWorld(TTC.world.provider.getDimension());
                });
            }
            else
                ChatUtils.print("Error: No seed given! ',seedoverlay <seed>' to set");
        })));
        subButtons.add(new GuiTTC.Button("Delete world data", text -> ThreadManager.run(() -> {
            world = null;
            generator.stopServer();
            generator = null;
        })));
    }
    
    private void update() {
        Map<BlockPos, Integer> toRender = new HashMap();
        EntityPlayer player = TTC.player;
        
        if(world != null) {
            world = generator.getWorld(TTC.world.provider.getDimension());
            //ChatUtils.print("Rendering SeedOverlay");
    
            for (int z = -8 * 16; z < 8 * 16; z++) {
                for (int x = -8 * 16; x < 8 * 16; x++) {
                    int theX = (int) (player.posX + x);
                    int theZ = (int) (player.posZ + z);
            
                    for (int y = 0; y < 256; y++) {
                        BlockPos bp = new BlockPos(theX, y, theZ);
                        if (TTC.mc.world.isBlockLoaded(bp, false) && world.getChunkFromBlockCoords(bp).isTerrainPopulated()) {
                            //ChatUtils.print("Check!");
                            IBlockState a = TTC.world.getBlockState(bp);
                            IBlockState b = world.getBlockState(bp);
                            if (!a.getMaterial().equals(b.getMaterial())) {
                                if(
                                        !a.getMaterial().isLiquid() && !b.getMaterial().isLiquid() &&
                                        !(BlockFalling.class.isAssignableFrom(a.getBlock().getClass())) && !(BlockFalling.class.isAssignableFrom(b.getBlock().getClass())) &&
                                        !disableCheck.contains(a.getBlock()) && !disableCheck.contains(b.getBlock())
                                ) {
                                    b.getBlock().updateTick(world, bp, b, world.rand);
                                    
                                    if(a.getMaterial() == Material.AIR)
                                        toRender.put(bp, -1);
                                    else if(b.getMaterial() == Material.AIR)
                                        toRender.put(bp, 1);
                                    else
                                        toRender.put(bp, 0);
                                        
                                }
                            }
                        }
                    }
                }
            }
        }
        
        lock = true;
        this.toRender = toRender;
        lock = false;
    }
    
    public static WorldGeneratorV2 createFreshWorldCopy(World worldIn, long seed) {
        WorldInfo i = worldIn.getWorldInfo();
        
        NBTTagCompound nbt = i.cloneNBTCompound(null);
        nbt.setLong("RandomSeed", seed);
        WorldSettings settings = new WorldSettings(seed, worldIn.getWorldInfo().getGameType(), true, false, worldIn.getWorldType());
        settings.setGeneratorOptions(worldIn.getWorldInfo().getGeneratorOptions());
        
        WorldGeneratorV2 w = WorldGeneratorV2.create(settings);
        w.startServerThread();
        while (!w.done) {
            ChatUtils.print(w.percentDone + "% Generated");
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ChatUtils.print("Done!");
        return w;
    }
    
    @Override
    public void onChat(String s, String[] args) {
        isUpdating = false;
        try {
            seed = Long.parseLong(s);
        }
        catch (NumberFormatException e) {
            seed = s.hashCode();
        }
        ChatUtils.print("Set! " + seed);
    }
    
    Vec3d pos = new Vec3d(0,0,0);
    
    @Override
    public void onTick() {
        if(isUpdating)
            return;
        isUpdating = true;
        ThreadManager.run(() -> {
            try {
                update();
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
            isUpdating = false;
        });
    }
    
    @SubscribeEvent
    public void onRenderWorld(Event event) {
        
        if(event instanceof RenderWorldLastEvent)
            if(this.enabled && TTC.isIngame()) {
                Entity e = TTC.mc.getRenderViewEntity();
                pos =
                        e.getPositionEyes(((RenderWorldLastEvent) event).getPartialTicks()).addVector(0, -e.getEyeHeight(), 0);
                while (lock);
                Map<BlockPos, Integer> toRender = this.toRender;
                BlockPos[] toRenderPositions = this.toRender.keySet().toArray(new BlockPos[0]);
                
                if(toRenderPositions.length > 50000) {
                    ChatUtils.print("Too many blocks to render!");
                    return;
                }
                
                for (int i = 0; i < toRenderPositions.length; i++) {
                    int color = toRender.get(toRenderPositions[i]);
                    
                    switch (color) {
                        case 1:
                            color = 0x2000ff00;
                            break;
                        case 0:
                            color = 0x20808000;
                            break;
                        case -1:
                            color = 0x20ff0000;
                            break;
                    }
                    
                    drawAroundBlock(
                            new Vector3d(
                                    toRenderPositions[i].getX() + 0.5,
                                    toRenderPositions[i].getY(),
                                    toRenderPositions[i].getZ() + 0.5
                            ),
                            color
                    );
                }
            }
    }
    
    public void drawAroundBlock(Vector3d pos, int color) {
        try {
            
            ready();
            translate(-this.pos.x, -this.pos.y, -this.pos.z);
            color(color);
            depth(false);
            begin(GL11.GL_QUADS);
    
            
            // bottom
            put(pos.getX() - 0.5, pos.getY() - 0.01, pos.getZ() + 0.5);
            put(pos.getX() + 0.5, pos.getY() - 0.01, pos.getZ() + 0.5);
            put(pos.getX() + 0.5, pos.getY() - 0.01, pos.getZ() - 0.5);
            put(pos.getX() - 0.5, pos.getY() - 0.01, pos.getZ() - 0.5);
            
            //next();
    
            // top
            put(pos.getX() - 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
            put(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
            put(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() - 0.5);
            put(pos.getX() - 0.5, pos.getY() + 1.01, pos.getZ() - 0.5);
    
            //next();
            
            // z -
            put(pos.getX() - 0.5, pos.getY() + 1.01, pos.getZ() - 0.5);
            put(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() - 0.5);
            put(pos.getX() + 0.5, pos.getY() - 0.01, pos.getZ() - 0.5);
            put(pos.getX() - 0.5, pos.getY() - 0.01, pos.getZ() - 0.5);
    
            //next();
            
            // z +
            put(pos.getX() - 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
            put(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
            put(pos.getX() + 0.5, pos.getY() - 0.01, pos.getZ() + 0.5);
            put(pos.getX() - 0.5, pos.getY() - 0.01, pos.getZ() + 0.5);
    
            //next();
    
            // x -
            put(pos.getX() - 0.5, pos.getY() + 1.01, pos.getZ() - 0.5);
            put(pos.getX() - 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
            put(pos.getX() - 0.5, pos.getY() - 0.01, pos.getZ() + 0.5);
            put(pos.getX() - 0.5, pos.getY() - 0.01, pos.getZ() - 0.5);
    
            //next();
    
            // y +
            put(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() - 0.5);
            put(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
            put(pos.getX() + 0.5, pos.getY() - 0.01, pos.getZ() + 0.5);
            put(pos.getX() + 0.5, pos.getY() - 0.01, pos.getZ() - 0.5);
            
            end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
