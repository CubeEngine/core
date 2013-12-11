package de.cubeisland.engine.core.world;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.BlockChangeDelegate;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeType;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.Core;

public class World implements org.bukkit.World
{
    private final org.bukkit.World bukkitWorld;
    private final Core core;

    public World(org.bukkit.World bukkitWorld, Core core)
    {
        this.bukkitWorld = bukkitWorld;
        this.core = core;
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire)
    {
        return bukkitWorld.createExplosion(x, y, z, power, setFire);
    }

    @Override
    public Block getBlockAt(int x, int y, int z)
    {
        return bukkitWorld.getBlockAt(x, y, z);
    }

    @Override
    public Block getBlockAt(Location location)
    {
        return bukkitWorld.getBlockAt(location);
    }

    @Override
    @Deprecated
    public int getBlockTypeIdAt(int x, int y, int z)
    {
        return bukkitWorld.getBlockTypeIdAt(x, y, z);
    }

    @Override
    @Deprecated
    public int getBlockTypeIdAt(Location location)
    {
        return bukkitWorld.getBlockTypeIdAt(location);
    }

    @Override
    public int getHighestBlockYAt(int x, int z)
    {
        return bukkitWorld.getHighestBlockYAt(x, z);
    }

    @Override
    public int getHighestBlockYAt(Location location)
    {
        return bukkitWorld.getHighestBlockYAt(location);
    }

    @Override
    public Block getHighestBlockAt(int x, int z)
    {
        return bukkitWorld.getHighestBlockAt(x, z);
    }

    @Override
    public Block getHighestBlockAt(Location location)
    {
        return bukkitWorld.getHighestBlockAt(location);
    }

    @Override
    public Chunk getChunkAt(int x, int z)
    {
        return bukkitWorld.getChunkAt(x, z);
    }

    @Override
    public Chunk getChunkAt(Location location)
    {
        return bukkitWorld.getChunkAt(location);
    }

    @Override
    public Chunk getChunkAt(Block block)
    {
        return bukkitWorld.getChunkAt(block);
    }

    @Override
    public boolean isChunkLoaded(Chunk chunk)
    {
        return bukkitWorld.isChunkLoaded(chunk);
    }

    @Override
    public Chunk[] getLoadedChunks()
    {
        return bukkitWorld.getLoadedChunks();
    }

    @Override
    public void loadChunk(Chunk chunk)
    {
        bukkitWorld.loadChunk(chunk);
    }

    @Override
    public boolean isChunkLoaded(int x, int z)
    {
        return bukkitWorld.isChunkLoaded(x, z);
    }

    @Override
    public boolean isChunkInUse(int x, int z)
    {
        return bukkitWorld.isChunkInUse(x, z);
    }

    @Override
    public void loadChunk(int x, int z)
    {
        bukkitWorld.loadChunk(x, z);
    }

    @Override
    public boolean loadChunk(int x, int z, boolean generate)
    {
        return bukkitWorld.loadChunk(x, z, generate);
    }

    @Override
    public boolean unloadChunk(Chunk chunk)
    {
        return bukkitWorld.unloadChunk(chunk);
    }

    @Override
    public boolean unloadChunk(int x, int z)
    {
        return bukkitWorld.unloadChunk(x, z);
    }

    @Override
    public boolean unloadChunk(int x, int z, boolean save)
    {
        return bukkitWorld.unloadChunk(x, z, save);
    }

    @Override
    public boolean unloadChunk(int x, int z, boolean save, boolean safe)
    {
        return bukkitWorld.unloadChunk(x, z, save, safe);
    }

    @Override
    public boolean unloadChunkRequest(int x, int z)
    {
        return bukkitWorld.unloadChunkRequest(x, z);
    }

    @Override
    public boolean unloadChunkRequest(int x, int z, boolean safe)
    {
        return bukkitWorld.unloadChunkRequest(x, z, safe);
    }

