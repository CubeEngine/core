package de.cubeisland.cubeengine.log.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.potion.PotionEffect;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.ActionType;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.LogManager;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.cubeengine.log.storage.ActionType.*;
import static org.bukkit.Material.*;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.PROJECTILE;

public class EntityListener implements Listener
{

    private LogManager manager;
    private Log module;
    private final UserManager um;

    public EntityListener(Log module, LogManager manager)
    {
        this.module = module;
        this.manager = manager;
        this.um = this.module.getCore().getUserManager();
    }

    private HashMap<Location,Long> plannedBreakHanging = new HashMap<Location, Long>();
    private HashMap<Location,Player> plannedVehiclePlace = new HashMap<Location, Player>();

    public void preplanBreakedHanging(Location location, Long causer)
    {
        this.plannedBreakHanging.put(location, causer);
    }

    public void preplanVehiclePlacement(Location location, Player player)
    {
        this.plannedVehiclePlace.put(location,player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event)
    {
        if (event.getCause().equals(HangingBreakEvent.RemoveCause.PHYSICS))
        {
            if (this.manager.isIgnored(event.getEntity().getWorld(),HANGING_BREAK)) return;
            Location location = event.getEntity().getLocation();
            Long causer = this.plannedBreakHanging.get(location);
            if (causer != null)
            {
                if (event.getEntity() instanceof ItemFrame)
                {
                    ItemStack itemStack = ((ItemFrame) event.getEntity()).getItem();
                    String itemInFrame = this.manager.getContainerListener().serializeItemData(new ItemData(itemStack));
                    this.manager.queueBlockChangeLog(location, HANGING_BREAK, causer,
                          ITEM_FRAME.name(), (byte)0, AIR.name(), (byte)0, itemInFrame);
                }
                else if (event.getEntity() instanceof Painting)
                {
                    this.manager.queueBlockChangeLog(location, HANGING_BREAK, causer,
                          PAINTING.name(), (byte)((Painting)event.getEntity()).getArt().getId(), AIR.name(), (byte)0, null);
                }
            }
            System.out.print("Unexpected HangingBreak event");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event)
    {
        if (this.manager.isIgnored(event.getEntity().getWorld(),HANGING_BREAK)) return;
        Location location = event.getEntity().getLocation();
        if (event.getRemover() instanceof Player || event.getRemover() instanceof Arrow)
        {
            Long causer;
            if (event.getRemover() instanceof Projectile)
            {
                Projectile projectile = (Projectile) event.getRemover();
                if (projectile.getShooter() instanceof Player)
                {
                    causer = um.getExactUser((Player) projectile.getShooter()).key;
                }
                else if (projectile.getShooter() != null)
                {
                    causer = -1L * projectile.getShooter().getType().getTypeId();
                }
                else
                {
                    causer = null;
                }
            }
            else
            {
                causer = um.getExactUser((Player) event.getRemover()).key;
            }
            if (event.getEntity() instanceof ItemFrame)
            {
                ItemStack itemStack = ((ItemFrame) event.getEntity()).getItem();
                String itemInFrame = this.manager.getContainerListener().serializeItemData(new ItemData(itemStack));
                this.manager.queueBlockChangeLog(location, HANGING_BREAK, causer, ITEM_FRAME.name(), (byte)0, AIR
                    .name(), (byte)0, itemInFrame);
            }
            else if (event.getEntity() instanceof Painting)
            {
                this.manager.queueBlockChangeLog(location, HANGING_BREAK, causer, PAINTING
                    .name(), (byte)((Painting)event.getEntity()).getArt().getId(), AIR.name(), (byte)0, null);
            }
        }
        else
            System.out.print("Not a player breaking Hanging?");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event)
    {
        if (this.manager.isIgnored(event.getEntity().getWorld(),HANGING_PLACE)) return;
        User user = um.getExactUser(event.getPlayer());
        if (event.getEntity() instanceof ItemFrame)
        {
            this.manager.queueBlockChangeLog(event.getEntity().getLocation(), HANGING_PLACE, user.key, AIR
                .name(), (byte)0, ITEM_FRAME.name(), (byte)0, null);
        }
        else if (event.getEntity() instanceof Painting)
        {
            this.manager.queueBlockChangeLog(event.getEntity().getLocation(), HANGING_PLACE, user.key, AIR
                .name(), (byte)0, PAINTING.name(), (byte)((Painting)event.getEntity()).getArt().getId(), null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event)
    {
        Location location = event.getEntity().getLocation();
        if (!this.manager.isIgnored(event.getEntity().getWorld(),ITEM_DROP))
        {
            for (ItemStack itemStack : event.getDrops())
            {
                String itemData = this.manager.getContainerListener().serializeItemData(new ItemData(itemStack));
                long causer;
                if (event.getEntity() instanceof Player)
                {
                    causer = um.getExactUser((Player)event.getEntity()).key;
                }
                else
                {
                    causer = -1L * event.getEntity().getType().getTypeId();
                }
                this.manager.queueKillLog(location, ITEM_DROP, causer, null, itemData);
            }
        }
        LivingEntity entity = event.getEntity();
        ActionType action;
        long killed;
        if (entity instanceof Player)
        {
            if (this.manager.isIgnored(entity.getWorld(),PLAYER_DEATH)) return;
            action = PLAYER_DEATH;
            killed = um.getExactUser((Player)entity).key;
        }
        else if (entity instanceof Wither || entity instanceof EnderDragon)
        {
            if (this.manager.isIgnored(entity.getWorld(),BOSS_DEATH)) return;
            action = BOSS_DEATH;
            killed = -entity.getType().getTypeId();
        }
        else if (entity instanceof Animals)
        {
            if (entity instanceof Tameable && ((Tameable) entity).isTamed())
            {
                if (this.manager.isIgnored(entity.getWorld(),PET_DEATH)) return;
                action = PET_DEATH;
                killed = -entity.getType().getTypeId();
            }
            else
            {
                if (this.manager.isIgnored(entity.getWorld(),ANIMAL_DEATH)) return;
                action = ANIMAL_DEATH;
                killed = -entity.getType().getTypeId();
            }
        }
        else if (entity instanceof Villager)
        {
            if (this.manager.isIgnored(entity.getWorld(),NPC_DEATH)) return;
            action = NPC_DEATH;
            killed = -entity.getType().getTypeId();
        }
        else if (entity instanceof Monster)
        {
            if (this.manager.isIgnored(entity.getWorld(),MONSTER_DEATH)) return;
            action = MONSTER_DEATH;
            killed = -entity.getType().getTypeId();
        }
        else
        {
            if (this.manager.isIgnored(entity.getWorld(),OTHER_DEATH)) return;
            action = OTHER_DEATH;
            killed = -entity.getType().getTypeId();
        }

        EntityDamageEvent dmgEvent = entity.getLastDamageCause();
        if (dmgEvent == null)
        {
            return; // squids dying in air, lazy bukkit :S -> https://bukkit.atlassian.net/browse/BUKKIT-3684
        }
        String additionalData = this.serializeData(dmgEvent.getCause(), entity, null);
        long causer;
        if (dmgEvent instanceof EntityDamageByEntityEvent)
        {
            Entity damager = ((EntityDamageByEntityEvent)dmgEvent).getDamager();
            if (dmgEvent.getCause().equals(PROJECTILE) && damager instanceof Projectile)
            {
                LivingEntity shooter = ((Projectile) damager).getShooter();
                if (shooter instanceof Player)
                {
                    if (this.manager.isIgnored(entity.getWorld(),PLAYER_KILL)) return;
                    causer = um.getExactUser((Player) shooter).key;
                }
                else if (shooter instanceof Skeleton || shooter instanceof Ghast)
                {
                    if (this.manager.isIgnored(entity.getWorld(),ENTITY_KILL)) return;
                    causer = -shooter.getType().getTypeId();
                }
                else if (shooter instanceof Wither)
                {
                    if (this.manager.isIgnored(entity.getWorld(),BOSS_KILL)) return;
                    causer = -shooter.getType().getTypeId();
                }
                else // Projectile shot by Dispenser
                {
                    System.out.print("Unknown Shooter: "+ ((Projectile) damager).getShooter());
                    return;
                }
            }
            else if (damager instanceof Player)
            {
                if (this.manager.isIgnored(entity.getWorld(),PLAYER_KILL)) return;
                causer = um.getExactUser((Player) damager).key;
            }
            else if (damager instanceof Wither || damager instanceof Wither)
            {
                if (this.manager.isIgnored(entity.getWorld(),BOSS_KILL)) return;
                causer = -damager.getType().getTypeId();
            }
            else
            {
                if (this.manager.isIgnored(entity.getWorld(),ENTITY_KILL)) return;
                causer = -damager.getType().getTypeId();
            }
        }
        else
        {
            if (this.manager.isIgnored(entity.getWorld(),ENVIRONMENT_KILL)) return;
            causer = 0;
        }
        this.manager.queueKillLog(location, action, causer, killed, additionalData);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleCreate(final VehicleCreateEvent event)
    {
        if (this.manager.isIgnored(event.getVehicle().getWorld(), VEHICLE_PLACE)) return;
        Location location = event.getVehicle().getLocation();
        Player player = this.plannedVehiclePlace.get(location);
        long vehicle = event.getVehicle().getType().getTypeId();
        if (player != null)
        {
            long userkey = um.getExactUser(player).key;
            this.manager.queueKillLog(location, VEHICLE_PLACE, userkey, vehicle, null);
        }
        else
            System.out.print("Unexpected VehiclePlacement: "+event.getVehicle());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent event)
    {
        if (this.manager.isIgnored(event.getVehicle().getWorld(),VEHICLE_BREAK)) return;
        Long causer = null;

        if (event.getAttacker() != null)
        {
            if (event.getAttacker() instanceof Player)
            {
                causer = um.getExactUser((Player) event.getAttacker()).key;
            }
            else if (event.getAttacker() instanceof Projectile)
            {
                Projectile projectile = (Projectile) event.getAttacker();
                if (projectile.getShooter() instanceof Player)
                {
                    causer = um.getExactUser((Player) event.getAttacker()).key;
                }
                else if (projectile.getShooter() != null)
                {
                    causer = -1L * projectile.getShooter().getType().getTypeId();
                }
            }
        }
        else if (event.getVehicle().getPassenger() instanceof Player)
        {
            causer = um.getExactUser((Player) event.getVehicle().getPassenger()).key;
        }
        long vehicleType = -event.getVehicle().getType().getTypeId();
        this.manager.queueKillLog(event.getVehicle().getLocation(), VEHICLE_BREAK, causer, vehicleType, null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event)
    {
        if (this.manager.isIgnored(event.getVehicle().getWorld(),VEHICLE_ENTER)) return;
        long vehicle = event.getVehicle().getType().getTypeId();
        if (event.getEntered() instanceof Player)
        {
            long userKey = um.getExactUser((Player)event.getEntered()).key;
            this.manager.queueKillLog(event.getVehicle().getLocation(),VEHICLE_ENTER,userKey,vehicle,null);
        }
        else
        {
            long entity = -event.getEntered().getType().getTypeId();
            this.manager.queueKillLog(event.getVehicle().getLocation(),VEHICLE_ENTER,entity,vehicle,null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(final VehicleExitEvent event)
    {
        if (this.manager.isIgnored(event.getVehicle().getWorld(),VEHICLE_EXIT)) return;
        long vehicle = event.getVehicle().getType().getTypeId();
        if (event.getExited() instanceof Player)
        {
            long userKey = um.getExactUser((Player)event.getExited()).key;
            this.manager.queueKillLog(event.getVehicle().getLocation(),VEHICLE_EXIT,userKey,vehicle,null);
        }
        else
        {
            long entity = -event.getExited().getType().getTypeId();
            this.manager.queueKillLog(event.getVehicle().getLocation(),VEHICLE_EXIT,entity,vehicle,null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        World world = event.getEntity().getWorld();
        switch (event.getSpawnReason())
        {
            case NATURAL:
            case JOCKEY:
            case CHUNK_GEN:
            case VILLAGE_DEFENSE:
            case VILLAGE_INVASION:
                if (this.manager.isIgnored(world, NATURAL_SPAWN)) return;
                this.manager.queueLog(event.getLocation(), NATURAL_SPAWN,-event.getEntityType().getTypeId());
                return;
            case SPAWNER:
                if (this.manager.isIgnored(world, SPAWNER_SPAWN)) return;
                this.manager.queueLog(event.getLocation(), SPAWNER_SPAWN,-event.getEntityType().getTypeId());
                return;
            case EGG:
            case BUILD_SNOWMAN:
            case BUILD_IRONGOLEM:
            case BUILD_WITHER:
            case BREEDING:
                if (this.manager.isIgnored(world, OTHER_SPAWN)) return;
                this.manager.queueLog(event.getLocation(), OTHER_SPAWN,-event.getEntityType().getTypeId());
                return;
            //case SPAWNER_EGG: //is already done
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShear(PlayerShearEntityEvent event)
    {
        if (this.manager.isIgnored(event.getEntity().getWorld(),ENTITY_SHEAR)) return;
        long spawnedEntity = -event.getEntity().getType().getTypeId();
        if (event.getEntity() instanceof LivingEntity)
        {
            this.manager.queueKillLog(event.getEntity().getLocation(), ENTITY_SHEAR, spawnedEntity, null, this
                .serializeData(null, (LivingEntity)event.getEntity(), null));
        }
        else
            System.out.print("Sheared something: "+event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event)
    {
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        Player player = event.getPlayer();
        LivingEntity entity = (LivingEntity) event.getRightClicked();
        if (player.getItemInHand().getType().equals(COAL) && entity instanceof PoweredMinecart)
        {
            if (this.manager.isIgnored(player.getWorld(),ITEM_INSERT)) return;
            this.manager.queueInteractionLog(entity.getLocation(), ITEM_INSERT, player, null);
        }
        else if(player.getItemInHand().getType().equals(INK_SACK) && entity instanceof Sheep || entity instanceof Wolf)
        {
            if (this.manager.isIgnored(player.getWorld(),ENTITY_DYE)) return;
            Dye dye = (Dye) player.getItemInHand().getData();
            this.manager.queueInteractionLog(entity.getLocation(), ENTITY_DYE, player, this
                .serializeData(null, entity, dye.getColor()));
        }
        else if (player.getItemInHand().getType().equals(BOWL) && entity instanceof MushroomCow)
        {
            if (this.manager.isIgnored(player.getWorld(),SOUP_FILL)) return;
            this.manager.queueInteractionLog(entity.getLocation(), SOUP_FILL, player, null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event)
    {
        if (this.manager.isIgnored(event.getPotion().getWorld(),POTION_SPLASH)) return;
        LivingEntity livingEntity = event.getPotion().getShooter();
        Long causer = null;
        if (livingEntity instanceof Player)
        {
            causer = um.getExactUser((Player)livingEntity).key;
        }
        else if (livingEntity != null)
        {
            causer = -1L * livingEntity.getType().getTypeId();
        }
        String additionalData = this.serializePotionLog(event);
        this.manager.queueKillLog(event.getPotion().getLocation(), POTION_SPLASH, causer, null, additionalData);
    }

    public String serializePotionLog(PotionSplashEvent event)
    {
        ObjectNode json = this.manager.mapper.createObjectNode();
        ArrayNode effects = json.putArray("effects");
        for (PotionEffect potionEffect : event.getPotion().getEffects())
        {
            ArrayNode effect = effects.addArray();
            effects.add(effect);
            effect.add(potionEffect.getType().getName());
            effect.add(potionEffect.getAmplifier());
            effect.add(potionEffect.getDuration());
        }
        if (!event.getAffectedEntities().isEmpty())
        {
            json.put("amount", event.getAffectedEntities().size());
            ArrayNode affected = json.putArray("affected");
            for (LivingEntity livingEntity : event.getAffectedEntities())
            {
                if (livingEntity instanceof Player)
                {
                    User user = um.getExactUser((Player)livingEntity);
                    affected.add(user.key);
                }
                else
                {
                    affected.add(-livingEntity.getType().getTypeId());
                }
            }
        }
        return json.toString();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (this.manager.isIgnored(event.getPlayer().getWorld(),PLAYER_JOIN)) return;
        String data = null;
        if (this.manager.getConfig(event.getPlayer().getWorld()).PLAYER_JOIN_ip)
        {
            data = event.getPlayer().getAddress().getAddress().getHostName();
        }
        this.manager.queueInteractionLog(event.getPlayer().getLocation(), PLAYER_JOIN, event.getPlayer(), data);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (this.manager.isIgnored(event.getPlayer().getWorld(),PLAYER_QUIT)) return;
        this.manager.queueInteractionLog(event.getPlayer().getLocation(), PLAYER_QUIT, event.getPlayer(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event)
    {
        if (this.manager.isIgnored(event.getPlayer().getWorld(),ITEM_DROP)) return;
        String itemData = this.manager.getContainerListener().serializeItemData(new ItemData(event.getItemDrop()
                                                                                                  .getItemStack()));
        this.manager.queueInteractionLog(event.getPlayer().getLocation(), ITEM_DROP, event.getPlayer(), itemData);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent  event)
    {
        if (this.manager.isIgnored(event.getPlayer().getWorld(),ITEM_PICKUP)) return;
        String itemData = this.manager.getContainerListener().serializeItemData(new ItemData(event.getItem()
                                                                                                  .getItemStack()));
        this.manager.queueInteractionLog(event.getPlayer().getLocation(), ITEM_PICKUP, event.getPlayer(), itemData);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExpPickup(PlayerExpChangeEvent event)
    {
        if (this.manager.isIgnored(event.getPlayer().getWorld(),XP_PICKUP)) return;
        this.manager.queueInteractionLog(event.getPlayer().getLocation(), XP_PICKUP, event.getPlayer(), String
            .valueOf(event.getAmount()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event)
    {
        if (this.manager.isIgnored(event.getPlayer().getWorld(),PLAYER_TELEPORT)) return;
        if (event.getFrom().equals(event.getTo())) return;
        String targetLocation = this.serializeLocation(false,event.getTo());
        String sourceLocation = this.serializeLocation(true, event.getFrom());
        this.manager.queueInteractionLog(event.getFrom(), PLAYER_TELEPORT, event.getPlayer(), targetLocation);
        this.manager.queueInteractionLog(event.getTo(), PLAYER_TELEPORT, event.getPlayer(), sourceLocation);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event)
    {
        if (this.manager.isIgnored(event.getEnchanter().getWorld(),ENCHANT_ITEM)) return;
        String applied = this.serializeEnchantments(event.getEnchantsToAdd());
        User user = um.getExactUser(event.getEnchanter());
        this.manager.queueItemLog(event.getEnchanter().getLocation(), ENCHANT_ITEM, user.key, event.getItem().getType()
                                                                                                   .name(), event
                                      .getItem().getDurability(), applied);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event)
    {
        if (this.manager.isIgnored(event.getWhoClicked().getWorld(),CRAFT_ITEM)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        User user = um.getExactUser((Player) event.getWhoClicked());
        this.manager.queueItemLog(event.getWhoClicked().getLocation(), CRAFT_ITEM, user.key, event.getRecipe()
                                                                                                  .getResult().getType()
                                                                                                  .name(), event
                                      .getRecipe().getResult().getDurability(), null);
    }

    private String serializeData(EntityDamageEvent.DamageCause cause, LivingEntity entity, DyeColor newColor)
    {
        if (entity instanceof Player)
        {
            if (cause == null)
            {
                return null;
            }
            return cause.name(); // only save cause
        }
        ObjectNode json = this.manager.mapper.createObjectNode();
        if (cause != null)
        {
            json.put("dmgC", cause.name());
        }
        if (entity instanceof Ageable)
        {
            json.put("isAdult", ((Ageable)entity).isAdult() ? 1 : 0);
        }
        if (entity instanceof Ocelot)
        {
            json.put("isSit", ((Ocelot)entity).isSitting() ? 1 : 0);
        }
        if (entity instanceof Wolf)
        {
            json.put("isSit", ((Wolf)entity).isSitting() ? 1 : 0);
            json.put("color", ((Wolf)entity).getCollarColor().name());
        }
        if (entity instanceof Sheep)
        {
            json.put("color", ((Sheep)entity).getColor().name());
        }
        if (entity instanceof Villager)
        {
            json.put("prof", ((Villager)entity).getProfession().name());
        }
        if (entity instanceof Tameable && ((Tameable) entity).isTamed())
        {
            json.put("owner", ((Tameable)entity).getOwner().getName());
        }
        if (newColor != null)
        {
            json.put("nColor", newColor.name());
        }
        return json.toString();
    }


    private String serializeLocation(boolean from, Location location)
    {
        ObjectNode json = this.manager.mapper.createObjectNode();
        json.put("dir", from ? "from" : "to");
        json.put("world",this.module.getCore().getWorldManager().getWorldId(location.getWorld()));
        json.put("x",location.getBlockX());
        json.put("y",location.getBlockY());
        json.put("z",location.getBlockZ());
        return json.toString();
    }

    private String serializeEnchantments(Map<Enchantment,Integer> enchantsToAdd)
    {
        ObjectNode enchs = this.manager.mapper.createObjectNode();
        for (Entry<Enchantment,Integer> ench : enchantsToAdd.entrySet())
        {
            enchs.put(String.valueOf(ench.getKey().getId()),ench.getValue());
        }
        return enchs.toString();
    }
}
