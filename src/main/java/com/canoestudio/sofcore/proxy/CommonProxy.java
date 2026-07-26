package com.canoestudio.sofcore.proxy;

import com.canoestudio.sofcore.compat.AetherDynamicTreesCompat;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {

    }

    public void init(FMLInitializationEvent event) {
        AetherDynamicTreesCompat.init();
    }

    public void postInit(FMLPostInitializationEvent event) {

    }

}
