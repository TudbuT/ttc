package tudbut.mod.client.ttc.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import de.tudbut.tools.Hasher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.Entity;
import tudbut.net.http.HTTPContentType;
import tudbut.net.http.HTTPHeader;
import tudbut.net.http.HTTPRequest;
import tudbut.net.http.HTTPRequestType;
import tudbut.mod.client.ttc.TTC;
import tudbut.tools.encryption.Key;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class Utils { // A bunch of utils that don't deserve their own class, self-explanatory
    
    public static Object getPrivateField(Class<?> clazz, Object instance, String field) {
        try {
            Object t;
            Field f = clazz.getDeclaredField(field);
            boolean b = f.isAccessible();
            f.setAccessible(true);
            t = f.get(instance);
            f.setAccessible(b);
            return t;
        } catch (Exception e) {
            return null;
        }
    }
    public static boolean setPrivateField(Class<?> clazz, Object instance, String field, Object content) {
        try {
            Field f = clazz.getDeclaredField(field);
            boolean b = f.isAccessible();
            f.setAccessible(true);
            f.set(instance, content);
            f.setAccessible(b);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static String[] getFieldsForType(Class<?> clazz, Class<?> type) {
        try {
            Field[] all = clazz.getDeclaredFields();
            ArrayList<String> names = new ArrayList<>();
            for (int i = 0; i < all.length; i++) {
                if(all[i].getType() == type) {
                    names.add(all[i].getName());
                }
            }
            return names.toArray(new String[0]);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static <T> Entity[] getEntities(Class<? extends T> entityType, Predicate<? super T> filter) {
        List<T> list = Lists.<T>newArrayList();
        
        List<Entity> loadedEntityList = TTC.world.loadedEntityList;
        for (int i = 0; i < loadedEntityList.size(); i++) {
            Entity entity4 = loadedEntityList.get(i);
            if (entityType.isAssignableFrom(entity4.getClass()) && filter.apply((T) entity4)) {
                list.add((T) entity4);
            }
        }
        
        return list.toArray(new Entity[0]);
    }
    
    public static String removeNewlines(String s) {
        if (s == null)
            return null;
        return s.replaceAll("\n", "").replaceAll("\r", "");
    }
    
    // Get a file from the GIT repo, the master argument indicates if it should always be taken from
    // the newest version, or if it should always use the one for the current version
    public static String getRemote(String file, boolean master) {
        try {
            URL updateCheckURL = new URL("https://raw.githubusercontent.com/TudbuT/ttc/" + (master ? "master" : TTC.VERSION) + "/" + file);
            InputStream stream = updateCheckURL.openConnection().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            
            String s;
            StringBuilder builder = new StringBuilder();
            while ((s = reader.readLine()) != null) {
                builder.append(s).append("\n");
            }
            
            return builder.toString();
        }
        catch (IOException ignore) { }
        return null; // No internet access
    }
    
    // Transforms Integer[] to int[]
    public static int[] objectArrayToNativeArray(Integer[] oa) {
        // Create the int array tp copy to
        int[] na = new int[oa.length];
        
        // Convert the integers one by one
        for (int i = 0; i < na.length; i++) {
            na[i] = oa[i];
        }
        
        return na;
    }
    
    public static int[] range(int min, int max) {
        int[] r = new int[max - min];
        for (int i = min, j = 0; i < max; i++, j++) {
            r[j] = i;
        }
        return r;
    }
    
    public static int[] add(int[] array0, int[] array1) {
        int[] r = new int[array0.length + array1.length];
        System.arraycopy(array0, 0, r, 0, array0.length);
        System.arraycopy(array1, 0, r, 0 - array0.length, array1.length);
        return r;
    }
    
    public static Map<String, String> stringToMap(String mapStringParsable) {
        HashMap<String, String> map = new HashMap<>();
        
        String[] splitTiles = mapStringParsable.split(";");
        for (int i = 0; i < splitTiles.length; i++) {
            String tile = splitTiles[i];
            String[] splitTile = tile.split(":");
            if (tile.contains(":")) {
                if (splitTile.length == 2)
                    map.put(
                            splitTile[0].replaceAll("%I", ":").replaceAll("%B", ";").replaceAll("%P", "%"),
                            splitTile[1].equals("%N") ? null : splitTile[1].replaceAll("%I", ":").replaceAll("%B", ";").replaceAll("%P", "%")
                    );
                else
                    map.put(splitTile[0].replaceAll("%I", ":").replaceAll("%B", ";").replaceAll("%P", "%"), "");
            }
        }
        
        return map;
    }
    
    public static String mapToString(Map<String, String> map) {
        StringBuilder r = new StringBuilder();
        
        for (String key : map.keySet().toArray(new String[0])) {
            
            r
                    .append(key.replaceAll("%", "%P").replaceAll(";", "%B").replaceAll(":", "%I"))
                    .append(":")
                    .append(map.get(key) == null ? "%N" : map.get(key).replaceAll("%", "%P").replaceAll(";", "%B").replaceAll(":", "%I"))
                    .append(";")
            ;
        }
        
        return r.toString();
    }
    
    
    public static Method[] getMethods(Class<GuiIngame> clazz, Class<?>... args) {
        ArrayList<Method> methods = new ArrayList<>();
        
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (int i = 0 ; i < declaredMethods.length ; i++) {
            Method m = declaredMethods[i];
            if(Arrays.equals(m.getParameterTypes(), args)) {
                methods.add(m);
            }
        }
        
        return methods.toArray(new Method[0]);
    }
}
