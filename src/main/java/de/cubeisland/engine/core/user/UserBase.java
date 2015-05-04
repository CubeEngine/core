/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 * <p>
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.user;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.sponge.BukkitUtils;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.data.manipulators.entities.ExperienceHolderData;
import org.spongepowered.api.data.manipulators.entities.FlyingData;
import org.spongepowered.api.data.manipulators.entities.FoodData;
import org.spongepowered.api.data.manipulators.entities.GameModeData;
import org.spongepowered.api.data.manipulators.entities.HealthData;
import org.spongepowered.api.data.manipulators.entities.IgniteableData;
import org.spongepowered.api.data.manipulators.entities.JoinData;
import org.spongepowered.api.data.manipulators.entities.PassengerData;
import org.spongepowered.api.data.manipulators.entities.SneakingData;
import org.spongepowered.api.data.manipulators.entities.VehicleData;
import org.spongepowered.api.data.manipulators.entities.VelocityData;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.entity.player.tab.TabList;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.types.CarriedInventory;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Literal;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.source.LocatedSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.weather.Weather;

import static de.cubeisland.engine.core.contract.Contract.expect;

/**
 * Wrapper around the BukkitPlayer/OfflinePlayer
 */
public class UserBase implements Player
{
    private static final int NBT_ID_TAGCOMPOUND = 10;
    private static final int NBT_ID_DOUBLE = 6;
    private static final int NBT_ID_FLOAT = 5;
    private final UUID uuid;
    User cachedOfflinePlayer = null;

    public UserBase(UUID uuid)
    {
        this.uuid = uuid;
    }

    public org.spongepowered.api.entity.player.User getOfflinePlayer()
    {
        if (this.cachedOfflinePlayer == null)
        {
            this.cachedOfflinePlayer = Bukkit.getPlayer(uuid);
            if (cachedOfflinePlayer == null)
            {
                this.cachedOfflinePlayer = Bukkit.getOfflinePlayer(uuid);
                CubeEngine.getLog().debug("Caching Offline Player");
            }
            else
            {
                CubeEngine.getLog().debug("Caching Online Player");
            }
        }
        return cachedOfflinePlayer;
    }

