package com.ikarsoft.rd;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.client.FMLClientHandler;

public class SessionData {
    private boolean onServer = false;
    private boolean joinedWorld = false;
    private String worldUid = null;

    private static SessionData __instance;

    private SessionData() {

    }

    public static SessionData instance() {
        if(__instance == null) {
            __instance = new SessionData();
        }

        return __instance;
    }

    public boolean isOnServer() {
        return onServer;
    }

    public void setOnServer(boolean onServer) {
        this.onServer = onServer;
    }

    public boolean isJoinedWorld() {
        return joinedWorld;
    }

    public boolean hasJoinedWorld() {
        return joinedWorld;
    }

    public void setJoinedWorld(boolean joinedWorld) {
        this.joinedWorld = joinedWorld;
    }

    public void setWorldUid(String worldUid) {
        this.worldUid = worldUid;
    }

    public void onConnectedToServer(boolean onServer) {
        this.onServer = onServer;
        this.joinedWorld = false;
        this.worldUid = null;
    }

    public String getWorldUid() {
        if (worldUid == null) {
            FMLClientHandler fmlClientHandler = FMLClientHandler.instance();
            final NetworkManager networkManager = fmlClientHandler.getClientToServerNetworkManager();
            if (networkManager == null) {
                worldUid = "default";
            } else if (networkManager.isLocalChannel()) {
                final MinecraftServer minecraftServer = fmlClientHandler.getServer();
                if (minecraftServer != null) {
                    worldUid = minecraftServer.getFolderName();
                }
            } else {
                final ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
                if (serverData != null) {
                    worldUid = serverData.serverIP + ' ' + serverData.serverName;
                }
            }

            if (worldUid == null) {
                worldUid = "default";
            }
            worldUid = "world" + Integer.toString(worldUid.hashCode());
        }
        return worldUid;
    }
}
