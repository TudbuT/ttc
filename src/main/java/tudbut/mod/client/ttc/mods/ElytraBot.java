package tudbut.mod.client.ttc.mods;

import de.tudbut.type.Vector2d;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.gui.GuiTTC;
import tudbut.mod.client.ttc.utils.ChatUtils;
import tudbut.mod.client.ttc.utils.FlightBot;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.ThreadManager;
import tudbut.obj.Atomic;
import tudbut.rendering.Maths2D;

import static tudbut.mod.client.ttc.utils.Tesselator.*;
import static tudbut.mod.client.ttc.utils.Tesselator.put;

public class ElytraBot extends Module {
    
    static ElytraBot bot;
    
    {
        bot = this;
    }
    
    public static ElytraBot getInstance() {
        return bot;
    }
    
    
    
    
    
    Atomic<Vec3d> dest = new Atomic<>();
    double orbitRotation = 0.1;
    private static final double PI_TIMES_TWO = Math.PI * 2;
    private static final Vector2d zeroZero = new Vector2d(0,0);
    private Vector2d original = zeroZero.clone();
    
    int task = -1;
    
    {updateButtons();}
    
    public void updateButtons() {
        subButtons.clear();
        if(task == -1 && !FlightBot.isActive()) {
            subButtons.add(new GuiTTC.Button("Mode", text -> displayModeMenu()));
        }
        else
            subButtons.add(new GuiTTC.Button("Stop", text -> {
                FlightBot.deactivate();
                task = -1;
                orbitRotation = 0.1;
                updateButtons();
            }));
    }
    
    public void displayModeMenu() {
        subButtons.clear();
        subButtons.add(new GuiTTC.Button("Back", text -> {
            updateButtons();
        }));
        subButtons.add(new GuiTTC.Button("Orbit spawn", text -> {
            original = zeroZero.clone();
            startOrbitSpawn();
            updateButtons();
        }));
        subButtons.add(new GuiTTC.Button("Orbit spawn from here", text -> {
            original = new Vector2d(Math.sqrt(TTC.player.posX*TTC.player.posX + TTC.player.posZ*TTC.player.posZ), 0);
            startOrbitSpawn();
            updateButtons();
        }));
    }
    
    public void startOrbitSpawn() {
        dest.set(new Vec3d(original.getX(), 260.1, original.getY()));
        FlightBot.deactivate();
        FlightBot.activate(dest);
        ChatUtils.chatPrinterDebug().println("Now flying to " + original);
        task = 0;
    }
    
    public void tickOrbitSpawn() {
        if(!FlightBot.isFlying() && !isRising) {
            Vector2d point = original.clone().add(orbitRotation * 5 * 16, 0);
            orbitRotation += (5 / (point.getX() * PI_TIMES_TWO));
            Maths2D.rotate(point, zeroZero, orbitRotation * PI_TIMES_TWO);
            Vec3d vec = dest.get();
            dest.set(new Vec3d(point.getX(), 260.1, point.getY()));
            ChatUtils.chatPrinterDebug().println("Distance traveled: " + (vec.distanceTo(dest.get())) +  "... Now flying to " + point);
        }
    }
    
    Atomic<Vec3d> theDest = new Atomic<>();
    
    @Override
    public void onEveryTick() {
        if(TTC.mc.world == null) {
            return;
        }
        EntityPlayerSP player = TTC.player;
    
        
        if(player.posY >= 260) {
            switch (task) {
                case -1:
                    break;
                case 0:
                    tickOrbitSpawn();
                    break;
                case 1:
                    FlightBot.updateDestination(dest);
                    if (!FlightBot.isFlying() && !isRising) {
                        task = -1;
                        FlightBot.deactivate();
                        updateButtons();
                    }
                    break;
            }
        }
        if (task != -1 && FlightBot.isActive()) {
            rise(260);
        }
    }
    
    boolean isRising = false;
    
    public void flyTo(BlockPos pos) {
        task = 1;
        FlightBot.deactivate();
        FlightBot.deactivate();
        dest.set(new Vec3d(pos.getX(), 260, pos.getZ()));
        FlightBot.activate(dest);
        updateButtons();
    }
    
    public void rise(double pos) {
        EntityPlayerSP player = TTC.player;
        
        if(!FlightBot.isActive()) {
            isRising = false;
            return;
        }
        
        if(player.posY < pos) {
            FlightBot.updateDestination(new Atomic<>(new Vec3d(player.posX, pos, player.posZ)));
            isRising = true;
        } else if(isRising) {
            FlightBot.updateDestination(dest);
            isRising = false;
        }
    }
    
    @Override
    public void onChat(String s, String[] args) {
        if(TTC.mc.world == null) {
            return;
        }
        
        if(task != -1 || FlightBot.isActive()) {
            ChatUtils.print("You have to stop your current task first.");
            return;
        }
    
        if(args.length == 2) {
            flyTo(new BlockPos(Integer.parseInt(args[0]), 260, Integer.parseInt(args[1])));
            ChatUtils.print("Flying...");
        }
        updateButtons();
    }
    
    @Override
    public int danger() {
        return 2;
    }
    
    @Override
    public void updateConfig() {
    }
    
    @Override
    public void loadConfig() {
    }
}
