package tudbut.mod.client.ttc.utils;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import tudbut.mod.client.ttc.TTC;
import tudbut.mod.client.ttc.mods.Freecam;
import tudbut.mod.client.ttc.mods.LSD;

import java.util.Objects;

public class LSDRenderer extends FreecamPlayer {
    public static final int MODE_EPILEPSY = 0x00;
    public static final int MODE_UPSIDE_DOWN = 0x01;
    public static final int MODE_HAND0 = 0x02;
    public static final int MODE_HAND1 = 0x03;
    public static final int MODE_CAMERA = 0x04;
    public static final int MODE_ROTATION0 = 0x05;
    public static final int MODE_ROTATION1 = 0x06;
    public static final int MODE_ROTATION2 = 0x07;
    public static final int MODE_ROTATION3 = 0x08;
    public static final int MODE_EXC = 0x09;
    public static final int MODE_ALL = 0x0a;
    public static int mode = 0;
    
    public LSDRenderer(EntityPlayerSP playerSP, World world) {
        super(playerSP, world);
    }
    
    public void onLivingUpdate()
    {
        if(!TTC.isIngame()) {
            LSD.getInstance().onDisable();
            return;
        }
    
        inventory.copyInventory(TTC.player.inventory);
    
        original.renderArmYaw = original.rotationYaw;
        original.renderArmPitch = original.rotationPitch;
        original.prevRenderArmYaw = prevRotationYaw;
        original.prevRenderArmPitch = prevRotationPitch;
        setRotation(original.rotationYaw, original.rotationPitch);
        cameraYaw = 0;
        cameraPitch = 0;
        prevCameraYaw = 0;
        prevCameraPitch = 0;
        
        switch (mode) {
            case MODE_ALL:
                exc();
                hand1();
                rotation3();
                epilepsy();
                break;
            case MODE_EPILEPSY:
                epilepsy();
                break;
            case MODE_HAND0:
                hand0();
                break;
            case MODE_HAND1:
                hand1();
                break;
            case MODE_CAMERA:
                camera();
                break;
            case MODE_ROTATION0:
                rotation0();
                break;
            case MODE_ROTATION1:
                rotation1();
                break;
            case MODE_UPSIDE_DOWN:
            case MODE_ROTATION2:
                rotation2();
                break;
            case MODE_ROTATION3:
                rotation3();
                break;
            case MODE_EXC:
                exc();
                break;
        }
        setRotationYawHead(-original.rotationYaw);
        updateEntityActionState();
        
        noClip = true;
    }
    
    public void epilepsy() {
        rotationPitch = 0;
        cameraYaw = (float) (5f - (Math.random() * 10f));
        cameraPitch = (float) (5f - (Math.random() * 10f));
        prevCameraYaw = (float) (5f - (Math.random() * 10f));
        prevCameraPitch = (float) (5f - (Math.random() * 10f));
    }
    public void hand0() {
        original.renderArmYaw = 0;
        original.renderArmPitch = 0;
        original.prevRenderArmYaw = 0;
        original.prevRenderArmPitch = 0;
    }
    public void hand1() {
        original.renderArmYaw = original.rotationYaw;
        original.renderArmPitch = original.rotationPitch;
        original.prevRenderArmYaw = 0;
        original.prevRenderArmPitch = 0;
    }
    public void camera() {
        cameraYaw = (float) (rotationYaw / 180 * Math.PI);
        cameraPitch = (float) (rotationPitch / 180 * Math.PI);
        prevCameraYaw = cameraYaw;
        prevCameraPitch = cameraPitch;
    }
    public void exc() {
        rotationYaw = rotationYaw - 90;
        rotationPitch = rotationPitch - 90;
    }
    public void rotation0() {
        rotationYaw = rotationYaw - 90;
        rotationPitch = rotationPitch - 90;
    }
    public void rotation1() {
        rotationYaw = original.rotationYaw + 90;
        rotationPitch = original.rotationPitch + 90;
    }
    public void rotation2() {
        rotationYaw = original.rotationYaw + 180;
        rotationPitch = original.rotationPitch + 180;
    }
    public void rotation3() {
        rotationYaw = original.rotationYaw - 180;
        rotationPitch = original.rotationPitch - 180;
    }
}
