package tudbut.mod.client.ttc;

import de.tudbut.async.Task;
import de.tudbut.io.StreamReader;
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
import tudbut.mod.client.ttc.utils.*;
import tudbut.mod.client.ttc.utils.isbpl.UpdateManager;
import tudbut.obj.TLMap;
import tudbut.parsing.TCN;
import tudbut.tools.Lock;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

@Mod(modid = TTC.MODID, name = TTC.NAME, version = TTC.VERSION)
public class TTC {
    // TODO: PLEASE change this when skidding or rebranding.
    //  It is used for analytics and doesn't affect gameplay
    public static final String MODID = "ttc";
    public static final String NAME = "TTC Client";
    public static final String VERSION = "v1.4.0a";
    public static final String REPO = "TudbuT/ttc", BRANCH = "master";
    // Now change the values in mcmod.info, and you're done!
    
    // Registered modules, will make an api for it later
    public static Module[] modules;
    // Player and current World(/Dimension), updated regularly in FMLEventHandler
    public static EntityPlayerSP player;
    public static World world;
    // Current Minecraft instance running
    public static Minecraft mc = Minecraft.getMinecraft();
    // Config
    public static FileRW file;
    public static TCN globalConfig;
    public static Map<String, String> cfg;
    public static TLMap<String, String> obfMap = new TLMap<>();
    // Prefix for chat-commands
    public static String prefix = ",";
    
    // Logger, provided by Forge
    public static Logger logger;
    
    
    private static TTC instance;
    
    public static TTC getInstance() {
        return instance;
    }
    
    {instance = this;}
    
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
    
    static Boolean obfEnvCached;
    public static boolean isObfEnv() {
        if(obfEnvCached == null) {
            try {
                Minecraft.class.getDeclaredField("world");
                obfEnvCached = false;
            } catch (NoSuchFieldException e) {
                obfEnvCached = true;
            }
        }
        return obfEnvCached;
    }
    
    private Task<Void> createDeobfMap() {
        return new Task<>((res, rej) -> {
            if (!isObfEnv())
                return;
            try {
                String[] srg = new StreamReader(ClassLoader.getSystemResourceAsStream("minecraft_obf.srg")).readAllAsString().split("\n");
                
                for (int i = 0 ; i < srg.length ; i++) {
                    if (srg[i].isEmpty())
                        continue;
                    String[] srgLine = srg[i].split(" ");
                    if (srgLine[0].equals("FD:") || srgLine[0].equals("CL:")) {
                        String out = srgLine[1];
                        String in = srgLine[srgLine.length - 1];
                        obfMap.set(out, in);
                    }
                    if (srgLine[0].equals("MD:")) {
                        String out = srgLine[1];
                        String in = srgLine[3];
                        obfMap.set(out, in);
                    }
                }
                res.call(null);
            }
            catch (Exception e) {
                rej.call(e);
            }
        });
    }
    
    // Runs when all important info is loaded and all mods are pre-initialized,
    // most game objects exist already when this is called
    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("TTC by TudbuT");
        
        mc.gameSettings.autoJump = false; // Fuck AutoJump, disable it on startup
        
        long sa; // For time measurements
        
        System.out.println("Init...");
        sa = new Date().getTime();
        try {
            cfg = Utils.stringToMap(file.getContent().join("\n"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            globalConfig = TCN.read(Objects.requireNonNull(Utils.getRemote("global_config.tcn", true)));
        }
        catch (Exception e) {
            try {
                globalConfig = TCN.read("" +
                                        "\n" +
                                        "messages {\n" +
                                        "    update: true\n" +
                                        "}\n" +
                                        "\n" +
                                        "startup {\n" +
                                        "    show_credit: true\n" +
                                        "}");
            }
            catch (TCN.TCNException ignored) { }
        }
        while(!WebServices2.handshake());
        if(globalConfig.getSub("startup").getBoolean("show_credit")) {
            // Show the "TTC by TudbuT" message
            ThreadManager.run(() -> {
                JOptionPane.showMessageDialog(null, "TTC by TudbuT");
            });
        }
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
    
        System.out.println("Update checking...");
        sa = new Date().getTime();
        new UpdateManager().run();
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
                new Friend(),
                new TPATools(),
                new ChatSuffix(),
                new AutoConfig(),
                new ChatColor(),
                new PlayerLog(),
                new DMAll(),
                new DM(),
                new DMChat(),
                new Debug(),
                new AltControl(),
                new KillAura(),
                new CreativeFlight(),
                new ElytraFlight(),
                new ElytraBot(),
                new HUD(),
                new SeedOverlay(),
                new Velocity(),
                new Bright(),
                new Freecam(),
                new LSD(),
                new Bind(),
                new PopCount(),
                new PlayerSelector(),
                new Takeoff(),
                new Notifications(),
                new ClickGUI(),
                new Update(),
                new Msg(),
                new ISBPLModules(),
                new R(),
                new C(),
                new Password(),
        };
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        
        // Registering event handlers
        MinecraftForge.EVENT_BUS.register(new FMLEventHandler());
        for (int i = 0; i < modules.length; i++) {
            MinecraftForge.EVENT_BUS.register(modules[i]);
        }
        
        System.out.println("Loading config...");
        sa = new Date().getTime();
    
        // Loading config from config/ttc.cfg
        loadConfig();
        
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        
        System.out.println("Starting threads...");
        sa = new Date().getTime();
    
        boolean[] b = {true, true};
    
        // Starting thread to regularly save config
        Thread saveThread = ThreadManager.run(() -> {
            Lock lock = new Lock();
            while (b[0]) {
                lock.lock(1000);
                WebServices2.play();
                try {
                    // Only save if on main
                    if(AltControl.getInstance().mode != 1)
                        saveConfig();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                lock.waitHere();
            }
            b[1] = false;
        });
    
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            b[0] = false;
            Lock timer = new Lock();
            timer.lock(5000);
            while(saveThread.isAlive() && b[1] && timer.isLocked());
            if(AltControl.getInstance().mode != 1) {
                try {
                    saveConfig();
                    for (int i = 0 ; i < modules.length ; i++) {
                        modules[i].onDisable();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
    
        System.out.println("Initializing modules...");
        sa = new Date().getTime();
    
        for (int i = 0 ; i < modules.length ; i++) {
            modules[i].init();
        }
    
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
    }
    
    public void saveConfig() throws IOException {
        setConfig();
        
        // Saving file
        file.safeSetContent(Utils.mapToString(cfg));
    }
    
    public void setConfig() {
        // Saving config for modules
        for (int i = 0; i < modules.length; i++) {
            cfg.put(modules[i].getClass().getSimpleName(), modules[i].saveConfig());
        }
        // Saving global config
        cfg.put("prefix", prefix);
    }
    
    public void loadConfig() {
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
    }
    
    public static boolean isIngame() {
        return mc.world != null && mc.player != null ;
    }
    
    public static <T extends Module> T getModule(Class<? extends T> module) {
        for (int i = 0; i < modules.length; i++) {
            if(modules[i].getClass() == module) {
                return (T) modules[i];
            }
        }
        throw new RuntimeException();
    }
    
    public static void addModule(Module module) {
        ArrayList<Module> list = new ArrayList<>(Arrays.asList(modules));
        list.add(module);
        modules = list.toArray(new Module[0]);
    }
    
    public static void removeModule(Module module) {
        ArrayList<Module> list = new ArrayList<>(Arrays.asList(modules));
        list.remove(module);
        modules = list.toArray(new Module[0]);
    }
}
