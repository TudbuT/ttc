package tudbut.mod.client.ttc.utils;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import com.mojang.authlib.yggdrasil.YggdrasilGameProfileRepository;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedPlayerList;
import net.minecraft.server.integrated.IntegratedServerCommandManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.CryptManager;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Util;
import net.minecraft.world.*;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tudbut.mod.client.ttc.TTC;
import tudbut.tools.Tools2;

@SideOnly(Side.CLIENT)
public class WorldGeneratorV2 extends MinecraftServer
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final WorldSettings worldSettings;
    public boolean done = false;
    private boolean isGamePaused;
    
    public static WorldGeneratorV2 create(WorldSettings settings) {
        YggdrasilAuthenticationService as = new YggdrasilAuthenticationService(TTC.mc.getProxy(), UUID.randomUUID().toString());
        YggdrasilGameProfileRepository gpr = new YggdrasilGameProfileRepository(as);
        return new WorldGeneratorV2(TTC.mc, settings, as, as.createMinecraftSessionService(), gpr, new PlayerProfileCache(gpr, new File("ttc/usercache.json")));
    }
    
    public WorldGeneratorV2(Minecraft clientIn, WorldSettings worldSettingsIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn)
    {
        super(new File("ttc/saves"), clientIn.getProxy(), clientIn.getDataFixer(), authServiceIn, sessionServiceIn, profileRepoIn, profileCacheIn);
    
        Tools2.deleteDir(new File("ttc/saves/main"));
        new File("ttc/saves/main").mkdirs();
        
        this.setServerOwner("TTC");
        this.setFolderName("main");
        this.setWorldName("main");
        this.setDemo(clientIn.isDemo());
        this.canCreateBonusChest(worldSettingsIn.isBonusChestEnabled());
        this.setBuildLimit(256);
        this.setPlayerList(new PlayerList(this) { });
        this.worldSettings = this.isDemo() ? WorldServerDemo.DEMO_WORLD_SETTINGS : worldSettingsIn;
    }
    
    public ServerCommandManager createCommandManager()
    {
        return new ServerCommandManager(this);
    }
    
    public void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions)
    {
        this.convertMapIfNeeded(saveName);
        ISaveHandler isavehandler = this.getActiveAnvilConverter().getSaveLoader(saveName, true);
        this.setResourcePackFromWorld(this.getFolderName(), isavehandler);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();
        
        if (worldinfo == null)
        {
            worldinfo = new WorldInfo(this.worldSettings, worldNameIn);
        }
        else
        {
            worldinfo.setWorldName(worldNameIn);
        }
        
        if (false) { //Forge: Dead Code, implement below.
            for (int i = 0; i < this.worlds.length; ++i)
            {
                int j = 0;
                
                if (i == 1)
                {
                    j = -1;
                }
                
                if (i == 2)
                {
                    j = 1;
                }
                
                if (i == 0)
                {
                    if (this.isDemo())
                    {
                        this.worlds[i] = (WorldServer)(new WorldServerDemo(this, isavehandler, worldinfo, j, this.profiler)).init();
                    }
                    else
                    {
                        this.worlds[i] = (WorldServer)(new WorldServer(this, isavehandler, worldinfo, j, this.profiler)).init();
                    }
                    
                    this.worlds[i].initialize(this.worldSettings);
                }
                else
                {
                    this.worlds[i] = (WorldServer)(new WorldServerMulti(this, isavehandler, j, this.worlds[0], this.profiler)).init();
                }
                
                this.worlds[i].addEventListener(new ServerWorldEventHandler(this, this.worlds[i]));
            }
        }// Forge: End Dead Code
        
        WorldServer overWorld = (isDemo() ? (WorldServer)(new WorldServerDemo(this,isavehandler, worldinfo, 0, this.profiler)).init() :
                                 (WorldServer)(new WorldServer(this, isavehandler, worldinfo, 0, this.profiler)).init());
        overWorld.initialize(this.worldSettings);
        for (int dim : net.minecraftforge.common.DimensionManager.getStaticDimensionIDs())
        {
            WorldServer world = (dim == 0 ? overWorld : (WorldServer)(new WorldServerMulti(this, isavehandler, dim, overWorld, this.profiler)).init());
            world.addEventListener(new ServerWorldEventHandler(this, world));
            if (!this.isSinglePlayer())
            {
                world.getWorldInfo().setGameType(getGameType());
            }
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Load(world));
        }
        
        this.getPlayerList().setPlayerManager(new WorldServer[]{ overWorld });
    
        this.initialWorldChunkLoad();
        
        done = true;
    }
    
    public boolean init() {
        LOGGER.info("Starting integrated minecraft server version 1.12.2");
        this.setOnlineMode(true);
        this.setCanSpawnAnimals(true);
        this.setCanSpawnNPCs(true);
        this.setAllowPvp(true);
        this.setAllowFlight(true);
        LOGGER.info("Generating keypair");
        this.setKeyPair(CryptManager.generateKeyPair());
        if (!net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerAboutToStart(this)) return false;
        this.loadAllWorlds(this.getFolderName(), this.getWorldName(), this.worldSettings.getSeed(), this.worldSettings.getTerrainType(), this.worldSettings.getGeneratorOptions());
        this.setMOTD(this.getServerOwner() + " - " + this.worlds[0].getWorldInfo().getWorldName());
        return net.minecraftforge.fml.common.FMLCommonHandler.instance().handleServerStarting(this);
    }
    
    public void tick()
    {
        boolean flag = this.isGamePaused;
        this.isGamePaused = Minecraft.getMinecraft().getConnection() != null && Minecraft.getMinecraft().isGamePaused();
        
        if (!flag && this.isGamePaused)
        {
            LOGGER.info("Saving and pausing game...");
            this.getPlayerList().saveAllPlayerData();
            this.saveAllWorlds(false);
        }
        
        if (this.isGamePaused)
        {
            synchronized (this.futureTaskQueue)
            {
                while (!this.futureTaskQueue.isEmpty())
                {
                    Util.runTask(this.futureTaskQueue.poll(), LOGGER);
                }
            }
        }
        else {
            super.tick();
    
            if (TTC.mc.gameSettings.renderDistanceChunks != this.getPlayerList().getViewDistance()) {
                LOGGER.info("Changing view distance to {}, from {}", TTC.mc.gameSettings.renderDistanceChunks, this.getPlayerList().getViewDistance());
                this.getPlayerList().setViewDistance(TTC.mc.gameSettings.renderDistanceChunks);
            }
    
            WorldInfo worldinfo1 = this.worlds[0].getWorldInfo();
            WorldInfo worldinfo = TTC.mc.world.getWorldInfo();
    
            if (!worldinfo1.isDifficultyLocked() && worldinfo.getDifficulty() != worldinfo1.getDifficulty()) {
                LOGGER.info("Changing difficulty to {}, from {}", worldinfo.getDifficulty(), worldinfo1.getDifficulty());
                this.setDifficultyForAllWorlds(worldinfo.getDifficulty());
            } else if (worldinfo.isDifficultyLocked() && !worldinfo1.isDifficultyLocked()) {
                LOGGER.info("Locking difficulty to {}", (Object) worldinfo.getDifficulty());
        
                for (WorldServer worldserver : this.worlds) {
                    if (worldserver != null) {
                        worldserver.getWorldInfo().setDifficultyLocked(true);
                    }
                }
            }
        }
    }
    
    public boolean canStructuresSpawn()
    {
        return false;
    }
    
    public GameType getGameType()
    {
        return this.worldSettings.getGameType();
    }
    
    public EnumDifficulty getDifficulty()
    {
        return TTC.mc.world.getWorldInfo().getDifficulty();
    }
    
    public boolean isHardcore()
    {
        return this.worldSettings.getHardcoreEnabled();
    }
    
    public boolean shouldBroadcastRconToOps()
    {
        return true;
    }
    
    public boolean shouldBroadcastConsoleToOps()
    {
        return true;
    }
    
    public void saveAllWorlds(boolean isSilent)
    {
        super.saveAllWorlds(isSilent);
    }
    
    public File getDataDirectory()
    {
        return new File("ttcSaves");
    }
    
    public boolean isDedicatedServer()
    {
        return false;
    }
    
    public boolean shouldUseNativeTransport()
    {
        return false;
    }
    
    public void finalTick(CrashReport report)
    {
        //this.mc.crashed(report);
    }
    
    public CrashReport addServerInfoToCrashReport(CrashReport report)
    {
        report = super.addServerInfoToCrashReport(report);
        report.getCategory().addDetail("Type", new ICrashReportDetail<String>()
        {
            public String call() throws Exception
            {
                return "Integrated Server (map_client.txt)";
            }
        });
        report.getCategory().addDetail("Is Modded", new ICrashReportDetail<String>()
        {
            public String call() throws Exception
            {
                String s = ClientBrandRetriever.getClientModName();
                
                if (!s.equals("vanilla"))
                {
                    return "Definitely; Client brand changed to '" + s + "'";
                }
                else
                {
                    s = WorldGeneratorV2.this.getServerModName();
                    
                    if (!"vanilla".equals(s))
                    {
                        return "Definitely; Server brand changed to '" + s + "'";
                    }
                    else
                    {
                        return Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and both client + server brands are untouched.";
                    }
                }
            }
        });
        return report;
    }
    
    public void setDifficultyForAllWorlds(EnumDifficulty difficulty)
    {
        super.setDifficultyForAllWorlds(difficulty);
    }
    
    @Override
    public void addServerStatsToSnooper(Snooper playerSnooper)
    {
        //super.addServerStatsToSnooper(playerSnooper);
    }
    
    public boolean isSnooperEnabled()
    {
        return Minecraft.getMinecraft().isSnooperEnabled();
    }
    
    @Override
    public String shareToLAN(GameType type, boolean allowCheats)
    {
        return "";
    }
    
    public void stopServer()
    {
        initiateShutdown();
        super.stopServer();
    }
    
    public void initiateShutdown()
    {
        // No need to check that, you cant join it anyway!
        /*
        if (isServerRunning())
            Futures.getUnchecked(this.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    for (EntityPlayerMP entityplayermp : Lists.newArrayList(WorldGeneratorV2.this.getPlayerList().getPlayers()))
                    {
                        if (!entityplayermp.getUniqueID().equals(WorldGeneratorV2.this.mc.player.getUniqueID()))
                        {
                            WorldGeneratorV2.this.getPlayerList().playerLoggedOut(entityplayermp);
                        }
                    }
                }
            }));
            
         */
        super.initiateShutdown();
        
    }
    
    public void setGameType(GameType gameMode)
    {
        super.setGameType(gameMode);
        this.getPlayerList().setGameType(gameMode);
    }
    
    public boolean isCommandBlockEnabled()
    {
        return true;
    }
    
    public int getOpPermissionLevel()
    {
        return 4;
    }
}
