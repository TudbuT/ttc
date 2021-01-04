package tudbut.mod.client.ttc.events;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import tudbut.mod.client.ttc.TTC;

import java.util.ArrayList;
import java.util.List;

public class ParticleLoop {
    
    static List<Particle> particleMap = new ArrayList<>();
    
    public static void register(Particle particle) {
        particleMap.add(particle);
    }
    
    public static void run() {
        Particle[] particles = particleMap.toArray(new Particle[0]);
    
        for (int i = 0; i < particles.length; i++) {
            if(particles[i].summon()) {
                Vec3d pos = particles[i].getPosition();
                if(TTC.mc.world != null)
                    TTC.mc.world.spawnParticle(particles[i].getType(), true, pos.x, pos.y, pos.z, 0, 0, 0);
            }
            else
                particleMap.remove(particles[i]);
        }
    }
    
    public interface Particle {
        boolean summon();
        EnumParticleTypes getType();
        Vec3d getPosition();
    }
}
