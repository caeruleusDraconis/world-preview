package caeruleusTait.world.preview.backend.stubs;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.DataFixer;
import net.minecraft.SystemReport;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Proxy;
import java.util.UUID;

public class DummyMinecraftServer extends MinecraftServer {
    public DummyMinecraftServer(
            Thread thread,
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            PackRepository packRepository,
            WorldStem worldStem,
            Proxy proxy,
            DataFixer dataFixer,
            Services services,
            ChunkProgressListenerFactory chunkProgressListenerFactory
    ) {
        super(thread, levelStorageAccess, packRepository, worldStem, proxy, dataFixer, services, chunkProgressListenerFactory);
        this.setSingleplayerProfile(new GameProfile(UUID.randomUUID(), "world-preview"));
        this.setDemo(false);
        this.setPlayerList(new DummyPlayerList(this, this.registries(), this.playerDataStorage, 1));
    }

    @Override
    protected boolean initServer() throws IOException {
        return false;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return 0;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return 0;
    }

    @Override
    public boolean shouldRconBroadcast() {
        return false;
    }

    @Override
    public @NotNull SystemReport fillServerSystemReport(@NotNull SystemReport report) {
        return report;
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return 0;
    }

    @Override
    public boolean isEpollEnabled() {
        return false;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return false;
    }

    @Override
    public boolean isPublished() {
        return false;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

    @Override
    public boolean isSingleplayerOwner(@NotNull GameProfile profile) {
        return false;
    }
}
