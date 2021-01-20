package tudbut.mod.client.ttc.utils;

import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

public class TTCWorld extends World {
    
    public static TTCWorld create(WorldInfo info, BiomeProvider provider) {
        TTCWorld[] w = new TTCWorld[1];
        w[0] = new TTCWorld(info, new WorldProvider() {
            @Override
            public long getSeed() {
                return info.getSeed();
            }
            
            @Override
            public DimensionType getDimensionType() {
                return DimensionType.OVERWORLD;
            }
            
            @Override
            public BiomeProvider getBiomeProvider() {
                return provider;
            }
            
            @Override
            public Biome getBiomeForCoords(BlockPos pos) {
                return w[0].getBiomeForCoordsBody(pos);
            }
        });
        return w[0];
    }
    
    TTCWorld(WorldInfo info, WorldProvider wp) {
        super(new SaveHandlerMP(), info, wp, new Profiler(), false);
    }
    
    @Override
    public long getSeed() {
        return worldInfo.getSeed();
    }
    
    @Override
    public Biome getBiomeForCoordsBody(BlockPos pos) {
        return getBiomeProvider().getBiome(pos);
    }
    
    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }
    
    @Override
    public BiomeProvider getBiomeProvider() {
        return provider.getBiomeProvider();
    }
    
    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }
}
