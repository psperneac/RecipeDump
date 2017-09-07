package com.ikarsoft.rd;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ProxyClient extends Proxy {

    protected boolean started = false;
    private final RecipeDumpStarter starter = new RecipeDumpStarter();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        // Reload when resources change
        Minecraft minecraft = Minecraft.getMinecraft();
        IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) minecraft.getResourceManager();
        reloadableResourceManager.registerReloadListener(resourceManager -> {
            if (SessionData.instance().hasJoinedWorld()) {
                // check that RecipeDump has been started before. if not, do nothing
                if (this.starter.hasStarted()) {
                    Log.get().info("Restarting RecipeDump.");
                    this.starter.start();
                }
            }
        });

        try {
            this.starter.start();
        } catch (Exception e) {
            Log.get().error("Exception on load", e);
        }
    }

    @SubscribeEvent
    public void onEntityJoinedWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote && !SessionData.instance().hasJoinedWorld() && Minecraft.getMinecraft().player != null) {
            SessionData.instance().setJoinedWorld(true);
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (!RecipeDump.MODID.equals(eventArgs.getModID())) {
            return;
        }

//        if (Config.syncAllConfig()) {
//            reloadItemList();
//        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
//        try {
//            Config.saveFilterText();
//        } catch (RuntimeException e) {
//            Log.get().error("Failed to save filter text.", e);
//        }
    }

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (!event.isLocal() && !event.getConnectionType().equals("MODDED")) {
            SessionData.instance().onConnectedToServer(false);
        }
    }
}
