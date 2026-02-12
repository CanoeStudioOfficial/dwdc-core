package com.canoestudio.dwdccore.kleeslabs;

import com.canoestudio.dwdccore.DWDCcore;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@Config(modid = DWDCcore.MOD_ID, name = "dwdc_core_kleeslabs")
@Mod.EventBusSubscriber(modid = DWDCcore.MOD_ID)
public class KleeSlabsConfig {

    @Config.Name("general")
    @Config.Comment("General Settings")
    public static final General general = new General();

    @Config.Name("compat")
    @Config.Comment("Compatibility Settings")
    public static final Map<String, Boolean> compat = new HashMap<>();

    public static class General {
        @Config.Name("Require Sneaking")
        @Config.Comment("Set this to true to only break half a slab when the player is sneaking.")
        public boolean requireSneaking = false;

        @Config.Name("Invert Sneaking Check")
        @Config.Comment("If Require Sneaking is enabled. Set this to true to invert the sneaking check for breaking only half a slab.")
        public boolean invertSneakingCheck = false;
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(DWDCcore.MOD_ID)) {
            ConfigManager.sync(DWDCcore.MOD_ID, Config.Type.INSTANCE);
        }
    }
}
