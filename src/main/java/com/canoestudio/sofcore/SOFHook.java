package com.canoestudio.sofcore;

import com.canoestudio.sofcore.compat.DynamicTreesCompat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = SOFcore.MOD_ID)
public class SOFHook {

    private static final Map<Integer, Set<BlockPos>> PENDING_DYNAMIC_TREE_ROOT_DESTRUCTIONS = new HashMap<>();

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
}