    @Override
    public boolean regenerateChunk(int x, int z)
    {
        return bukkitWorld.regenerateChunk(x, z);
    }

    @Override
    public boolean refreshChunk(int x, int z)
    {
        return bukkitWorld.refreshChunk(x, z);
    }

    @Override
    public Item dropItem(Location location, ItemStack item)
    {
        return bukkitWorld.dropItem(location, item);
    }

    @Override
    public Item dropItemNaturally(Location location, ItemStack item)
    {
        return bukkitWorld.dropItemNaturally(location, item);
    }

    @Override
    public Arrow spawnArrow(Location location, Vector direction, float speed, float spread)
    {
        return bukkitWorld.spawnArrow(location, direction, speed, spread);
    }

    @Override
    public boolean generateTree(Location location, TreeType type)
    {
        return bukkitWorld.generateTree(location, type);
    }

    @Override
    public boolean generateTree(Location loc, TreeType type, BlockChangeDelegate delegate)
    {
        return bukkitWorld.generateTree(loc, type, delegate);
    }

    @Override
    public Entity spawnEntity(Location loc, EntityType type)
    {
        return bukkitWorld.spawnEntity(loc, type);
    }

    @Override
    @Deprecated
    public LivingEntity spawnCreature(Location loc, EntityType type)
    {
        return bukkitWorld.spawnCreature(loc, type);
    }

    @Override
    @Deprecated
    public LivingEntity spawnCreature(Location loc, CreatureType type)
    {
        return bukkitWorld.spawnCreature(loc, type);
    }

    @Override
    public LightningStrike strikeLightning(Location loc)
    {
        return bukkitWorld.strikeLightning(loc);
    }

    @Override
    public LightningStrike strikeLightningEffect(Location loc)
    {
        return bukkitWorld.strikeLightningEffect(loc);
    }

    @Override
    public List<Entity> getEntities()
    {
        return bukkitWorld.getEntities();
    }

    @Override
    public List<LivingEntity> getLivingEntities()
    {
        return bukkitWorld.getLivingEntities();
    }

