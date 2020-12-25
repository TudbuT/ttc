package tudbut.mod.client.ttc.utils;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    
    public static Map<String, String> stringToMap(String mapStringParsable) {
        HashMap<String, String> map = new HashMap<>();
        
        for (int i = 0; i < mapStringParsable.split(";").length; i++) {
            String tile = mapStringParsable.split(";")[i];
            if (tile.contains(":")) {
                if (tile.split(":").length == 2)
                    map.put(
                            tile.split(":")[0].replaceAll("%I", ":").replaceAll("%B", ";").replaceAll("%P", "%"),
                            tile.split(":")[1].equals("%N") ? null : tile.split(":")[1].replaceAll("%I", ":").replaceAll("%B", ";").replaceAll("%P", "%")
                    );
                else
                    map.put(tile.split(":")[0].replaceAll("%I", ":").replaceAll("%B", ";").replaceAll("%P", "%"), "");
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
}
