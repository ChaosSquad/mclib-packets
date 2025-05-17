package net.chaossquad.mclib.packets.packetevents;

import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is fired by {@link PacketEventHandler} when a clientbound NMS packet is sent to a player.
 */
public class ClientboundPacketEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Packet<?> packet;
    private boolean cancelled;
    private Packet<?> replacement;

    /**
     * Creates a ClientboundPacketEvent.
     * @param player player
     * @param packet packet
     */
    public ClientboundPacketEvent(Player player, Packet<?> packet) {
        super(true);
        this.player = player;
        this.packet = packet;
        this.cancelled = false;
        this.replacement = null;
    }

    /**
     * Returns the player the event has been fired for.
     * @return player
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Returns the packet the event is fired for.
     * @return packet
     */
    public Packet<?> getPacket() {
        return this.packet;
    }

    /**
     * Returns if the event has been cancelled.
     * @return cancelled
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Sets the cancelled status of the event
     * @param b true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    /**
     * Returns the replacement packet
     * @return replacement packet
     */
    public Packet<?> getReplacement() {
        return this.replacement;
    }

    /**
     * Sets the packet to replace the old with
     * @param replacement replacement packet
     */
    public void setReplacement(Packet<?> replacement) {
        this.replacement = replacement;
    }

    /**
     * Returns the HandlerList.
     * @return handler list
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Returns the HanderList.
     * @return handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
