package com.canoestudio.sofcore.compat.aetherbaubles;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.gildedgames.the_aether.api.player.util.IAccessoryInventory;
import com.gildedgames.the_aether.items.ItemsAether;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class BaublesAccessoryInventory implements IAccessoryInventory {

    private static final int AETHER_SLOTS = 8;

    private final EntityPlayer player;

    public BaublesAccessoryInventory(EntityPlayer player) {
        this.player = player;
    }

    public void migrateFrom(IAccessoryInventory oldInventory) {
        if (oldInventory == null || oldInventory == this) {
            return;
        }

        for (int slot = 0; slot < oldInventory.getSizeInventory(); slot++) {
            ItemStack stack = oldInventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                setAccessorySlot(stack.copy());
                oldInventory.setInventorySlotContents(slot, ItemStack.EMPTY);
            }
        }
    }

    private IBaublesItemHandler baubles() {
        return BaublesApi.getBaublesHandler(this.player);
    }

    @Override
    public int getSizeInventory() {
        return AETHER_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return getAccessories().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        IBaublesItemHandler baubles = baubles();
        int baublesSlot = AetherAccessoryBridge.toBaublesSlot(index);
        if (baubles == null || baublesSlot < 0 || baublesSlot >= baubles.getSlots()) {
            return ItemStack.EMPTY;
        }
        return baubles.getStackInSlot(baublesSlot);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = getStackInSlot(index);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = stack.splitStack(count);
        if (stack.getCount() <= 0) {
            setInventorySlotContents(index, ItemStack.EMPTY);
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = getStackInSlot(index);
        setInventorySlotContents(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        IBaublesItemHandler baubles = baubles();
        int baublesSlot = AetherAccessoryBridge.toBaublesSlot(index);
        if (baubles != null && baublesSlot >= 0 && baublesSlot < baubles.getSlots()) {
            baubles.setStackInSlot(baublesSlot, stack);
        }
    }

    @Override
    public String getName() {
        return "baubles_accessories";
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return !player.isDead && player.getDistanceSq(this.player) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        IBaublesItemHandler baubles = baubles();
        int baublesSlot = AetherAccessoryBridge.toBaublesSlot(index);
        return baubles != null && baublesSlot >= 0 && baublesSlot < baubles.getSlots() && baubles.isItemValidForSlot(baublesSlot, stack, this.player);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        int count = 0;
        for (ItemStack stack : getAccessories()) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void clear() {
        for (int slot = 0; slot < getSizeInventory(); slot++) {
            setInventorySlotContents(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public void dropAccessories() {
        for (int slot = 0; slot < getSizeInventory(); slot++) {
            ItemStack stack = removeStackFromSlot(slot);
            if (!stack.isEmpty()) {
                this.player.dropItem(stack, true, true);
            }
        }
    }

    @Override
    public void damageWornStack(int damage, ItemStack stack) {
        ItemStack current = getStackFromItem(stack.getItem());
        if (!current.isEmpty() && !this.player.capabilities.isCreativeMode) {
            current.damageItem(damage, this.player);
            if (current.getItemDamage() >= current.getMaxDamage()) {
                breakItem(current.getItem());
            }
        }
    }

    public int breakItem(Item item) {
        int count = 0;
        for (int slot = 0; slot < getSizeInventory(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() == item && (stack.getTagCompound() == null || !stack.getTagCompound().getBoolean("Unbreakable"))) {
                count += stack.getCount();
                setInventorySlotContents(slot, ItemStack.EMPTY);
            }
        }
        return count;
    }

    @Override
    public boolean setAccessorySlot(ItemStack stack) {
        return AetherAccessoryBridge.insertIntoBaubles(this.player, stack);
    }

    @Override
    public boolean wearingAccessory(ItemStack stack) {
        return getAccessoryCount(stack) > 0;
    }

    @Override
    public boolean wearingArmor(ItemStack stack) {
        for (ItemStack armor : this.player.inventory.armorInventory) {
            if (!armor.isEmpty() && armor.getItem() == stack.getItem()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        NonNullList<ItemStack> oldStacks = NonNullList.withSize(AETHER_SLOTS, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, oldStacks);
        for (ItemStack stack : oldStacks) {
            if (!stack.isEmpty()) {
                setAccessorySlot(stack);
            }
        }
    }

    @Override
    public void writeData(ByteBuf buf) {
        for (ItemStack stack : getAccessories()) {
            ByteBufUtils.writeItemStack(buf, stack);
        }
    }

    @Override
    public void readData(ByteBuf buf) {
        for (int i = 0; i < AETHER_SLOTS; i++) {
            setInventorySlotContents(i, ByteBufUtils.readItemStack(buf));
        }
    }

    @Override
    public boolean isWearingZaniteSet() {
        return wearingArmor(new ItemStack(ItemsAether.zanite_helmet)) && wearingArmor(new ItemStack(ItemsAether.zanite_chestplate)) && wearingArmor(new ItemStack(ItemsAether.zanite_leggings)) && wearingArmor(new ItemStack(ItemsAether.zanite_boots)) && wearingAccessory(new ItemStack(ItemsAether.zanite_gloves));
    }

    @Override
    public boolean isWearingGravititeSet() {
        return wearingArmor(new ItemStack(ItemsAether.gravitite_helmet)) && wearingArmor(new ItemStack(ItemsAether.gravitite_chestplate)) && wearingArmor(new ItemStack(ItemsAether.gravitite_leggings)) && wearingArmor(new ItemStack(ItemsAether.gravitite_boots)) && wearingAccessory(new ItemStack(ItemsAether.gravitite_gloves));
    }

    @Override
    public boolean isWearingNeptuneSet() {
        return wearingArmor(new ItemStack(ItemsAether.neptune_helmet)) && wearingArmor(new ItemStack(ItemsAether.neptune_chestplate)) && wearingArmor(new ItemStack(ItemsAether.neptune_leggings)) && wearingArmor(new ItemStack(ItemsAether.neptune_boots)) && wearingAccessory(new ItemStack(ItemsAether.neptune_gloves));
    }

    @Override
    public boolean isWearingPhoenixSet() {
        return wearingArmor(new ItemStack(ItemsAether.phoenix_helmet)) && wearingArmor(new ItemStack(ItemsAether.phoenix_chestplate)) && wearingArmor(new ItemStack(ItemsAether.phoenix_leggings)) && wearingArmor(new ItemStack(ItemsAether.phoenix_boots)) && wearingAccessory(new ItemStack(ItemsAether.phoenix_gloves));
    }

    @Override
    public boolean isWearingObsidianSet() {
        return wearingArmor(new ItemStack(ItemsAether.obsidian_helmet)) && wearingArmor(new ItemStack(ItemsAether.obsidian_chestplate)) && wearingArmor(new ItemStack(ItemsAether.obsidian_leggings)) && wearingArmor(new ItemStack(ItemsAether.obsidian_boots)) && wearingAccessory(new ItemStack(ItemsAether.obsidian_gloves));
    }

    @Override
    public boolean isWearingValkyrieSet() {
        return wearingArmor(new ItemStack(ItemsAether.valkyrie_helmet)) && wearingArmor(new ItemStack(ItemsAether.valkyrie_chestplate)) && wearingArmor(new ItemStack(ItemsAether.valkyrie_leggings)) && wearingArmor(new ItemStack(ItemsAether.valkyrie_boots)) && wearingAccessory(new ItemStack(ItemsAether.valkyrie_gloves));
    }

    @Override
    public NonNullList<ItemStack> getAccessories() {
        NonNullList<ItemStack> stacks = NonNullList.withSize(AETHER_SLOTS, ItemStack.EMPTY);
        for (int slot = 0; slot < AETHER_SLOTS; slot++) {
            stacks.set(slot, getStackInSlot(slot));
        }
        return stacks;
    }

    @Override
    public int getAccessoryCount(ItemStack stack) {
        int count = 0;
        for (ItemStack worn : getAccessories()) {
            if (!worn.isEmpty() && worn.getItem() == stack.getItem()) {
                count++;
            }
        }
        return count;
    }

    private ItemStack getStackFromItem(Item item) {
        for (ItemStack stack : getAccessories()) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