    public Text getDisplayName()
    {
        Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().getDisplayNameData().getDisplayName();
        }
        return Texts.of(this.getOfflinePlayer().getName());
    }

    public void setDisplayName(String string)
    {
        Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().getDisplayNameData().setDisplayName(Texts.of(string));
        }
    }

    public InetSocketAddress getAddress()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().getConnection().getAddress();
        }
        return null;
    }

    @Override
    public void kick(Literal reason)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().kick(reason);
        }
    }

    @Override
    public void chat(String string)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().chat(string);
        }
    }

    @Override
    public boolean performCommand(String string)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        return player.isPresent() && player.get().run(string);
    }

    public boolean isSneaking()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        return player.isPresent() && player.get().getData(SneakingData.class).isPresent();
    }

    @Override
    @Deprecated
    public void updateInventory()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().updateInventory();
        }
    }

    @Override
    public void setPlayerTime(long l, boolean bln)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().setPlayerTime(l, bln);
        }
    }

    @Override
    public void resetPlayerTime()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
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

    @Override
    public boolean getAllowFlight()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        return player.isPresent() && player.get().getAllowFlight();
    }

    @Override
    public void setAllowFlight(boolean bln)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().setAllowFlight(bln);
        }
    }

    @Override
    public void hidePlayer(Player playerToHide)
    {
        final Player player = this.getPlayer();
        if (player.isPresent())
        {
            if (playerToHide instanceof User)
            {
                playerToHide = playerToHide.getPlayer();
            }
            if (playerToHide != null)
            {
                player.get().hidePlayer(playerToHide);
            }
        }
    }

    @Override
    public void showPlayer(Player playerToShow)
    {
        final Player player = this.getPlayer();
        if (player.isPresent())
        {
            if (playerToShow instanceof User)
            {
                playerToShow = playerToShow.getPlayer();
            }
            if (playerToShow != null)
            {
                player.get().showPlayer(playerToShow);
            }
        }
    }

    @Override
    public boolean canSee(Player playerToCheck)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        return player.isPresent() && player.get().canSee(playerToCheck);
    }

    @Override
    public boolean isOnGround()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().isOnGround();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return data.getBoolean("OnGround");
            }
        }
        return true;
    }

    public boolean isFlying()
    {
        return getOfflinePlayer().getData(FlyingData.class).isPresent();
    }

    @Override
    public void setFlying(boolean bln)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().setFlying(bln);
        }
    }

    @Override
    public String getName()
    {
        return this.getOfflinePlayer().getName();
    }

    @Override
    public CarriedInventory<? extends Carrier> getInventory()
    {
        return getOfflinePlayer().getInventory();
    }

    @Override
    public Optional<Inventory> getOpenInventory()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().getOpenInventory();
        }
        return null;
    }

    @Override
    public void openInventory(Inventory invntr)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().openInventory(invntr);
        }
    }

    @Override
    public void closeInventory()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().closeInventory();
        }
    }

    @Override
    public Optional<ItemStack> getItemInHand()
    {
        return getOfflinePlayer().getItemInHand();
    }

    @Override
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


    @Override
    public Location getEyeLocation()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().getEyeLocation();
        }
        return null;
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<T> type)
    {
        return getPlayer().transform(input -> input.launchProjectile(type)).orNull();
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<T> type, Vector3d vector)
    {
        return getPlayer().transform(input -> input.launchProjectile(type, vector)).orNull();
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

    public void setMaxHealth(double v)
    {
        getOfflinePlayer().getData(HealthData.class).get().setMaxHealth(v);
    }

    @Override
    public Location getLocation()
    {
        return this.getLocation(new Location(null, 0, 0, 0));
    }

    @Override
    public Location getLocation(Location loc)
    {
        return getPlayer().transform(Player::getLocation).orNull();
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
    }

    public Vector3d getRotation()
    {
        return getPlayer().transform(Player::getRotation).orNull();
        NBTTagCompound data = this.getData();
        list = data.getList("Rotation", NBT_ID_FLOAT);
        if (list != null)
        {
            loc.setPitch(list.e(0));
            loc.setYaw(list.e(1));
        }
        return rotation;
    }

    public void setVelocity(Vector3d vector)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().getData(VelocityData.class).get().setVelocity(vector);
        }
    }

    @Override
    public World getWorld()
    {
        return getPlayer().transform(LocatedSource::getWorld).orNull();
        NBTTagCompound data = this.getData();
        if (data != null)
        {
            return this.getServer().getWorld(new UUID(data.getLong("WorldUUIDMost"), data.getLong("WorldUUIDLeast")));
        }
    }

    @Override
    public boolean teleport(Location lctn)
    {
        expect(CubeEngine.isMainThread(), "Must be called from the main thread!");

        if (lctn == null)
        {
            return false;
        }
        return this.teleport(lctn, TeleportCause.PLUGIN);
    }

    @Override
    public boolean teleport(Location lctn, TeleportCause tc)
    {
        expect(CubeEngine.isMainThread(), "Must be called from the main thread!");
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().teleport(lctn, tc);
        }
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

    @Override
    public boolean teleport(Entity entity, TeleportCause tc)
    {
        if (entity == null)
        {
            return false;
        }
        return this.teleport(entity.getLocation(), tc);
    }

    @Override
    public List<Entity> getNearbyEntities(double d, double d1, double d2)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().getNearbyEntities(d, d1, d2);
        }
        return null;
    }

    public void setFireTicks(int i)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().getData(IgniteableData.class).get().setFireTicks(i);
        }
    }

    @Override
    public void remove()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().remove();
        }
    }

    @Override
    public Server getServer()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.getServer();
        }
        return Bukkit.getServer();
    }

    @Override
    public void setLastDamageCause(EntityDamageEvent ede)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().setLastDamageCause(ede);
        }
    }

    @Override
    public UUID getUniqueId()
    {
        return this.getOfflinePlayer().getUniqueId();
    }

    @Override
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

    @Override
    public boolean hasPermission(String string)
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        return player.isPresent() && player.get().hasPermission(string);
    }
    @Override
    public boolean isOp()
    {
        return this.getOfflinePlayer().isOp();
    }

    @Override
    public void setOp(boolean bln)
    {
        this.getOfflinePlayer().setOp(bln);
    }

    public void sendMessage(String string)
    {
        sendMessage(Texts.of(string));
    }

    @Override
    public boolean isOnline()
    {
        return this.getOfflinePlayer().isOnline();
    }

    @Override
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

    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().setFlySpeed(value);
        }
    }

    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().setWalkSpeed(value);
        }
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch, double minVolume)
    {
        Optional<Player> player = getPlayer();
        if (player.isPresent())
        {
            player.get().playSound(sound, position, volume, pitch, minVolume);
        }
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume)
    {
        Optional<Player> player = getPlayer();
        if (player.isPresent())
        {
            player.get().playSound(sound, position, volume);
        }
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch)
    {
        Optional<Player> player = getPlayer();
        if (player.isPresent())
        {
            player.get().playSound(sound, position, volume, pitch);
        }
    }

    @Override
    public Inventory getEnderChest()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().getEnderChest();
        }
        Player offlinePlayer = BukkitUtils.getOfflinePlayerAsPlayer(this.getOfflinePlayer());
        if (offlineplayer.get().hasPlayedBefore())
        {
            return offlineplayer.get().getEnderChest();
        }
        return null;
    }

    @Override
    public void setPlayerWeather(Weather wt)
    {

        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().setPlayerWeather(wt);
        }
    }

    @Override
    public void resetPlayerWeather()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().resetPlayerWeather();
        }
    }

    @Override
    public Scoreboard getScoreboard()
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            return player.get().getScoreboard();
        }
        return null;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().setScoreboard(scoreboard);
        }
    }

    @Override
    public void sendSignChange(Location location, String[] strings) throws IllegalArgumentException
    {
        final Optional<Player> player = this.getOfflinePlayer().getPlayer();
        if (player.isPresent())
        {
            player.get().sendSignChange(location, strings);
        }
    }
}
