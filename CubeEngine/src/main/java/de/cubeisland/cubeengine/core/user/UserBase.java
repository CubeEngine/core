package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.BukkitDependend;
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

    @Override
    public String getDisplayName()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getDisplayName();
        }
        return null;
    }

    @Override
    public void setDisplayName(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setDisplayName(string);
        }
    }

    @Override
    public String getPlayerListName()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPlayerListName();
        }
        return null;
    }

    @Override
    public void setPlayerListName(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setPlayerListName(string);
        }
    }

    @Override
    public void setCompassTarget(Location lctn)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setCompassTarget(lctn);
        }
    }

    @Override
    public Location getCompassTarget()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getCompassTarget();
        }
        return null;
    }

    @Override
    public InetSocketAddress getAddress()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getAddress();
        }
        return null;
    }

    @Override
    public void sendRawMessage(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendRawMessage(string);
        }
    }

    @Override
    public void kickPlayer(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.kickPlayer(string);
        }
    }

    @Override
    public void chat(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.chat(string);
        }
    }

    @Override
    public boolean performCommand(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.performCommand(string);
        }
        return false;
    }

    @Override
    public boolean isSneaking()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isSneaking();
        }
        return false;
    }

    @Override
    public void setSneaking(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSneaking(bln);
        }
    }

    @Override
    public boolean isSprinting()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isSprinting();
        }
        return false;
    }

    @Override
    public void setSprinting(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSprinting(bln);
        }
    }

    @Override
    public void saveData()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.saveData();
        }
    }

    @Override
    public void loadData()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.loadData();
        }
    }

    @Override
    public void setSleepingIgnored(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSleepingIgnored(bln);
        }
    }

    @Override
    public boolean isSleepingIgnored()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isSleepingIgnored();
        }
        return false;
    }

    @Override
    public void playNote(Location lctn, byte b, byte b1)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playNote(lctn, b, b1);
        }
    }

    @Override
    public void playNote(Location lctn, Instrument i, Note note)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playNote(lctn, i, note);
        }
    }

    @Override
    public void playEffect(Location lctn, Effect effect, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playEffect(lctn, effect, i);
        }
    }

    @Override
    public <T> void playEffect(Location lctn, Effect effect, T t)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playEffect(lctn, effect, t);
        }
    }

    @Override
    public void sendBlockChange(Location lctn, Material mtrl, byte b)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendBlockChange(lctn, mtrl, b);
        }
    }

    @Override
    public boolean sendChunkChange(Location lctn, int i, int i1, int i2, byte[] bytes)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.sendChunkChange(lctn, i, i1, i2, bytes);
        }
        return false;
    }

    @Override
    public void sendBlockChange(Location lctn, int i, byte b)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendBlockChange(lctn, i, b);
        }
    }

    @Override
    public void sendMap(MapView mv)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendMap(mv);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void updateInventory()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.updateInventory();
        }
    }

    @Override
    public void awardAchievement(Achievement a)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.awardAchievement(a);
        }
    }

    @Override
    public void incrementStatistic(Statistic ststc)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc);
        }
    }

    @Override
    public void incrementStatistic(Statistic ststc, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, i);
        }
    }

    @Override
    public void incrementStatistic(Statistic ststc, Material mtrl)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, mtrl);
        }
    }

    @Override
    public void incrementStatistic(Statistic ststc, Material mtrl, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.incrementStatistic(ststc, mtrl, i);
        }
    }

    @Override
    public void setPlayerTime(long l, boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setPlayerTime(l, bln);
        }
    }

    @Override
    public long getPlayerTime()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPlayerTime();
        }
        return 0;
    }

    @Override
    public long getPlayerTimeOffset()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPlayerTimeOffset();
        }
        return 0;
    }

    @Override
    public boolean isPlayerTimeRelative()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isPlayerTimeRelative();
        }
        return false;
    }

    @Override
    public void resetPlayerTime()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.resetPlayerTime();
        }
    }

    @Override
    public void giveExp(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.giveExp(i);
        }
    }

    @Override
    public float getExp()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getExp();
        }
        return 0;
    }

    @Override
    public void setExp(float f)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setExp(f);
        }
    }

    @Override
    public int getLevel()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLevel();
        }
        return 0;
    }

    @Override
    public void setLevel(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setLevel(i);
        }
    }

    @Override
    public int getTotalExperience()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getTotalExperience();
        }
        return 0;
    }

    @Override
    public void setTotalExperience(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setTotalExperience(i);
        }
    }

    @Override
    public float getExhaustion()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getExhaustion();
        }
        return 0;
    }

    @Override
    public void setExhaustion(float f)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setExhaustion(f);
        }
    }

    @Override
    public float getSaturation()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getSaturation();
        }
        return 0;
    }

    @Override
    public void setSaturation(float f)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setSaturation(f);
        }
    }

    @Override
    public int getFoodLevel()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getFoodLevel();
        }
        return 0;
    }

    @Override
    public void setFoodLevel(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
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
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setBedSpawnLocation(lctn);
        }
    }

    @Override
    public boolean getAllowFlight()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getAllowFlight();
        }
        return false;
    }

    @Override
    public void setAllowFlight(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setAllowFlight(bln);
        }
    }

    @Override
    public void hidePlayer(Player playerToHide)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.hidePlayer(playerToHide);
        }
    }

    @Override
    public void showPlayer(Player playerToShow)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.showPlayer(playerToShow);
        }
    }

    @Override
    public boolean canSee(Player playerToCheck)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.canSee(playerToCheck);
        }
        return false;
    }

    @Override
    public boolean isFlying()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isFlying();
        }
        return false;
    }

    @Override
    public void setFlying(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
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
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getInventory();
        }
        return null;
    }

    @Override
    public boolean setWindowProperty(Property prprt, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.setWindowProperty(prprt, i);
        }
        return false;
    }

    @Override
    public InventoryView getOpenInventory()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getOpenInventory();
        }
        return null;
    }

    @Override
    public InventoryView openInventory(Inventory invntr)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.openInventory(invntr);
        }
        return null;
    }

    @Override
    public InventoryView openWorkbench(Location lctn, boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.openWorkbench(lctn, bln);
        }
        return null;
    }

    @Override
    public InventoryView openEnchanting(Location lctn, boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.openEnchanting(lctn, bln);
        }
        return null;
    }

    @Override
    public void openInventory(InventoryView iv)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.openInventory(iv);
        }
    }

    @Override
    public void closeInventory()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.closeInventory();
        }
    }

    @Override
    public ItemStack getItemInHand()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getItemInHand();
        }
        return null;
    }

    @Override
    public void setItemInHand(ItemStack is)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setItemInHand(is);
        }
    }

    @Override
    public ItemStack getItemOnCursor()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getItemOnCursor();
        }
        return null;
    }

    @Override
    public void setItemOnCursor(ItemStack is)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setItemOnCursor(is);
        }
    }

    @Override
    public boolean isSleeping()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isSleeping();
        }
        return false;
    }

    @Override
    public int getSleepTicks()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getSleepTicks();
        }
        return 0;
    }

    @Override
    public GameMode getGameMode()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getGameMode();
        }
        return null;
    }

    @Override
    public void setGameMode(GameMode gm)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setGameMode(gm);
        }
    }

    @Override
    public boolean isBlocking()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isBlocking();
        }
        return false;
    }

    @Override
    public int getHealth()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getHealth();
        }
        return 0;
    }

    @Override
    public void setHealth(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setHealth(i);
        }
    }

    @Override
    public int getMaxHealth()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaxHealth();
        }
        return 0;
    }

    @Override
    public double getEyeHeight()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaxHealth();
        }
        return 0;
    }

    @Override
    public double getEyeHeight(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEyeHeight();
        }
        return 0;
    }

    @Override
    public Location getEyeLocation()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEyeLocation();
        }
        return null;
    }

    @Override
    public List<Block> getLineOfSight(HashSet<Byte> hs, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLineOfSight(hs, i);
        }
        return null;
    }

    @Override
    public Block getTargetBlock(HashSet<Byte> hs, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getTargetBlock(hs, i);
        }
        return null;
    }

    @Override
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
    @Override
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
    @Override
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
    @Override
    public Arrow shootArrow()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.shootArrow();
        }
        return null;
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> type)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.launchProjectile(type);
        }
        return null;
    }

    @Override
    public int getRemainingAir()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getRemainingAir();
        }
        return 0;
    }

    @Override
    public void setRemainingAir(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setRemainingAir(i);
        }
    }

    @Override
    public int getMaximumAir()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaximumAir();
        }
        return 0;
    }

    @Override
    public void setMaximumAir(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setMaximumAir(i);
        }
    }

    @Override
    public void damage(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.damage(i);
        }
    }

    @Override
    public void damage(int i, Entity entity)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.damage(i, entity);
        }
    }

    @Override
    public int getMaximumNoDamageTicks()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaximumNoDamageTicks();
        }
        return 0;
    }

    @Override
    public void setMaximumNoDamageTicks(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setMaximumNoDamageTicks(i);
        }
    }

    @Override
    public int getLastDamage()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLastDamage();
        }
        return 0;
    }

    @Override
    public void setLastDamage(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setLastDamage(i);
        }
    }

    @Override
    public int getNoDamageTicks()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getNoDamageTicks();
        }
        return 0;
    }

    @Override
    public void setNoDamageTicks(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setNoDamageTicks(i);
        }
    }

    @Override
    public Player getKiller()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getKiller();
        }
        return null;
    }

    @Override
    public boolean addPotionEffect(PotionEffect pe)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addPotionEffect(pe);
        }
        return false;
    }

    @Override
    public boolean addPotionEffect(PotionEffect pe, boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addPotionEffect(pe);
        }
        return false;
    }

    @Override
    public boolean addPotionEffects(Collection<PotionEffect> clctn)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addPotionEffects(clctn);
        }
        return false;
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType pet)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.hasPotionEffect(pet);
        }
        return false;
    }

    @Override
    public void removePotionEffect(PotionEffectType pet)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.removePotionEffect(pet);
        }
    }

    @Override
    public Collection<PotionEffect> getActivePotionEffects()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getActivePotionEffects();
        }
        return null;
    }

    @Override
    public Location getLocation()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLocation();
        }
        return null;
    }

    @Override
    public void setVelocity(Vector vector)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setVelocity(vector);
        }
    }

    @Override
    public Vector getVelocity()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getVelocity();
        }
        return null;
    }

    @Override
    public World getWorld()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getWorld();
        }
        return null;
    }

    @Override
    public boolean teleport(Location lctn)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.teleport(lctn);
        }
        return false;
    }

    @Override
    public boolean teleport(Location lctn, TeleportCause tc)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.teleport(lctn, tc);
        }
        return false;
    }

    @Override
    public boolean teleport(Entity entity)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.teleport(entity);
        }
        return false;
    }

    @Override
    public boolean teleport(Entity entity, TeleportCause tc)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.teleport(entity, tc);
        }
        return false;
    }

    @Override
    public List<Entity> getNearbyEntities(double d, double d1, double d2)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getNearbyEntities(d, d1, d2);
        }
        return null;
    }

    @Override
    public int getEntityId()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEntityId();
        }
        return -1;
    }

    @Override
    public int getFireTicks()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getFireTicks();
        }
        return 0;
    }

    @Override
    public int getMaxFireTicks()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMaxFireTicks();
        }
        return 0;
    }

    @Override
    public void setFireTicks(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFireTicks(i);
        }
    }

    @Override
    public void remove()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.remove();
        }
    }

    @Override
    public boolean isDead()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isDead();
        }
        return false;
    }

    @Override
    public Server getServer()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getServer();
        }
        return null;
    }

    @Override
    public Entity getPassenger()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getPassenger();
        }
        return null;
    }

    @Override
    public boolean setPassenger(Entity entity)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.setPassenger(entity);
        }
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isEmpty();
        }
        return false;
    }

    @Override
    public boolean eject()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.eject();
        }
        return false;
    }

    @Override
    public float getFallDistance()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getFallDistance();
        }
        return 0;
    }

    @Override
    public void setFallDistance(float f)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFallDistance(f);
        }
    }

    @Override
    public void setLastDamageCause(EntityDamageEvent ede)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setLastDamageCause(ede);
        }
    }

    @Override
    public EntityDamageEvent getLastDamageCause()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getLastDamageCause();
        }
        return null;
    }

    @Override
    public UUID getUniqueId()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getUniqueId();
        }
        return null;
    }

    @Override
    public int getTicksLived()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getTicksLived();
        }
        return 0;
    }

    @Override
    public void setTicksLived(int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setTicksLived(i);
        }
    }

    @Override
    public void playEffect(EntityEffect ee)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playEffect(ee);
        }
    }

    @Override
    public EntityType getType()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getType();
        }
        return null;
    }

    @Override
    public boolean isInsideVehicle()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isInsideVehicle();
        }
        return false;
    }

    @Override
    public boolean leaveVehicle()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.leaveVehicle();
        }
        return false;
    }

    @Override
    public Entity getVehicle()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getVehicle();
        }
        return null;
    }

    @Override
    public void setMetadata(String string, MetadataValue mv)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setMetadata(string, mv);
        }
    }

    @Override
    public List<MetadataValue> getMetadata(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getMetadata(string);
        }
        return null;
    }

    @Override
    public boolean hasMetadata(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.hasMetadata(string);
        }
        return false;
    }

    @Override
    public void removeMetadata(String string, Plugin plugin)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.removeMetadata(string, plugin);
        }
    }

    @Override
    public boolean isPermissionSet(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isPermissionSet(string);
        }
        return false;
    }

    @Override
    public boolean isPermissionSet(Permission prmsn)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isPermissionSet(prmsn);
        }
        return false;
    }

    @Override
    public boolean hasPermission(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.hasPermission(string);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Permission prmsn)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.hasPermission(prmsn);
        }
        return false;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, string, bln);
        }
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin);
        }
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, string, bln, i);
        }
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.addAttachment(plugin, i);
        }
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment pa)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.removeAttachment(pa);
        }
    }

    @Override
    public void recalculatePermissions()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.recalculatePermissions();
        }
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEffectivePermissions();
        }
        return null;
    }

    @Override
    public boolean isOp()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isOp();
        }
        return false;
    }

    @Override
    public void setOp(boolean bln)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setOp(bln);
        }
    }

    @Override
    public boolean isConversing()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isConversing();
        }
        return false;
    }

    @Override
    public void acceptConversationInput(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.acceptConversationInput(string);
        }
    }

    @Override
    public boolean beginConversation(Conversation c)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.beginConversation(c);
        }
        return false;
    }

    @Override
    public void abandonConversation(Conversation c)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.abandonConversation(c);
        }
    }

    @Override
    public void abandonConversation(Conversation c, ConversationAbandonedEvent cae)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.abandonConversation(c, cae);
        }
    }

    @Override
    public void sendMessage(String string)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendMessage(string);
        }
    }

    @Override
    public void sendMessage(String[] strings)
    {
        Player player = this.offlinePlayer.getPlayer();
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
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.sendPluginMessage(plugin, string, bytes);
        }
    }

    @Override
    public Set<String> getListeningPluginChannels()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getListeningPluginChannels();
        }
        return null;
    }

    @Override
    public int getExpToLevel()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getExpToLevel();
        }
        return 0;
    }

    @Override
    public boolean hasLineOfSight(Entity other)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.hasLineOfSight(other);
        }
        return false;
    }

    @Override
    public boolean isValid()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.isValid();
        }
        return false;
    }

    @Override
    public void setFlySpeed(float value) throws IllegalArgumentException
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setFlySpeed(value);
        }
    }

    @Override
    public void setWalkSpeed(float value) throws IllegalArgumentException
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.setWalkSpeed(value);
        }
    }

    @Override
    public float getFlySpeed()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getFlySpeed();
        }
        return 0;
    }

    @Override
    public float getWalkSpeed()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getWalkSpeed();
        }
        return 0;
    }

    @Override
    public void playSound(Location location, Sound sound, float volume, float pitch)
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            player.playSound(location, sound, volume, pitch);
        }
    }

    @Override
    public Inventory getEnderChest()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            return player.getEnderChest();
        }
        return null;
    }
    
    /*
    public String getLanguage()
    {
        Player player = this.offlinePlayer.getPlayer();
        if (player != null)
        {
            // TODO implement if Bukkit pulled https://github.com/Bukkit/Bukkit/pull/683
            // return this.offlinePlayer.getLanguage();
        }
        return I18n.SOURCE_LANGUAGE;
    }*/
}