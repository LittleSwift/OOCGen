package moe.littleswift.oocgen;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "oocgen", dist = Dist.CLIENT)
public class Oocgen {
    public Oocgen(IEventBus eventBus) {
        eventBus.addListener(OocgenClient::registerKeys);
        NeoForge.EVENT_BUS.addListener(OocgenClient::onKeyInput);
        NeoForge.EVENT_BUS.addListener(OocgenClient::onRenderWorld);
        NeoForge.EVENT_BUS.addListener(OocgenClient::onLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(OocgenClient::onRightClickBlock);
    }
}
