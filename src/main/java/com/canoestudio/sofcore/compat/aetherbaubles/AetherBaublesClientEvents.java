package com.canoestudio.sofcore.compat.aetherbaubles;

import com.canoestudio.sofcore.SOFcore;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = SOFcore.MOD_ID, value = Side.CLIENT)
public final class AetherBaublesClientEvents {

    private AetherBaublesClientEvents() {
    }

    @SubscribeEvent
    public static void hideAetherAccessoryButton(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!AetherBaublesCompat.isLoaded() || !(event.getGui() instanceof GuiContainer)) {
            return;
        }

        event.getButtonList().removeIf(button -> button.id == 18067);
    }
}
