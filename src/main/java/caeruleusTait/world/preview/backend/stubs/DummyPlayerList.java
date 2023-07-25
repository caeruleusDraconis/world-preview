package caeruleusTait.world.preview.backend.stubs;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;

public class DummyPlayerList extends PlayerList {
    public DummyPlayerList(
            MinecraftServer minecraftServer,
            LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
            PlayerDataStorage playerDataStorage,
            int maxPlayers
    ) {
        super(minecraftServer, layeredRegistryAccess, playerDataStorage, maxPlayers);
    }
}
