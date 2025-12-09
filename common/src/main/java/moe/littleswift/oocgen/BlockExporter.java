package moe.littleswift.oocgen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockExporter {
    private static final Queue<BlockPos> pendingNbtRequestsQueue = new ArrayDeque<>();
    private static final Map<Integer, BlockPos> pendingNbtRequestsMap = new HashMap<>();
    private static final AtomicInteger transactionIdCounter = new AtomicInteger(1);
    private static BlockPos minPos;
    private static final List<BlockData> exportedDataList = new ArrayList<>();

    public static void exportSelection(ClientLevel world, Player player) {
        BlockPos pos1 = SelectionManager.getPos1();
        BlockPos pos2 = SelectionManager.getPos2();

        if (pos1 == null || pos2 == null) {
            player.displayClientMessage(Component.literal("Please set both Pos1 and Pos2 first.").withColor(CommonColors.SOFT_RED), true);
            return;
        }

        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        minPos = new BlockPos(minX, minY, minZ);

        exportedDataList.clear();
        pendingNbtRequestsQueue.clear();
        pendingNbtRequestsMap.clear();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pendingNbtRequestsQueue.offer(new BlockPos(x, y, z));
                }
            }
        }

        requestNextBlockData((LocalPlayer) player);
    }

    public static void requestNextBlockData(LocalPlayer player) {
        if (pendingNbtRequestsQueue.isEmpty()) {
            if (pendingNbtRequestsMap.isEmpty()) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("Time", 1);
                ListTag list = new ListTag();
                list.add(DoubleTag.valueOf(0.0));
                list.add(DoubleTag.valueOf(-10.0));
                list.add(DoubleTag.valueOf(0.0));
                tag.put("Motion", list);
                CompoundTag tag2 = new CompoundTag();
                tag2.putString("Name", "minecraft:redstone_block");
                tag.put("BlockState", tag2);
                ListTag passengers = new ListTag();
                CompoundTag activatorRail = new CompoundTag();
                activatorRail.putString("id", "minecraft:falling_block");
                activatorRail.putInt("Time", 1);
                tag2 = new CompoundTag();
                tag2.putString("Name", "minecraft:activator_rail");
                activatorRail.put("BlockState", tag2);
                passengers.add(activatorRail);
                for (BlockData blockData : exportedDataList) {
                    CompoundTag block = new CompoundTag();
                    block.putString("id", "minecraft:command_block_minecart");
                    String command = "setblock ~" + (blockData.relativePos().getX() + 1)
                            + " ~" + (blockData.relativePos().getY() - 2)
                            + " ~" + blockData.relativePos().getZ()
                            + " " + blockData.blockId() + blockData.blockState();
                    if (blockData.nbt() != null) {
                        command += blockData.nbt().toString();
                    }
                    block.putString("Command", command);
                    passengers.add(block);
                }
                CompoundTag clear = new CompoundTag();
                clear.putString("id", "minecraft:command_block_minecart");
                clear.putString("Command", "setblock ~ ~1 ~ minecraft:command_block{Command:\"fill ~ ~ ~ ~ ~-3 ~ minecraft:air\",auto:1}");
                passengers.add(clear);
                CompoundTag kill = new CompoundTag();
                kill.putString("id", "minecraft:command_block_minecart");
                kill.putString("Command", "kill @e[type=minecraft:command_block_minecart,distance=..1]");
                passengers.add(kill);
                tag.put("Passengers", passengers);
                String fullCommand = "summon minecraft:falling_block ~ ~1.5 ~ " + tag.getAsString();

                ClickEvent copyEvent = new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        fullCommand
                );

                String nbt = createActivatedCommandBlockNBT(fullCommand);

                ClickEvent giveEvent = new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/give @s minecraft:command_block[minecraft:block_entity_data={id:\"minecraft:command_block\"," + nbt.substring(1, nbt.length() - 1) + "}]"
                );

                MutableComponent copyButton = Component.literal("[Copy]")
                        .setStyle(Style.EMPTY
                                .withColor(TextColor.fromRgb(0x00AAAA))
                                .withBold(true)
                                .withClickEvent(copyEvent)
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy OOC")))
                        );

                MutableComponent giveButton = Component.literal("[Give]")
                        .setStyle(Style.EMPTY
                                .withColor(TextColor.fromRgb(0xFFA500)) // Orange/Gold color
                                .withBold(true)
                                .withClickEvent(giveEvent)
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to spawn item")))
                        );

                MutableComponent finalMessage = Component.literal("OOC generated ")
                        .withColor(CommonColors.GRAY);
                finalMessage.append(copyButton);
                finalMessage.append(Component.literal(" "));
                finalMessage.append(giveButton);
                player.displayClientMessage(finalMessage, false);
            }
            return;
        }

        BlockPos nextPos = pendingNbtRequestsQueue.poll();

        int id = transactionIdCounter.getAndIncrement();

        pendingNbtRequestsMap.put(id, nextPos);

        ServerboundBlockEntityTagQueryPacket packet = new ServerboundBlockEntityTagQueryPacket(id, nextPos);
        player.connection.send(packet);
    }

    public static void processNbtResponse(ClientLevel world, int id, CompoundTag nbt) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        BlockPos pos = pendingNbtRequestsMap.remove(id);

        if (pos == null) {
            return;
        }
        BlockPos relativePos = pos.subtract(minPos);

        BlockState state = world.getBlockState(pos);
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        String[] stateList = state.toString().split("}");
        String stateData = stateList.length > 1 ? stateList[1] : "";

        exportedDataList.add(new BlockData(relativePos, blockId, stateData, nbt));

        requestNextBlockData(client.player);
    }

    private static String createActivatedCommandBlockNBT(String command) {
        CompoundTag blockEntityTag = new CompoundTag();
        blockEntityTag.putString("Command", command);
        blockEntityTag.putByte("auto", (byte) 1);
        return blockEntityTag.getAsString();
    }
}