    @Override
    @Deprecated
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... classes)
    {
        return bukkitWorld.getEntitiesByClass(classes);
    }

    @Override
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> cls)
    {
        return bukkitWorld.getEntitiesByClass(cls);
    }

    @Override
    public Collection<Entity> getEntitiesByClasses(Class<?>... classes)
    {
        return bukkitWorld.getEntitiesByClasses(classes);
    }

    @Override
    public List<Player> getPlayers()
    {
        return bukkitWorld.getPlayers();
    }

    @Override
    public String getName()
    {
        return bukkitWorld.getName();
    }

    @Override
    public UUID getUID()
    {
        return bukkitWorld.getUID();
    }

    @Override
    public Location getSpawnLocation()
    {
        return bukkitWorld.getSpawnLocation();
    }

    @Override
    public boolean setSpawnLocation(int x, int y, int z)
    {
        return bukkitWorld.setSpawnLocation(x, y, z);
    }

    @Override
    public long getTime()
    {
        return bukkitWorld.getTime();
    }

    @Override
    public void setTime(long time)
    {
        bukkitWorld.setTime(time);
    }

    @Override
    public long getFullTime()
    {
        return bukkitWorld.getFullTime();
    }

    @Override
    public void setFullTime(long time)
    {
        bukkitWorld.setFullTime(time);
    }

    @Override
    public boolean hasStorm()
    {
        return bukkitWorld.hasStorm();
    }

    @Override
    public void setStorm(boolean hasStorm)
    {
        bukkitWorld.setStorm(hasStorm);
    }

    @Override
    public int getWeatherDuration()
    {
        return bukkitWorld.getWeatherDuration();
    }

    @Override
    public void setWeatherDuration(int duration)
    {
        bukkitWorld.setWeatherDuration(duration);
    }

    @Override
    public boolean isThundering()
    {
        return bukkitWorld.isThundering();
    }

    @Override
    public void setThundering(boolean thundering)
    {
        bukkitWorld.setThundering(thundering);
    }

    @Override
    public int getThunderDuration()
    {
        return bukkitWorld.getThunderDuration();
    }

    @Override
    public void setThunderDuration(int duration)
    {
        bukkitWorld.setThunderDuration(duration);
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power)
    {
        return bukkitWorld.createExplosion(x, y, z, power);
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks)
    {
        return bukkitWorld.createExplosion(x, y, z, power, setFire, breakBlocks);
    }

    @Override
    public boolean createExplosion(Location loc, float power)
    {
        return bukkitWorld.createExplosion(loc, power);
    }

    @Override
    public boolean createExplosion(Location loc, float power, boolean setFire)
    {
        return bukkitWorld.createExplosion(loc, power, setFire);
    }

    @Override
    public Environment getEnvironment()
    {
        return bukkitWorld.getEnvironment();
    }

    @Override
    public long getSeed()
    {
        return bukkitWorld.getSeed();
    }

    @Override
    public boolean getPVP()
    {
        return bukkitWorld.getPVP();
    }

    @Override
    public void setPVP(boolean pvp)
    {
        bukkitWorld.setPVP(pvp);
    }

    @Override
    public ChunkGenerator getGenerator()
    {
        return bukkitWorld.getGenerator();
    }

    @Override
    public void save()
    {
        bukkitWorld.save();
    }

    @Override
    public List<BlockPopulator> getPopulators()
    {
        return bukkitWorld.getPopulators();
    }

    @Override
    public <T extends Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException
    {
        return bukkitWorld.spawn(location, clazz);
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, Material material, byte data) throws IllegalArgumentException
    {
        return bukkitWorld.spawnFallingBlock(location, material, data);
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, int blockId, byte blockData) throws IllegalArgumentException
    {
        return bukkitWorld.spawnFallingBlock(location, blockId, blockData);
    }

    @Override
    public void playEffect(Location location, Effect effect, int data)
    {
        bukkitWorld.playEffect(location, effect, data);
    }

    @Override
    public void playEffect(Location location, Effect effect, int data, int radius)
    {
        bukkitWorld.playEffect(location, effect, data, radius);
    }

    @Override
    public <T> void playEffect(Location location, Effect effect, T data)
    {
        bukkitWorld.playEffect(location, effect, data);
    }

    @Override
    public <T> void playEffect(Location location, Effect effect, T data, int radius)
    {
        bukkitWorld.playEffect(location, effect, data, radius);
    }

    @Override
    public ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome, boolean includeBiomeTempRain)
    {
        return bukkitWorld.getEmptyChunkSnapshot(x, z, includeBiome, includeBiomeTempRain);
    }

    @Override
    public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals)
    {
        bukkitWorld.setSpawnFlags(allowMonsters, allowAnimals);
    }

    @Override
    public boolean getAllowAnimals()
    {
        return bukkitWorld.getAllowAnimals();
    }

    @Override
    public boolean getAllowMonsters()
    {
        return bukkitWorld.getAllowMonsters();
    }

    @Override
    public Biome getBiome(int x, int z)
    {
        return bukkitWorld.getBiome(x, z);
    }

    @Override
    public void setBiome(int x, int z, Biome bio)
    {
        bukkitWorld.setBiome(x, z, bio);
    }

    @Override
    public double getTemperature(int x, int z)
    {
        return bukkitWorld.getTemperature(x, z);
    }

    @Override
    public double getHumidity(int x, int z)
    {
        return bukkitWorld.getHumidity(x, z);
    }

    @Override
    public int getMaxHeight()
    {
        return bukkitWorld.getMaxHeight();
    }

    @Override
    public int getSeaLevel()
    {
        return bukkitWorld.getSeaLevel();
    }

    @Override
    public boolean getKeepSpawnInMemory()
    {
        return bukkitWorld.getKeepSpawnInMemory();
    }

    @Override
    public void setKeepSpawnInMemory(boolean keepLoaded)
    {
        bukkitWorld.setKeepSpawnInMemory(keepLoaded);
    }

    @Override
    public boolean isAutoSave()
    {
        return bukkitWorld.isAutoSave();
    }

    @Override
    public void setAutoSave(boolean value)
    {
        bukkitWorld.setAutoSave(value);
    }

    @Override
    public void setDifficulty(Difficulty difficulty)
    {
        bukkitWorld.setDifficulty(difficulty);
    }

    @Override
    public Difficulty getDifficulty()
    {
        return bukkitWorld.getDifficulty();
    }

    @Override
    public File getWorldFolder()
    {
        return bukkitWorld.getWorldFolder();
    }

    @Override
    public WorldType getWorldType()
    {
        return bukkitWorld.getWorldType();
    }

    @Override
    public boolean canGenerateStructures()
    {
        return bukkitWorld.canGenerateStructures();
    }

    @Override
    public long getTicksPerAnimalSpawns()
    {
        return bukkitWorld.getTicksPerAnimalSpawns();
    }

    @Override
    public void setTicksPerAnimalSpawns(int ticksPerAnimalSpawns)
    {
        bukkitWorld.setTicksPerAnimalSpawns(ticksPerAnimalSpawns);
    }

    @Override
    public long getTicksPerMonsterSpawns()
    {
        return bukkitWorld.getTicksPerMonsterSpawns();
    }

    @Override
    public void setTicksPerMonsterSpawns(int ticksPerMonsterSpawns)
    {
        bukkitWorld.setTicksPerMonsterSpawns(ticksPerMonsterSpawns);
    }

    @Override
    public int getMonsterSpawnLimit()
    {
        return bukkitWorld.getMonsterSpawnLimit();
    }

    @Override
    public void setMonsterSpawnLimit(int limit)
    {
        bukkitWorld.setMonsterSpawnLimit(limit);
    }

    @Override
    public int getAnimalSpawnLimit()
    {
        return bukkitWorld.getAnimalSpawnLimit();
    }

    @Override
    public void setAnimalSpawnLimit(int limit)
    {
        bukkitWorld.setAnimalSpawnLimit(limit);
    }

    @Override
    public int getWaterAnimalSpawnLimit()
    {
        return bukkitWorld.getWaterAnimalSpawnLimit();
    }

    @Override
    public void setWaterAnimalSpawnLimit(int limit)
    {
        bukkitWorld.setWaterAnimalSpawnLimit(limit);
    }

    @Override
    public int getAmbientSpawnLimit()
    {
        return bukkitWorld.getAmbientSpawnLimit();
    }

    @Override
    public void setAmbientSpawnLimit(int limit)
    {
        bukkitWorld.setAmbientSpawnLimit(limit);
    }

    @Override
    public void playSound(Location location, Sound sound, float volume, float pitch)
    {
        bukkitWorld.playSound(location, sound, volume, pitch);
    }

    @Override
    public String[] getGameRules()
    {
        return bukkitWorld.getGameRules();
    }

    @Override
    public String getGameRuleValue(String rule)
    {
        return bukkitWorld.getGameRuleValue(rule);
    }

    @Override
    public boolean setGameRuleValue(String rule, String value)
    {
        return bukkitWorld.setGameRuleValue(rule, value);
    }

    @Override
    public boolean isGameRule(String rule)
    {
        return bukkitWorld.isGameRule(rule);
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message)
    {
        bukkitWorld.sendPluginMessage(source, channel, message);
    }

    @Override
    public Set<String> getListeningPluginChannels()
    {
        return bukkitWorld.getListeningPluginChannels();
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue)
    {
        bukkitWorld.setMetadata(metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey)
    {
        return bukkitWorld.getMetadata(metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey)
    {
        return bukkitWorld.hasMetadata(metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin)
    {
        bukkitWorld.removeMetadata(metadataKey, owningPlugin);
    }
}
