package com.canoestudio.dwdccore;

import com.google.common.collect.BiMap;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Mod.EventBusSubscriber(modid = DWDCcore.MOD_ID)
public class DWDCHooks {

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
