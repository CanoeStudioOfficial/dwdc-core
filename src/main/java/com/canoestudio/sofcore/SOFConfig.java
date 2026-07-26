package com.canoestudio.sofcore;

import net.minecraftforge.common.config.Config;

@Config(modid = SOFcore.MOD_ID)
@Config.LangKey("sofcore.config.title")
public class SOFConfig {

    @Config.Name("dynamic_trees")
    @Config.LangKey("sofcore.config.dynamic_trees")
    @Config.Comment("Compatibility fixes for Dynamic Trees.")
    public static DynamicTrees dynamicTrees = new DynamicTrees();

    @Config.Name("waystones_to_xaero")
    @Config.LangKey("sofcore.config.waystones_to_xaero")
    @Config.Comment("Automatically creates Xaero's Minimap waypoints when Waystones are activated.")
    public static WaystonesToXaero waystonesToXaero = new WaystonesToXaero();

    @Config.Name("monster_currency_drops")
    @Config.LangKey("sofcore.config.monster_currency_drops")
    @Config.Comment("Drops SOF Core currency items from monsters based on their max health.")
    public static MonsterCurrencyDrops monsterCurrencyDrops = new MonsterCurrencyDrops();

    public static class DynamicTrees {

        @Config.Name("destroy_explosion_roots")
        @Config.LangKey("sofcore.config.dynamic_trees.destroy_explosion_roots")
        @Config.Comment("When Dynamic Trees are destroyed by explosions, remove their rooty dirt after the explosion finishes.")
        public boolean destroyExplosionRoots = true;

        @Config.Name("enable_aether_world_gen_patch")
        @Config.LangKey("sofcore.config.dynamic_trees.enable_aether_world_gen_patch")
        @Config.Comment("Registers a small Aether Legacy worldgen bridge for Dynamic Trees and forces the Aether dimension out of the Dynamic Trees blacklist.")
        public boolean enableAetherWorldGenPatch = true;
    }

    public static class WaystonesToXaero {

        @Config.Name("enabled")
        @Config.LangKey("sofcore.config.waystones_to_xaero.enabled")
        @Config.Comment("Set to true to create Xaero waypoints when right-clicking Waystones.")
        public boolean enabled = true;

        @Config.Name("prevent_duplicates")
        @Config.LangKey("sofcore.config.waystones_to_xaero.prevent_duplicates")
        @Config.Comment("Set to true to skip creating a waypoint if the current Xaero waypoint set already has one with the same name and position.")
        public boolean preventDuplicates = true;
    }

    public static class MonsterCurrencyDrops {

        @Config.Name("enabled")
        @Config.LangKey("sofcore.config.monster_currency_drops.enabled")
        @Config.Comment("Set to true to make hostile mobs drop currency when killed.")
        public boolean enabled = false;

        @Config.Name("require_player_kill")
        @Config.LangKey("sofcore.config.monster_currency_drops.require_player_kill")
        @Config.Comment("Set to true to drop currency only when the mob was killed by a real player.")
        public boolean requirePlayerKill = true;

        @Config.Name("drop_chance")
        @Config.LangKey("sofcore.config.monster_currency_drops.drop_chance")
        @Config.RangeDouble(min = 0.0D, max = 1.0D)
        @Config.Comment("Chance for a killed monster to drop currency. 1.0 means always drop.")
        public double dropChance = 1.0D;

        @Config.Name("mob_division_value")
        @Config.LangKey("sofcore.config.monster_currency_drops.mob_division_value")
        @Config.RangeDouble(min = 1.0D)
        @Config.Comment("Currency amount is calculated as mob max health divided by this value.")
        public double mobDivisionValue = 1.0D;
    }
}
