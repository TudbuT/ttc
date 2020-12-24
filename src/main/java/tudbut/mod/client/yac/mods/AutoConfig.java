package tudbut.mod.client.yac.mods;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.yac.gui.GuiYAC;
import tudbut.mod.client.yac.utils.Module;
import tudbut.mod.client.yac.utils.ThreadManager;

public class AutoConfig extends Module {
    
    private boolean mode = false;
    
    private boolean stackedTots = false;
    private boolean pvp = false;
    private boolean tpa = false;
    
    private Server server = Server._8b8t;
    
    private enum Server {
        _8b8t
                ("8b8t.xyz",
                 true , true , true ),
        
        _5b5t
                ("5b5t.net",
                 false, false, false),
        
        _0t0t
                ("0b0t.org",
                 false, false, true ),
        
        _2b2t
                ("2b2t.org",
                 false, false, false),
        
        crystalpvp
                ("crystalpvp.cc",
                 false, false, false),
        
        ;
        
        String name;
        boolean stackedTots;
        boolean pvp;
        boolean tpa;
        
        Server(String name, boolean stackedTots, boolean pvp, boolean tpa) {
            this.name = name;
            this.stackedTots = stackedTots;
            this.pvp = pvp;
            this.tpa = tpa;
        }
    }
    
    @Override
    public void onEnable() {
        updateButtons();
    }
    
    public void updateButtons() {
        subButtons.clear();
        subButtons.add(new GuiYAC.Button("Mode: " + (mode ? "Server" : "Custom"), text -> {
            mode = !mode;
            text.set("Mode: " + (mode ? "Server" : "Custom"));
            updateButtons();
        }));
        if(mode) {
            subButtons.add(new GuiYAC.Button("Server: " + server.name, text -> {
                int i = server.ordinal();
                
                if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                    i--;
                else
                    i++;
                
                if(i >= Server.values().length)
                    i = 0;
                if(i < 0)
                    i = Server.values().length - 1;
                
                server = Server.values()[i];
                
                text.set("Server: " + server.name);
            }));
        }
        else {
            subButtons.add(new GuiYAC.Button("Has stacked totems: " + stackedTots, text -> {
                stackedTots = !stackedTots;
                text.set("Has stacked totems: " + stackedTots);
            }));
            subButtons.add(new GuiYAC.Button("PvP meta: " + (pvp ? "32k" : "Crystal"), text -> {
                pvp = !pvp;
                text.set("PvP meta: " + (pvp ? "32k" : "Crystal"));
            }));
            subButtons.add(new GuiYAC.Button("Has /tpa: " + tpa, text -> {
                tpa = !tpa;
                text.set("Has /tpa: " + tpa);
            }));
        }
        subButtons.add(new GuiYAC.Button("Set", text -> {
            if(mode) {
                stackedTots = server.stackedTots;
                pvp = server.pvp;
                tpa = server.tpa;
            }
            int i = 0;
            if(stackedTots) {
                i += (pvp ? 4 : 2);
            }
            AutoTotem.getInstance().orig_min_count = i;
            
            Team.getInstance().enabled = tpa;
            if(!tpa)
                TPAParty.getInstance().enabled = false;
            TPATools.getInstance().enabled = tpa;
    
            ThreadManager.run(() -> {
                text.set("Done");
                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                text.set("Set");
            });
        }));
    }
    
    @Override
    public void onTick() {
    
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
    
    @Override
    public void updateConfig() {
        cfg.put("mode", mode + "");
        
        cfg.put("stackedTotems", stackedTots + "");
        cfg.put("pvp", pvp + "");
        cfg.put("tpa", tpa + "");
        
        cfg.put("server", server.ordinal() + "");
    }
    
    @Override
    public void loadConfig() {
        mode = Boolean.parseBoolean(cfg.get("mode"));
        
        stackedTots = Boolean.parseBoolean(cfg.get("stackedTotems"));
        pvp = Boolean.parseBoolean(cfg.get("pvp"));
        tpa = Boolean.parseBoolean(cfg.get("tpa"));
        
        server = Server.values()[Integer.parseInt(cfg.get("server"))];
        
        updateButtons();
    }
}
