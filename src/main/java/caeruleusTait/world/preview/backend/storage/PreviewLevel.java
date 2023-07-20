package caeruleusTait.world.preview.backend.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class PreviewLevel implements WorldGenLevel {

    private final RegistryAccess registryAccess;
    private final LevelHeightAccessor levelHeightAccessor;
    private final Registry<Biome> biomeRegistry;
    private final Long2ObjectMap<ProtoChunk> chunks = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());

    public PreviewLevel(RegistryAccess registryAccess, LevelHeightAccessor levelHeightAccessor) {
        this.registryAccess = registryAccess;
        this.levelHeightAccessor = levelHeightAccessor;
        this.biomeRegistry = this.registryAccess.registryOrThrow(Registries.BIOME);
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return new ProtoChunk(new ChunkPos(x, z), UpgradeData.EMPTY, levelHeightAccessor, biomeRegistry, null);

        // Actually storing chunks would take up too much space
        /*
        return chunks.computeIfAbsent(
                ChunkPos.asLong(x, z),
                i -> new ProtoChunk(new ChunkPos(x, z), UpgradeData.EMPTY, levelHeightAccessor, biomeRegistry, null)
        );
         */
    }

    @Override
    public RegistryAccess registryAccess() {
        return registryAccess;
    }

    // Stuff we don't need but still need to implement:

    @Override
    public long getSeed() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ServerLevel getLevel() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public long nextSubTickCount() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public LevelData getLevelData() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        throw new NotImplementedException("Not implemented");
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public ChunkSource getChunkSource() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public RandomSource getRandom() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public void playSound(@Nullable Player player, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public void addParticle(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public void gameEvent(GameEvent event, Vec3 position, GameEvent.Context context) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public LevelLightEngine getLightEngine() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public WorldBorder getWorldBorder() {
        throw new NotImplementedException("Not implemented");
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB area, Predicate<? super Entity> predicate) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds, Predicate<? super T> predicate) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public List<? extends Player> players() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public int getSkyDarken() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public BiomeManager getBiomeManager() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public boolean isClientSide() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public int getSeaLevel() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public DimensionType dimensionType() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> state) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public boolean isFluidAtPosition(BlockPos pos, Predicate<FluidState> predicate) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
        throw new NotImplementedException("Not implemented");
    }
}
