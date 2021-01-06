package tudbut.mod.client.ttc;

import de.tudbut.tools.FileRW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import tudbut.mod.client.ttc.events.FMLEventHandler;
import tudbut.mod.client.ttc.mods.*;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.ThreadManager;
import tudbut.mod.client.ttc.utils.Utils;

import javax.swing.*;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Mod(modid = TTC.MODID, name = TTC.NAME, version = TTC.VERSION)
public class TTC {
    // FML stuff and version
    public static final String MODID = "ttc";
    public static final String NAME = "TTC Client";
    public static final String VERSION = "vB1.4.1a";
    
    // Registered modules, will make an api for it later
    public static Module[] modules;
    // Player and current World(/Dimension), updated regularly in FMLEventHandler
    public static EntityPlayerSP player;
    public static World world;
    // Current Minecraft instance running
    public static Minecraft mc = Minecraft.getMinecraft();
    // Config
    public static FileRW file;
    public static Map<String, String> cfg;
    // Prefix for chat-commands
    public static String prefix = ",";
    
    // Logger, provided by Forge
    public static Logger logger;
    
    // Runs a slight moment after the game is started, not all mods are initialized yet
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        try {
            file = new FileRW("config/ttc.cfg");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Runs when all important info is loaded and all mods are pre-initialized,
    // most game objects exist already when this is called
    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("TTC by TudbuT");
        
        long sa; // For time measurements
        
        // Show the "TTC by TudbuT" message
        ThreadManager.run(() -> {
            JOptionPane.showMessageDialog(null, "TTC by TudbuT");
        });
        System.out.println("Init...");
        sa = new Date().getTime();
        try {
            cfg = Utils.stringToMap(file.getContent().join("\n"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        
        System.out.println("Constructing modules...");
        sa = new Date().getTime();
        // Constructing modules to be usable
        modules = new Module[] {
                new AutoTotem(),
                new TPAParty(),
                new Prefix(),
                new Team(),
                new TPATools(),
                new ChatSuffix(),
                new AutoConfig(),
                new ChatColor(),
                new Trap(),
                new PlayerLog(),
                new DMAll(),
                new DM(),
                new DMChat(),
                new Debug(),
                new ClickGUI(),
                };
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        
        // Registering event handlers
        MinecraftForge.EVENT_BUS.register(new FMLEventHandler());
        
        System.out.println("Loading config...");
        sa = new Date().getTime();
        
        // Loading config from config/ttc.cfg
        // Loading config for modules
        for (int i = 0; i < modules.length; i++) {
            try {
                logger.info(modules[i].toString());
                modules[i].saveConfig();
                if (cfg.containsKey(modules[i].toString())) {
                    modules[i].loadConfig(Utils.stringToMap(cfg.get(modules[i].getClass().getSimpleName())));
                }
            }
            catch (Exception e) {
                logger.warn("Couldn't load config of module " + modules[i].toString() + "!");
                logger.warn(e);
            }
        }
        // Loading global config
        prefix = cfg.getOrDefault("prefix", ",");
        
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        
        System.out.println("Starting threads...");
        sa = new Date().getTime();
        
        // Starting thread to regularly save config
        ThreadManager.run(() -> {
            while (true) {
                try {
                    try {
                        // Saving config for modules
                        for (int i = 0; i < modules.length; i++) {
                            cfg.put(modules[i].getClass().getSimpleName(), modules[i].saveConfig());
                        }
                        // Saving global config
                        cfg.put("prefix", prefix);
                        
                        // Saving file
                        file.setContent(Utils.mapToString(cfg));
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
    }
}
