package net.chaossquad.mclib.packets.packetentity;

import net.chaossquad.mclib.PlayerUtils;
import net.chaossquad.mclib.command.TabCompletingCommandExecutor;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

/**
 * A command which manages packet entities created by the {@link PacketEntityManager}.
 */
public class PacketEntitiesSubcommand implements TabCompletingCommandExecutor {
    private final PacketEntityManagerProvider provider;
    private final String permission;

    /**
     * Creates a PacketEntitiesSubcommand.
     * @param provider provider
     * @param permission permission
     */
    public PacketEntitiesSubcommand(PacketEntityManagerProvider provider, String permission) {
        if (!(provider instanceof Plugin)) throw new IllegalArgumentException("Provided packet entity provider is not a plugin");
        this.provider = provider;
        this.permission = permission;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender != ((Plugin) this.provider).getServer().getConsoleSender() && !sender.hasPermission(this.permission)) {
            sender.sendMessage("§cNo permission");
            return true;
        }

        try {

            PacketEntityManager manager = this.provider.getPacketEntityManager();

            if (args.length > 0) {

                PacketEntity<?> entity;

                String[] splitted = args[0].split(":");
                if (splitted.length > 1) {
                    if (splitted[0].equalsIgnoreCase("e") || splitted[0].equalsIgnoreCase("entity") || splitted[0].equalsIgnoreCase("entityid")) {
                        entity = manager.getPacketEntityFromId(Integer.parseInt(splitted[1]));
                    } else {
                        sender.sendMessage("§cUse e:<entityid> or nothing for the list id");
                        return true;
                    }
                } else {
                    entity = manager.getEntities().get(Integer.parseInt(args[0]));
                }

                if (entity == null) {
                    sender.sendMessage("§cUnknown entity");
                    return true;
                }

                if (args.length > 1) {

                    switch (args[1]) {
                        case "entityid" -> sender.sendMessage("§7entityid: " + entity.getEntity().getId());
                        case "type" -> sender.sendMessage("§7type: " + entity.getEntity().getType());
                        case "location" -> {

                            if (args.length > 2) {
                                if (args.length > 4) {

                                    entity.getEntity().setPos(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                                    sender.sendMessage("§aLocation updated");

                                } else {
                                    sender.sendMessage("§cUsage: [...] packetentities <id/e:entityid> location <x> <y> <z>");
                                }
                            } else {
                                sender.sendMessage("§7location: " + entity.getEntity().getX() + " " + entity.getEntity().getY() + " " + entity.getEntity().getZ());
                            }

                        }
                        case "tags" -> {

                            if (args.length > 2) {

                                switch (args[2]) {
                                    case "add" -> {

                                        if (args.length > 3) {
                                            entity.getEntity().addTag(args[3]);
                                            sender.sendMessage("§aTag added");
                                        } else {
                                            sender.sendMessage("§cUsage: [...] packetentities <id/e:entityid> tags add <tag>");
                                        }

                                    }
                                    case "remove" -> {

                                        if (args.length > 3) {
                                            entity.getEntity().removeTag(args[3]);
                                            sender.sendMessage("§aTag removed");
                                        } else {
                                            sender.sendMessage("§cUsage: [...] packetentities <id/e:entityid> tags remove <tag>");
                                        }

                                    }
                                    case "clear" -> {
                                        entity.getEntity().getTags().clear();
                                        sender.sendMessage("§cCleared tags");
                                    }
                                    default -> sender.sendMessage("§cUnknown subcommand");
                                }

                            } else {
                                sender.sendMessage("§7tags: " + entity.getEntity().getTags() + " (" + entity.getEntity().getTags().size() + ")");
                            }

                        }
                        case "synchedentitydata" -> {

                            List<SynchedEntityData.DataValue<?>> dataValues = entity.getEntity().getEntityData().getNonDefaultValues();
                            sender.sendMessage("§7§lEntity data:");

                            if (dataValues != null) {

                                for (SynchedEntityData.DataValue<?> dataValue : dataValues) {
                                    sender.sendMessage("§7" + dataValue.id() + " " + dataValue.value());
                                }

                            } else {
                                sender.sendMessage("§7No non-default values available");
                            }

                        }
                        case "removed" -> {

                            if (args.length > 2) {

                                if (args[2].equalsIgnoreCase("true")) {
                                    entity.remove();
                                    sender.sendMessage("§aEntity removed");
                                } else {
                                    sender.sendMessage("§cValue can only be set to true");
                                }

                            } else {
                                sender.sendMessage("§7removed: " + entity.isRemoved());
                            }

                        }
                        case "players" -> {

                            if (args.length > 2) {

                                switch (args[2]) {
                                    case "add" -> {

                                        if (args.length > 3) {

                                            Player player = PlayerUtils.getPlayerFromString(args[3]);
                                            if (player == null) {
                                                sender.sendMessage("§cPlayer does not exist");
                                                return true;
                                            }

                                            entity.addPlayer(player);
                                            sender.sendMessage("§aPlayer added");
                                        } else {
                                            sender.sendMessage("§cUsage: [...] packetentity <id/e:id> players add <player>");
                                        }

                                    }
                                    case "remove" -> {

                                        if (args.length > 3) {

                                            Player player = PlayerUtils.getPlayerFromString(args[3]);
                                            if (player == null) {
                                                sender.sendMessage("§cPlayer does not exist");
                                                return true;
                                            }

                                            entity.removePlayer(player);
                                            sender.sendMessage("§aPlayer removed");
                                        } else {
                                            sender.sendMessage("§cUsage: [...] packetentity <id/e:id> players remove <player>");
                                        }

                                    }
                                    case "clear" -> {
                                        entity.removeAllPlayers();
                                        sender.sendMessage("§aCleared players");
                                    }
                                    case "update" -> {

                                        if (args.length < 4) {
                                            sender.sendMessage("§cUsage: [...] packetentities <id/e:entityid> players update <full> [player]");
                                            return true;
                                        }

                                        boolean full = Boolean.parseBoolean(args[3]);

                                        if (args.length > 4) {

                                            Player player = PlayerUtils.getPlayerFromString(args[4]);
                                            if (player == null) {
                                                sender.sendMessage("§cPlayer does not exist");
                                                return true;
                                            }

                                            entity.sendEntityData(player, full);
                                            sender.sendMessage("§aUpdated entity for specified player (full=" + full + ")");

                                        } else {
                                            entity.sendEntityData(full);
                                            sender.sendMessage("§aUpdated entity for all players (full=" + full + ")");
                                        }

                                    }
                                    default -> {

                                    }
                                }

                            } else {

                                sender.sendMessage("§7§lEntity Players:");

                                for (Player player : entity.getPlayers()) {
                                    sender.sendMessage("§7" + player.getName() + " " + player.getUniqueId());
                                }

                            }

                        }
                        case "customdata" -> {

                            if (args.length > 2) {

                                if (args.length > 3) {

                                    switch (args[3]) {
                                        case "remove" -> {
                                            entity.removeData(args[2]);
                                            sender.sendMessage("§aData removed");
                                        }
                                        default -> sender.sendMessage("§cUnknown subcommand");
                                    }

                                } else {

                                    PacketEntityData<?> data = entity.getData(args[2]);
                                    if (data != null) {
                                        sender.sendMessage("§7" + args[2] + ": " + data.data());
                                    } else {
                                        sender.sendMessage("§cData does not exist");
                                    }

                                }

                            } else {

                                sender.sendMessage("§7§lCustom data:");

                                Map<String, PacketEntityData<?>> dataMap = entity.getData();
                                for (String key : dataMap.keySet()) {
                                    PacketEntityData<?> data = dataMap.get(key);
                                    if (data == null) continue;
                                    sender.sendMessage("§7" + key + ": " + data.data());
                                }

                            }

                        }
                        default -> sender.sendMessage("§cUnknown subcommand");
                    }

                } else {

                    sender.sendMessage(
                            "§7§lEntity Info:§r\n" +
                                    "§7entityid: " + entity.getEntity().getId() + "\n" +
                                    "§7type: " + entity.getEntity().getType() + "\n" +
                                    "§7location: " + entity.getEntity().getX() + " " + entity.getEntity().getY() + " " + entity.getEntity().getZ() + " (modifiable)\n" +
                                    "§7tags: " + entity.getEntity().getTags().size() + " entries (modifiable)\n" +
                                    "§7synchedentitydata: run command for info\n" +
                                    "§7removed: " + entity.isRemoved() + " (editable)\n" +
                                    "§7players: " + entity.getPlayers().size() + " entries (modifiable)\n" +
                                    "§7customdata: " + entity.getData().size() + " entries (modifiable)"
                    );

                }

            } else {

                sender.sendMessage("§7Usage: [...] packetentities <id/e:entityId> remove/update/value");
                sender.sendMessage("§7§lPacket Entities:");

                List<PacketEntity<?>> entityList = manager.getEntities();
                for (int i = 0; i < entityList.size(); i++) {
                    PacketEntity<?> packetEntity = entityList.get(i);

                    sender.sendMessage("§7" + i + " " + packetEntity.getEntity().getId() + " " + packetEntity.getEntity().getType() + " " + packetEntity.getEntity().getX() + " " + packetEntity.getEntity().getY() + " " + packetEntity.getEntity().getZ());
                }

            }

        } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
            sender.sendMessage("§cIllegal argument");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        return switch (args.length) {
            case 2 -> List.of("entityid", "type", "location", "tags", "synchedentitydata", "removed", "players", "customdata");
            case 3 -> switch (args[2]) {
                case "tags", "players" -> List.of("add", "remove", "clear");
                case "removed" -> List.of("true");
                case "customdata" -> List.of("remove");
                default -> List.of();
            };
            default -> List.of();
        };

    }

    /**
     * Returns the PacketEntityManagerProvider.
     * @return provider
     */
    public PacketEntityManagerProvider getProvider() {
        return provider;
    }

}
