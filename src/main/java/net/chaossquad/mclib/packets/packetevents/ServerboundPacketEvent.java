package net.chaossquad.mclib.packets.packetevents;

import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event is fired by {@link PacketEventHandler} when a serverbound NMS packet is received from a player.
 */
public class ServerboundPacketEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Packet<?> packet;
    private boolean cancelled;
    private Packet<?> replacement;

    /**
     * Creates a ServerboundPacketEvent.
     * @param player player
     * @param packet packet
     */
    @ApiStatus.Internal
    public ServerboundPacketEvent(Player player, Packet<?> packet) {
        super(true);
        this.player = player;
        this.packet = packet;
        this.cancelled = false;
        this.replacement = null;
    }

    /**
     * Returns the player
     * @return player
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Returns the packet.
     * @return packet
     */
    public Packet<?> getPacket() {
        return this.packet;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    /**
     * Get the replacement packet.
     * @return replacement packet
     */
    public Packet<?> getReplacement() {
        return this.replacement;
    }

    /**
     * Set a packet to replace the current packet with.
     * @param replacement replacement packet.
     */
    public void setReplacement(Packet<?> replacement) {
        this.replacement = replacement;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Returns the HandlerList.
     * @return HandlerList
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
