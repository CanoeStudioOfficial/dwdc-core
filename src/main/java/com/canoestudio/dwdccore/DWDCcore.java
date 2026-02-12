package com.canoestudio.dwdccore;

import com.canoestudio.dwdccore.proxy.CommonProxy;
import com.canoestudio.dwdccore.kleeslabs.KleeSlabs;

import net.minecraftforge.common.util.PacketUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class DWDCcore {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    public static final String MOD_ID = Tags.MOD_ID;

    @SidedProxy(clientSide = "com.canoestudio.dwdccore.proxy.ClientProxy", serverSide = "com.canoestudio.dwdccore.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static final KleeSlabs kleeslabs = new KleeSlabs();


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);
        kleeslabs.preInit(event);
        proxy.preInit(event); }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) { proxy.init(event); }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        kleeslabs.postInit(event);
        proxy.postInit(event); }
}
