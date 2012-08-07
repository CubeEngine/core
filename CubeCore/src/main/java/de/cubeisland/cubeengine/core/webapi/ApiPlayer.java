package de.cubeisland.cubeengine.core.webapi;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 *
 * @author Phillip Schichtel
 */
public class ApiPlayer extends ApiCommandSender implements Player
{
    private String displayName;
    private String listName;

    public ApiPlayer(final String name, final Server server)
    {
        super(name, server);
        this.displayName = name;
        this.listName = name;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getPlayerListName()
    {
        return this.listName;
    }

    public void setPlayerListName(String listName)
    {
        this.listName = listName;
    }

    public Player getPlayer()
    {
        return this;
    }

    public Map<String, Object> serialize()
    {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("name", this.getName());

        return result;
    }

    @Override
    public String toString()
    {
        return "ApiPlayer{" + "name=" + this.getName() + '}';
    }

    public void setCompassTarget(Location lctn)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Location getCompassTarget()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InetSocketAddress getAddress()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendRawMessage(String string)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void kickPlayer(String string)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void chat(String string)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean performCommand(String string)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSneaking()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSneaking(boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSprinting()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSprinting(boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveData()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void loadData()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSleepingIgnored(boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSleepingIgnored()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void playNote(Location lctn, byte b, byte b1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void playNote(Location lctn, Instrument i, Note note)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void playEffect(Location lctn, Effect effect, int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T> void playEffect(Location lctn, Effect effect, T t)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendBlockChange(Location lctn, Material mtrl, byte b)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean sendChunkChange(Location lctn, int i, int i1, int i2, byte[] bytes)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendBlockChange(Location lctn, int i, byte b)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendMap(MapView mv)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    public void updateInventory()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void awardAchievement(Achievement a)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void incrementStatistic(Statistic ststc)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void incrementStatistic(Statistic ststc, int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void incrementStatistic(Statistic ststc, Material mtrl)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void incrementStatistic(Statistic ststc, Material mtrl, int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPlayerTime(long l, boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getPlayerTime()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getPlayerTimeOffset()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isPlayerTimeRelative()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resetPlayerTime()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void giveExp(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getExp()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setExp(float f)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLevel()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLevel(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getTotalExperience()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTotalExperience(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getExhaustion()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setExhaustion(float f)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getSaturation()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSaturation(float f)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getFoodLevel()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFoodLevel(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Location getBedSpawnLocation()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setBedSpawnLocation(Location lctn)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean getAllowFlight()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAllowFlight(boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void hidePlayer(Player player)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void showPlayer(Player player)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean canSee(Player player)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PlayerInventory getInventory()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean setWindowProperty(Property prprt, int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InventoryView getOpenInventory()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InventoryView openInventory(Inventory invntr)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InventoryView openWorkbench(Location lctn, boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InventoryView openEnchanting(Location lctn, boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void openInventory(InventoryView iv)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void closeInventory()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ItemStack getItemInHand()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setItemInHand(ItemStack is)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ItemStack getItemOnCursor()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setItemOnCursor(ItemStack is)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSleeping()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getSleepTicks()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public GameMode getGameMode()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setGameMode(GameMode gm)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getHealth()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setHealth(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaxHealth()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getEyeHeight()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getEyeHeight(boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Location getEyeLocation()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Block> getLineOfSight(HashSet<Byte> hs, int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Block getTargetBlock(HashSet<Byte> hs, int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hs, int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    public Egg throwEgg()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    public Snowball throwSnowball()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    public Arrow shootArrow()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends Projectile> T launchProjectile(Class<? extends T> type)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getRemainingAir()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRemainingAir(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaximumAir()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMaximumAir(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void damage(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void damage(int i, Entity entity)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaximumNoDamageTicks()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMaximumNoDamageTicks(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLastDamage()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLastDamage(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getNoDamageTicks()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNoDamageTicks(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Player getKiller()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addPotionEffect(PotionEffect pe)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addPotionEffect(PotionEffect pe, boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addPotionEffects(Collection<PotionEffect> clctn)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasPotionEffect(PotionEffectType pet)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePotionEffect(PotionEffectType pet)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<PotionEffect> getActivePotionEffects()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Location getLocation()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setVelocity(Vector vector)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector getVelocity()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public World getWorld()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean teleport(Location lctn)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean teleport(Location lctn, TeleportCause tc)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean teleport(Entity entity)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean teleport(Entity entity, TeleportCause tc)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Entity> getNearbyEntities(double d, double d1, double d2)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getEntityId()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getFireTicks()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaxFireTicks()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFireTicks(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remove()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isDead()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Entity getPassenger()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean setPassenger(Entity entity)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEmpty()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean eject()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getFallDistance()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFallDistance(float f)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLastDamageCause(EntityDamageEvent ede)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EntityDamageEvent getLastDamageCause()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UUID getUniqueId()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getTicksLived()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTicksLived(int i)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void playEffect(EntityEffect ee)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EntityType getType()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isInsideVehicle()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean leaveVehicle()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Entity getVehicle()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMetadata(String string, MetadataValue mv)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<MetadataValue> getMetadata(String string)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasMetadata(String string)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeMetadata(String string, Plugin plugin)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isConversing()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void acceptConversationInput(String string)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean beginConversation(Conversation c)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void abandonConversation(Conversation c)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isOnline()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isBanned()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setBanned(boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isWhitelisted()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWhitelisted(boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getFirstPlayed()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getLastPlayed()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasPlayedBefore()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendPluginMessage(Plugin plugin, String string, byte[] bytes)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<String> getListeningPluginChannels()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isBlocking()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isFlying()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFlying(boolean bln)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getExpToLevel()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasLineOfSight(Entity entity)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isValid()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}