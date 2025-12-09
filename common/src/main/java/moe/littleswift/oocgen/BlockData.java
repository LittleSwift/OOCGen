package moe.littleswift.oocgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public record BlockData(
        BlockPos relativePos,
        String blockId,
        String blockState,
        CompoundTag nbt
) {}