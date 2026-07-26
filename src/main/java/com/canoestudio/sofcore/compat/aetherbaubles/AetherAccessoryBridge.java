package com.canoestudio.sofcore.compat.aetherbaubles;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.gildedgames.the_aether.api.AetherAPI;
import com.gildedgames.the_aether.api.accessories.AccessoryType;
import com.gildedgames.the_aether.api.accessories.AetherAccessory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

final class AetherAccessoryBridge {

    private AetherAccessoryBridge() {
    }

    static boolean isAetherAccessory(ItemStack stack) {
        return !stack.isEmpty() && AetherAPI.getInstance().isAccessory(stack);
    }

    static AccessoryType getAetherType(ItemStack stack) {
        AetherAccessory accessory = AetherAPI.getInstance().getAccessory(stack);
        return accessory == null ? AccessoryType.MISC : accessory.getAccessoryType();
    }

    static BaubleType toBaubleType(ItemStack stack) {
        switch (getAetherType(stack)) {
            case RING:
                return BaubleType.RING;
            case PENDANT:
                return BaubleType.AMULET;
            case CAPE:
                return BaubleType.BODY;
            case GLOVE:
                return BaubleType.BELT;
            case SHIELD:
                return BaubleType.CHARM;
            case MISC:
            default:
                return BaubleType.TRINKET;
        }
    }

    static int toBaublesSlot(int aetherSlot) {
        switch (aetherSlot) {
            case 0:
                return 0;
            case 1:
                return 5;
            case 2:
                return 6;
            case 4:
                return 1;
            case 5:
                return 2;
            case 6:
                return 3;
            case 3:
                return 4;
            case 7:
            default:
                return -1;
        }
    }

    static boolean insertIntoBaubles(EntityPlayer player, ItemStack stack) {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        if (baubles == null || stack.isEmpty()) {
            return false;
        }

        for (int slot : toBaubleType(stack).getValidSlots()) {
            if (slot < baubles.getSlots() && baubles.getStackInSlot(slot).isEmpty() && baubles.isItemValidForSlot(slot, stack, player)) {
                baubles.setStackInSlot(slot, stack.copy());
                return true;
            }
        }

        return false;
    }
}
