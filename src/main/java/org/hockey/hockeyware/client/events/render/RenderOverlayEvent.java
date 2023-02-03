package org.hockey.hockeyware.client.events.render;

import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class RenderOverlayEvent extends Event {

    // HUD overlay
    private final RenderBlockOverlayEvent.OverlayType overlayType;

    public RenderOverlayEvent(RenderBlockOverlayEvent.OverlayType overlayType) {
        this.overlayType = overlayType;
    }

    public RenderBlockOverlayEvent.OverlayType getOverlayType() {
        return overlayType;
    }
}