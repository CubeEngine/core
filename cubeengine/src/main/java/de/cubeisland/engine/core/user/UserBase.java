/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.user;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.NBTTagCompound;
import net.minecraft.server.v1_7_R1.NBTTagDouble;
import net.minecraft.server.v1_7_R1.NBTTagFloat;
import net.minecraft.server.v1_7_R1.NBTTagList;
import net.minecraft.server.v1_7_R1.PlayerInteractManager;
import net.minecraft.server.v1_7_R1.WorldNBTStorage;
import net.minecraft.server.v1_7_R1.WorldServer;
import org.bukkit.craftbukkit.v1_7_R1.CraftServer;

import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.BukkitUtils;
import net.minecraft.util.com.mojang.authlib.GameProfile;

/**
 * Wrapper around the BukkitPlayer/OfflinePlayer
 */
public class UserBase implements Player
{
    private final String playerName;
    private EntityPlayer dummy = null;

    private static final int NBT_ID_DOUBLE = new NBTTagDouble(1.0).getTypeId();
    private static final int NBT_ID_FLOAT = new NBTTagFloat(1.0f).getTypeId();

    public UserBase(String name)
    {
        this.playerName = name;
    }

    public OfflinePlayer getOfflinePlayer()
    {
        return Bukkit.getOfflinePlayer(playerName);
    }

    private EntityPlayer getDummy()
    {
        if (this.dummy == null)
        {
            CraftServer srv = (CraftServer)this.getServer();
            WorldServer world = srv.getServer().getWorldServer(0);
            // LoginListener is doing this
            // UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.i.getName()).getBytes(Charsets.UTF_8));
            // this.i = new GameProfile(uuid.toString().replaceAll("-", ""), this.i.getName());
            // TODO verify me
            this.dummy = new EntityPlayer(srv.getServer(), world, new GameProfile("", this.getName()), new PlayerInteractManager(world));
        }
        return this.dummy;
    }

    private NBTTagCompound getData()
    {
        EntityPlayer dummy = this.getDummy();
        WorldNBTStorage storage = (WorldNBTStorage)dummy.playerInteractManager.world.getDataManager();
        return storage.getPlayerData(this.getName());
    }

    private void saveData0()
    {
        EntityPlayer dummy = this.getDummy();
        WorldNBTStorage storage = (WorldNBTStorage)dummy.playerInteractManager.world.getDataManager();
        storage.save(dummy);
    }

