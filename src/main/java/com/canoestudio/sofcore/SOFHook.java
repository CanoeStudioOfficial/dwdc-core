package com.canoestudio.sofcore;

import com.canoestudio.sofcore.compat.DynamicTreesCompat;
import com.google.common.collect.BiMap;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = SOFcore.MOD_ID)
public class SOFHook {

    private static final Map<Integer, Set<BlockPos>> PENDING_DYNAMIC_TREE_ROOT_DESTRUCTIONS = new HashMap<>();

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (SOFcore.MOD_ID.equals(event.getModID())) {
            ConfigManager.sync(SOFcore.MOD_ID, Config.Type.INSTANCE);
            if (!SOFConfig.dynamicTrees.destroyExplosionRoots) {
                PENDING_DYNAMIC_TREE_ROOT_DESTRUCTIONS.clear();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        World world = event.getWorld();
        if (!SOFConfig.dynamicTrees.destroyExplosionRoots || world.isRemote) {
            return;
        }

        Set<BlockPos> roots = DynamicTreesCompat.collectExplosionRoots(world, event.getAffectedBlocks());
        if (roots.isEmpty()) {
            return;
        }

        int dimension = world.provider.getDimension();
        Set<BlockPos> pendingRoots = PENDING_DYNAMIC_TREE_ROOT_DESTRUCTIONS.computeIfAbsent(dimension, key -> new HashSet<>());
        pendingRoots.addAll(roots);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side != Side.SERVER || event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!SOFConfig.dynamicTrees.destroyExplosionRoots) {
            PENDING_DYNAMIC_TREE_ROOT_DESTRUCTIONS.clear();
            return;
        }

        World world = event.world;
        Set<BlockPos> roots = PENDING_DYNAMIC_TREE_ROOT_DESTRUCTIONS.remove(world.provider.getDimension());
        if (roots != null) {
            DynamicTreesCompat.destroyRoots(world, roots);
        }
    }

    // --- Fast Fly Break Logic ---
    @SubscribeEvent
    public static void blockBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!player.onGround && player.capabilities.isFlying) {
            event.setNewSpeed(event.getOriginalSpeed() * 5);
        }
    }

    // --- Texture Stitch Logic (Client Only) ---
    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        BiMap<String, Fluid> masterFluidReference = ObfuscationReflectionHelper.getPrivateValue(FluidRegistry.class, null, "masterFluidReference");
        TextureMap map = event.getMap();

        for (Fluid fluid : masterFluidReference.values()) {
            map.registerSprite(fluid.getStill());
            map.registerSprite(fluid.getFlowing());
        }
    }

    // --- Screenshot to Clipboard Logic (Client Only) ---
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void handleScreenshot(ScreenshotEvent event) {
        new Thread(() -> {
            Transferable trans = getTransferableImage(event.getImage());
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            c.setContents(trans, null);
        }).start();
    }

    private static Transferable getTransferableImage(final BufferedImage bufferedImage) {
        return new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { DataFlavor.imageFlavor };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.imageFlavor.equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (DataFlavor.imageFlavor.equals(flavor)) {
                    return bufferedImage;
                }
                throw new UnsupportedFlavorException(flavor);
            }
        };
    }
}
