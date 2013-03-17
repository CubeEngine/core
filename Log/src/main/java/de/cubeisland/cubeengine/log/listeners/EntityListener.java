package de.cubeisland.cubeengine.log.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogManager;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static de.cubeisland.cubeengine.log.storage.LogManager.*;
import static org.bukkit.Material.*;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.PROJECTILE;

public class EntityListener implements Listener
{

    private LogManager manager;
    private Log module;

    public EntityListener(Log module, LogManager manager)
    {
        this.module = module;
        this.manager = manager;
    }

    private HashMap<Location,Long> plannedBreakHanging = new HashMap<Location, Long>();

    public void preplanBreakedHanging(Location location, Long causer)
    {
        this.plannedBreakHanging.put(location, causer);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event)
    {
        if (event.getCause().equals(HangingBreakEvent.RemoveCause.PHYSICS))
        {
            if (this.manager.isIgnored(HANGING_BREAK)) return;
            Location location = event.getEntity().getLocation();
            Long causer = this.plannedBreakHanging.get(location);
            if (causer != null)
            {
                if (event.getEntity() instanceof ItemFrame)
                {
                    try
                    {
                        ItemStack itemStack = ((ItemFrame) event.getEntity()).getItem();
                        String itemInFrame = itemStack == null ? null : CubeEngine.getCore().getJsonObjectMapper().writeValueAsString(itemStack);
                        this.manager.queueLog(location, HANGING_BREAK, causer,
                                ITEM_FRAME.name(), (byte)0,
                                AIR.name(),(byte) 0, itemInFrame);
                    }
                    catch (JsonProcessingException e) {
                        throw new IllegalStateException("Could not parse Bukkit-ItemStack!",e);
                    }
                }
                else if (event.getEntity() instanceof Painting)
                {
                    this.manager.queueLog(location, HANGING_BREAK, causer,
                            PAINTING.name(), (byte)((Painting)event.getEntity()).getArt().getId(),
                            AIR.name(),(byte) 0, null);
                }
            }
            System.out.print("Unexpected HangingBreak event");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event)
    {
        if (this.manager.isIgnored(HANGING_BREAK)) return;
        Location location = event.getEntity().getLocation();
        if (event.getRemover() instanceof Player || event.getRemover() instanceof Arrow)
        {
            long causer;
            if (event.getRemover() instanceof Arrow)
            {
                Arrow arrow = (Arrow) event.getRemover();
                if (arrow.getShooter() instanceof Player)
                {
                    causer = this.module.getUserManager().getExactUser((Player) arrow.getShooter()).key;
                }
                else if (arrow.getShooter() instanceof Skeleton)
                {
                    causer = -arrow.getShooter().getType().getTypeId();
                }
                else
                {
                    //TODO ghast
                    //TODO wither
                    //TODO snowgolem
                    System.out.print("Unkown Shooter"); //TODO dispenser ???
                    return; // unknown shooter
                }
            }
            else
            {
                causer = this.module.getUserManager().getExactUser((Player) event.getRemover()).key;
            }
            if (event.getEntity() instanceof ItemFrame)
            {
                try
                {
                    ItemStack itemStack = ((ItemFrame) event.getEntity()).getItem();
                    String itemInFrame = itemStack == null ? null : CubeEngine.getCore().getJsonObjectMapper().writeValueAsString(itemStack);
                    this.manager.queueLog(location, HANGING_BREAK, causer,
                            ITEM_FRAME.name(), (byte)0,
                            AIR.name(),(byte) 0, itemInFrame);
                }
                catch (JsonProcessingException e) {
                    throw new IllegalStateException("Could not parse Bukkit-ItemStack!",e);
                }
            }
            else if (event.getEntity() instanceof Painting)
            {
                this.manager.queueLog(location, HANGING_BREAK, causer,
                        PAINTING.name(), (byte)((Painting)event.getEntity()).getArt().getId(),
                        AIR.name(),(byte) 0, null);
            }
        }
        else
            System.out.print("Not a player breaking Hanging Arrow?");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event)
    {
        if (this.manager.isIgnored(LogManager.HANGING_PLACE)) return;
        User user = this.module.getUserManager().getExactUser(event.getPlayer());
        if (event.getEntity() instanceof ItemFrame)
        {
            this.manager.queueLog(event.getEntity().getLocation(), HANGING_PLACE, user.key,
                    ITEM_FRAME.name(), (byte)0,
                    AIR.name(),(byte) 0, null);
        }
        else if (event.getEntity() instanceof Painting)
        {
            this.manager.queueLog(event.getEntity().getLocation(), HANGING_PLACE, user.key,
                    PAINTING.name(), (byte)((Painting)event.getEntity()).getArt().getId(),
                    AIR.name(),(byte) 0, null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event)
    {
        //TODO itemdrops
        LivingEntity entity = event.getEntity();
        int action;
        long killed;
        if (entity instanceof Player)
        {
            if (this.manager.isIgnored(PLAYER_DEATH)) return;
            action = PLAYER_DEATH;
            killed = this.module.getUserManager().getExactUser((Player)entity).key;
        }
        else
        {
            if (this.manager.isIgnored(ENTITY_DEATH)) return;
            action = ENTITY_DEATH;
            killed = -entity.getType().getTypeId();
        }
        EntityDamageEvent dmgEvent = entity.getLastDamageCause();
        if (dmgEvent == null)
        {
            return; // squids dying in air, lazy bukkit :S -> https://bukkit.atlassian.net/browse/BUKKIT-3684
        }
        String additionalData = this.serializeKillData(dmgEvent.getCause(),entity);
        long causer;
        if (dmgEvent instanceof EntityDamageByEntityEvent)
        {
            Entity damager = ((EntityDamageByEntityEvent)dmgEvent).getDamager();
            if (dmgEvent.getCause().equals(PROJECTILE) && damager instanceof Projectile)
            {
                LivingEntity shooter = ((Projectile) damager).getShooter();
                if (shooter instanceof Player)
                {
                    if (this.manager.isIgnored(PLAYER_KILL)) return;
                    causer = this.module.getUserManager().getExactUser((Player) shooter).key;
                }
                else if (shooter instanceof Skeleton || shooter instanceof Ghast || shooter instanceof Wither)
                {
                    if (this.manager.isIgnored(ENTITY_KILL)) return;
                    causer = -shooter.getType().getTypeId();
                }
                else // Projectile shot by Dispenser
                {
                    System.out.print("Unkown Shooter: "+ ((Projectile) damager).getShooter());
                    return;
                }
            }
            else if (damager instanceof Player)
            {
                if (this.manager.isIgnored(PLAYER_KILL)) return;
                causer = this.module.getUserManager().getExactUser((Player) damager).key;
            }
            else
            {
                if (this.manager.isIgnored(ENTITY_KILL)) return;
                causer = -damager.getType().getTypeId();
            }
        }
        else
        {
            if (this.manager.isIgnored(ENVIRONEMENT_KILL)) return;
            causer = 0;
        }
        this.manager.queueLog(event.getEntity().getLocation(),action,causer,killed,additionalData);
    }

    private String serializeKillData(EntityDamageEvent.DamageCause cause, LivingEntity entity)
    {
        if (entity instanceof Player)
        {
            return cause.name(); // only save cause
        }
        Map<String,Object> dataMap = new HashMap<String, Object>();
        dataMap.put("DamageCause",cause.name());
        if (entity instanceof Ageable)
        {
            dataMap.put("isAdult",((Ageable) entity).isAdult());
        }
        if (entity instanceof Ocelot)
        {
            dataMap.put("isSitting",((Ocelot) entity).isSitting());
        }
        if (entity instanceof Wolf)
        {
            dataMap.put("isSitting",((Wolf) entity).isSitting());
            dataMap.put("color",((Wolf) entity).getCollarColor().name());
        }
        if (entity instanceof Sheep)
        {
            dataMap.put("color",((Sheep) entity).getColor().name());
        }
        if (entity instanceof Villager)
        {
            dataMap.put("profession",((Villager) entity).getProfession().name());
        }
        if (entity instanceof Tameable && ((Tameable) entity).isTamed())
        {
            dataMap.put("owner",((Tameable) entity).getOwner().getName());
        }
        try
        {
            return CubeEngine.getCore().getJsonObjectMapper().writeValueAsString(dataMap);
        }
        catch (JsonProcessingException e)
        {
            throw new IllegalStateException("Could not parse KillData!",e);
        }
    }

    //TODO vehicle events
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleCreate(final VehicleCreateEvent event)
    {
        if (this.manager.isIgnored(VEHICLE_PLACE)) return;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent event)
    {
        if (this.manager.isIgnored(VEHICLE_BREAK)) return;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event)
    {
        if (this.manager.isIgnored(VEHICLE_ENTER)) return;
        if (event.getEntered() instanceof Player)
        {
            this.manager.queueLog(event.getVehicle().getLocation(),VEHICLE_ENTER, (Player)event.getEntered(),null);
        }
        else
        {
            this.manager.queueLog(event.getVehicle().getLocation(),VEHICLE_ENTER, -event.getEntered().getType().getTypeId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(final VehicleExitEvent event)
    {
        if (this.manager.isIgnored(VEHICLE_EXIT)) return;
        if (event.getExited() instanceof Player)
        {
            this.manager.queueLog(event.getVehicle().getLocation(),VEHICLE_ENTER, (Player)event.getExited(),null);
        }
        else
        {
            this.manager.queueLog(event.getVehicle().getLocation(),VEHICLE_ENTER, -event.getExited().getType().getTypeId());
        }
    }

    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        //TODO
    }

    public void onEntityShear(PlayerShearEntityEvent event)
    {
        //TODO
    }

    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event)
    {
        //TODO
    }

    public void onPotionSplash(PotionSplashEvent event)
    {
        //TODO
    }

    public void onPlayerJoin(PlayerJoinEvent event)
    {
        //TODO
    }

    public void onPlayerQuit(PlayerQuitEvent event)
    {
        //TODO
    }

    public void onItemDrop(PlayerDropItemEvent event)
    {
        //TODO
    }

    public void onItemPickup(PlayerPickupItemEvent  event)
    {
        //TODO
    }

    public void onExpPickup(PlayerExpChangeEvent   event)
    {
        //TODO
    }

    public void onTeleport(PlayerTeleportEvent    event)
    {
        //TODO
    }

    public void onEnchant(EnchantItemEvent event)
    {
        //TODO
    }

    public void onCraft(CraftItemEvent event)
    {
        //TODO
    }
}