    @Override
    public String getDisplayName()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getDisplayName();
        }
        return this.getOfflinePlayer().getName();
    }

    @Override
    public void setDisplayName(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setDisplayName(string);
        }
    }

    @Override
    public String getPlayerListName()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getPlayerListName();
        }
        return null;
    }

    @Override
    public void setPlayerListName(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setPlayerListName(string);
        }
    }

    @Override
    public void setCompassTarget(Location lctn)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setCompassTarget(lctn);
        }
    }

    @Override
    public Location getCompassTarget()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getCompassTarget();
        }
        return null;
    }

    @Override
    public InetSocketAddress getAddress()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getAddress();
        }
        return null;
    }

    @Override
    public void sendRawMessage(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.sendRawMessage(string);
        }
    }

    @Override
    public void kickPlayer(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.kickPlayer(string);
        }
    }

    @Override
    public void chat(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.chat(string);
        }
    }

    @Override
    public boolean performCommand(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.performCommand(string);
    }

    @Override
    public boolean isSneaking()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isSneaking();
    }

    @Override
    public void setSneaking(boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setSneaking(bln);
        }
    }

    @Override
    public boolean isSprinting()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isSprinting();
    }

    @Override
    public void setSprinting(boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setSprinting(bln);
        }
    }

    @Override
    public void saveData()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.saveData();
        }
        else
        {
            this.saveData0();
        }
    }

    @Override
    public void loadData()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.loadData();
        }
    }

    @Override
    public void setSleepingIgnored(boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setSleepingIgnored(bln);
        }
    }

    @Override
    public boolean isSleepingIgnored()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isSleepingIgnored();
    }

    @Override
    public void playNote(Location lctn, byte b, byte b1)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.playNote(lctn, b, b1);
        }
    }

    @Override
    public void playNote(Location lctn, Instrument i, Note note)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.playNote(lctn, i, note);
        }
    }

    @Override
    public void playEffect(Location lctn, Effect effect, int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.playEffect(lctn, effect, i);
        }
    }

    @Override
    public <T> void playEffect(Location lctn, Effect effect, T t)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.playEffect(lctn, effect, t);
        }
    }

    @Override
    public void sendBlockChange(Location lctn, Material mtrl, byte b)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.sendBlockChange(lctn, mtrl, b);
        }
    }

    @Override
    public boolean sendChunkChange(Location lctn, int i, int i1, int i2, byte[] bytes)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.sendChunkChange(lctn, i, i1, i2, bytes);
    }

    @Override
    public void sendBlockChange(Location lctn, int i, byte b)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.sendBlockChange(lctn, i, b);
        }
    }

    @Override
    public void sendMap(MapView mv)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.sendMap(mv);
        }
    }

    @Override
    @Deprecated
    public void updateInventory()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.updateInventory();
        }
    }

    @Override
    public void awardAchievement(Achievement a)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.awardAchievement(a);
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.incrementStatistic(statistic);
        }
    }

    @Override
    public void incrementStatistic(Statistic ststc, int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, i);
        }
    }

    @Override
    public void incrementStatistic(Statistic ststc, Material mtrl)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, mtrl);
        }
    }

    @Override
    public void incrementStatistic(Statistic ststc, Material mtrl, int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, mtrl, i);
        }
    }

    @Override
    public void setPlayerTime(long l, boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setPlayerTime(l, bln);
        }
    }

    @Override
    public long getPlayerTime()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getPlayerTime();
        }
        return 0;
    }

    @Override
    public long getPlayerTimeOffset()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getPlayerTimeOffset();
        }
        return 0;
    }

    @Override
    public boolean isPlayerTimeRelative()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isPlayerTimeRelative();
    }

    @Override
    public void resetPlayerTime()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.resetPlayerTime();
        }
    }

    @Override
    public void giveExp(int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.giveExp(i);
        }
    }

    @Override
    public float getExp()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getExp();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return data.getFloat("XpP");
            }
        }
        return 0;
    }

    @Override
    public void setExp(float f)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setExp(f);
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                data.setFloat("XpP", f);
                this.saveData();
            }
        }
    }

    @Override
    public int getLevel()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getLevel();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return data.getInt("XpLevel");
            }
        }
        return 0;
    }

    @Override
    public void setLevel(int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setLevel(i);
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                data.setInt("XpLevel", i);
                this.saveData();
            }
        }
    }

    @Override
    public int getTotalExperience()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getTotalExperience();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return data.getInt("XpTotal");
            }
        }
        return 0;
    }

    @Override
    public void setTotalExperience(int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setTotalExperience(i);
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                data.setInt("XpTotal", i);
                this.saveData();
            }
        }
    }

    @Override
    public float getExhaustion()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getExhaustion();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return data.getFloat("foodExhaustionLevel");
            }
        }
        return 0;
    }

    @Override
    public void setExhaustion(float f)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setExhaustion(f);
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                data.setFloat("foodExhaustionLevel", f);
                this.saveData();
            }
        }
    }

    @Override
    public float getSaturation()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getSaturation();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return data.getFloat("foodSaturationLevel");
            }
        }
        return 0;
    }

    @Override
    public void setSaturation(float f)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setSaturation(f);
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                data.setFloat("foodSaturationLevel", f);
                this.saveData();
            }
        }
    }

    @Override
    public int getFoodLevel()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getFoodLevel();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return data.getInt("foodLevel");
            }
        }
        return 0;
    }

    @Override
    public void setFoodLevel(int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setFoodLevel(i);
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                data.setInt("foodLevel", i);
                this.saveData();
            }
        }
    }

    @Override
    public Location getBedSpawnLocation()
    {
        return this.getOfflinePlayer().getBedSpawnLocation();
    }

    @Override
    public void setBedSpawnLocation(Location lctn)
    {
        if (lctn == null)
        {
            return;
        }
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setBedSpawnLocation(lctn);
        }
    }

    @Override
    public boolean getAllowFlight()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.getAllowFlight();
    }

    @Override
    public void setAllowFlight(boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setAllowFlight(bln);
        }
    }

    @Override
    public void hidePlayer(Player playerToHide)
    {
        final Player player = this.getPlayer();
        if (player != null)
        {
            if (playerToHide instanceof User)
            {
                playerToHide = playerToHide.getPlayer();
            }
            player.hidePlayer(playerToHide);
        }
    }

    @Override
    public void showPlayer(Player playerToShow)
    {
        final Player player = this.getPlayer();
        if (player != null)
        {
            if (playerToShow instanceof User)
            {
                playerToShow = playerToShow.getPlayer();
            }
            player.showPlayer(playerToShow);
        }
    }

    @Override
    public boolean canSee(Player playerToCheck)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.canSee(playerToCheck);
    }

    @Override
    @Deprecated
    public boolean isOnGround()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.isOnGround();
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

    @Override
    public boolean isFlying()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isFlying();
    }

    @Override
    public void setFlying(boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setFlying(bln);
        }
    }

    @Override
    public String getName()
    {
        return this.getOfflinePlayer().getName();
    }

    @Override
    public PlayerInventory getInventory()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getInventory();
        }
        Player offlinePlayer = BukkitUtils.getOfflinePlayerAsPlayer(this.getOfflinePlayer());
        if (offlinePlayer.hasPlayedBefore())
        {
            return offlinePlayer.getInventory();
        }
        return null;
    }

    @Override
    public boolean setWindowProperty(Property prprt, int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.setWindowProperty(prprt, i);
    }

    @Override
    public InventoryView getOpenInventory()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getOpenInventory();
        }
        return null;
    }

    @Override
    public InventoryView openInventory(Inventory invntr)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.openInventory(invntr);
        }
        return null;
    }

    @Override
    public InventoryView openWorkbench(Location lctn, boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.openWorkbench(lctn, bln);
        }
        return null;
    }

    @Override
    public InventoryView openEnchanting(Location lctn, boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.openEnchanting(lctn, bln);
        }
        return null;
    }

    @Override
    public void openInventory(InventoryView iv)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.openInventory(iv);
        }
    }

    @Override
    public void closeInventory()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.closeInventory();
        }
    }

    @Override
    public ItemStack getItemInHand()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getItemInHand();
        }
        return null;
    }

    @Override
    public void setItemInHand(ItemStack is)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setItemInHand(is);
        }
    }

    @Override
    public ItemStack getItemOnCursor()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getItemOnCursor();
        }
        return null;
    }

    @Override
    public void setItemOnCursor(ItemStack is)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setItemOnCursor(is);
        }
    }

    @Override
    public boolean isSleeping()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.isSleeping();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return data.getBoolean("Sleeping");
            }
        }
        return false;
    }

    @Override
    public int getSleepTicks()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getSleepTicks();
        }
        return 0;
    }

    @Override
    public GameMode getGameMode()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getGameMode();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return GameMode.getByValue(data.getInt("playerGameType"));
            }
        }
        return null;
    }

    @Override
    public void setGameMode(GameMode gm)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setGameMode(gm);
        }
        NBTTagCompound data = this.getData();
        if (data != null)
        {
            data.setInt("playerGameType", gm.getValue());
            this.saveData();
        }
    }

    @Override
    public boolean isBlocking()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isBlocking();
    }

    @Override
    public double getEyeHeight()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getEyeHeight();
        }
        return 0;
    }

    @Override
    public double getEyeHeight(boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getEyeHeight();
        }
        return 0;
    }

    @Override
    public Location getEyeLocation()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getEyeLocation();
        }
        return null;
    }

    @Override
    public List<Block> getLineOfSight(HashSet<Byte> hs, int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getLineOfSight(hs, i);
        }
        return null;
    }

    @Override
    public Block getTargetBlock(HashSet<Byte> hs, int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getTargetBlock(hs, i);
        }
        return null;
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hs, int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getLastTwoTargetBlocks(hs, i);
        }
        return null;
    }

    @Deprecated
    @Override
    public Egg throwEgg()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.throwEgg();
        }
        return null;
    }

    @Deprecated
    @Override
    public Snowball throwSnowball()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.throwSnowball();
        }
        return null;
    }

    @Deprecated
    @Override
    public Arrow shootArrow()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.shootArrow();
        }
        return null;
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> type)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.launchProjectile(type);
        }
        return null;
    }

    @Override
    public int getRemainingAir()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getRemainingAir();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return data.getInt("Air");
            }
        }
        return this.getMaximumAir();
    }

    @Override
    public void setRemainingAir(int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setRemainingAir(i);
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                data.setShort("Air", (short)i);
                this.saveData();
            }
        }
    }

    @Override
    public int getMaximumAir()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getMaximumAir();
        }
        return 300;
    }

    @Override
    public void setMaximumAir(int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setMaximumAir(i);
        }
    }

    @Override
    public int getMaximumNoDamageTicks()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getMaximumNoDamageTicks();
        }
        return 0;
    }

    @Override
    public void setMaximumNoDamageTicks(int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setMaximumNoDamageTicks(i);
        }
    }

    @Override
    public boolean isHealthScaled()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isHealthScaled();
    }

    @Override
    public void setHealthScaled(boolean b)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setHealthScaled(b);
        }
    }


    @Override
    public void setHealthScale(double v) throws IllegalArgumentException
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setHealthScale(v);
        }
    }

    @Override
    public double getHealthScale()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getHealthScale();
        }
        return 0;
    }


    @Override
    public int _INVALID_getLastDamage()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLastDamage(double v)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setLastDamage(v);
        }
    }

    @Override
    public void _INVALID_setLastDamage(int i)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void damage(double v)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.damage(v);
        }
    }

    @Override
    public void _INVALID_damage(int i)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void damage(double v, Entity entity)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.damage(v, entity);
        }
    }

    @Override
    public void _INVALID_damage(int i, Entity entity)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int _INVALID_getHealth()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHealth(double v)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setHealth(v);
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                data.setDouble("HealF", v);
                this.saveData();
            }
        }
    }

    @Override
    public void _INVALID_setHealth(int i)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int _INVALID_getMaxHealth()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMaxHealth(double v)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setMaxHealth(v);
        }
        else
        {
//            NBTTagCompound data = this.getData();
//            if (data != null)
//            {
//                NBTTagList list = data.getList("Attributes", NBT_ID_DOUBLE);
//                if (list == null)
//                {
//                    list = new NBTTagList();
//                    data.set("Attributes", list);
//                }
//                if (list.size() == 0)
//                {
//                    list.add(new NBTTagDouble("generic.MaxHealth",v));
//                }
//                else
//                {
//                    NBTTagDouble nbt = (NBTTagDouble)list.get(0);
//                    nbt.data = v;
//                }
//                this.saveData();
//            }
        }
    }

    @Override
    public void _INVALID_setMaxHealth(int i)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getLastDamage()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getLastDamage();
        }
        return 0;
    }

    @Override
    public double getHealth()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getHealth();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                if (data.get("HealF") == null)
                {
                    return data.getShort("Health");
                }
                return data.getFloat("HealF");
            }
        }
        return 0;
    }

    @Override
    public double getMaxHealth()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getMaxHealth();
        }
        else
        {
//            NBTTagCompound data = this.getData();
//            if (data != null)
//            {
//                NBTTagList list = data.getList("Attributes");
//                if (list == null || list.size() == 0)
//                {
//                    NBTTagInt nbt = (NBTTagInt)data.get("Bukkit.MaxHealth");
//                    if (nbt == null) return 0;
//                    return nbt.data;
//                }
//                data = (NBTTagCompound)list.get(0);
//                return data.getDouble("Base");
//            }
        }
        return 0;
    }

    @Override
    public int getNoDamageTicks()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getNoDamageTicks();
        }
        return 0;
    }

    @Override
    public void setNoDamageTicks(int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setNoDamageTicks(i);
        }
    }

    @Override
    public Player getKiller()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getKiller();
        }
        return null;
    }

    @Override
    public boolean addPotionEffect(PotionEffect pe)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.addPotionEffect(pe);
    }

    @Override
    public boolean addPotionEffect(PotionEffect pe, boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.addPotionEffect(pe);
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> clctn)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.addPotionEffects(clctn);
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType pet)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.hasPotionEffect(pet);
    }

    @Override
    public void removePotionEffect(PotionEffectType pet)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.removePotionEffect(pet);
        }
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getActivePotionEffects();
        }
        return null;
    }

    @Override
    public Location getLocation()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getLocation();
        }
        else
        {
//            NBTTagCompound data = this.getData();
//            if (data != null)
//            {
//                World world = this.getWorld();
//                if (world != null)
//                {
//                    NBTTagList list = data.getList("Pos", NBT_ID_DOUBLE);
//                    if (list != null)
//                    {
//                        Location loc = new Location(world, 0, 0, 0, 0, 0);
//                        loc.setX(((NBTTagDouble)list.get(0)).data);
//                        loc.setY(((NBTTagDouble)list.get(1)).data);
//                        loc.setZ(((NBTTagDouble)list.get(2)).data);
//                        list = data.getList("Rotation");
//                        if (list != null)
//                        {
//                            loc.setPitch(((NBTTagFloat)list.get(0)).data);
//                            loc.setYaw(((NBTTagFloat)list.get(1)).data);
//                        }
//                        return loc;
//                    }
//                }
//            }
        }
        return null;
    }

    public Location getLocation(Location loc)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getLocation(loc);
        }
        else
        {
//            NBTTagCompound data = this.getData();
//            if (data != null)
//            {
//                World world = this.getWorld();
//                if (world != null)
//                {
//                    NBTTagList list = data.getList("Pos", NBT_ID_DOUBLE);
//                    if (list != null)
//                    {
//                        loc.setX(((NBTTagDouble)list.get(0)).data);
//                        loc.setY(((NBTTagDouble)list.get(1)).data);
//                        loc.setZ(((NBTTagDouble)list.get(2)).data);
//                        list = data.getList("Rotation", NBT_ID_FLOAT);
//                        if (list != null)
//                        {
//                            loc.setPitch(((NBTTagFloat)list.get(0)).data);
//                            loc.setYaw(((NBTTagFloat)list.get(1)).data);
//                        }
//                    }
//                }
//            }
        }
        return loc;
    }

    @Override
    public void setVelocity(Vector vector)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setVelocity(vector);
        }
    }

    @Override
    public Vector getVelocity()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getVelocity();
        }
        return null;
    }

    @Override
    public World getWorld()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getWorld();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return this.getServer()
                           .getWorld(new UUID(data.getLong("WorldUUIDMost"), data.getLong("WorldUUIDLeast")));
            }
        }
        return null;
    }

    @Override
    public boolean teleport(Location lctn)
    {
        assert CubeEngine.isMainThread(): "Must be called from the main thread!";

        if (lctn == null)
        {
            return false;
        }
        return this.teleport(lctn, TeleportCause.PLUGIN);
    }

    @Override
    public boolean teleport(Location lctn, TeleportCause tc)
    {
        assert CubeEngine.isMainThread(): "Must be called from the main thread!";

        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.teleport(lctn, tc);
        }
        else
        {
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
        }
        return false;
    }

    @Override
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
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getNearbyEntities(d, d1, d2);
        }
        return null;
    }

    @Override
    public int getEntityId()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getEntityId();
        }
        return -1;
    }

    @Override
    public int getFireTicks()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getFireTicks();
        }
        return 0;
    }

    @Override
    public int getMaxFireTicks()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getMaxFireTicks();
        }
        return 0;
    }

    @Override
    public void setFireTicks(int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setFireTicks(i);
        }
    }

    @Override
    public void remove()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.remove();
        }
    }

    @Override
    public boolean isDead()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isDead();
    }

    @Override
    public Server getServer()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getServer();
        }
        return Bukkit.getServer();
    }

    @Override
    public Entity getPassenger()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getPassenger();
        }
        return null;
    }

    @Override
    public boolean setPassenger(Entity entity)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.setPassenger(entity);
    }

    @Override
    public boolean isEmpty()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isEmpty();
    }

    @Override
    public boolean eject()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.eject();
    }

    @Override
    public float getFallDistance()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getFallDistance();
        }
        return 0;
    }

    @Override
    public void setFallDistance(float f)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setFallDistance(f);
        }
    }

    @Override
    public void setLastDamageCause(EntityDamageEvent ede)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setLastDamageCause(ede);
        }
    }

    @Override
    public EntityDamageEvent getLastDamageCause()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getLastDamageCause();
        }
        return null;
    }

    @Override
    public UUID getUniqueId()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getUniqueId();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast"));
            }
        }
        return null;
    }

    @Override
    public int getTicksLived()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getTicksLived();
        }
        return 0;
    }

    @Override
    public void setTicksLived(int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setTicksLived(i);
        }
    }

    @Override
    public void playEffect(EntityEffect ee)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.playEffect(ee);
        }
    }

    @Override
    public EntityType getType()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getType();
        }
        return null;
    }

    @Override
    public boolean isInsideVehicle()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isInsideVehicle();
    }

    @Override
    public boolean leaveVehicle()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.leaveVehicle();
    }

    @Override
    public Entity getVehicle()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getVehicle();
        }
        return null;
    }

    @Override
    public void setMetadata(String string, MetadataValue mv)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setMetadata(string, mv);
        }
    }

    @Override
    public List<MetadataValue> getMetadata(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getMetadata(string);
        }
        return null;
    }

    @Override
    public boolean hasMetadata(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.hasMetadata(string);
    }

    @Override
    public void removeMetadata(String string, Plugin plugin)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.removeMetadata(string, plugin);
        }
    }

    @Override
    public boolean isPermissionSet(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isPermissionSet(string);
    }

    @Override
    public boolean isPermissionSet(Permission prmsn)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isPermissionSet(prmsn);
    }

    @Override
    public boolean hasPermission(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.hasPermission(string);
    }

    @Override
    public boolean hasPermission(Permission prmsn)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.hasPermission(prmsn);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, string, bln);
        }
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin);
        }
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, string, bln, i);
        }
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, i);
        }
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment pa)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.removeAttachment(pa);
        }
    }

    @Override
    public void recalculatePermissions()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.recalculatePermissions();
        }
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getEffectivePermissions();
        }
        return null;
    }

    @Override
    public boolean isOp()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isOp();
    }

    @Override
    public void setOp(boolean bln)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setOp(bln);
        }
    }

    @Override
    public boolean isConversing()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isConversing();
    }

    @Override
    public void acceptConversationInput(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.acceptConversationInput(string);
        }
    }

    @Override
    public boolean beginConversation(Conversation c)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.beginConversation(c);
    }

    @Override
    public void abandonConversation(Conversation c)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.abandonConversation(c);
        }
    }

    @Override
    public void abandonConversation(Conversation c, ConversationAbandonedEvent cae)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.abandonConversation(c, cae);
        }
    }

    @Override
    public void sendMessage(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.sendMessage(string);
        }
    }

    @Override
    public void sendMessage(String[] strings)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.sendMessage(strings);
        }
    }

    @Override
    public boolean isOnline()
    {
        return this.getOfflinePlayer().isOnline();
    }

    @Override
    public boolean isBanned()
    {
        return this.getOfflinePlayer().isBanned();
    }

    @Override
    public void setBanned(boolean bln)
    {
        this.getOfflinePlayer().setBanned(bln);
    }

    @Override
    public boolean isWhitelisted()
    {
        return this.getOfflinePlayer().isWhitelisted();
    }

    @Override
    public void setWhitelisted(boolean bln)
    {
        this.getOfflinePlayer().setWhitelisted(bln);
    }

    @Override
    public Player getPlayer()
    {
        return this.getOfflinePlayer().getPlayer();
    }

    @Override
    public long getFirstPlayed()
    {
        return this.getOfflinePlayer().getFirstPlayed();
    }

    @Override
    public long getLastPlayed()
    {
        return this.getOfflinePlayer().getLastPlayed();
    }

    @Override
    public boolean hasPlayedBefore()
    {
        return this.getOfflinePlayer().hasPlayedBefore();
    }

    @Override
    public Map<String, Object> serialize()
    {
        return this.getOfflinePlayer().serialize();
    }

    @Override
    public void sendPluginMessage(Plugin plugin, String string, byte[] bytes)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.sendPluginMessage(plugin, string, bytes);
        }
    }

    @Override
    public Set<String> getListeningPluginChannels()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getListeningPluginChannels();
        }
        return null;
    }

    @Override
    public int getExpToLevel()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getExpToLevel();
        }
        return 0;
    }

    @Override
    public boolean hasLineOfSight(Entity other)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.hasLineOfSight(other);
    }

    @Override
    public boolean isValid()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isValid();
    }

    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setFlySpeed(value);
        }
    }

    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setWalkSpeed(value);
        }
    }

    @Override
    public float getFlySpeed()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getFlySpeed();
        }
        return 0;
    }

    @Override
    public float getWalkSpeed()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getWalkSpeed();
        }
        return 0;
    }

    @Override
    public void playSound(Location location, Sound sound, float volume, float pitch)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.playSound(location, sound, volume, pitch);
        }
    }

    @Override
    public Inventory getEnderChest()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getEnderChest();
        }
        Player offlinePlayer = BukkitUtils.getOfflinePlayerAsPlayer(this.getOfflinePlayer());
        if (offlinePlayer.hasPlayedBefore())
        {
            return offlinePlayer.getEnderChest();
        }
        return null;
    }

    @Override
    public void giveExpLevels(int amount)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.giveExpLevels(amount);
        }
    }

    @Override
    public void setBedSpawnLocation(Location location, boolean force)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setBedSpawnLocation(location, force);
        }
    }

    @Override
    public boolean getRemoveWhenFarAway()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.getRemoveWhenFarAway();
    }

    @Override
    public void setRemoveWhenFarAway(boolean state)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setRemoveWhenFarAway(state);
        }
    }

    @Override
    public EntityEquipment getEquipment()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getEquipment();
        }
        return null;
    }

    @Override
    public void setCanPickupItems(boolean pickup)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setCanPickupItems(pickup);
        }
    }

    @Override
    public boolean getCanPickupItems()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.getCanPickupItems();
    }

    @Override
    public void setTexturePack(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setTexturePack(string);
        }
    }

    @Override
    public void setResourcePack(String string)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setResourcePack(string);
        }
    }

    @Override
    public void resetMaxHealth()
    {
        Player player = this.getPlayer();
        if (player != null)
        {
            player.resetMaxHealth();
        }
        else
        {
            this.setMaxHealth(20);
        }
    }

    @Override
    public void setCustomName(String name)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setCustomName(name);
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                data.setString("CustomName", name);
                this.saveData();
            }
        }
    }

    @Override
    public String getCustomName()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getCustomName();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                data.getString("CustomName");
            }
        }
        return null;
    }

    @Override
    public void setCustomNameVisible(boolean flag)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setCustomNameVisible(flag);
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                data.setBoolean("CustomNameVisible", flag);
                this.saveData();
            }
        }
    }

    @Override
    public boolean isCustomNameVisible()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.isCustomNameVisible();
        }
        else
        {
            NBTTagCompound data = this.getData();
            if (data != null)
            {
                return data.getBoolean("CustomNameVisible");
            }
        }
        return false;
    }

    @Override
    public boolean isLeashed()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.isLeashed();
    }

    @Override
    public Entity getLeashHolder() throws IllegalStateException
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player == null ? null : player.getLeashHolder();
    }

    @Override
    public boolean setLeashHolder(Entity entity)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        return player != null && player.setLeashHolder(entity);
    }

    @Override
    public void setPlayerWeather(WeatherType wt)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setPlayerWeather(wt);
        }
    }

    @Override
    public WeatherType getPlayerWeather()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getPlayerWeather();
        }
        return null;
    }

    @Override
    public void resetPlayerWeather()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.resetPlayerWeather();
        }
    }

    @Override
    public Scoreboard getScoreboard()
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            return player.getScoreboard();
        }
        return null;
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) throws IllegalArgumentException, IllegalStateException
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.setScoreboard(scoreboard);
        }
    }

    @Override
    public void playSound(Location location, String s, float v, float v2)
    {
        final Player player = this.getOfflinePlayer().getPlayer();
        if (player != null)
        {
            player.playSound(location, s, v, v2);
        }
    }
}
