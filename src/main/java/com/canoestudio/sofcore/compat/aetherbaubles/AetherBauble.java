package com.canoestudio.sofcore.compat.aetherbaubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class AetherBauble implements IBauble {

    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return AetherAccessoryBridge.toBaubleType(itemstack);
    }

    @Override
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
        return AetherAccessoryBridge.isAetherAccessory(itemstack);
    }

    @Override
    public boolean willAutoSync(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }
}
