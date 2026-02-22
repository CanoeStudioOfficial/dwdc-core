package com.canoestudio.dwdccore;

import com.google.common.collect.BiMap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Mod.EventBusSubscriber(modid = DWDCcore.MOD_ID)
public class DWDCHooks {

    private static java.lang.reflect.Field workbenchPosField;
    private static java.lang.reflect.Field furnaceTileEntityField;
    private static boolean reflectionFieldsInitialized = false;

    private static void initReflectionFields() {
        if (reflectionFieldsInitialized) {
            return;
        }
        try {
            workbenchPosField = ContainerWorkbench.class.getDeclaredField("pos");
            workbenchPosField.setAccessible(true);
            
            furnaceTileEntityField = net.minecraft.inventory.ContainerFurnace.class.getDeclaredField("tileFurnace");
            furnaceTileEntityField.setAccessible(true);
            
            reflectionFieldsInitialized = true;
        } catch (NoSuchFieldException e) {
            DWDCcore.LOGGER.error("Failed to initialize reflection fields", e);
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

    // --- Config Reload Handler ---
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(DWDCcore.MOD_ID)) {
            ConfigManager.sync(DWDCcore.MOD_ID, net.minecraftforge.common.config.Config.Type.INSTANCE);
            DWDCConfig.clearCache();
        }
    }

    // --- Crafting Table Disposable Handler ---
    @SubscribeEvent
    public static void onItemCrafted(net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent event) {
        if (!DWDCConfig.enableDisposableCrafting) {
            return;
        }

        EntityPlayer player = event.player;
        Container container = player.openContainer;

        if (!(container instanceof ContainerWorkbench)) {
            return;
        }

        BlockPos pos = getWorkbenchPos(container);
        if (pos == null) {
            return;
        }

        World world = player.world;
        Block block = world.getBlockState(pos).getBlock();
        String blockId = Block.REGISTRY.getNameForObject(block).toString();

        if (DWDCConfig.getCachedCraftingTables().contains(blockId)) {
            scheduleBlockBreak(world, pos, block);
        }
    }

    // --- Furnace Disposable Handler ---
    @SubscribeEvent
    public static void onItemSmelted(net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent event) {
        if (!DWDCConfig.enableDisposableFurnace) {
            return;
        }

        EntityPlayer player = event.player;
        Container container = player.openContainer;

        if (container == null) {
            return;
        }

        BlockPos pos = getFurnacePos(container);
        if (pos == null) {
            return;
        }

        World world = player.world;
        Block block = world.getBlockState(pos).getBlock();
        String blockId = Block.REGISTRY.getNameForObject(block).toString();

        if (DWDCConfig.getCachedFurnaces().contains(blockId)) {
            scheduleBlockBreak(world, pos, block);
        }
    }

    private static BlockPos getWorkbenchPos(Container container) {
        initReflectionFields();
        if (workbenchPosField == null) {
            return null;
        }
        try {
            Object pos = workbenchPosField.get(container);
            if (pos instanceof BlockPos) {
                return (BlockPos) pos;
            }
        } catch (Exception e) {
            DWDCcore.LOGGER.error("Failed to get workbench position", e);
        }
        return null;
    }

    private static BlockPos getFurnacePos(Container container) {
        initReflectionFields();
        if (furnaceTileEntityField == null) {
            return null;
        }
        try {
            Object tileEntity = furnaceTileEntityField.get(container);
            if (tileEntity instanceof TileEntityFurnace) {
                return ((TileEntityFurnace) tileEntity).getPos();
            } else if (tileEntity instanceof IInventory) {
                IInventory inv = (IInventory) tileEntity;
                if (inv instanceof TileEntityFurnace) {
                    return ((TileEntityFurnace) inv).getPos();
                }
            }
        } catch (Exception e) {
            DWDCcore.LOGGER.error("Failed to get furnace position", e);
        }
        return null;
    }

    private static void scheduleBlockBreak(World world, BlockPos pos, Block block) {
        if (DWDCConfig.breakDelay > 0) {
            world.scheduleUpdate(pos, block, DWDCConfig.breakDelay);
        } else {
            breakBlock(world, pos);
        }
    }

    private static void breakBlock(World world, BlockPos pos) {
        if (DWDCConfig.dropItemsOnBreak) {
            Block block = world.getBlockState(pos).getBlock();
            block.dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
        }
        world.setBlockToAir(pos);
    }
}
