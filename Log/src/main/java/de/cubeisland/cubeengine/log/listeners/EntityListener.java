package de.cubeisland.cubeengine.log.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogManager;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.PoweredMinecart;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.potion.PotionEffect;

import java.util.*;

import static de.cubeisland.cubeengine.log.storage.LogManager.*;
import static org.bukkit.Material.*;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.PROJECTILE;

public class EntityListener implements Listener
{
    private ObjectMapper mapper;
    private LogManager manager;
    private Log module;

    public EntityListener(Log module, LogManager manager)
    {
        this.module = module;
        this.manager = manager;
        this.mapper = new ObjectMapper();
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
            if (this.manager.isIgnored(HANGING_BREAK)) return;
            Location location = event.getEntity().getLocation();
            Long causer = this.plannedBreakHanging.get(location);
            if (causer != null)
            {
                if (event.getEntity() instanceof ItemFrame)
                {
                    ItemStack itemStack = ((ItemFrame) event.getEntity()).getItem();
                    String itemInFrame = this.serializeItemStack(itemStack);
                    this.manager.queueLog(location, HANGING_BREAK, causer,
                            ITEM_FRAME.name(), (byte)0,
                            AIR.name(),(byte) 0, itemInFrame);
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
            Long causer;
            if (event.getRemover() instanceof Projectile)
            {
                Projectile projectile = (Projectile) event.getRemover();
                if (projectile.getShooter() instanceof Player)
                {
                    causer = this.module.getUserManager().getExactUser((Player) projectile.getShooter()).key;
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
                causer = this.module.getUserManager().getExactUser((Player) event.getRemover()).key;
            }
            if (event.getEntity() instanceof ItemFrame)
            {
                ItemStack itemStack = ((ItemFrame) event.getEntity()).getItem();
                String itemInFrame = this.serializeItemStack(itemStack);
                this.manager.queueLog(location, HANGING_BREAK, causer,
                        ITEM_FRAME.name(), (byte)0,
                        AIR.name(),(byte) 0, itemInFrame);
            }
            else if (event.getEntity() instanceof Painting)
            {
                this.manager.queueLog(location, HANGING_BREAK, causer,
                        PAINTING.name(), (byte)((Painting)event.getEntity()).getArt().getId(),
                        AIR.name(),(byte) 0, null);
            }
        }
        else
            System.out.print("Not a player breaking Hanging?");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event)
    {
        if (this.manager.isIgnored(LogManager.HANGING_PLACE)) return;
        User user = this.module.getUserManager().getExactUser(event.getPlayer());
        if (event.getEntity() instanceof ItemFrame)
        {
            this.manager.queueLog(event.getEntity().getLocation(), HANGING_PLACE, user.key,
                    AIR.name(),(byte) 0,
                    ITEM_FRAME.name(), (byte)0, null);
        }
        else if (event.getEntity() instanceof Painting)
        {
            this.manager.queueLog(event.getEntity().getLocation(), HANGING_PLACE, user.key,
                    AIR.name(),(byte) 0,
                    PAINTING.name(), (byte)((Painting)event.getEntity()).getArt().getId(),
                    null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event)
    {
        Location location = event.getEntity().getLocation();
        if (!this.manager.isIgnored(ITEM_DROP))
        {
            for (ItemStack itemStack : event.getDrops())
            {
                String itemData = this.serializeItemStack(itemStack);
                long causer;
                if (event.getEntity() instanceof Player)
                {
                    causer = this.module.getUserManager().getExactUser((Player)event.getEntity()).key;
                }
                else
                {
                    causer = -1L * event.getEntity().getType().getTypeId();
                }
                this.manager.queueLog(location,ITEM_DROP,causer,null,itemData);
            }
        }
        LivingEntity entity = event.getEntity();
        int action;
        long killed;
        if (entity instanceof Player)
        {
            if (this.manager.isIgnored(PLAYER_DEATH)) return;
            action = PLAYER_DEATH;
            killed = this.module.getUserManager().getExactUser((Player)entity).key;
        }
        else if (entity instanceof Wither || entity instanceof EnderDragon)
        {
            if (this.manager.isIgnored(BOSS_DEATH)) return;
            action = BOSS_DEATH;
            killed = -entity.getType().getTypeId();
        }
        else if (entity instanceof Animals)
        {
            if (entity instanceof Tameable && ((Tameable) entity).isTamed())
            {
                if (this.manager.isIgnored(PET_DEATH)) return;
                action = PET_DEATH;
                killed = -entity.getType().getTypeId();
            }
            else
            {
                if (this.manager.isIgnored(ANIMAL_DEATH)) return;
                action = ANIMAL_DEATH;
                killed = -entity.getType().getTypeId();
            }
        }
        else if (entity instanceof Villager)
        {
            if (this.manager.isIgnored(NPC_DEATH)) return;
            action = NPC_DEATH;
            killed = -entity.getType().getTypeId();
        }
        else if (entity instanceof Monster)
        {
            if (this.manager.isIgnored(MONSTER_DEATH)) return;
            action = MONSTER_DEATH;
            killed = -entity.getType().getTypeId();
        }
        else
        {
            if (this.manager.isIgnored(OTHER_DEATH)) return;
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
                    if (this.manager.isIgnored(PLAYER_KILL)) return;
                    causer = this.module.getUserManager().getExactUser((Player) shooter).key;
                }
                else if (shooter instanceof Skeleton || shooter instanceof Ghast)
                {
                    if (this.manager.isIgnored(ENTITY_KILL)) return;
                    causer = -shooter.getType().getTypeId();
                }
                else if (shooter instanceof Wither)
                {
                    if (this.manager.isIgnored(BOSS_KILL)) return;
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
                if (this.manager.isIgnored(PLAYER_KILL)) return;
                causer = this.module.getUserManager().getExactUser((Player) damager).key;
            }
            else if (damager instanceof Wither || damager instanceof Wither)
            {
                if (this.manager.isIgnored(BOSS_KILL)) return;
                causer = -damager.getType().getTypeId();
            }
            else
            {
                if (this.manager.isIgnored(ENTITY_KILL)) return;
                causer = -damager.getType().getTypeId();
            }
        }
        else
        {
            if (this.manager.isIgnored(ENVIRONMENT_KILL)) return;
            causer = 0;
        }
        this.manager.queueLog(location,action,causer,killed,additionalData);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleCreate(final VehicleCreateEvent event)
    {
        if (this.manager.isIgnored(VEHICLE_PLACE)) return;
        Location location = event.getVehicle().getLocation();
        Player player = this.plannedVehiclePlace.get(location);
        if (player != null)
        {
            this.manager.queueLog(location,VEHICLE_PLACE,player,null);
        }
        else
            System.out.print("Unexpected VehiclePlacement: "+event.getVehicle());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent event)
    {
        if (this.manager.isIgnored(VEHICLE_BREAK)) return;
        Long causer = null;
        if (event.getAttacker() != null)
        {
            if (event.getAttacker() instanceof Player)
            {
                causer = this.module.getUserManager().getExactUser((Player) event.getAttacker()).key;
            }
            else if (event.getAttacker() instanceof Projectile)
            {
                Projectile projectile = (Projectile) event.getAttacker();
                if (projectile.getShooter() instanceof Player)
                {
                    causer = this.module.getUserManager().getExactUser((Player) event.getAttacker()).key;
                }
                else if (projectile.getShooter() != null)
                {
                    causer = -1L * projectile.getShooter().getType().getTypeId();
                }
            }
        }
        else if (event.getVehicle().getPassenger() instanceof Player)
        {
            causer = this.module.getUserManager().getExactUser((Player) event.getVehicle().getPassenger()).key;
        }
        this.manager.queueLog(event.getVehicle().getLocation(),VEHICLE_BREAK,causer);
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        switch (event.getSpawnReason())
        {
            case NATURAL:
            case JOCKEY:
            case CHUNK_GEN:
            case VILLAGE_DEFENSE:
            case VILLAGE_INVASION:
                if (this.manager.isIgnored(NATURAL_SPAWN)) return;
                this.manager.queueLog(event.getLocation(), NATURAL_SPAWN,-event.getEntityType().getTypeId());
                return;
            case SPAWNER:
                if (this.manager.isIgnored(SPAWNER_SPAWN)) return;
                this.manager.queueLog(event.getLocation(), SPAWNER_SPAWN,-event.getEntityType().getTypeId());
                return;
            case EGG:
            case BUILD_SNOWMAN:
            case BUILD_IRONGOLEM:
            case BUILD_WITHER:
            case BREEDING:
                if (this.manager.isIgnored(OTHER_SPAWN)) return;
                this.manager.queueLog(event.getLocation(), OTHER_SPAWN,-event.getEntityType().getTypeId());
                return;
            //case SPAWNER_EGG: //is already done
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShear(PlayerShearEntityEvent event)
    {
        if (this.manager.isIgnored(ENTITY_SHEAR)) return;
        long spawnedEntity = -event.getEntity().getType().getTypeId();
        if (event.getEntity() instanceof LivingEntity)
        {
            this.manager.queueLog(event.getEntity().getLocation(), ENTITY_SHEAR,
                    spawnedEntity, null,
                    this.serializeData(null,(LivingEntity)event.getEntity(),null));
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
            if (this.manager.isIgnored(ITEM_INSERT)) return;
            this.manager.queueLog(entity.getLocation(),ITEM_INSERT,player,null);
        }
        else if(player.getItemInHand().getType().equals(INK_SACK) && entity instanceof Sheep || entity instanceof Wolf)
        {
            if (this.manager.isIgnored(ENTITY_DYE)) return;
            Dye dye = (Dye) player.getItemInHand().getData();
            this.manager.queueLog(entity.getLocation(),ENTITY_DYE,player,this.serializeData(null,entity,dye.getColor()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event)
    {
        LivingEntity livingEntity = event.getPotion().getShooter();
        Long causer = null;
        if (livingEntity instanceof Player)
        {
            causer = this.module.getUserManager().getExactUser((Player)livingEntity).key;
        }
        else if (livingEntity != null)
        {
            causer = -1L * livingEntity.getType().getTypeId();
        }
        String additionalData = this.serializePotionLog(event);
        this.manager.queueLog(event.getPotion().getLocation(),POTION_SPLASH,causer,null,additionalData);
    }

    public String serializePotionLog(PotionSplashEvent event)
    {
        Map<String,Object> dataMap = new HashMap<String, Object>();
        Collection<Object> effects = new ArrayList<Object>();
        dataMap.put("effects",effects);
        for (PotionEffect effect : event.getPotion().getEffects())
        {
            Object[] effectObject = new Object[]{effect.getType().getName(),effect.getAmplifier(),effect.getDuration()};
            effects.add(effectObject);
        }
        if (!event.getAffectedEntities().isEmpty())
        {
            dataMap.put("amount",event.getAffectedEntities().size());
            Collection<Long> affected = new HashSet<Long>();
            dataMap.put("affected",affected);
            for (LivingEntity livingEntity : event.getAffectedEntities())
            {
                if (livingEntity instanceof Player)
                {
                    User user = this.module.getUserManager().getExactUser((Player)livingEntity);
                    affected.add(user.key);
                }
                else
                {
                    affected.add(-1L * livingEntity.getType().getTypeId());
                }
            }
        }
        try {
            return this.mapper.writeValueAsString(dataMap);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not parse locationmap!",e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (this.manager.isIgnored(PLAYER_JOIN)) return;
        //TODO config log ip on login
        this.manager.queueLog(event.getPlayer().getLocation(),PLAYER_JOIN,event.getPlayer(),null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (this.manager.isIgnored(PLAYER_QUIT)) return;
        this.manager.queueLog(event.getPlayer().getLocation(),PLAYER_JOIN,event.getPlayer(),null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event)
    {
        if (this.manager.isIgnored(ITEM_DROP)) return;
        String itemData = this.serializeItemStack(event.getItemDrop().getItemStack());
        this.manager.queueLog(event.getPlayer().getLocation(),ITEM_DROP,event.getPlayer(),itemData);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent  event)
    {
        if (this.manager.isIgnored(ITEM_PICKUP)) return;
        String itemData = this.serializeItemStack(event.getItem().getItemStack());
        this.manager.queueLog(event.getPlayer().getLocation(),ITEM_PICKUP,event.getPlayer(),itemData);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExpPickup(PlayerExpChangeEvent event)
    {
        if (this.manager.isIgnored(XP_PICKUP)) return;
        this.manager.queueLog(event.getPlayer().getLocation(),XP_PICKUP,event.getPlayer(),String.valueOf(event.getAmount()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event)
    {
        if (this.manager.isIgnored(PLAYER_TELEPORT)) return;
        if (event.getFrom().equals(event.getTo())) return;
        String targetLocation = this.serializeLocation(false,event.getTo());
        String sourceLocation = this.serializeLocation(true, event.getFrom());
        this.manager.queueLog(event.getFrom(),PLAYER_TELEPORT,event.getPlayer(),targetLocation);
        this.manager.queueLog(event.getTo(),PLAYER_TELEPORT,event.getPlayer(),sourceLocation);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event)
    {
        if (this.manager.isIgnored(ENCHANT_ITEM)) return;
        String applied = this.serializeEnchantments(event.getEnchantsToAdd());
        User user = this.module.getUserManager().getExactUser(event.getEnchanter());
        this.manager.queueLog(event.getEnchanter().getLocation(),ENCHANT_ITEM,user.key,
                event.getItem().getType().name(),event.getItem().getDurability(),applied);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event)
    {
        if (this.manager.isIgnored(CRAFT_ITEM)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        User user = this.module.getUserManager().getExactUser((Player) event.getWhoClicked());
        this.manager.queueLog(event.getWhoClicked().getLocation(),CRAFT_ITEM,user.key,
                event.getRecipe().getResult().getType().name(),event.getRecipe().getResult().getDurability(),null);
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
        Map<String,Object> dataMap = new HashMap<String, Object>();
        if (cause != null)
        {
            dataMap.put("DamageCause",cause.name());
        }
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
        if (newColor != null)
        {
            dataMap.put("newColor",newColor.name());
        }
        try
        {
            return mapper.writeValueAsString(dataMap);
        }
        catch (JsonProcessingException e)
        {
            throw new IllegalStateException("Could not parse KillData!",e);
        }
    }

    public String serializeItemStack(ItemStack itemStack)
    {
        Map<String,Object> dataMap = new HashMap<String, Object>();
        dataMap.put("type",itemStack.getType().name());
        dataMap.put("data",itemStack.getDurability());
        dataMap.put("amount",itemStack.getAmount());
        if (itemStack.hasItemMeta())
        {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasDisplayName())
            {
                dataMap.put("display",meta.getDisplayName());
            }
            if (meta.hasLore())
            {
                dataMap.put("lore",meta.getLore());
            }
            if (meta.hasEnchants())
            {
                Map<String,Integer> enchantments = new HashMap<String, Integer>();
                for (Map.Entry<Enchantment,Integer> entry : meta.getEnchants().entrySet())
                {
                    enchantments.put(entry.getKey().getName(),entry.getValue());
                }
                dataMap.put("enchant",enchantments);
            }
        }
        try {
            return this.mapper.writeValueAsString(dataMap);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not parse itemmap!",e);
        }
    }

    private String serializeLocation(boolean from, Location location)
    {
        Map<String,Object> dataMap = new HashMap<String, Object>();
        dataMap.put("direction",from ? "from":"to");
        dataMap.put("world",this.module.getCore().getWorldManager().getWorldId(location.getWorld()));
        dataMap.put("x",location.getBlockX());
        dataMap.put("y",location.getBlockY());
        dataMap.put("z",location.getBlockZ());
        try {
            return this.mapper.writeValueAsString(dataMap);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not parse locationmap!",e);
        }
    }

    private String serializeEnchantments(Map<Enchantment,Integer> enchantsToAdd)
    {
        if (enchantsToAdd.isEmpty())
        {
            return null;
        }
        Map<String,Integer> enchantments = new HashMap<String, Integer>();
        for (Map.Entry<Enchantment,Integer> entry : enchantsToAdd.entrySet())
        {
            enchantments.put(entry.getKey().getName(),entry.getValue());
        }
        try {
            return this.mapper.writeValueAsString(enchantments);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not parse locationmap!",e);
        }
    }
}
