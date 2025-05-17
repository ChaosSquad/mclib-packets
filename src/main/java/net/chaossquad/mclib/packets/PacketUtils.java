package net.chaossquad.mclib.packets;

import net.chaossquad.mclib.blocks.BlockStructure;
import net.chaossquad.mclib.blocks.BlockStructureEntry;
import net.chaossquad.mclib.packets.packetentity.PacketEntity;
import net.chaossquad.mclib.packets.packetentity.PacketEntityData;
import net.chaossquad.mclib.packets.packetentity.PacketEntityManager;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for NMS packets.
 */
public final class PacketUtils {

    private PacketUtils() {}

    // CONNECTION

    /**
     * Returns the connection of a NMS Player.
     * @param serverPlayer nms player
     * @return connection
     */
    public static Connection getConnection(ServerPlayer serverPlayer) {

        try {
            ServerGamePacketListenerImpl serverGamePacketListener = serverPlayer.connection;
            Field field = ServerCommonPacketListenerImpl.class.getDeclaredField("connection");
            field.setAccessible(true);

            return  (Connection) field.get(serverGamePacketListener);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }

    }

    // PACKET ENTITIES

    /**
     * Key of relative position of a spawned block structure.
     * Will be added when a {@link BlockStructure} is spawned by {@link #spawnBlockStructure(PacketEntityManager, BlockStructure, Location, List)}.
     */
    public static final String DATA_VALUE_BLOCKSTRUCTURE_RELATIVE_POSITION = "mclib.blockstructure.relativeposition";

    /**
     * Creates a list of block displays for the specified block structure at the specified location and adds them to the specified {@link PacketEntityManager}.
     * @param manager the packet entity manager the entities should be added to
     * @param structure the block structure that should be spawned
     * @param location the location where the structure should be spawned
     * @param scoreboardTags list of scoreboard tags that should be added
     * @return a list of the spawned packet entities
     */
    public static List<PacketEntity<Display.BlockDisplay>> spawnBlockStructure(PacketEntityManager manager, BlockStructure structure, Location location, List<String> scoreboardTags) {
        location = location.clone();
        structure = structure.clone();
        List<PacketEntity<Display.BlockDisplay>> blockDisplays = new ArrayList<>();

        int rx = 0;
        for (int x = location.getBlockX(); x < location.getBlockX() + structure.getXLength(); x++) {
            int ry = 0;
            for (int y = location.getBlockY(); y < location.getBlockY() + structure.getYLength(); y++) {
                int rz = 0;
                for (int z = location.getBlockZ(); z < location.getBlockZ() + structure.getZLength(); z++) {
                    BlockStructureEntry entry = structure.getBlock(rx, ry, rz);

                    if (entry.type() != Material.AIR) {

                        Display.BlockDisplay blockDisplay = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, ((CraftWorld) location.getWorld()).getHandle());
                        blockDisplay.setPos(x, y, z);
                        blockDisplay.setNoGravity(true);
                        blockDisplay.setBlockState(((CraftBlockData) entry.data()).getState());

                        if (scoreboardTags != null) {
                            for (String tag : List.copyOf(scoreboardTags)) {
                                blockDisplay.addTag(tag);
                            }
                        }

                        PacketEntity<Display.BlockDisplay> packetEntity = (PacketEntity<Display.BlockDisplay>) manager.addEntity(blockDisplay);
                        packetEntity.addData(DATA_VALUE_BLOCKSTRUCTURE_RELATIVE_POSITION, new PacketEntityData<>(new Vector(rx, ry, rz)));
                        blockDisplays.add(packetEntity);

                    }

                    rz++;
                }
                ry++;
            }
            rx++;
        }

        return List.copyOf(blockDisplays);
    }

}
