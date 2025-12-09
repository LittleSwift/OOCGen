package moe.littleswift.oocgen.mixin;

import moe.littleswift.oocgen.BlockExporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleTagQueryPacket", at = @At("HEAD"))
    private void oocgen$handleTagQueryPacket(ClientboundTagQueryPacket packet, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        ClientPacketListener networkHandler = (ClientPacketListener) (Object) this;

        client.execute(() -> {
            BlockExporter.processNbtResponse(
                    networkHandler.getLevel(),
                    packet.getTransactionId(),
                    packet.getTag()
            );
        });
    }
}