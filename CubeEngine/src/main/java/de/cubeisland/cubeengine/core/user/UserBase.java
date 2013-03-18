package de.cubeisland.cubeengine.core.user;

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
import org.bukkit.util.Vector;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Wrapper around the BukkitPlayer/OfflinePlayer
 */
public class UserBase implements Player
{
    protected OfflinePlayer offlinePlayer;

    public UserBase(OfflinePlayer offlinePlayer)
    {
        this.offlinePlayer = offlinePlayer;
    }

    @Override
    public String getDisplayName()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getDisplayName();
        }
        return null;
    }

    @Override
    public void setDisplayName(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setDisplayName(string);
        }
    }

    @Override
    public String getPlayerListName()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPlayerListName();
        }
        return null;
    }

    @Override
    public void setPlayerListName(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setPlayerListName(string);
        }
    }

    @Override
    public void setCompassTarget(Location lctn)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setCompassTarget(lctn);
        }
    }

    @Override
    public Location getCompassTarget()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getCompassTarget();
        }
        return null;
    }

    @Override
    public InetSocketAddress getAddress()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getAddress();
        }
        return null;
    }

    @Override
    public void sendRawMessage(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendRawMessage(string);
        }
    }

    @Override
    public void kickPlayer(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.kickPlayer(string);
        }
    }

    @Override
    public void chat(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.chat(string);
        }
    }

    @Override
    public boolean performCommand(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.performCommand(string);
    }

    @Override
    public boolean isSneaking()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isSneaking();
    }

    @Override
    public void setSneaking(boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSneaking(bln);
        }
    }

    @Override
    public boolean isSprinting()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isSprinting();
    }

    @Override
    public void setSprinting(boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSprinting(bln);
        }
    }

    @Override
    public void saveData()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.saveData();
        }
    }

    @Override
    public void loadData()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.loadData();
        }
    }

    @Override
    public void setSleepingIgnored(boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSleepingIgnored(bln);
        }
    }

    @Override
    public boolean isSleepingIgnored()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isSleepingIgnored();
    }

    @Override
    public void playNote(Location lctn, byte b, byte b1)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playNote(lctn, b, b1);
        }
    }

    @Override
    public void playNote(Location lctn, Instrument i, Note note)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playNote(lctn, i, note);
        }
    }

    @Override
    public void playEffect(Location lctn, Effect effect, int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playEffect(lctn, effect, i);
        }
    }

    @Override
    public <T> void playEffect(Location lctn, Effect effect, T t)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playEffect(lctn, effect, t);
        }
    }

    @Override
    public void sendBlockChange(Location lctn, Material mtrl, byte b)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendBlockChange(lctn, mtrl, b);
        }
    }

    @Override
    public boolean sendChunkChange(Location lctn, int i, int i1, int i2, byte[] bytes)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.sendChunkChange(lctn, i, i1, i2, bytes);
    }

    @Override
    public void sendBlockChange(Location lctn, int i, byte b)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendBlockChange(lctn, i, b);
        }
    }

    @Override
    public void sendMap(MapView mv)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendMap(mv);
        }
    }

    @Override
    @Deprecated
    public void updateInventory()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.updateInventory();
        }
    }

    @Override
    public void awardAchievement(Achievement a)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.awardAchievement(a);
        }
    }

    @Override
    public void incrementStatistic(Statistic statistic)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(statistic);
        }
    }

    @Override
    public void incrementStatistic(Statistic ststc, int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, i);
        }
    }

    @Override
    public void incrementStatistic(Statistic ststc, Material mtrl)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, mtrl);
        }
    }

    @Override
    public void incrementStatistic(Statistic ststc, Material mtrl, int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, mtrl, i);
        }
    }

    @Override
    public void setPlayerTime(long l, boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setPlayerTime(l, bln);
        }
    }

    @Override
    public long getPlayerTime()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPlayerTime();
        }
        return 0;
    }

    @Override
    public long getPlayerTimeOffset()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPlayerTimeOffset();
        }
        return 0;
    }

    @Override
    public boolean isPlayerTimeRelative()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isPlayerTimeRelative();
    }

    @Override
    public void resetPlayerTime()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.resetPlayerTime();
        }
    }

    @Override
    public void giveExp(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.giveExp(i);
        }
    }

    @Override
    public float getExp()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getExp();
        }
        return 0;
    }

    @Override
    public void setExp(float f)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setExp(f);
        }
    }

    @Override
    public int getLevel()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLevel();
        }
        return 0;
    }

    @Override
    public void setLevel(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setLevel(i);
        }
    }

    @Override
    public int getTotalExperience()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getTotalExperience();
        }
        return 0;
    }

    @Override
    public void setTotalExperience(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setTotalExperience(i);
        }
    }

    @Override
    public float getExhaustion()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getExhaustion();
        }
        return 0;
    }

    @Override
    public void setExhaustion(float f)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setExhaustion(f);
        }
    }

    @Override
    public float getSaturation()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getSaturation();
        }
        return 0;
    }

    @Override
    public void setSaturation(float f)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSaturation(f);
        }
    }

    @Override
    public int getFoodLevel()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getFoodLevel();
        }
        return 0;
    }

    @Override
    public void setFoodLevel(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFoodLevel(i);
        }
    }

    @Override
    public Location getBedSpawnLocation()
    {
        return this.offlinePlayer.getBedSpawnLocation();
    }

    @Override
    public void setBedSpawnLocation(Location lctn)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setBedSpawnLocation(lctn);
        }
    }

    @Override
    public boolean getAllowFlight()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.getAllowFlight();
    }

    @Override
    public void setAllowFlight(boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setAllowFlight(bln);
        }
    }

    @Override
    public void hidePlayer(Player playerToHide)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.hidePlayer(playerToHide);
        }
    }

    @Override
    public void showPlayer(Player playerToShow)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.showPlayer(playerToShow);
        }
    }

    @Override
    public boolean canSee(Player playerToCheck)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.canSee(playerToCheck);
    }

    @Override
    @Deprecated
    public boolean isOnGround()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isOnGround();
    }

    @Override
    public boolean isFlying()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isFlying();
    }

    @Override
    public void setFlying(boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFlying(bln);
        }
    }

    @Override
    public String getName()
    {
        return this.offlinePlayer.getName();
    }

    @Override
    public PlayerInventory getInventory()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getInventory();
        }
        return null;
    }

    @Override
    public boolean setWindowProperty(Property prprt, int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.setWindowProperty(prprt, i);
    }

    @Override
    public InventoryView getOpenInventory()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getOpenInventory();
        }
        return null;
    }

    @Override
    public InventoryView openInventory(Inventory invntr)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.openInventory(invntr);
        }
        return null;
    }

    @Override
    public InventoryView openWorkbench(Location lctn, boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.openWorkbench(lctn, bln);
        }
        return null;
    }

    @Override
    public InventoryView openEnchanting(Location lctn, boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.openEnchanting(lctn, bln);
        }
        return null;
    }

    @Override
    public void openInventory(InventoryView iv)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.openInventory(iv);
        }
    }

    @Override
    public void closeInventory()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.closeInventory();
        }
    }

    @Override
    public ItemStack getItemInHand()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getItemInHand();
        }
        return null;
    }

    @Override
    public void setItemInHand(ItemStack is)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setItemInHand(is);
        }
    }

    @Override
    public ItemStack getItemOnCursor()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getItemOnCursor();
        }
        return null;
    }

    @Override
    public void setItemOnCursor(ItemStack is)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setItemOnCursor(is);
        }
    }

    @Override
    public boolean isSleeping()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isSleeping();
    }

    @Override
    public int getSleepTicks()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getSleepTicks();
        }
        return 0;
    }

    @Override
    public GameMode getGameMode()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getGameMode();
        }
        return null;
    }

    @Override
    public void setGameMode(GameMode gm)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setGameMode(gm);
        }
    }

    @Override
    public boolean isBlocking()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isBlocking();
    }

    @Override
    public int getHealth()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getHealth();
        }
        return 0;
    }

    @Override
    public void setHealth(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setHealth(i);
        }
    }

    @Override
    public int getMaxHealth()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaxHealth();
        }
        return 0;
    }

    @Override
    public double getEyeHeight()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaxHealth();
        }
        return 0;
    }

    @Override
    public double getEyeHeight(boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEyeHeight();
        }
        return 0;
    }

    @Override
    public Location getEyeLocation()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEyeLocation();
        }
        return null;
    }

    @Override
    public List<Block> getLineOfSight(HashSet<Byte> hs, int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLineOfSight(hs, i);
        }
        return null;
    }

    @Override
    public Block getTargetBlock(HashSet<Byte> hs, int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getTargetBlock(hs, i);
        }
        return null;
    }

    @Override
    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hs, int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
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
        final Player player = this.offlinePlayer.getPlayer();
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
        final Player player = this.offlinePlayer.getPlayer();
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
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.shootArrow();
        }
        return null;
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> type)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.launchProjectile(type);
        }
        return null;
    }

    @Override
    public int getRemainingAir()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getRemainingAir();
        }
        return 0;
    }

    @Override
    public void setRemainingAir(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setRemainingAir(i);
        }
    }

    @Override
    public int getMaximumAir()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaximumAir();
        }
        return 0;
    }

    @Override
    public void setMaximumAir(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setMaximumAir(i);
        }
    }

    @Override
    public void damage(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.damage(i);
        }
    }

    @Override
    public void damage(int i, Entity entity)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.damage(i, entity);
        }
    }

    @Override
    public int getMaximumNoDamageTicks()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaximumNoDamageTicks();
        }
        return 0;
    }

    @Override
    public void setMaximumNoDamageTicks(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setMaximumNoDamageTicks(i);
        }
    }

    @Override
    public int getLastDamage()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLastDamage();
        }
        return 0;
    }

    @Override
    public void setLastDamage(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setLastDamage(i);
        }
    }

    @Override
    public int getNoDamageTicks()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getNoDamageTicks();
        }
        return 0;
    }

    @Override
    public void setNoDamageTicks(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setNoDamageTicks(i);
        }
    }

    @Override
    public Player getKiller()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getKiller();
        }
        return null;
    }

    @Override
    public boolean addPotionEffect(PotionEffect pe)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.addPotionEffect(pe);
    }

    @Override
    public boolean addPotionEffect(PotionEffect pe, boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.addPotionEffect(pe);
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> clctn)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.addPotionEffects(clctn);
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType pet)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.hasPotionEffect(pet);
    }

    @Override
    public void removePotionEffect(PotionEffectType pet)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.removePotionEffect(pet);
        }
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getActivePotionEffects();
        }
        return null;
    }

    @Override
    public Location getLocation()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLocation();
        }
        return null;
    }

    public Location getLocation(Location location)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLocation(location);
        }
        return null;
    }

    @Override
    public void setVelocity(Vector vector)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setVelocity(vector);
        }
    }

    @Override
    public Vector getVelocity()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getVelocity();
        }
        return null;
    }

    @Override
    public World getWorld()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getWorld();
        }
        return null;
    }

    @Override
    public boolean teleport(Location lctn)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.teleport(lctn);
    }

    @Override
    public boolean teleport(Location lctn, TeleportCause tc)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.teleport(lctn, tc);
    }

    @Override
    public boolean teleport(Entity entity)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.teleport(entity);
    }

    @Override
    public boolean teleport(Entity entity, TeleportCause tc)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.teleport(entity, tc);
    }

    @Override
    public List<Entity> getNearbyEntities(double d, double d1, double d2)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getNearbyEntities(d, d1, d2);
        }
        return null;
    }

    @Override
    public int getEntityId()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEntityId();
        }
        return -1;
    }

    @Override
    public int getFireTicks()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getFireTicks();
        }
        return 0;
    }

    @Override
    public int getMaxFireTicks()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaxFireTicks();
        }
        return 0;
    }

    @Override
    public void setFireTicks(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFireTicks(i);
        }
    }

    @Override
    public void remove()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.remove();
        }
    }

    @Override
    public boolean isDead()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isDead();
    }

    @Override
    public Server getServer()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getServer();
        }
        return Bukkit.getServer();
    }

    @Override
    public Entity getPassenger()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPassenger();
        }
        return null;
    }

    @Override
    public boolean setPassenger(Entity entity)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.setPassenger(entity);
    }

    @Override
    public boolean isEmpty()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isEmpty();
    }

    @Override
    public boolean eject()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.eject();
    }

    @Override
    public float getFallDistance()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getFallDistance();
        }
        return 0;
    }

    @Override
    public void setFallDistance(float f)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFallDistance(f);
        }
    }

    @Override
    public void setLastDamageCause(EntityDamageEvent ede)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setLastDamageCause(ede);
        }
    }

    @Override
    public EntityDamageEvent getLastDamageCause()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLastDamageCause();
        }
        return null;
    }

    @Override
    public UUID getUniqueId()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getUniqueId();
        }
        return null;
    }

    @Override
    public int getTicksLived()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getTicksLived();
        }
        return 0;
    }

    @Override
    public void setTicksLived(int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setTicksLived(i);
        }
    }

    @Override
    public void playEffect(EntityEffect ee)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playEffect(ee);
        }
    }

    @Override
    public EntityType getType()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getType();
        }
        return null;
    }

    @Override
    public boolean isInsideVehicle()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isInsideVehicle();
    }

    @Override
    public boolean leaveVehicle()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.leaveVehicle();
    }

    @Override
    public Entity getVehicle()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getVehicle();
        }
        return null;
    }

    @Override
    public void setMetadata(String string, MetadataValue mv)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setMetadata(string, mv);
        }
    }

    @Override
    public List<MetadataValue> getMetadata(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMetadata(string);
        }
        return null;
    }

    @Override
    public boolean hasMetadata(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.hasMetadata(string);
    }

    @Override
    public void removeMetadata(String string, Plugin plugin)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.removeMetadata(string, plugin);
        }
    }

    @Override
    public boolean isPermissionSet(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isPermissionSet(string);
    }

    @Override
    public boolean isPermissionSet(Permission prmsn)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isPermissionSet(prmsn);
    }

    @Override
    public boolean hasPermission(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.hasPermission(string);
    }

    @Override
    public boolean hasPermission(Permission prmsn)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.hasPermission(prmsn);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, string, bln);
        }
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin);
        }
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, string, bln, i);
        }
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, i);
        }
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment pa)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.removeAttachment(pa);
        }
    }

    @Override
    public void recalculatePermissions()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.recalculatePermissions();
        }
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEffectivePermissions();
        }
        return null;
    }

    @Override
    public boolean isOp()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isOp();
    }

    @Override
    public void setOp(boolean bln)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setOp(bln);
        }
    }

    @Override
    public boolean isConversing()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isConversing();
    }

    @Override
    public void acceptConversationInput(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.acceptConversationInput(string);
        }
    }

    @Override
    public boolean beginConversation(Conversation c)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.beginConversation(c);
    }

    @Override
    public void abandonConversation(Conversation c)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.abandonConversation(c);
        }
    }

    @Override
    public void abandonConversation(Conversation c, ConversationAbandonedEvent cae)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.abandonConversation(c, cae);
        }
    }

    @Override
    public void sendMessage(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendMessage(string);
        }
    }

    @Override
    public void sendMessage(String[] strings)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendMessage(strings);
        }
    }

    @Override
    public boolean isOnline()
    {
        return this.offlinePlayer.isOnline();
    }

    @Override
    public boolean isBanned()
    {
        return this.offlinePlayer.isBanned();
    }

    @Override
    public void setBanned(boolean bln)
    {
        this.offlinePlayer.setBanned(bln);
    }

    @Override
    public boolean isWhitelisted()
    {
        return this.offlinePlayer.isWhitelisted();
    }

    @Override
    public void setWhitelisted(boolean bln)
    {
        this.offlinePlayer.setWhitelisted(bln);
    }

    @Override
    public Player getPlayer()
    {
        return this.offlinePlayer.getPlayer();
    }

    @Override
    public long getFirstPlayed()
    {
        return this.offlinePlayer.getFirstPlayed();
    }

    @Override
    public long getLastPlayed()
    {
        return this.offlinePlayer.getLastPlayed();
    }

    @Override
    public boolean hasPlayedBefore()
    {
        return this.offlinePlayer.hasPlayedBefore();
    }

    @Override
    public Map<String, Object> serialize()
    {
        return this.offlinePlayer.serialize();
    }

    @Override
    public void sendPluginMessage(Plugin plugin, String string, byte[] bytes)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendPluginMessage(plugin, string, bytes);
        }
    }

    @Override
    public Set<String> getListeningPluginChannels()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getListeningPluginChannels();
        }
        return null;
    }

    @Override
    public int getExpToLevel()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getExpToLevel();
        }
        return 0;
    }

    @Override
    public boolean hasLineOfSight(Entity other)
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.hasLineOfSight(other);
    }

    @Override
    public boolean isValid()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.isValid();
    }

    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFlySpeed(value);
        }
    }

    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setWalkSpeed(value);
        }
    }

    @Override
    public float getFlySpeed()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getFlySpeed();
        }
        return 0;
    }

    @Override
    public float getWalkSpeed()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getWalkSpeed();
        }
        return 0;
    }

    @Override
    public void playSound(Location location, Sound sound, float volume, float pitch)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playSound(location, sound, volume, pitch);
        }
    }

    @Override
    public Inventory getEnderChest()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEnderChest();
        }
        return null;
    }

    /*
     * public String getLanguage()
     * {
     * final Player player = this.offlinePlayer.getPlayer();
     * if (player != null)
     * {
     * // TODO implement if Bukkit pulled https://github.com/Bukkit/Bukkit/pull/683
     * // return this.offlinePlayer.getLanguage();
     * }
     * return I18n.sourceLanguage;
     * } */

    @Override
    public void giveExpLevels(int amount)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.giveExpLevels(amount);
        }
    }

    @Override
    public void setBedSpawnLocation(Location location, boolean force)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setBedSpawnLocation(location, force);
        }
    }

    @Override
    public boolean getRemoveWhenFarAway()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.getRemoveWhenFarAway();
    }

    @Override
    public void setRemoveWhenFarAway(boolean state)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setRemoveWhenFarAway(state);
        }
    }

    @Override
    public EntityEquipment getEquipment()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEquipment();
        }
        return null;
    }

    @Override
    public void setCanPickupItems(boolean pickup)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setCanPickupItems(pickup);
        }
    }

    @Override
    public boolean getCanPickupItems()
    {
        final Player player = this.offlinePlayer.getPlayer();
        return player != null && player.getCanPickupItems();
    }

    @Override
    public void setTexturePack(String string)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setTexturePack(string);
        }
    }

    @Override
    public void setMaxHealth(int health)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setMaxHealth(health);
        }
    }

    @Override
    public void resetMaxHealth()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.resetMaxHealth();
        }
    }

    @Override
    public void setCustomName(String name)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setCustomName(name);
        }
    }

    @Override
    public String getCustomName()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getCustomName();
        }
        return null;
    }

    @Override
    public void setCustomNameVisible(boolean flag)
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setCustomNameVisible(flag);
        }
    }

    @Override
    public boolean isCustomNameVisible()
    {
        final Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isCustomNameVisible();
        }
        return false;
    }
}
