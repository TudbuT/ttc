package tudbut.mod.client.ttc.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiCustomizeWorldScreen;
import net.minecraft.client.gui.GuiScreenCustomizePresets;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.ForgeModContainer;
import scala.util.parsing.json.JSON;
import tudbut.mod.client.ttc.TTC;
import tudbut.obj.Vector2i;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class WorldGeneratorProvider extends WorldProvider implements IChunkProvider {
    int dim;
    IChunkGenerator generator;
    long seed;
    World w;
    
    public WorldGeneratorProvider(WorldInfo info, long seed, int dim) {
        this.seed = seed;
        this.dim = dim;
        NBTTagCompound nbt = info.cloneNBTCompound(null);
        nbt.setLong("RandomSeed", seed);
        info = new WorldInfo(nbt);
        biomeProvider = new BiomeProvider(info);
        TTCWorld[] w = new TTCWorld[1];
        WorldInfo finalInfo = info;
        ForgeModContainer.fixVanillaCascading = false;
        w[0] = new TTCWorld(finalInfo, this) {
            @Override
            protected IChunkProvider createChunkProvider() {
                return WorldGeneratorProvider.this;
            }
        
            @Override
            public void tick() {
                super.tick();
                chunkProvider.tick();
            }
            
            @Override
            protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
                return getLoadedChunk(x, z) != null;
            }
            
            {
                chunkProvider = createChunkProvider();
            }
        };
        
        this.w = w[0];
        setWorld(w[0]);
        
        String gs = info.getGeneratorOptions();
        ChunkGeneratorSettings.Factory cgs = new ChunkGeneratorSettings.Factory();
        generator = new WorldType("").getChunkGenerator(w[0], gs);
    }
    
    @Override
    public long getSeed() {
        return seed;
    }
    
    public World getWorld() {
        return w;
    }
    
    @Override
    public DimensionType getDimensionType() {
        return DimensionType.getById(dim);
    }
    
    @Override
    public BiomeProvider getBiomeProvider() {
        return biomeProvider;
    }
    
    @Override
    public Biome getBiomeForCoords(BlockPos pos) {
        return w.getBiomeForCoordsBody(pos);
    }
    
    
    
    Map<Integer, Map<Integer, Chunk>> chunks = new HashMap<>();
    
    @Nullable
    @Override
    public Chunk getLoadedChunk(int x, int z) {
        return chunks.containsKey(x) ? chunks.get(x).get(z) : null;
    }
    
    @Override
    public Chunk provideChunk(int x, int z) {
        return getLoadedChunk(x, z) != null ? getLoadedChunk(x, z) : gen(x, z);
    }
    
    public Chunk gen(int x, int z) {
        ChatUtils.chatPrinterDebug().println("Generating SeedOverlay chunk at " + x + " " + z);
        
        Chunk chunk = generator.generateChunk(x, z);
        if(!chunks.containsKey(x)) {
            chunks.put(x, new HashMap<>());
        }
        chunks.get(x).put(z, chunk);
        chunk.onLoad();
        chunk.populate(this, generator);
        chunk.onTick(true);
        
        return chunk;
    }
    
    @Override
    public boolean tick() {
        Integer[] keys0 = chunks.keySet().toArray(new Integer[0]);
        
        for (int i = 0; i < keys0.length; i++) {
            Integer[] keys1 = chunks.get(keys0[i]).keySet().toArray(new Integer[0]);
            
            for (int j = 0; j < keys1.length; j++) {
                Vector2i coord = new Vector2i(keys0[i], keys1[j]);
                
                Vector2i block = new Vector2i(coord.getX() * 16, coord.getY() * 16);
                
                chunks.get(coord.getX()).get(coord.getY()).onTick(true);
                
                if(TTC.player.getDistance(block.getX(), TTC.player.posY, block.getY()) > 8 * 16) {
                    // Unload
                    chunks.get(coord.getX()).get(coord.getY()).onUnload();
                    chunks.get(coord.getX()).remove(coord.getY());
                    if(chunks.get(coord.getX()).isEmpty())
                        chunks.remove(coord.getX());
                }
            }
        }
        
        return false;
    }
    
    @Override
    public String makeString() {
        return "";
    }
    
    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return getLoadedChunk(x, z) != null;
    }
    
    
}
