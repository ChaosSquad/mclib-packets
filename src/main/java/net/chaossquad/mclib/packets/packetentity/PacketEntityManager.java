package net.chaossquad.mclib.packets.packetentity;

import net.minecraft.world.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * The packet entity manager handles packet entities.
 * It adds, removes and cleans up packet entities, allows to access the packet entities and updates the information of packet entities.
 */
public class PacketEntityManager implements Listener {
    private final Plugin plugin;
    private final List<PacketEntity<?>> entities;

    /**
     * Creates a PacketEntityManager.
     * @param plugin plugin
     */
    public PacketEntityManager(Plugin plugin) {
        this.plugin = plugin;
        entities = new ArrayList<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupEntities();
                cleanupPlayers();
                updateEntities();
            }
        }.runTaskTimer(this.plugin, 1, 1200);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    // TASKS

    /**
     * Cleans up all packet entities.
     */
    public void cleanupEntities() {

        for (PacketEntity<?> entity : List.copyOf(this.entities)) {

            if (entity.isRemoved()) {
                entity.removeAllPlayers();
                this.entities.remove(entity);
            }

        }

    }

    /**
     * Cleans up all players of all packet entities.
     */
    public void cleanupPlayers() {

        for (PacketEntity<?> entity : List.copyOf(this.entities)) {
            if (entity.isRemoved()) continue;
            entity.cleanupPlayers();
        }

    }

    /**
     * Updates all PacketEntities for the players.
     */
    public void updateEntities() {

        for (PacketEntity<?> entity : List.copyOf(this.entities)) {
            if (entity.isRemoved()) continue;

            if (!this.plugin.getServer().getWorlds().contains(entity.getWorld())) {
                entity.remove();
                continue;
            }

            if (entity.getEntity().getEntityData().isDirty()) {
                entity.sendEntityData();
            }

        }

    }

    // EVENTS

    /**
     * Cleanup players when the player has left the server.
     * @param event event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        new BukkitRunnable() {

            @Override
            public void run() {
                PacketEntityManager.this.cleanupPlayers();
            }

        }.runTaskLater(this.plugin, 1);
    }

    /**
     * Remove packet entities when the player has changed the world.
     * @param event event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        new BukkitRunnable() {

            @Override
            public void run() {
                PacketEntityManager.this.cleanupPlayers();
            }

        }.runTaskLater(this.plugin, 1);
    }

    /**
     * Cleanup PacketEntities when the world has been unloaded.
     * @param event event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        new BukkitRunnable() {

            @Override
            public void run() {
                PacketEntityManager.this.updateEntities();
            }

        }.runTaskLater(this.plugin, 1);
    }

    // API

    /**
     * Returns the plugin.
     * @return plugin
     */
    public Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * Returns a list of all available packet entities.
     * @return list of packet entities
     */
    public List<PacketEntity<?>> getEntities() {
        List<PacketEntity<?>> list = new ArrayList<>();

        for (PacketEntity<?> entity : List.copyOf(this.entities)) {
            if (entity.isRemoved()) continue;
            list.add(entity);
        }

        return list;
    }

    /**
     * Removes all packet entities.
     */
    public void removeAll() {

        for (PacketEntity<?> entity : List.copyOf(this.entities)) {
            entity.remove();
        }

        this.cleanupEntities();

    }

    /**
     * Searches for the {@link PacketEntity} which manages the entity with the specified id.
     * @param id entity id
     * @return PacketEntity
     */
    public PacketEntity<?> getPacketEntityFromId(int id) {
        for (PacketEntity<?> packetEntity : this.entities) {
            if (packetEntity.getEntity().getId() == id) return packetEntity;
        }
        return null;
    }

    /**
     * Searches for the {@link PacketEntity} that is managing the specified entity.
     * @param entity nms entity
     * @return PacketEntity
     */
    public PacketEntity<?> getPacketEntityFromEntity(Entity entity) {
        for (PacketEntity<?> packetEntity : this.entities) {
            if (packetEntity.getEntity().equals(entity)) return packetEntity;
        }
        return null;
    }

    /**
     * Adds a new packet entity.
     * @param entity nms entity
     * @return packet entity object
     */
    public PacketEntity<?> addEntity(Entity entity) {
        if (entity == null || entity.isRemoved()) return null;
        PacketEntity<?> packetEntity = new PacketEntity<>(this, entity);
        this.entities.add(packetEntity);
        return packetEntity;
    }

}
