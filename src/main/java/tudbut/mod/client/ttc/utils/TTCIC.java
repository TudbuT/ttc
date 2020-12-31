package tudbut.mod.client.ttc.utils;

import java.util.Arrays;

public class TTCIC {
    
    public enum PacketsCS {
        INIT("init"),
        CLOSE("close"),
        LIST("list"),
        ;
        
        String msg = "TTCIC cs";
        String id;
        PacketsCS(String id) {
            this.id = id;
            msg += " " + id + " ";
        }
        
        public String run(String data) {
            return msg + data.replaceAll("%", "%0").replaceAll("\n", "%1");
        }
    }
    
    public interface PacketCS {
        PacketsCS packetType();
        String data();
    }
    
    public static PacketCS deserializeCS(String msg) throws NotTTCICException {
        try {
            String[] split = msg.split(" ");
            if (!split[0].equals("TTCIC")) {
                throw new Exception();
            }
            if (!split[1].equals("cs")) {
                throw new Exception();
            }
            PacketsCS packetType = Arrays.stream(PacketsCS.values()).filter(packetsCS -> packetsCS.id.equals(split[1])).findFirst().get();
            String data = msg.substring(("TTCIC " + split[2]).length() + 3).replaceAll("%1", "\n").replaceAll("%0", "%");
            return new PacketCS() {
                @Override
                public PacketsCS packetType() {
                    return packetType;
                }
    
                @Override
                public String data() {
                    return data;
                }
            };
        } catch (Exception e) {
            throw new NotTTCICException();
        }
    }
    
    public enum PacketsSC {
        INIT("init"),
        CLOSE("close"),
        LIST("list"),
        ;
        
        String msg = "TTCIC sc";
        String id;
        PacketsSC(String id) {
            this.id = id;
            msg += " " + id + " ";
        }
        
        public String run(String data) {
            return msg + data.replaceAll("%", "%0").replaceAll("\n", "%1");
        }
    }
    
    public interface PacketSC {
        PacketsSC packetType();
        String data();
    }
    
    public static PacketSC deserializeSC(String msg) throws NotTTCICException {
        try {
            String[] split = msg.split(" ");
            if (!split[0].equals("TTCIC")) {
                throw new Exception();
            }
            if (!split[1].equals("sc")) {
                throw new Exception();
            }
            PacketsSC packetType = Arrays.stream(PacketsSC.values()).filter(packetsSC -> packetsSC.id.equals(split[1])).findFirst().get();
            String data = msg.substring(("TTCIC " + split[2]).length() + 3).replaceAll("%1", "\n").replaceAll("%0", "%");
            return new PacketSC() {
                @Override
                public PacketsSC packetType() {
                    return packetType;
                }
                
                @Override
                public String data() {
                    return data;
                }
            };
        } catch (Exception e) {
            throw new NotTTCICException();
        }
    }
    
    public static class NotTTCICException extends Exception {
    
    }
}
