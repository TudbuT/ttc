package tudbut.mod.client.ttc.utils;

import org.lwjgl.input.Keyboard;
import tudbut.mod.client.ttc.gui.GuiTTC;

import java.lang.reflect.Field;
import java.util.Objects;

public class Setting {
    
    public static GuiTTC.Button createInt(int min, int max, int step, String string, Module module, String field, Runnable onClick) {
        final int[] locVal = {(Integer) field(module, field)};
        return new GuiTTC.Button(
                string.replaceAll("\\$val", String.valueOf(locVal[0])),
                text -> {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                        locVal[0] -= step;
                    }
                    else {
                        locVal[0] += step;
                    }
                    
                    if(locVal[0] < min) {
                        locVal[0] = max;
                    }
                    if(locVal[0] > max) {
                        locVal[0] = min;
                    }
                    
                    
                    field(module, field, locVal[0]);
                    text.set(string.replaceAll("\\$val", String.valueOf(locVal[0])));
                    onClick.run();
                }
        );
    }
    
    public static GuiTTC.Button createInt(int min, int max, int step, String string, Module module, String field) {
        return Setting.createInt(min, max, step, string, module, field, () -> {});
    }
    
    public static GuiTTC.Button createFloat(float min, float max, float step, String string, Module module, String field) {
        final float[] locVal = {(Float) field(module, field)};
        return new GuiTTC.Button(
                string.replaceAll("\\$val", String.valueOf(locVal[0])),
                text -> {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                        locVal[0] -= step;
                    }
                    else {
                        locVal[0] += step;
                    }
                    
                    if(locVal[0] < min) {
                        locVal[0] = max;
                    }
                    if(locVal[0] > max) {
                        locVal[0] = min;
                    }
                    
                    field(module, field, locVal[0]);
                    text.set(string.replaceAll("\\$val", String.valueOf(locVal[0])));
                }
        );
    }
    
    public static GuiTTC.Button createSecureFloat(int min, int max, int step, int dec, String string, Module module, String field) {
        final int[] locVal = {(int) ((Float) field(module, field) * dec)};
        return new GuiTTC.Button(
                string.replaceAll("\\$val", String.valueOf((float) locVal[0] / dec)),
                text -> {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                        locVal[0] -= step;
                    }
                    else {
                        locVal[0] += step;
                    }
                    
                    if(locVal[0] < min) {
                        locVal[0] = max;
                    }
                    if(locVal[0] > max) {
                        locVal[0] = min;
                    }
                    
                    field(module, field, (float) locVal[0] / dec);
                    text.set(string.replaceAll("\\$val", String.valueOf((float) locVal[0] / dec)));
                }
        );
    }
    
    public static GuiTTC.Button createBoolean(String string, Module module, String field) {
        final boolean[] locVal = {(Boolean) field(module, field)};
        return new GuiTTC.Button(
                string.replaceAll("\\$val", String.valueOf(locVal[0])),
                text -> {
                    locVal[0] = !locVal[0];
                    
                    field(module, field, locVal[0]);
                    text.set(string.replaceAll("\\$val", String.valueOf(locVal[0])));
                }
        );
    }
    
    public static GuiTTC.Button createKey(String string, Module.KeyBind keyBind) {
        return new GuiTTC.Button(
                string.replaceAll("\\$val", keyBind.key == null ? "NONE" : Keyboard.getKeyName(keyBind.key)),
                text -> {
                    int i;
                    if ((i = getKeyPress()) != -1) {
                        keyBind.key = i;
                        text.set(string.replaceAll("\\$val", Keyboard.getKeyName(keyBind.key)));
                    }
                    else {
                        keyBind.key = null;
                        text.set(string.replaceAll("\\$val", "NONE (Press while clicking)"));
                        new Thread(() -> {
                            try {
                                Thread.sleep(3000);
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            text.set(string.replaceAll("\\$val", "NONE"));
                        }).start();
                    }
                }
        );
    }
    
    private static int getKeyPress() {
        for (int i = 0 ; i < 256 ; i++) {
            if(Keyboard.isKeyDown(i))
                return i;
        }
        return -1;
    }
    
    private static Object field(Module m, String s) {
        try {
            Field f = m.getClass().getDeclaredField(s);
            f.setAccessible(true);
            return f.get(m);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void field(Module m, String s, Object o) {
        try {
            Field f = m.getClass().getDeclaredField(s);
            f.setAccessible(true);
            f.set(m, o);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
