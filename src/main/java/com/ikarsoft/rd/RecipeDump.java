package com.ikarsoft.rd;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

@Mod(modid = RecipeDump.MODID,
        name = RecipeDump.NAME,
        version = RecipeDump.VERSION,
        //guiFactory = "mezz.jei.config.JEIModGuiFactory",
        acceptedMinecraftVersions = "[1.12,1.13)",
        dependencies = "required-after:forge@[14.21.0.2348,);")
public class RecipeDump
{
    public static final String NAME = "RecipeDump";
    public static final String MODID = "recipedump";
    public static final String VERSION = "1.0";

    @SidedProxy(clientSide = "com.ikarsoft.rd.ProxyClient", serverSide = "com.ikarsoft.rd.Proxy")
    private static Proxy proxy;

    public static Proxy getProxy() {
        return proxy;
    }

    @NetworkCheckHandler
    public boolean checkModLists(Map<String, String> modList, Side side) {
        if (side == Side.SERVER) {
            boolean onServer = modList.containsKey(MODID);
            SessionData.instance().onConnectedToServer(onServer);
        }

        return true;
    }

    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadComplete(event);
    }
}
