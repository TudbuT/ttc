package tudbut.mod.client.ttc.utils;

import tudbut.net.ic.PBIC;

public class TTCIC {
    
    public enum PacketsSC {
        INIT,
        NAME,
        UUID,
        TPA,
        EXECUTE,
        LIST,
        KILL,
        FOLLOW,
        STOP,
        CONFIG,
        WALK,
        ELYTRA,
        
        ;
    }
    
    public enum PacketsCS {
        NAME,
        UUID,
        
        ;
    }
    
    public interface PacketSC {
        PacketsSC type();
        String content();
    }
    public interface PacketCS {
        PacketsCS type();
        String content();
    }
    
    public static PBIC.Packet getPacketSC(PacketsSC packetType, String content) {
        return () -> packetType.name() + " " + content;
    }
    
    public static PBIC.Packet getPacketCS(PacketsCS packetType, String content) {
        return () -> packetType.name() + " " + content;
    }
    
    public static PacketSC getPacketSC(PBIC.Packet packet) {
        String content = packet.getContent();
        PacketsSC type;
        type = PacketsSC.valueOf(content.split(" ")[0]);
        content = content.substring(content.indexOf(" ") + 1);
    
        String finalContent = content;
        return new PacketSC() {
            @Override
            public PacketsSC type() {
                return type;
            }
    
            @Override
            public String content() {
                return finalContent;
            }
        };
    }
    
    public static PacketCS getPacketCS(PBIC.Packet packet) {
        String content = packet.getContent();
        PacketsCS type;
        type = PacketsCS.valueOf(content.split(" ")[0]);
        content = content.substring(content.indexOf(" ") + 1);
    
        String finalContent = content;
        return new PacketCS() {
            @Override
            public PacketsCS type() {
                return type;
            }
        
            @Override
            public String content() {
                return finalContent;
            }
        };
    }
    
}
