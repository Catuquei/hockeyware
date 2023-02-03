package org.hockey.hockeyware.client.events;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class PacketEvent extends Event {
    private final Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public <T extends Packet<?>> T getPacket() {
        return (T) packet;
    }

    public static class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            super(packet);
        }
    }
    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet);
        }
    }
    @Cancelable
    public static class PacketReceiveEvent extends PacketEvent {
        public PacketReceiveEvent(Packet<?> packet) {
            super(packet);
        }
    }

    @Cancelable
    public static class PacketSendEvent extends PacketEvent {
        public PacketSendEvent(Packet<?> packet) {
            super(packet);
        }
    }
}