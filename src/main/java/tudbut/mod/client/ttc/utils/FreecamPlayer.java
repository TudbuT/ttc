package tudbut.mod.client.ttc.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.entity.MoverType;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.mods.Freecam;

@SideOnly(Side.CLIENT)
public class FreecamPlayer extends EntityOtherPlayerMP
{
    public MovementInput movementInput;
    protected Minecraft mc;
    protected final EntityPlayerSP original;
    
    public FreecamPlayer(EntityPlayerSP playerSP, World world)
    {
        super(world, playerSP.getGameProfile());
        this.dimension = playerSP.dimension;
        this.original = playerSP;
        this.mc = Minecraft.getMinecraft();
        this.movementInput = playerSP.movementInput;
        preparePlayerToSpawn();
        capabilities.allowFlying = true;
        capabilities.isFlying = true;
        this.setPositionAndRotation(playerSP.posX, playerSP.posY, playerSP.posZ, playerSP.rotationYaw, playerSP.rotationPitch);
    }
    
    @Override
    public boolean isSpectator() {
        return true;
    }
    
    public void onLivingUpdate()
    {
        if(TTC.mc.world == null) {
            Freecam.getInstance().onDisable();
            Freecam.getInstance().enabled = false;
            return;
        }
        TTC.mc.renderChunksMany = false;
        TTC.mc.player.setInvisible(false);
        setInvisible(true);
        
        inventory.copyInventory(TTC.player.inventory);
    
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
        prevRotationYawHead = rotationYawHead;
        setRotation(original.rotationYaw, original.rotationPitch);
        setRotationYawHead(original.rotationYaw);
        original.prevRenderArmYaw = original.renderArmYaw;
        original.prevRenderArmPitch = original.renderArmPitch;
        original.renderArmPitch = (float)((double)original.renderArmPitch + (double)(original.rotationPitch - original.renderArmPitch) * 0.5D);
        original.renderArmYaw = (float)((double)original.renderArmYaw + (double)(original.rotationYaw - original.renderArmYaw) * 0.5D);
        updateEntityActionState();
    
        movementInput.updatePlayerMoveState();
        Vec2f movementVec = movementInput.getMoveVector();
    
        float f1 = MathHelper.sin(rotationYaw * 0.017453292F);
        float f2 = MathHelper.cos(rotationYaw * 0.017453292F);
        double x = movementVec.x * f2 - movementVec.y * f1;
        double y = (movementInput.jump ? 1 : 0) + (movementInput.sneak ? -1 : 0);
        double z = movementVec.y * f2 + movementVec.x * f1;
        float d = (float) Math.sqrt(x * x + y * y + z * z);
    
        if(d < 1) {
            d = 1;
        }
    
        motionX = x / d;
        motionY = y / d;
        motionZ = z / d;
        
        noClip = true;
        move(MoverType.SELF, motionX, motionY, motionZ);
        
        prevCameraYaw = cameraYaw;
        prevCameraPitch = cameraPitch;
    }
}
