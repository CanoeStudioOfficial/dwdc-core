package com.canoestudio.sofcore.compat.aetherbaubles;

import baubles.api.cap.BaublesCapabilities;
import com.canoestudio.sofcore.SOFcore;
import com.gildedgames.the_aether.api.AetherAPI;
import com.gildedgames.the_aether.api.player.IPlayerAether;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = SOFcore.MOD_ID)
public final class AetherBaublesCompat {

    private static final ResourceLocation AETHER_BAUBLE_CAP = new ResourceLocation(SOFcore.MOD_ID, "aether_bauble");

    private AetherBaublesCompat() {
    }

    public static boolean isLoaded() {
        return Loader.isModLoaded("aether_legacy") && Loader.isModLoaded("baubles");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @Optional.Method(modid = "baubles")
    public static void attachBaubleCapability(AttachCapabilitiesEvent<ItemStack> event) {
        if (!isLoaded()) {
            return;
        }

        ItemStack stack = event.getObject();
        if (stack.isEmpty() || stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null) || !AetherAccessoryBridge.isAetherAccessory(stack)) {
            return;
        }

        event.addCapability(AETHER_BAUBLE_CAP, new ICapabilityProvider() {
            private final AetherBauble bauble = new AetherBauble();

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable net.minecraft.util.EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
            }

            @Override
            @Nullable
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable net.minecraft.util.EnumFacing facing) {
                return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE ? BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(this.bauble) : null;
            }
        });
    }

    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent event) {
        installInventoryBridge(event.player);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        installInventoryBridge(event.getEntityPlayer());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            installInventoryBridge(event.player);
        }
    }

    @SubscribeEvent
    public static void hideAetherAccessoryButton(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!isLoaded() || !(event.getGui() instanceof GuiContainer)) {
            return;
        }

        event.getButtonList().removeIf(button -> button.id == 18067);
    }

    public static void installInventoryBridge(EntityPlayer player) {
        if (!isLoaded() || player == null) {
            return;
        }

        IPlayerAether playerAether = AetherAPI.getInstance().get(player);
        if (playerAether != null && !(playerAether.getAccessoryInventory() instanceof BaublesAccessoryInventory)) {
            BaublesAccessoryInventory bridge = new BaublesAccessoryInventory(player);
            bridge.migrateFrom(playerAether.getAccessoryInventory());
            playerAether.setAccessoryInventory(bridge);
        }
    }
}
