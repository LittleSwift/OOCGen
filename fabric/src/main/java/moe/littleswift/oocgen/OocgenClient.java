package moe.littleswift.oocgen;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class OocgenClient implements ClientModInitializer {
    public static final Item SELECTION_TOOL = Items.GOLDEN_HOE;
    private static KeyMapping exportKey;
    @Override
    public void onInitializeClient() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.getItemInHand(hand).getItem() == SELECTION_TOOL) {
                if (world.isClientSide) {
                    SelectionManager.setPos1(pos);
                    player.displayClientMessage(Component.translatable("Pos1 set at %s", pos.toShortString()), true);
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.getItemInHand(hand).getItem() == SELECTION_TOOL) {
                if (world.isClientSide) {
                    SelectionManager.setPos2(hitResult.getBlockPos());
                    player.displayClientMessage(Component.translatable("Pos2 set at %s", hitResult.getBlockPos().toShortString()), true);
                }
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        WorldRenderEvents.LAST.register(context -> {
            SelectionRenderer.render(context);
        });

        exportKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.oocgen.export_selection",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_ENTER,
                "category.oocgen.selection"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (exportKey.consumeClick()) {
                if (client.player != null && client.player.getMainHandItem().getItem() == SELECTION_TOOL) {
                    BlockExporter.exportSelection(client.level, client.player);
                    client.player.displayClientMessage(Component.literal("Generating..."), true);
                }
            }
        });
    }
}
