package net.chaossquad.mclib.packets.packetentity;

/**
 * Represents custom data stored in the {@link PacketEntity}
 * @param data data
 * @param <T> data type
 */
public record PacketEntityData<T>(T data) {
}
