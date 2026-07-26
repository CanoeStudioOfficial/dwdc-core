package com.canoestudio.sofcore.proxy;

import com.canoestudio.sofcore.SOFcore;
import com.canoestudio.sofcore.compat.WaystonesToXaeroCompat;
import com.gildedgames.the_aether.client.AetherKeybinds;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);


    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        hideAetherAccessoryKeybind();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        if (Loader.isModLoaded("waystones") && Loader.isModLoaded("xaerominimap")) {
            MinecraftForge.EVENT_BUS.register(WaystonesToXaeroCompat.class);
            SOFcore.LOGGER.info("Enabled Waystones to Xaero waypoint compatibility.");
        }
    }

    private static void hideAetherAccessoryKeybind() {
        if (AetherKeybinds.keyBindingAccessories == null) {
            return;
        }

        ObfuscationReflectionHelper.setPrivateValue(KeyBinding.class, AetherKeybinds.keyBindingAccessories, Keyboard.KEY_NONE, "keyCode", "field_151474_i");
        ObfuscationReflectionHelper.setPrivateValue(KeyBinding.class, AetherKeybinds.keyBindingAccessories, Keyboard.KEY_NONE, "keyCodeDefault", "field_151472_e");
        KeyBinding.resetKeyBindingArrayAndHash();
    }
}
