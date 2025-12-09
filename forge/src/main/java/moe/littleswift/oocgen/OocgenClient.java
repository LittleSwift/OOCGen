package moe.littleswift.oocgen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "oocgen", value = Dist.CLIENT)
public class OocgenClient {
    public static final Item SELECTION_TOOL = Items.GOLDEN_HOE;

    private static KeyMapping exportKey = new KeyMapping(
            "key.oocgen.export_selection",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_ENTER,
            "category.oocgen.selection"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(exportKey);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getItemStack().getItem() == SELECTION_TOOL) {
            if (event.getLevel().isClientSide) {
                SelectionManager.setPos1(event.getPos());
                event.getEntity().displayClientMessage(
                        Component.translatable("Pos1 set at %s", event.getPos().toShortString()), true
                );
            }
            event.setCanceled(true);     // Same effect as InteractionResult.FAIL
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() == SELECTION_TOOL) {
            if (event.getLevel().isClientSide) {
                SelectionManager.setPos2(event.getPos());
                event.getEntity().displayClientMessage(
                        Component.translatable("Pos2 set at %s", event.getPos().toShortString()), true
                );
            }
            event.setCanceled(true);     // Same effect as InteractionResult.FAIL
        }
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_WEATHER)) {
            SelectionRenderer.render(event);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        System.out.println("Registering key mappings" + exportKey);
        if (exportKey != null && exportKey.consumeClick()) {
            var player = net.minecraft.client.Minecraft.getInstance().player;
            var level = net.minecraft.client.Minecraft.getInstance().level;

            if (player != null && player.getMainHandItem().getItem() == SELECTION_TOOL) {
                BlockExporter.exportSelection(level, player);
                player.displayClientMessage(Component.literal("Generating..."), true);
            }
        }
    }
}
