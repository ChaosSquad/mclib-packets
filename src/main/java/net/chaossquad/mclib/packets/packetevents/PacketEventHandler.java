package net.chaossquad.mclib.packets.packetevents;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.chaossquad.mclib.packets.PacketUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Fires events when NMS packets are being sent or received.
 * Also allows editing or cancelling these packets inside the events.
 * The events are: {@link ClientboundPacketEvent} and {@link ServerboundPacketEvent}.
 * When you create an object of this class, everything should work as intended, and you just need to listen for the events.
 */
public final class PacketEventHandler implements Listener {
    private final Plugin plugin;
    private final UUID uuid;

    /**
     * Creates the PacketEventHandler.
     * @param plugin plugin
     */
    public PacketEventHandler(Plugin plugin) {
        this.plugin = plugin;
        this.uuid = UUID.randomUUID();
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    // PACKET LISTENERS

    /**
     * Injects the packet listener into the players pipeline.
     * @param event event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Connection connection = PacketUtils.getConnection(((CraftPlayer) event.getPlayer()).getHandle());

        if (connection == null) {
            return;
        }

        connection.channel.pipeline().addBefore("packet_handler", this.getReaderName(), new ChannelInboundHandlerAdapter() {

            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                boolean cancelled = false;
                Packet replacement = null;

                if (msg instanceof Packet<?> packet) {

                    ClientboundPacketEvent packetEvent = new ClientboundPacketEvent(event.getPlayer(), packet);
                    plugin.getServer().getPluginManager().callEvent(packetEvent);
                    cancelled = packetEvent.isCancelled();
                    replacement = packetEvent.getReplacement();

                }

                if (cancelled) {
                    return;
                }

                if (replacement != null) {
                    ctx.fireChannelRead(replacement);
                    return;
                }

                ctx.fireChannelRead(msg);
            }

        });

        connection.channel.pipeline().addBefore("packet_handler", this.getWriterName(), new ChannelOutboundHandlerAdapter() {

            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                boolean cancelled = false;
                Packet replacement = null;

                if (msg instanceof Packet<?> packet) {

                    ServerboundPacketEvent packetEvent = new ServerboundPacketEvent(event.getPlayer(), packet);
                    plugin.getServer().getPluginManager().callEvent(packetEvent);
                    cancelled = packetEvent.isCancelled();
                    replacement = packetEvent.getReplacement();

                }

                if (cancelled) {
                    return;
                }

                if (replacement != null) {
                    ctx.write(replacement, promise);
                    return;
                }

                ctx.write(msg, promise);
            }

        });

    }

    /**
     * Removes the packet entity listener from the player's pipeline.
     * @param event event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        Connection connection = PacketUtils.getConnection(((CraftPlayer) event.getPlayer()).getHandle());

        if (connection == null) {
            return;
        }

        try {
            connection.channel.pipeline().remove(this.getReaderName());
        } catch (NoSuchElementException ignored) {
            // should already be removed at this point
        }

        try {
            connection.channel.pipeline().remove(this.getWriterName());
        } catch (NoSuchElementException ignored) {
            // should already be removed at this point
        }

    }

    // GETTER

    /**
     * Returns the plugin.
     * @return plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Returns the UUID of the PacketEventHandler.<br/>
     * It is used for identification of the pipeline listeners
     * @return uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns the packet reader name.
     * @return packet reader name
     */
    public String getReaderName() {
        return "MCLIB-READER-" + uuid.toString();
    }

    /**
     * Returns the packet writer name.
     * @return packet writer name
     */
    public String getWriterName() {
        return "MCLIB-WRITER-" + uuid.toString();
    }

}
