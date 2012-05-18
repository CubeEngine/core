package de.cubeisland.cubeengine.core.user;

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
 * @author Anselm
 */
public class CubeUserBase implements Player
{
    protected OfflinePlayer offlinePlayer;
    
    public CubeUserBase(OfflinePlayer offlinePlayer)
    {
        this.offlinePlayer = offlinePlayer;
    }
    
    public CubeUserBase(Player player)
    {
        this.offlinePlayer = player;
    }
    
    public String getDisplayName()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getDisplayName();
        }
        return null;
    }

    public void setDisplayName(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setDisplayName(string);
        }
    }

    public String getPlayerListName()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getPlayerListName();
        }
        return null;
    }

    public void setPlayerListName(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setPlayerListName(string);
        }
    }

    public void setCompassTarget(Location lctn)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setCompassTarget(lctn);
        }
    }

    public Location getCompassTarget()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getCompassTarget();
        }
        return null;
    }

    public InetSocketAddress getAddress()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getAddress();
        }
        return null;
    }

    public void sendRawMessage(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.sendRawMessage(string);
        }
    }

    public void kickPlayer(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.kickPlayer(string);
        }
    }

    public void chat(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.chat(string);
        }
    }

    public boolean performCommand(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.performCommand(string);
        }
        return false;
    }

    public boolean isSneaking()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isSneaking();
        }
        return false;
    }

    public void setSneaking(boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setSneaking(bln);
        }
    }

    public boolean isSprinting()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isSprinting();
        }
        return false;
    }

    public void setSprinting(boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setSprinting(bln);
        }
    }

    public void saveData()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.saveData();
        }
    }

    public void loadData()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.loadData();
        }
    }

    public void setSleepingIgnored(boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .setSleepingIgnored(bln);
        }
    }

    public boolean isSleepingIgnored()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .isSleepingIgnored();
        }
        return false;
    }

    public void playNote(Location lctn, byte b, byte b1)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .playNote(lctn, b, b1);
        }
    }

    public void playNote(Location lctn, Instrument i, Note note)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .playNote(lctn, i, note);
        }
    }

    public void playEffect(Location lctn, Effect effect, int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .playEffect(lctn, effect, i);
        }
    }

    public <T> void playEffect(Location lctn, Effect effect, T t)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .playEffect(lctn, effect, t);
        }
    }

    public void sendBlockChange(Location lctn, Material mtrl, byte b)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .sendBlockChange(lctn, mtrl, b);
        }
    }

    public boolean sendChunkChange(Location lctn, int i, int i1, int i2, byte[] bytes)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .sendChunkChange(lctn, i, i1, i2, bytes);
        }
        return false;
    }

    public void sendBlockChange(Location lctn, int i, byte b)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .sendBlockChange(lctn, i, b);
        }
    }

    public void sendMap(MapView mv)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .sendMap(mv);
        }
    }

    public void updateInventory()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .updateInventory();
        }
    }

    public void awardAchievement(Achievement a)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .awardAchievement(a);
        }
    }

    public void incrementStatistic(Statistic ststc)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .incrementStatistic(ststc);
        }
    }

    public void incrementStatistic(Statistic ststc, int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .incrementStatistic(ststc, i);
        }
    }

    public void incrementStatistic(Statistic ststc, Material mtrl)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .incrementStatistic(ststc, mtrl);
        }
    }

    public void incrementStatistic(Statistic ststc, Material mtrl, int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .incrementStatistic(ststc, mtrl, i);
        }
    }

    public void setPlayerTime(long l, boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .setPlayerTime(l, bln);
        }
    }

    public long getPlayerTime()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .getPlayerTime();
        }
        return 0;
    }

    public long getPlayerTimeOffset()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .getPlayerTimeOffset();
        }
        return 0;
    }

    public boolean isPlayerTimeRelative()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .isPlayerTimeRelative();
        }
        return false;
    }

    public void resetPlayerTime()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .resetPlayerTime();
        }
    }

    public void giveExp(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .giveExp(i);
        }
    }

    public float getExp()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .getExp();
        }
        return 0;
    }

    public void setExp(float f)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .setExp(f);
        }
    }

    public int getLevel()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .getLevel();
        }
        return 0;
    }

    public void setLevel(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .setLevel(i);
        }
    }

    public int getTotalExperience()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .getTotalExperience();
        }
        return 0;
    }

    public void setTotalExperience(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .setTotalExperience(i);
        }
    }

    public float getExhaustion()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .getExhaustion();
        }
        return 0;
    }

    public void setExhaustion(float f)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .setExhaustion(f);
        }
    }

    public float getSaturation()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .getSaturation();
        }
        return 0;
    }

    public void setSaturation(float f)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .setSaturation(f);
        }
    }

    public int getFoodLevel()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .getFoodLevel();
        }
        return 0;
    }

    public void setFoodLevel(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .setFoodLevel(i);
        }
    }

    public Location getBedSpawnLocation()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .getBedSpawnLocation();
        }
        return null;
    }

    public void setBedSpawnLocation(Location lctn)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .setBedSpawnLocation(lctn);
        }
    }

    public boolean getAllowFlight()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            return user .getAllowFlight();
        }
        return false;
    }

    public void setAllowFlight(boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user  != null)
        {
            user .setAllowFlight(bln);
        }
    }

    public void hidePlayer(Player player)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.hidePlayer(player);
        }
    }

    public void showPlayer(Player player)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.showPlayer(player);
        }
    }

    public boolean canSee(Player player)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.canSee(player);
        }
        return false;
    }

    public boolean isFlying()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isFlying();
        }
        return false;
    }

    public void setFlying(boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setFlying(bln);
        }
    }

    public String getName()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getName();
        }
        return null;
    }

    public PlayerInventory getInventory()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getInventory();
        }
        return null;
    }

    public boolean setWindowProperty(Property prprt, int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.setWindowProperty(prprt, i);
        }
        return false;
    }

    public InventoryView getOpenInventory()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getOpenInventory();
        }
        return null;
    }

    public InventoryView openInventory(Inventory invntr)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.openInventory(invntr);
        }
        return null;
    }

    public InventoryView openWorkbench(Location lctn, boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.openWorkbench(lctn, bln);
        }
        return null;
    }

    public InventoryView openEnchanting(Location lctn, boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.openEnchanting(lctn, bln);
        }
        return null;
    }

    public void openInventory(InventoryView iv)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.openInventory(iv);
        }
    }

    public void closeInventory()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.closeInventory();
        }
    }

    public ItemStack getItemInHand()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getItemInHand();
        }
        return null;
    }

    public void setItemInHand(ItemStack is)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setItemInHand(is);
        }
    }

    public ItemStack getItemOnCursor()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getItemOnCursor();
        }
        return null;
    }

    public void setItemOnCursor(ItemStack is)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setItemOnCursor(is);
        }
    }

    public boolean isSleeping()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isSleeping();
        }
        return false;
    }

    public int getSleepTicks()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getSleepTicks();
        }
        return 0;
    }

    public GameMode getGameMode()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getGameMode();
        }
        return null;
    }

    public void setGameMode(GameMode gm)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setGameMode(gm);
        }
    }

    public boolean isBlocking()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isBlocking();
        }
        return false;
    }

    public int getHealth()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getHealth();
        }
        return 0;
    }

    public void setHealth(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setHealth(i);
        }
    }

    public int getMaxHealth()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getMaxHealth();
        }
        return 0;
    }

    public double getEyeHeight()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getMaxHealth();
        }
        return 0;
    }

    public double getEyeHeight(boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getEyeHeight();
        }
        return 0;
    }

    public Location getEyeLocation()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getEyeLocation();
        }
        return null;
    }

    public List<Block> getLineOfSight(HashSet<Byte> hs, int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getLineOfSight(hs, i);
        }
        return null;
    }

    public Block getTargetBlock(HashSet<Byte> hs, int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getTargetBlock(hs, i);
        }
        return null;
    }

    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hs, int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getLastTwoTargetBlocks(hs, i);
        }
        return null;
    }

    public Egg throwEgg()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.throwEgg();
        }
        return null;
    }

    public Snowball throwSnowball()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.throwSnowball();
        }
        return null;
    }

    public Arrow shootArrow()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.shootArrow();
        }
        return null;
    }

    public <T extends Projectile> T launchProjectile(Class<? extends T> type)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.launchProjectile(type);
        }
        return null;
    }

    public int getRemainingAir()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getRemainingAir();
        }
        return 0;
    }

    public void setRemainingAir(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setRemainingAir(i);
        }
    }

    public int getMaximumAir()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getMaximumAir();
        }
        return 0;
    }

    public void setMaximumAir(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setMaximumAir(i);
        }
    }

    public void damage(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.damage(i);
        }
    }

    public void damage(int i, Entity entity)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.damage(i, entity);
        }
    }

    public int getMaximumNoDamageTicks()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getMaximumNoDamageTicks();
        }
        return 0;
    }

    public void setMaximumNoDamageTicks(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setMaximumNoDamageTicks(i);
        }
    }

    public int getLastDamage()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getLastDamage();
        }
        return 0;
    }

    public void setLastDamage(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setLastDamage(i);
        }
    }

    public int getNoDamageTicks()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getNoDamageTicks();
        }
        return 0;
    }

    public void setNoDamageTicks(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setNoDamageTicks(i);
        }
    }

    public Player getKiller()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getKiller();
        }
        return null;
    }

    public boolean addPotionEffect(PotionEffect pe)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.addPotionEffect(pe);
        }
        return false;
    }

    public boolean addPotionEffect(PotionEffect pe, boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.addPotionEffect(pe);
        }
        return false;
    }

    public boolean addPotionEffects(Collection<PotionEffect> clctn)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.addPotionEffects(clctn);
        }
        return false;
    }

    public boolean hasPotionEffect(PotionEffectType pet)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.hasPotionEffect(pet);
        }
        return false;
    }

    public void removePotionEffect(PotionEffectType pet)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.removePotionEffect(pet);
        }
    }

    public Collection<PotionEffect> getActivePotionEffects()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getActivePotionEffects();
        }
        return null;
    }

    public Location getLocation()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getLocation();
        }
        return null;
    }

    public void setVelocity(Vector vector)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setVelocity(vector);
        }
    }

    public Vector getVelocity()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getVelocity();
        }
        return null;
    }

    public World getWorld()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getWorld();
        }
        return null;
    }

    public boolean teleport(Location lctn)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.teleport(lctn);
        }
        return false;
    }

    public boolean teleport(Location lctn, TeleportCause tc)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.teleport(lctn, tc);
        }
        return false;
    }

    public boolean teleport(Entity entity)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.teleport(entity);
        }
        return false;
    }

    public boolean teleport(Entity entity, TeleportCause tc)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.teleport(entity, tc);
        }
        return false;
    }

    public List<Entity> getNearbyEntities(double d, double d1, double d2)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getNearbyEntities(d, d1, d2);
        }
        return null;
    }

    public int getEntityId()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getEntityId();
        }
        return -1;
    }

    public int getFireTicks()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getFireTicks();
        }
        return 0;
    }

    public int getMaxFireTicks()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getMaxFireTicks();
        }
        return 0;
    }

    public void setFireTicks(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setFireTicks(i);
        }
    }

    public void remove()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.remove();
        }
    }

    public boolean isDead()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isDead();
        }
        return false;
    }

    public Server getServer()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getServer();
        }
        return null;
    }

    public Entity getPassenger()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getPassenger();
        }
        return null;
    }

    public boolean setPassenger(Entity entity)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.setPassenger(entity);
        }
        return false;
    }

    public boolean isEmpty()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isEmpty();
        }
        return false;
    }

    public boolean eject()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.eject();
        }
        return false;
    }

    public float getFallDistance()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getFallDistance();
        }
        return 0;
    }

    public void setFallDistance(float f)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setFallDistance(f);
        }
    }

    public void setLastDamageCause(EntityDamageEvent ede)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setLastDamageCause(ede);
        }
    }

    public EntityDamageEvent getLastDamageCause()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getLastDamageCause();
        }
        return null;
    }

    public UUID getUniqueId()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getUniqueId();
        }
        return null;
    }

    public int getTicksLived()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getTicksLived();
        }
        return 0;
    }

    public void setTicksLived(int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setTicksLived(i);
        }
    }

    public void playEffect(EntityEffect ee)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.playEffect(ee);
        }
    }

    public EntityType getType()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getType();
        }
        return null;
    }

    public boolean isInsideVehicle()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isInsideVehicle();
        }
        return false;
    }

    public boolean leaveVehicle()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.leaveVehicle();
        }
        return false;
    }

    public Entity getVehicle()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getVehicle();
        }
        return null;
    }

    public void setMetadata(String string, MetadataValue mv)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setMetadata(string, mv);
        }
    }

    public List<MetadataValue> getMetadata(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getMetadata(string);
        }
        return null;
    }

    public boolean hasMetadata(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.hasMetadata(string);
        }
        return false;
    }

    public void removeMetadata(String string, Plugin plugin)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.removeMetadata(string, plugin);
        }
    }

    public boolean isPermissionSet(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isPermissionSet(string);
        }
        return false;
    }

    public boolean isPermissionSet(Permission prmsn)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isPermissionSet(prmsn);
        }
        return false;
    }

    public boolean hasPermission(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.hasPermission(string);
        }
        return false;
    }

    public boolean hasPermission(Permission prmsn)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.hasPermission(prmsn);
        }
        return false;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.addAttachment(plugin, string, bln);
        }
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.addAttachment(plugin);
        }
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.addAttachment(plugin, string, bln, i);
        }
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, int i)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.addAttachment(plugin, i);
        }
        return null;
    }

    public void removeAttachment(PermissionAttachment pa)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.removeAttachment(pa);
        }
    }

    public void recalculatePermissions()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.recalculatePermissions();
        }
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getEffectivePermissions();
        }
        return null;
    }

    public boolean isOp()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isOp();
        }
        return false;
    }

    public void setOp(boolean bln)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.setOp(bln);
        }
    }

    public boolean isConversing()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.isConversing();
        }
        return false;
    }

    public void acceptConversationInput(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.acceptConversationInput(string);
        }
    }

    public boolean beginConversation(Conversation c)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.beginConversation(c);
        }
        return false;
    }

    public void abandonConversation(Conversation c)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.abandonConversation(c);
        }
    }

    public void abandonConversation(Conversation c, ConversationAbandonedEvent cae)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.abandonConversation(c,cae);
        }
    }

    public void sendMessage(String string)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.sendMessage(string);
        }
    }

    public void sendMessage(String[] strings)
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.sendMessage(strings);
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
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            user.sendPluginMessage(plugin, string, bytes);
        }
    }

    public Set<String> getListeningPluginChannels()
    {
        Player user = this.offlinePlayer.getPlayer();
        if (user != null)
        {
            return user.getListeningPluginChannels();
        }
        return null;
    }
    
}
