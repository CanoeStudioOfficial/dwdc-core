package com.canoestudio.dwdccore.compat.fastflybreak;

import com.canoestudio.dwdccore.DWDCcore;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = DWDCcore.MOD_ID)
public class FFBB {
    @SubscribeEvent
    @SideOnly(Side.SERVER)
    public void blockBreakSpeed(PlayerEvent.BreakSpeed event){
        if(!event.getEntityPlayer().onGround && (event.getEntityPlayer().capabilities.isFlying)){
            event.setNewSpeed(event.getOriginalSpeed() * 5);
        }
    }

}
