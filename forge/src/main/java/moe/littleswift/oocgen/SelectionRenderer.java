package moe.littleswift.oocgen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public class SelectionRenderer {

    public static void render(RenderLevelStageEvent event) {
        Minecraft client = Minecraft.getInstance();

        if (client.level == null || client.player == null) return;

        if (client.player.getMainHandItem().getItem() != OocgenClient.SELECTION_TOOL) {
            return;
        }

        BlockPos pos1 = SelectionManager.getPos1();
        BlockPos pos2 = SelectionManager.getPos2();

        if (pos1 == null && pos2 == null) return;
        if (pos1 != null && pos2 == null) pos2 = pos1;
        if (pos1 == null) pos1 = pos2;

        PoseStack matrices = new PoseStack();
        matrices.mulPose(event.getPoseStack());
        Vec3 cameraPos = event.getCamera().getPosition();

        AABB box = new AABB(
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ()),
                Math.max(pos1.getX(), pos2.getX()) + 1,
                Math.max(pos1.getY(), pos2.getY()) + 1,
                Math.max(pos1.getZ(), pos2.getZ()) + 1
        );

        matrices.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        VertexConsumer buffer = Minecraft.getInstance()
                .renderBuffers()
                .bufferSource()
                .getBuffer(RenderType.lines());

        drawBox(matrices, buffer, box, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void drawBox(PoseStack matrices, VertexConsumer buffer, AABB box,
                                float r, float g, float b, float a) {

        PoseStack.Pose matrix = matrices.last();

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(1, 0, 0);
        buffer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setNormal(1, 0, 0);

        buffer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setNormal(0, 0, 1);
        buffer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setNormal(0, 0, 1);

        buffer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setNormal(-1, 0, 0);
        buffer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setNormal(-1, 0, 0);

        buffer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setNormal(0, 0, -1);
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(0, 0, -1);

        buffer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setNormal(1, 0, 0);
        buffer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setNormal(1, 0, 0);

        buffer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setNormal(0, 0, 1);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(0, 0, 1);

        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(-1, 0, 0);
        buffer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setNormal(-1, 0, 0);

        buffer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setNormal(0, 0, -1);
        buffer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setNormal(0, 0, -1);

        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setNormal(0, 1, 0);
        buffer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setNormal(0, 1, 0);

        buffer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setNormal(0, 1, 0);
        buffer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setNormal(0, 1, 0);

        buffer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setNormal(0, 1, 0);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setNormal(0, 1, 0);

        buffer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setNormal(0, 1, 0);
        buffer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setNormal(0, 1, 0);
    }
}
