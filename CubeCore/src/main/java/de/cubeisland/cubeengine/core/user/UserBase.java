package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.BukkitDependend;
import de.cubeisland.cubeengine.CubeEngine;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
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

/**
 *
 * @author Anselm Brehme
 */
@BukkitDependend("Implementes Bukkit's Player")
public class UserBase implements Player
{
    protected OfflinePlayer offlinePlayer;

    public UserBase(OfflinePlayer offlinePlayer)
    {
        this.offlinePlayer = offlinePlayer;
    }

    public UserBase(Player player)
    {
        this.offlinePlayer = player;
    }
    
    @BukkitDependend("Uses the OfflinePlayer")
    public UserBase(String playername)
    {
        this.offlinePlayer = CubeEngine.getOfflinePlayer(playername);
    }

    public String getDisplayName()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getDisplayName();
        }
        return null;
    }

    public void setDisplayName(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setDisplayName(string);
        }
    }

    public String getPlayerListName()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPlayerListName();
        }
        return null;
    }

    public void setPlayerListName(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setPlayerListName(string);
        }
    }

    public void setCompassTarget(Location lctn)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setCompassTarget(lctn);
        }
    }

    public Location getCompassTarget()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getCompassTarget();
        }
        return null;
    }

    public InetSocketAddress getAddress()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getAddress();
        }
        return null;
    }

    public void sendRawMessage(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendRawMessage(string);
        }
    }

    public void kickPlayer(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.kickPlayer(string);
        }
    }

    public void chat(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.chat(string);
        }
    }

    public boolean performCommand(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.performCommand(string);
        }
        return false;
    }

    public boolean isSneaking()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isSneaking();
        }
        return false;
    }

    public void setSneaking(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSneaking(bln);
        }
    }

    public boolean isSprinting()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isSprinting();
        }
        return false;
    }

    public void setSprinting(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSprinting(bln);
        }
    }

    public void saveData()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.saveData();
        }
    }

    public void loadData()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.loadData();
        }
    }

    public void setSleepingIgnored(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSleepingIgnored(bln);
        }
    }

    public boolean isSleepingIgnored()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isSleepingIgnored();
        }
        return false;
    }

    public void playNote(Location lctn, byte b, byte b1)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playNote(lctn, b, b1);
        }
    }

    public void playNote(Location lctn, Instrument i, Note note)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playNote(lctn, i, note);
        }
    }

    public void playEffect(Location lctn, Effect effect, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playEffect(lctn, effect, i);
        }
    }

    public <T> void playEffect(Location lctn, Effect effect, T t)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playEffect(lctn, effect, t);
        }
    }

    public void sendBlockChange(Location lctn, Material mtrl, byte b)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendBlockChange(lctn, mtrl, b);
        }
    }

    public boolean sendChunkChange(Location lctn, int i, int i1, int i2, byte[] bytes)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.sendChunkChange(lctn, i, i1, i2, bytes);
        }
        return false;
    }

    public void sendBlockChange(Location lctn, int i, byte b)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendBlockChange(lctn, i, b);
        }
    }

    public void sendMap(MapView mv)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendMap(mv);
        }
    }

    @Deprecated
    public void updateInventory()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.updateInventory();
        }
    }

    public void awardAchievement(Achievement a)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.awardAchievement(a);
        }
    }

    public void incrementStatistic(Statistic ststc)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc);
        }
    }

    public void incrementStatistic(Statistic ststc, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, i);
        }
    }

    public void incrementStatistic(Statistic ststc, Material mtrl)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, mtrl);
        }
    }

    public void incrementStatistic(Statistic ststc, Material mtrl, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, mtrl, i);
        }
    }

    public void setPlayerTime(long l, boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setPlayerTime(l, bln);
        }
    }

    public long getPlayerTime()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPlayerTime();
        }
        return 0;
    }

    public long getPlayerTimeOffset()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPlayerTimeOffset();
        }
        return 0;
    }

    public boolean isPlayerTimeRelative()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isPlayerTimeRelative();
        }
        return false;
    }

    public void resetPlayerTime()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.resetPlayerTime();
        }
    }

    public void giveExp(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.giveExp(i);
        }
    }

    public float getExp()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getExp();
        }
        return 0;
    }

    public void setExp(float f)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setExp(f);
        }
    }

    public int getLevel()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLevel();
        }
        return 0;
    }

    public void setLevel(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setLevel(i);
        }
    }

    public int getTotalExperience()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getTotalExperience();
        }
        return 0;
    }

    public void setTotalExperience(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setTotalExperience(i);
        }
    }

    public float getExhaustion()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getExhaustion();
        }
        return 0;
    }

    public void setExhaustion(float f)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setExhaustion(f);
        }
    }

    public float getSaturation()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getSaturation();
        }
        return 0;
    }

    public void setSaturation(float f)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSaturation(f);
        }
    }

    public int getFoodLevel()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getFoodLevel();
        }
        return 0;
    }

    public void setFoodLevel(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFoodLevel(i);
        }
    }

    public Location getBedSpawnLocation()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getBedSpawnLocation();
        }
        return null;
    }

    public void setBedSpawnLocation(Location lctn)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setBedSpawnLocation(lctn);
        }
    }

    public boolean getAllowFlight()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getAllowFlight();
        }
        return false;
    }

    public void setAllowFlight(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setAllowFlight(bln);
        }
    }

    public void hidePlayer(Player playerToHide)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.hidePlayer(playerToHide);
        }
    }

    public void showPlayer(Player playerToShow)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.showPlayer(playerToShow);
        }
    }

    public boolean canSee(Player playerToCheck)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.canSee(playerToCheck);
        }
        return false;
    }

    public boolean isFlying()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isFlying();
        }
        return false;
    }

    public void setFlying(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFlying(bln);
        }
    }

    public String getName()
    {
        return this.offlinePlayer.getName();
    }

    public PlayerInventory getInventory()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getInventory();
        }
        return null;
    }

    public boolean setWindowProperty(Property prprt, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.setWindowProperty(prprt, i);
        }
        return false;
    }

    public InventoryView getOpenInventory()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getOpenInventory();
        }
        return null;
    }

    public InventoryView openInventory(Inventory invntr)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.openInventory(invntr);
        }
        return null;
    }

    public InventoryView openWorkbench(Location lctn, boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.openWorkbench(lctn, bln);
        }
        return null;
    }

    public InventoryView openEnchanting(Location lctn, boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.openEnchanting(lctn, bln);
        }
        return null;
    }

    public void openInventory(InventoryView iv)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.openInventory(iv);
        }
    }

    public void closeInventory()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.closeInventory();
        }
    }

    public ItemStack getItemInHand()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getItemInHand();
        }
        return null;
    }

    public void setItemInHand(ItemStack is)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setItemInHand(is);
        }
    }

    public ItemStack getItemOnCursor()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getItemOnCursor();
        }
        return null;
    }

    public void setItemOnCursor(ItemStack is)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setItemOnCursor(is);
        }
    }

    public boolean isSleeping()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isSleeping();
        }
        return false;
    }

    public int getSleepTicks()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getSleepTicks();
        }
        return 0;
    }

    public GameMode getGameMode()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getGameMode();
        }
        return null;
    }

    public void setGameMode(GameMode gm)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setGameMode(gm);
        }
    }

    public boolean isBlocking()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isBlocking();
        }
        return false;
    }

    public int getHealth()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getHealth();
        }
        return 0;
    }

    public void setHealth(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setHealth(i);
        }
    }

    public int getMaxHealth()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaxHealth();
        }
        return 0;
    }

    public double getEyeHeight()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaxHealth();
        }
        return 0;
    }

    public double getEyeHeight(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEyeHeight();
        }
        return 0;
    }

    public Location getEyeLocation()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEyeLocation();
        }
        return null;
    }

    public List<Block> getLineOfSight(HashSet<Byte> hs, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLineOfSight(hs, i);
        }
        return null;
    }

    public Block getTargetBlock(HashSet<Byte> hs, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getTargetBlock(hs, i);
        }
        return null;
    }

    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hs, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLastTwoTargetBlocks(hs, i);
        }
        return null;
    }

    @Deprecated
    public Egg throwEgg()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.throwEgg();
        }
        return null;
    }

    @Deprecated
    public Snowball throwSnowball()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.throwSnowball();
        }
        return null;
    }

    @Deprecated
    public Arrow shootArrow()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.shootArrow();
        }
        return null;
    }

    public <T extends Projectile> T launchProjectile(Class<? extends T> type)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.launchProjectile(type);
        }
        return null;
    }

    public int getRemainingAir()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getRemainingAir();
        }
        return 0;
    }

    public void setRemainingAir(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setRemainingAir(i);
        }
    }

    public int getMaximumAir()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaximumAir();
        }
        return 0;
    }

    public void setMaximumAir(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setMaximumAir(i);
        }
    }

    public void damage(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.damage(i);
        }
    }

    public void damage(int i, Entity entity)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.damage(i, entity);
        }
    }

    public int getMaximumNoDamageTicks()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaximumNoDamageTicks();
        }
        return 0;
    }

    public void setMaximumNoDamageTicks(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setMaximumNoDamageTicks(i);
        }
    }

    public int getLastDamage()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLastDamage();
        }
        return 0;
    }

    public void setLastDamage(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setLastDamage(i);
        }
    }

    public int getNoDamageTicks()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getNoDamageTicks();
        }
        return 0;
    }

    public void setNoDamageTicks(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setNoDamageTicks(i);
        }
    }

    public Player getKiller()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getKiller();
        }
        return null;
    }

    public boolean addPotionEffect(PotionEffect pe)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addPotionEffect(pe);
        }
        return false;
    }

    public boolean addPotionEffect(PotionEffect pe, boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addPotionEffect(pe);
        }
        return false;
    }

    public boolean addPotionEffects(Collection<PotionEffect> clctn)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addPotionEffects(clctn);
        }
        return false;
    }

    public boolean hasPotionEffect(PotionEffectType pet)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.hasPotionEffect(pet);
        }
        return false;
    }

    public void removePotionEffect(PotionEffectType pet)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.removePotionEffect(pet);
        }
    }

    public Collection<PotionEffect> getActivePotionEffects()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getActivePotionEffects();
        }
        return null;
    }

    public Location getLocation()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLocation();
        }
        return null;
    }

    public void setVelocity(Vector vector)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setVelocity(vector);
        }
    }

    public Vector getVelocity()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getVelocity();
        }
        return null;
    }

    public World getWorld()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getWorld();
        }
        return null;
    }

    public boolean teleport(Location lctn)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.teleport(lctn);
        }
        return false;
    }

    public boolean teleport(Location lctn, TeleportCause tc)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.teleport(lctn, tc);
        }
        return false;
    }

    public boolean teleport(Entity entity)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.teleport(entity);
        }
        return false;
    }

    public boolean teleport(Entity entity, TeleportCause tc)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.teleport(entity, tc);
        }
        return false;
    }

    public List<Entity> getNearbyEntities(double d, double d1, double d2)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getNearbyEntities(d, d1, d2);
        }
        return null;
    }

    public int getEntityId()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEntityId();
        }
        return -1;
    }

    public int getFireTicks()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getFireTicks();
        }
        return 0;
    }

    public int getMaxFireTicks()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaxFireTicks();
        }
        return 0;
    }

    public void setFireTicks(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFireTicks(i);
        }
    }

    public void remove()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.remove();
        }
    }

    public boolean isDead()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isDead();
        }
        return false;
    }

    public Server getServer()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getServer();
        }
        return null;
    }

    public Entity getPassenger()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPassenger();
        }
        return null;
    }

    public boolean setPassenger(Entity entity)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.setPassenger(entity);
        }
        return false;
    }

    public boolean isEmpty()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isEmpty();
        }
        return false;
    }

    public boolean eject()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.eject();
        }
        return false;
    }

    public float getFallDistance()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getFallDistance();
        }
        return 0;
    }

    public void setFallDistance(float f)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFallDistance(f);
        }
    }

    public void setLastDamageCause(EntityDamageEvent ede)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setLastDamageCause(ede);
        }
    }

    public EntityDamageEvent getLastDamageCause()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLastDamageCause();
        }
        return null;
    }

    public UUID getUniqueId()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getUniqueId();
        }
        return null;
    }

    public int getTicksLived()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getTicksLived();
        }
        return 0;
    }

    public void setTicksLived(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setTicksLived(i);
        }
    }

    public void playEffect(EntityEffect ee)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playEffect(ee);
        }
    }

    public EntityType getType()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getType();
        }
        return null;
    }

    public boolean isInsideVehicle()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isInsideVehicle();
        }
        return false;
    }

    public boolean leaveVehicle()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.leaveVehicle();
        }
        return false;
    }

    public Entity getVehicle()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getVehicle();
        }
        return null;
    }

    public void setMetadata(String string, MetadataValue mv)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setMetadata(string, mv);
        }
    }

    public List<MetadataValue> getMetadata(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMetadata(string);
        }
        return null;
    }

    public boolean hasMetadata(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.hasMetadata(string);
        }
        return false;
    }

    public void removeMetadata(String string, Plugin plugin)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.removeMetadata(string, plugin);
        }
    }

    public boolean isPermissionSet(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isPermissionSet(string);
        }
        return false;
    }

    public boolean isPermissionSet(Permission prmsn)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isPermissionSet(prmsn);
        }
        return false;
    }

    public boolean hasPermission(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.hasPermission(string);
        }
        return false;
    }

    public boolean hasPermission(Permission prmsn)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.hasPermission(prmsn);
        }
        return false;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, string, bln);
        }
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin);
        }
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, string, bln, i);
        }
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, i);
        }
        return null;
    }

    public void removeAttachment(PermissionAttachment pa)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.removeAttachment(pa);
        }
    }

    public void recalculatePermissions()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.recalculatePermissions();
        }
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEffectivePermissions();
        }
        return null;
    }

    public boolean isOp()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isOp();
        }
        return false;
    }

    public void setOp(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setOp(bln);
        }
    }

    public boolean isConversing()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isConversing();
        }
        return false;
    }

    public void acceptConversationInput(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.acceptConversationInput(string);
        }
    }

    public boolean beginConversation(Conversation c)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.beginConversation(c);
        }
        return false;
    }

    public void abandonConversation(Conversation c)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.abandonConversation(c);
        }
    }

    public void abandonConversation(Conversation c, ConversationAbandonedEvent cae)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.abandonConversation(c, cae);
        }
    }

    public void sendMessage(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendMessage(string);
        }
    }

    public void sendMessage(String[] strings)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendMessage(strings);
        }
    }

    public boolean isOnline()
    {
        return this.offlinePlayer.isOnline();
    }

    public boolean isBanned()
    {
        return this.offlinePlayer.isBanned();
    }

    public void setBanned(boolean bln)
    {
        this.offlinePlayer.setBanned(bln);
    }

    public boolean isWhitelisted()
    {
        return this.offlinePlayer.isWhitelisted();
    }

    public void setWhitelisted(boolean bln)
    {
        this.offlinePlayer.setWhitelisted(bln);
    }

    public Player getPlayer()
    {
        return this.offlinePlayer.getPlayer();
    }

    public long getFirstPlayed()
    {
        return this.offlinePlayer.getFirstPlayed();
    }

    public long getLastPlayed()
    {
        return this.offlinePlayer.getLastPlayed();
    }

    public boolean hasPlayedBefore()
    {
        return this.offlinePlayer.hasPlayedBefore();
    }

    public Map<String, Object> serialize()
    {
        return this.offlinePlayer.serialize();
    }

    public void sendPluginMessage(Plugin plugin, String string, byte[] bytes)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendPluginMessage(plugin, string, bytes);
        }
    }

    public Set<String> getListeningPluginChannels()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getListeningPluginChannels();
        }
        return null;
    }

    public int getExpToLevel()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getExpToLevel();
        }
        return 0;
    }

    public boolean hasLineOfSight(Entity other)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.hasLineOfSight(other);
        }
        return false;
    }

    public boolean isValid()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return this.isValid();
        }
        return false;
    }

    public void setFlySpeed(float value) throws IllegalArgumentException
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            this.setFlySpeed(value);
        }
    }

    public void setWalkSpeed(float value) throws IllegalArgumentException
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            this.setWalkSpeed(value);
        }
    }

    public float getFlySpeed()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return this.getFlySpeed();
        }
        return 0;
    }

    public float getWalkSpeed()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return this.getWalkSpeed();
        }
        return 0;
    }
}
