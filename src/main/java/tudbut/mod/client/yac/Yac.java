package tudbut.mod.client.yac;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import tudbut.mod.client.yac.events.FMLEventHandler;
import tudbut.mod.client.yac.mods.AutoTotem;
import tudbut.mod.client.yac.mods.ClickGUI;
import tudbut.mod.client.yac.mods.Prefix;
import tudbut.mod.client.yac.mods.TPAParty;
import tudbut.mod.client.yac.utils.FileRW;
import tudbut.mod.client.yac.utils.Module;
import tudbut.mod.client.yac.utils.ThreadManager;
import tudbut.mod.client.yac.utils.Utils;

import javax.swing.*;
import java.io.IOException;
import java.util.Map;

@Mod(modid = Yac.MODID, name = Yac.NAME, version = Yac.VERSION)
public class Yac {
    public static final String MODID = "yac";
    public static final String NAME = "YAC Client";
    public static final String VERSION = "vB0.2.0b";
    
    public static Module[] modules ;
    public static EntityPlayerSP player;
    public static Minecraft mc = Minecraft.getMinecraft();
    public static FileRW file;
    public static Map<String, String> cfg;
    public static String prefix = ",";

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        try {
            file = new FileRW("config/yac.cfg");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("YAC by TudbuT, public version");
        ThreadManager.run(() -> {
            JOptionPane.showMessageDialog(null, "YAC by TudbuT, public version");
        });
        player = Minecraft.getMinecraft().player;
        try {
            cfg = Utils.stringToMap(file.getContent());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        modules = new Module[] {
                new AutoTotem(),
                new TPAParty(),
                new Prefix(),
                new ClickGUI()
        };
        MinecraftForge.EVENT_BUS.register(new FMLEventHandler());
        
        for (int i = 0; i < modules.length; i++) {
            logger.info(modules[i].toString());
            if(cfg.containsKey(modules[i].toString())) {
                logger.info(modules[i].toString());
                logger.info(cfg);
                modules[i].loadConfig(Utils.stringToMap(cfg.get(modules[i].getClass().getSimpleName())));
            }
        }
        prefix = cfg.getOrDefault("prefix", ",");
    
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    
        ThreadManager.run(() -> {
            while (true) {
                try {
                    try {
                        for (int i = 0; i < modules.length; i++) {
                            cfg.put(modules[i].getClass().getSimpleName(), modules[i].saveConfig());
                        }
                        cfg.put("prefix", prefix);
        
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
    }
}
