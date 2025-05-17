package net.chaossquad.mclib.packets.packetentity;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Represents a packet entity.
 * Contains the NMS entity, the players that can see it and custom data for that entity.
 * @param <T> entity type if relevant
 */
public class PacketEntity<T extends Entity> implements EntityInLevelCallback {
    private final PacketEntityManager manager;
    private final T entity;
    private final List<Player> players;
    private final Map<String, PacketEntityData<?>> data;

    /**
     * Creates the PacketEntity.
     * @param manager manager
     * @param entity entity
     */
    public PacketEntity(PacketEntityManager manager, T entity) {
        this.manager = manager;
        this.entity = entity;
        this.entity.setLevelCallback(this);
        this.players = new ArrayList<>();
        this.data = new HashMap<>();
    }

    // ENTITY

    /**
     * Returns the entity.
     * @return entity
     */
    public T getEntity() {
        return this.entity;
    }

    /**
     * Checks if the entity is removed.
     * @return removed
     */
    public boolean isRemoved() {
        return this.entity.isRemoved();
    }

    /**
     * Set the entity removed and removes all players.
     */
    public void remove() {
        this.entity.setRemoved(Entity.RemovalReason.DISCARDED);
    }

    /**
     * Returns the bukkit world where the packet entity currently is.
     * @return bukkit world
     */
    public World getWorld() {
        return this.entity.level().getWorld();
    }

    // PLAYERS

    /**
     * Returns the condition if the entity should be showed to a player.
     * This does not check if the player is in the player list of that entity.
     * @param player player
     * @return true if entity can be shown to a player if the player is in the list for the entity
     */
    private boolean showEntityCondition(Player player) {
        return player != null && player.isOnline() && ((CraftWorld) player.getWorld()).getHandle() == this.entity.level();
    }

    /**
     * Removes all players from the packet entity.
     */
    public void cleanupPlayers() {

        for (Player player : List.copyOf(this.players)) {

            if (!this.showEntityCondition(player)) {
                this.players.remove(player);
            }

        }

    }

    /**
     * Returns a list of all players that can see the entity.
     * This does not include players in the list that don't meet the conditions for seeing the entity
     * @return list of players
     */
    public List<Player> getPlayers() {
        List<Player> playerList = new ArrayList<>();

        for (Player player : List.copyOf(this.players)) {
            if (this.showEntityCondition(player)) {
                playerList.add(player);
            }
        }

        return List.copyOf(playerList);
    }

    /**
     * Returns true if the specified player can see the entity.
     * Else, it returns false.
     * @param player player that should be checked for
     * @return true if player can see entity, false if not
     */
    public boolean hasPlayer(Player player) {
        return this.showEntityCondition(player) && this.players.contains(player);
    }

    /**
     * Adds a player so that the player can see the entity.
     * @param player the player
     * @return true if adding the player was successful
     */
    public boolean addPlayer(Player player) {
        if (this.entity.isRemoved()) return false;
        if (!this.showEntityCondition(player)) return false;

        if (!this.players.contains(player)) this.players.add(player);

        this.sendEntityData(player, true);

        return true;
    }

    /**
     * Removes a player from seeing the entity.
     * @param player player that should be removed
     */
    public void removePlayer(Player player) {

        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(this.entity.getId());
        ((CraftPlayer) player).getHandle().connection.send(packet);

        this.players.remove(player);
    }

    /**
     * Removes all players from seeing the entity.
     */
    public void removeAllPlayers() {

        for (Player player : List.copyOf(this.players)) {
            this.removePlayer(player);
        }

    }

    // ENTITY DATA

    /**
     * Sends all entity data modifications to the specified player.
     * @param player player
     * @param full re-add the entity to the player
     * @return entity data
     */
    public boolean sendEntityData(Player player, boolean full) {
        if (!this.hasPlayer(player)) return false;
        if (!this.showEntityCondition(player)) return false;

        // Send entity data if full is set
        if (full) {
            ServerEntity serverEntity = new ServerEntity(this.entity.level().getMinecraftWorld(), this.entity, 0, false, packet -> {}, Set.of());
            ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(this.entity, serverEntity);
            ((CraftPlayer) player).getHandle().connection.send(packet);
        }

        List<SynchedEntityData.DataValue<?>> data = this.entity.getEntityData().getNonDefaultValues();
        if (data == null || data.isEmpty()) return false;

        ((CraftPlayer) player).getHandle().connection.send(new ClientboundSetEntityDataPacket(this.entity.getId(), data));
        return true;
    }

    /**
     * Sends all entity data modifications to the players.
     * @param full Re-add the entity to the player
     */
    public void sendEntityData(boolean full) {

        for (Player player : this.getPlayers()) {
            this.sendEntityData(player, full);
        }

        if (this.entity.getEntityData().isDirty()) {
            this.entity.getEntityData().packDirty();
        }

    }

    /**
     * Sends all entity data modifications to the specified player.
     * @param player player
     * @return entity data
     */
    public boolean sendEntityData(Player player) {
        return this.sendEntityData(player, false);
    }

    /**
     * Sends all entity data modifications to the players.
     */
    public void sendEntityData() {
        this.sendEntityData(false);
    }

    // CUSTOM DATA

    /**
     * Returns all custom entity data.
     * @return map of custom entity data
     */
    public Map<String, PacketEntityData<?>> getData() {
        return Map.copyOf(this.data);
    }

    /**
     * Returns the custom entity data of the specified key.
     * Returns null if the data does not exist.
     * @param key key
     * @return data
     */
    public PacketEntityData<?> getData(String key) {
        return this.data.get(key);
    }

    /**
     * Add or update a custom entity data.
     * @param key key to add
     * @param data value to add
     */
    public void addData(String key, PacketEntityData<?> data) {
        this.data.put(key, data);
    }

    /**
     * Removes a custom entity data.
     * @param key key to remove
     */
    public void removeData(String key) {
        this.data.remove(key);
    }

    /**
     * Clears all custom entity data.
     */
    public void removeAllData() {
        this.data.clear();
    }

    // GETTER

    /**
     * Returns the manager.
     * @return manager
     */
    public PacketEntityManager getManager() {
        return manager;
    }

    // LEVEL CALLBACK

    @Override
    public void onMove() {
        // Currently nothing
    }

    @Override
    public void onRemove(Entity.RemovalReason removalReason) {
        this.removeAllPlayers();
        this.manager.cleanupEntities();
    }

}
