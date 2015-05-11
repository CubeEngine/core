package de.cubeisland.engine.module.core.user;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.module.core.CubeEngine;
import de.cubeisland.engine.module.core.sponge.SpongeCore;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulators.entities.ExperienceHolderData;
import org.spongepowered.api.data.manipulators.entities.FlyingData;
import org.spongepowered.api.data.manipulators.entities.FoodData;
import org.spongepowered.api.data.manipulators.entities.GameModeData;
import org.spongepowered.api.data.manipulators.entities.HealthData;
import org.spongepowered.api.data.manipulators.entities.IgniteableData;
import org.spongepowered.api.data.manipulators.entities.JoinData;
import org.spongepowered.api.data.manipulators.entities.PassengerData;
import org.spongepowered.api.data.manipulators.entities.SneakingData;
import org.spongepowered.api.data.manipulators.entities.VelocityData;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.types.CarriedInventory;
import org.spongepowered.api.text.Text.Literal;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.source.LocatedSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.weather.Weather;

import static de.cubeisland.engine.module.core.contract.Contract.expect;
import static java.util.stream.Collectors.toList;

/**
 * Wrapper around the Sponge Player/User
 */
public class UserBase
{
    protected final Game game;
    private final UUID uuid;
    org.spongepowered.api.entity.player.User cachedOfflinePlayer = null;
    private SpongeCore core;

    public UserBase(SpongeCore core, UUID uuid)
    {
        this.core = core;
        this.game = core.getGame();

        this.uuid = uuid;
    }

    public org.spongepowered.api.entity.player.User getOfflinePlayer()
    {
        if (this.cachedOfflinePlayer == null)
        {
            this.cachedOfflinePlayer = game.getServer().getPlayer(uuid).orNull();
            if (cachedOfflinePlayer == null)
            {
                this.cachedOfflinePlayer = Bukkit.getOfflinePlayer(uuid);
                core.getProvided(Log.class).debug("Caching Offline Player");
            }
            else
            {
                core.getProvided(Log.class).debug("Caching Online Player");
            }
        }
        return cachedOfflinePlayer;
    }

