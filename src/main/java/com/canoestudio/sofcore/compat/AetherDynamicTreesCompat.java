package com.canoestudio.sofcore.compat;

import com.canoestudio.sofcore.SOFConfig;
import com.canoestudio.sofcore.SOFcore;
import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDataBasePopulator;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBasePopulatorJson;
import com.ferreusveritas.dynamictrees.worldgen.WorldGeneratorTrees;
import com.gildedgames.the_aether.AetherConfig;
import com.gildedgames.the_aether.blocks.BlocksAether;
import maxhyper.dynamictreestheaether.DynamicTreesTheAether;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SOFcore.MOD_ID)
public final class AetherDynamicTreesCompat {

    private static final ResourceLocation WORLDGEN_DATA = new ResourceLocation(SOFcore.MOD_ID, "worldgen/aether_dynamic_trees.json");

    private AetherDynamicTreesCompat() {
    }

    public static void init() {
        if (!SOFConfig.dynamicTrees.enableAetherWorldGenPatch) {
            return;
        }

        allowAetherDynamicTreeGeneration();
        registerSaplingReplacement(BlocksAether.golden_oak_sapling, "goldenoak");
        registerSaplingReplacement(BlocksAether.skyroot_sapling, "skyroot");
    }

    @SubscribeEvent
    public static void registerBiomeDataBasePopulator(WorldGenRegistry.BiomeDataBasePopulatorRegistryEvent event) {
        if (SOFConfig.dynamicTrees.enableAetherWorldGenPatch) {
            event.register(new AetherPopulator());
        }
    }

    private static void allowAetherDynamicTreeGeneration() {
        int aetherDimension = AetherConfig.dimension.aether_dimension_id;
        ModConfigs.dimensionBlacklist.remove(aetherDimension);
        WorldGeneratorTrees.dimensionForceGeneration.add(world -> world.provider.getDimension() == aetherDimension);
        SOFcore.LOGGER.info("Enabled Dynamic Trees world generation in Aether dimension {}.", aetherDimension);
    }

    private static void registerSaplingReplacement(Block saplingBlock, String speciesName) {
        TreeRegistry.registerSaplingReplacer(
                saplingBlock.getDefaultState(),
                TreeRegistry.findSpecies(new ResourceLocation(DynamicTreesTheAether.MODID, speciesName))
        );
    }

    private static final class AetherPopulator implements IBiomeDataBasePopulator {

        private final BiomeDataBasePopulatorJson jsonPopulator = new BiomeDataBasePopulatorJson(WORLDGEN_DATA);

        @Override
        public void populate(BiomeDataBase dataBase) {
            allowAetherDynamicTreeGeneration();
            jsonPopulator.populate(dataBase);
        }
    }
}
