package com.canoestudio.dwdccore;

import com.google.common.collect.ImmutableSet;
import net.minecraftforge.common.config.Config;

import java.util.Set;

@Config(modid = DWDCcore.MOD_ID, name = DWDCcore.MOD_ID)
public class DWDCConfig {

    @Config.Comment("Enable/disable disposable crafting tables")
    @Config.LangKey("dwdccore.config.enableDisposableCrafting")
    public static boolean enableDisposableCrafting = true;

    @Config.Comment("Enable/disable disposable furnaces")
    @Config.LangKey("dwdccore.config.enableDisposableFurnace")
    public static boolean enableDisposableFurnace = true;

    @Config.Comment("List of crafting table block IDs that should be disposable")
    @Config.LangKey("dwdccore.config.disposableCraftingTables")
    public static String[] disposableCraftingTables = {
        "minecraft:crafting_table"
    };

    @Config.Comment("List of furnace block IDs that should be disposable")
    @Config.LangKey("dwdccore.config.disposableFurnaces")
    public static String[] disposableFurnaces = {
        "minecraft:furnace",
        "minecraft:lit_furnace"
    };

    @Config.Comment("Should the block drop items when broken after crafting/smelting")
    @Config.LangKey("dwdccore.config.dropItemsOnBreak")
    public static boolean dropItemsOnBreak = false;

    @Config.Comment("Delay in ticks before breaking the block after crafting/smelting (0 = immediate)")
    @Config.LangKey("dwdccore.config.breakDelay")
    @Config.RangeInt(min = 0, max = 100)
    public static int breakDelay = 0;

    private static Set<String> cachedCraftingTables;
    private static Set<String> cachedFurnaces;

    public static Set<String> getCachedCraftingTables() {
        if (cachedCraftingTables == null) {
            cachedCraftingTables = ImmutableSet.copyOf(disposableCraftingTables);
        }
        return cachedCraftingTables;
    }

    public static Set<String> getCachedFurnaces() {
        if (cachedFurnaces == null) {
            cachedFurnaces = ImmutableSet.copyOf(disposableFurnaces);
        }
        return cachedFurnaces;
    }

    public static void clearCache() {
        cachedCraftingTables = null;
        cachedFurnaces = null;
    }
}