    public String getDisplayName()
    {
        Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            return player.get().getDisplayNameData().getDisplayName().toString();
        }
        return this.getOfflinePlayer().getName();
    }

    public void setDisplayName(String string)
    {
        Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            player.get().getDisplayNameData().setDisplayName(Texts.of(string));
        }
    }

    public InetSocketAddress getAddress()
    {
        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            return player.get().getConnection().getAddress();
        }
        return null;
    }

    public void kick(Literal reason)
    {
        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            player.get().kick(reason);
        }
    }

    public void chat(String string)
    {
        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            player.get().chat(string);
        }
    }

    public boolean performCommand(String string)
    {
        final Optional<Player> player = this.getPlayer();
        return player.isPresent() && player.get().run(string);
    }

    public boolean isSneaking()
    {
        final Optional<Player> player = this.getPlayer();
        return player.isPresent() && player.get().getData(SneakingData.class).isPresent();
    }

    public void setPlayerTime(long l, boolean bln)
    {
        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            player.get().setPlayerTime(l, bln);
        }
    }

    public void resetPlayerTime()
    {
        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            player.get().resetPlayerTime();
        }
    }

    public float getExpPerecent()
    {
        ExperienceHolderData expData = getOfflinePlayer().getData(ExperienceHolderData.class).get();
        return (float)expData.getExperienceSinceLevel() / expData.getExperienceBetweenLevels();
    }

    public int getLevel()
    {
        ExperienceHolderData expData = getOfflinePlayer().getData(ExperienceHolderData.class).get();
        return expData.getLevel();
    }

    public void setLevel(int i)
    {
        ExperienceHolderData expData = getOfflinePlayer().getData(ExperienceHolderData.class).get();
        expData.setLevel(i);
    }

    public void setExhaustion(double d)
    {
        FoodData foodData = getOfflinePlayer().getData(FoodData.class).get();
        foodData.setExhaustion(d);
    }

    public double getSaturation()
    {
        FoodData foodData = getOfflinePlayer().getData(FoodData.class).get();
        return foodData.getSaturation();
    }

    public void setSaturation(double d)
    {
        FoodData foodData = getOfflinePlayer().getData(FoodData.class).get();
        foodData.setSaturation(d);
    }

    public double getFoodLevel()
    {
        FoodData foodData = getOfflinePlayer().getData(FoodData.class).get();
        return foodData.getFoodLevel();
    }

    public void setFoodLevel(double d)
    {
        FoodData foodData = getOfflinePlayer().getData(FoodData.class).get();
        foodData.setFoodLevel(d);
    }

    public boolean getAllowFlight()
    {
        final Optional<Player> player = this.getPlayer();
        return player.isPresent() && player.get().getAllowFlight();
    }

    public void setAllowFlight(boolean bln)
    {
        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            player.get().setAllowFlight(bln);
        }
    }

    public void hidePlayer(Player playerToHide)
    {
        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            if (playerToHide instanceof User)
            {
                playerToHide = playerToHide.getPlayer().get();
            }
            if (playerToHide != null)
            {
                player.get().hidePlayer(playerToHide);
            }
        }
    }

    public void showPlayer(Player playerToShow)
    {
        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            if (playerToShow instanceof User)
            {
                playerToShow = playerToShow.getPlayer().get();
            }
            if (playerToShow != null)
            {
                player.get().showPlayer(playerToShow);
            }
        }
    }

    public boolean canSee(Player playerToCheck)
    {
        final Optional<Player> player = this.getPlayer();
        return player.isPresent() && player.get().canSee(playerToCheck);
    }

    public boolean isOnGround()
    {
        return getPlayer().transform(Entity::isOnGround).or(true);
    }

    public boolean isFlying()
    {
        return getOfflinePlayer().getData(FlyingData.class).isPresent();
    }

    public void setFlying(boolean bln)
    {
        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            if (bln)
            {
                player.get().getOrCreate(FlyingData.class);
            }
            else
            {
                player.get().remove(FlyingData.class);
            }
        }
    }

    public String getName()
    {
        return this.getOfflinePlayer().getName();
    }

    public CarriedInventory<? extends Carrier> getInventory()
    {
        return getOfflinePlayer().getInventory();
    }

    public Optional<Inventory> getOpenInventory()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().getOpenInventory();
        }
        return Optional.absent();
    }

    public void openInventory(Inventory invntr)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().openInventory(invntr);
        }
    }

    public void closeInventory()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().closeInventory();
        }
    }

    public Optional<ItemStack> getItemInHand()
    {
        return getOfflinePlayer().getItemInHand();
    }

    public void setItemInHand(ItemStack is)
    {
        getOfflinePlayer().setItemInHand(is);
    }

    public GameMode getGameMode()
    {
        return getOfflinePlayer().getData(GameModeData.class).get().getGameMode();
    }

    public void setGameMode(GameMode gm)
    {
        GameModeData gameModeData = getOfflinePlayer().getData(GameModeData.class).get();
        gameModeData.setGameMode(gm);
    }


    public Location getEyeLocation()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().getEyeLocation();
        }
        return null;
    }

    public <T extends Projectile> T launchProjectile(Class<T> type)
    {
        return getPlayer().transform(input -> input.launchProjectile(type)).orNull();
    }

    public void damage(double v)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().getHealthData().damage(v);
        }
    }

    public double getHealth()
    {
        return getOfflinePlayer().getData(HealthData.class).get().getHealth();
    }

    public void setHealth(double v)
    {
        getOfflinePlayer().getData(HealthData.class).get().setHealth(v);
    }

    public double getMaxHealth()
    {
        return getOfflinePlayer().getData(HealthData.class).get().getMaxHealth();
    }

    public Location getLocation()
    {
        return getPlayer().transform(Player::getLocation).orNull();
         /*  // TODO
        NBTTagCompound data = this.getData();
        if (data != null)
        {
            World world = this.getWorld();
            loc.setWorld(world);
            if (world != null)
            {
                NBTTagList list = data.getList("Pos", NBT_ID_DOUBLE);
                if (list != null)
                {
                    loc.setX(list.d(0));
                    loc.setY(list.d(1));
                    loc.setZ(list.d(2));

                    return loc;
                }
            }
        }
        */
    }

    public void setRotation(Vector3d vector)
    {
        if (getPlayer().isPresent())
        {
            getPlayer().get().setRotation(vector);
        }
        // TODO offline
    }

    public Vector3d getRotation()
    {
        return getPlayer().transform(Player::getRotation).orNull();
        /* // TODO
        NBTTagCompound data = this.getData();
        list = data.getList("Rotation", NBT_ID_FLOAT);
        if (list != null)
        {
            loc.setPitch(list.e(0));
            loc.setYaw(list.e(1));
        }
        return rotation;
        */
    }

    public void setVelocity(Vector3d vector)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().getData(VelocityData.class).get().setVelocity(vector);
        }
    }

    public World getWorld()
    {
        return getPlayer().transform(LocatedSource::getWorld).orNull();
        /* TODO offline World
        NBTTagCompound data = this.getData();
        if (data != null)
        {
            return this.getServer().getWorld(new UUID(data.getLong("WorldUUIDMost"), data.getLong("WorldUUIDLeast")));
        }
        */
    }

    public boolean teleport(Location lctn)
    {
        expect(CubeEngine.isMainThread(), "Must be called from the main thread!");
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().setLocation(lctn);
            return true;
        }
        /* TODO offline TP
        NBTTagCompound data = this.getData();
        if (data != null)
        {
            NBTTagList list = new NBTTagList();
            list.add(new NBTTagDouble(lctn.getX()));
            list.add(new NBTTagDouble(lctn.getY()));
            list.add(new NBTTagDouble(lctn.getZ()));
            data.set("Pos", list);

            list = new NBTTagList();
            list.add(new NBTTagFloat(lctn.getPitch()));
            list.add(new NBTTagFloat(lctn.getYaw()));
            data.set("Rotation", list);

            UUID id = lctn.getWorld().getUID();
            data.setLong("WorldUUIDMost", id.getMostSignificantBits());
            data.setLong("WorldUUIDLeast", id.getLeastSignificantBits());

            this.saveData();
        }
        */
        return false;
    }

    public boolean teleport(Entity entity)
    {
        if (entity == null)
        {
            return false;
        }
        return this.teleport(entity.getLocation());
    }

    public List<Entity> getNearbyEntities(double radius)
    {
        Vector3d center = getLocation().getPosition();
        double squared = radius * radius;
        return getWorld().getEntities().stream()
                  .filter(e ->  center.distanceSquared(e.getLocation().getPosition()) < squared)
                  .collect(toList());
    }

    public void setFireTicks(int i)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().getData(IgniteableData.class).get().setFireTicks(i);
        }
    }

    public UUID getUniqueId()
    {
        return this.getOfflinePlayer().getUniqueId();
    }

    public EntityType getType()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().getType();
        }
        return EntityTypes.PLAYER;
    }

    public Entity getVehicle()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().getData(PassengerData.class).transform(PassengerData::getVehicle).orNull();
        }
        return null;
    }

    public boolean hasPermission(String string)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        return player.isPresent() && player.get().hasPermission(string);
    }

    public boolean isOp()
    {
        return this.getOfflinePlayer().isOp();
    }

    public void setOp(boolean bln)
    {
        this.getOfflinePlayer().setOp(bln);
    }

    public void sendMessage(String string)
    {
        if (getPlayer().isPresent())
        {
            getPlayer().get().sendMessage(Texts.of(string));
        }
    }

    public boolean isOnline()
    {
        return this.getOfflinePlayer().isOnline();
    }

    public Optional<Player> getPlayer()
    {
        return this.getOfflinePlayer().getPlayer();
    }

    public Date getFirstPlayed()
    {
        return this.getOfflinePlayer().getData(JoinData.class).get().getFirstPlayed();
    }

    public Date getLastPlayed()
    {
        return this.getOfflinePlayer().getData(JoinData.class).get().getLastPlayed();
    }

    public boolean hasPlayedBefore()
    {
        return this.getOfflinePlayer().getData(JoinData.class).get().hasJoinedBefore();
    }

    public void setFlySpeed(float value) throws IllegalArgumentException
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().setFlySpeed(value);
        }
    }

    public void setWalkSpeed(float value) throws IllegalArgumentException
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().setWalkSpeed(value);
        }
    }


    public void playSound(SoundType sound, Vector3d position, double volume)
    {
        Optional<Player> player = getPlayer();
        if (player.isPresent())
        {
            player.get().playSound(sound, position, volume);
        }
    }

    public void playSound(SoundType sound, Vector3d position, double volume, double pitch)
    {
        Optional<Player> player = getPlayer();
        if (player.isPresent())
        {
            player.get().playSound(sound, position, volume, pitch);
        }
    }

    public Inventory getEnderChest()
    {
        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            return player.get().getEnderChest();
        }
        // TODO
        return null;
    }

    public void setPlayerWeather(Weather wt)
    {

        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            player.get().setPlayerWeather(wt);
        }
    }

    public void resetPlayerWeather()
    {
        final Optional<Player> player = this.getPlayer();
        if (player.isPresent())
        {
            player.get().resetPlayerWeather();
        }
    }

    public <T extends DataManipulator<T>> Optional<T> getData(Class<T> clazz)
    {
        return getOfflinePlayer().getData(clazz);
    }

    public <T extends DataManipulator<T>> DataTransactionResult offer(T manipulatorData)
    {
        return getOfflinePlayer().offer(manipulatorData);
    }
}
