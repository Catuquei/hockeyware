package org.hockey.hockeyware.client.events.render;

import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class RenderParticleEvent extends Event {

    // particle type
    private final EnumParticleTypes particleTypes;

    public RenderParticleEvent(EnumParticleTypes particleTypes) {
        this.particleTypes = particleTypes;
    }

    /**
     * Gets the particle type of the spawned particle
     * @return The particle type of the spawned particle
     */
    public EnumParticleTypes getParticleType() {
        return particleTypes;
    }
}