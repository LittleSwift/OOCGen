package moe.littleswift.oocgen;

import net.minecraft.core.BlockPos;

public class SelectionManager {
    private static BlockPos pos1;
    private static BlockPos pos2;

    public static void setPos1(BlockPos pos) {
        pos1 = pos;
    }

    public static void setPos2(BlockPos pos) {
        pos2 = pos;
    }

    public static BlockPos getPos1() {
        return pos1;
    }

    public static BlockPos getPos2() {
        return pos2;
    }

    public static void clear() {
        pos1 = null;
        pos2 = null;
    }
}