package org.hockey.hockeyware.client.manager;

import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.hockey.hockeyware.client.HockeyWare;
import org.hockey.hockeyware.client.events.PacketEvent;
import org.hockey.hockeyware.client.mixin.mixins.accessor.IMinecraft;
import org.hockey.hockeyware.client.mixin.mixins.accessor.ITimer;
import org.hockey.hockeyware.client.util.Globals;
import org.hockey.hockeyware.client.util.math.MathUtil;

/**
 * @author Rigamortis, linustouchtips
 * @since 06/08/2021
 */
public class TickManager implements Globals {

    // array of last 20 TPS calculations
    private final float[] TPS = new float[20];

    // time
    private long prevTime;
    private int currentTick;

    public TickManager() {
        prevTime = -1;

        // initialize an empty array
        for (int i = 0, len = TPS.length; i < len; i++) {
            TPS[i] = 0;
        }

        HockeyWare.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (event.getPacket() instanceof SPacketTimeUpdate) {
            if (prevTime != -1) {
                // update our TPS
                TPS[currentTick % TPS.length] = MathHelper.clamp((20 / (System.currentTimeMillis() - prevTime / 1000F)), 0, 20);
                currentTick++;
            }

            // mark as last response
            prevTime = System.currentTimeMillis();
        }
    }

    public float getTPS(TPS tps) {
        // do not calculate TPS if we are not on a server
        if (mc.isSingleplayer()) {
            return 20;
        }

        else {
            switch (tps) {
                case Current:
                    // use the last TPS calculation
                    return MathUtil.roundFloat(MathHelper.clamp(TPS[0], 0, 20), 2);
                case Average:
                default:
                    int tickCount = 0;
                    float tickRate = 0;

                    // calculate the average TPS
                    for (float tick : TPS) {
                        if (tick > 0) {
                            tickRate += tick;
                            tickCount++;
                        }
                    }

                    return MathUtil.roundFloat(MathHelper.clamp((tickRate / tickCount), 0, 20), 2);
            }
        }
    }

    /**
     * Sets the client tick length
     * @param ticks The new tick length
     */
    public void setClientTicks(float ticks) {
        ((ITimer) ((IMinecraft) mc).getTimer()).setTickLength((50 / ticks));
    }

    /**
     * Gets the client tick length
     * @return The tick length
     */
    public float getTickLength() {
        return ((ITimer) ((IMinecraft) mc).getTimer()).getTickLength();
    }

    public enum TPS {

        /**
         * Uses the latest TPS calculation
         */
        Current,

        /**
         * Uses the average TPS (over last 20 ticks) calculation
         */
        Average,

        /**
         * Does not calculate TPS
         */
        None
    }
}