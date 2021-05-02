package tudbut.mod.client.ttc.gui;

import de.tudbut.type.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.mods.Notifications;
import tudbut.mod.client.ttc.mods.PlayerSelector;
import tudbut.mod.client.ttc.utils.FontRenderer;
import tudbut.mod.client.ttc.utils.Module;
import tudbut.mod.client.ttc.utils.Utils;
import tudbut.obj.Vector2i;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GuiTTCIngame extends Gui {
    
    static FontRenderer fontRenderer = new FontRenderer(6);
    
    public static void draw() {
        new GuiTTCIngame().drawImpl();
    }
    
    public static void drawOffhandSlot(int x, int y) {
        new GuiTTCIngame().drawOffhandSlot0(x,y);
    }
    
    public void drawOffhandSlot0(int x, int y) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        TTC.mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/widgets.png"));
        drawTexturedModalRect(x, y, 24, 22, 29, 24);
    }
    
    public static void drawItem(int x, int y, float partialTicks, EntityPlayer player, ItemStack stack) {
        Method m = Utils.getMethods(GuiIngame.class, int.class, int.class, float.class, EntityPlayer.class, ItemStack.class)[0];
        m.setAccessible(true);
        try {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderHelper.enableGUIStandardItemLighting();
            m.invoke(Minecraft.getMinecraft().ingameGUI, x, y, partialTicks, player, stack);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    public void drawImpl() {
        ScaledResolution sr = new ScaledResolution(TTC.mc);
        Vector2i screenSize = new Vector2i(sr.getScaledWidth(), sr.getScaledHeight());
    
        int y = sr.getScaledHeight() - (5 + TTC.mc.fontRenderer.FONT_HEIGHT);
        int x = screenSize.getX() - 5;
    
        if(!TTC.isIngame())
            return;
    
        y = drawPos(TTC.player, "Player", x, y);
        if(TTC.mc.getRenderViewEntity() != TTC.player)
            y = drawPos(TTC.mc.getRenderViewEntity(), "Camera", x, y);
        
        y-=10;
        
        for (int i = 0; i < TTC.modules.length; i++) {
            Module module = TTC.modules[i];
            
            if(module.enabled && module.displayOnClickGUI()) {
                int color = 0x000000;
                
                switch (module.danger()) {
                    case 0:
                        color = 0x00ff00;
                        break;
                    case 1:
                        color = 0x80ff00;
                        break;
                    case 2:
                        color = 0xffff00;
                        break;
                    case 3:
                        color = 0xff8000;
                        break;
                    case 4:
                        color = 0xff0000;
                        break;
                    case 5:
                        color = 0xff00ff;
                        break;
                }
                
                
                drawString(module.toString(), x, y, color);
                y-=10;
            }
        }
    
        Notifications notifications = TTC.getModule(Notifications.class);
        if(notifications.enabled) {
            x = sr.getScaledWidth() / 2 - (300 / 2);
            y = sr.getScaledHeight() / 4;
        
            Notifications.Notification[] notifs = Notifications.getNotifications().toArray(new Notifications.Notification[0]);
            for (int i = 0; i < notifs.length; i++) {
                drawRect(x, y, x + 300, y + 30, 0x80202040);
                drawStringL(notifs[i].text, x + 10, y + (15 - (9 / 2)), 0xffffffff);
                y -= 35;
            }
        }
    
        if(TTC.getModule(PlayerSelector.class).enabled) {
            PlayerSelector.render();
        }
    }
    
    private void drawString(String s, int x, int y, int color) {
        drawString(
                TTC.mc.fontRenderer,
                s,
                x - TTC.mc.fontRenderer.getStringWidth(s),
                y,
                color
        );
    }
    
    private void drawStringL(String s, int x, int y, int color) {
        drawString(
                TTC.mc.fontRenderer,
                s,
                x,
                y,
                color
        );
    }
    
    private int drawPos(Entity e, String s, int x, int y) {
        Vector3d p = new Vector3d(e.posX, e.posY, e.posZ);
        
        p.setX(Math.round(p.getX() * 10d) / 10d);
        p.setY(Math.round(p.getY() * 10d) / 10d);
        p.setZ(Math.round(p.getZ() * 10d) / 10d);
    
        if(TTC.mc.world.provider.getDimension() == -1)
            drawString(
                    s + " Overworld " + Math.round(p.getX() * 8 * 10d) / 10d + " " + Math.round(p.getY() * 8 * 10d) / 10d + " " + Math.round(p.getZ() * 8 * 10d) / 10d,
                    x, y, 0xff00ff00
            );
        if(TTC.mc.world.provider.getDimension() == 0)
            drawString(
                    s + " Nether " + Math.round(p.getX() / 8 * 10d) / 10d + " " + Math.round(p.getY() / 8 * 10d) / 10d + " " + Math.round(p.getZ() / 8 * 10d) / 10d,
                    x, y, 0xff00ff00
            );
        y -= 10;
        drawString(s + " " + p.getX() + " " + p.getY() + " " + p.getZ(), x, y, 0xff00ff00);
        return y - 10;
    }
}
