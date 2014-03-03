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
package de.cubeisland.engine.locker;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Door;
import org.bukkit.material.Openable;
import org.bukkit.projectiles.ProjectileSource;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.locker.storage.Lock;
import de.cubeisland.engine.locker.storage.LockManager;

import static de.cubeisland.engine.locker.storage.ProtectionFlag.*;

public class LockerListener implements Listener
{
    private final LockManager manager;
    private final Locker module;

    public LockerListener(Locker module, LockManager manager)
    {
        this.module = module;
        this.manager = manager;
        this.module.getCore().getEventManager().registerListener(module, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (!this.module.getConfig().protectBlockFromRClick) return;
        if (event.useInteractedBlock().equals(Result.DENY)) return;
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        Location location = event.getClickedBlock().getLocation();
        Lock lock = this.manager.getLockAtLocation(location, user);
        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof InventoryHolder)
        {
            if (module.perms().DENY_CONTAINER.isAuthorized(user))
            {
                user.sendTranslated(MessageType.NEGATIVE, "Strong magic prevents you from accessing any inventory!");
                event.setCancelled(true);
                return;
            }
            if (lock == null) return;
            lock.handleInventoryOpen(event, null, null, user);
        }
        else if (event.getClickedBlock().getState().getData() instanceof Openable)
        {
            if (module.perms().DENY_DOOR.isAuthorized(user))
            {
                user.sendTranslated(MessageType.NEGATIVE, "Strong magic prevents you from accessing any door!");
                event.setCancelled(true);
                return;
            }
            if (lock == null) return;
            lock.handleBlockDoorUse(event, user, location);
        }
        else if (lock != null)// other interact e.g. repeater
        {
            lock.handleBlockInteract(event, user);
        }
        if (event.isCancelled()) event.setUseInteractedBlock(Result.DENY);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if (!this.module.getConfig().protectEntityFromRClick) return;
        Entity entity = event.getRightClicked();
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (module.perms().DENY_ENTITY.isAuthorized(user))
        {
            user.sendTranslated(MessageType.NEGATIVE, "Strong magic prevents you from reaching this Entity!");
            event.setCancelled(true);
            return;
        }
        Lock lock = this.manager.getLockForEntityUID(entity.getUniqueId());
        if (lock == null) return;
        if (entity instanceof StorageMinecart
            || entity instanceof HopperMinecart
            || (entity.getType() == EntityType.HORSE && event.getPlayer().isSneaking()))
        {
            lock.handleInventoryOpen(event, null, null, user);
        }
        else
        {
            lock.handleEntityInteract(event, user);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        if (!(event.getPlayer() instanceof Player)) return;
        Location holderLoc = new Location(null, 0,0,0);
        Lock lock = this.manager.getLockOfInventory(event.getInventory(), holderLoc);
        if (lock == null) return;
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        lock.handleInventoryOpen(event, event.getInventory(), holderLoc, user);
    }

    public void onEntityDamageEntity(EntityDamageByEntityEvent event)
    {
        Entity entity = event.getEntity();
        Lock lock = this.manager.getLockForEntityUID(entity.getUniqueId());
        if (lock == null) return;
        if (event.getDamager() instanceof Player)
        {
            User user = this.module.getCore().getUserManager().getExactUser(((Player)event.getDamager()).getName());
            lock.handleEntityDamage(event, user);
            return;
        }
        else if (event.getDamager() instanceof TNTPrimed)
        {
            Entity source = ((TNTPrimed)event.getDamager()).getSource();
            if (source != null && source instanceof Player)
            {
                User user = this.module.getCore().getUserManager().getExactUser(((Player)source).getPlayer().getName());
                lock.handleEntityDamage(event, user);
                return;
            }
        }
        else if (event.getDamager() instanceof Projectile)
        {
            ProjectileSource shooter = ((Projectile)event.getDamager()).getShooter();
            if (shooter != null && shooter instanceof Player)
            {
                User user = this.module.getCore().getUserManager().getExactUser(((Player)shooter).getName());
                lock.handleEntityDamage(event, user);
                return;
            }
        }
        // else other source
        if (module.getConfig().protectEntityFromEnvironementalDamage)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event)
    {
        if (!this.module.getConfig().protectEntityFromDamage) return;
        if (event instanceof EntityDamageByEntityEvent)
        {
            this.onEntityDamageEntity((EntityDamageByEntityEvent)event);
        }
        else if (module.getConfig().protectEntityFromEnvironementalDamage)
        {
            Entity entity = event.getEntity();
            Lock lock = this.manager.getLockForEntityUID(entity.getUniqueId());
            if (lock == null) return;
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event)
    {
        // no need to check if allowed to kill as this would have caused an DamageEvent before / this is only to cleanup database
        Lock lock = this.manager.getLockForEntityUID(event.getEntity().getUniqueId());
        if (lock == null) return;
        EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();
        User user = null;
        if (lastDamage != null && lastDamage instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)lastDamage).getDamager() instanceof Player)
        {
            user = this.module.getCore().getUserManager().getExactUser(((Player)((EntityDamageByEntityEvent)lastDamage).getDamager()).getName());
        }
        lock.handleEntityDeletion(user);
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleBreak(VehicleDestroyEvent event)
    {
        if (!this.module.getConfig().protectVehicleFromBreak) return;
        Lock lock = this.manager.getLockForEntityUID(event.getVehicle().getUniqueId());
        if (lock == null) return;
        if (event.getAttacker() == null)
        {
            if (module.getConfig().protectVehicleFromEnvironmental)
            {
                event.setCancelled(true);
            }
            return;
        }
        User user = this.module.getCore().getUserManager().getExactUser(((Player)event.getAttacker()).getName());
        if (lock.isOwner(user))
        {
            lock.handleEntityDeletion(user);
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event)
    {
        if (!event.canBuild()) return;
        Block placed = event.getBlockPlaced();
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (placed.getType() == Material.CHEST || placed.getType() == Material.TRAPPED_CHEST)
        {
            Location relativeLoc = new Location(null,0,0,0);
            for (BlockFace blockFace : BlockUtil.CARDINAL_DIRECTIONS)
            {
                if (placed.getType() == placed.getRelative(blockFace).getType()) // bindable chest
                {
                    placed.getRelative(blockFace).getLocation(relativeLoc);
                    Lock lock = this.manager.getLockAtLocation(relativeLoc, user, false, false);
                    if (lock != null)
                    {
                        if (!lock.validateTypeAt(relativeLoc))
                        {
                            user.sendTranslated(MessageType.NEUTRAL, "Nearby BlockProtection is not valid!");
                            lock.delete(user);
                        }
                        else if (lock.isOwner(user) || lock.hasAdmin(user) || module.perms().EXPAND_OTHER.isAuthorized(user))
                        {
                            this.manager.extendLock(lock, event.getBlockPlaced().getLocation());
                            user.sendTranslated(MessageType.POSITIVE, "Protection expanded!");
                        }
                        else
                        {
                            event.setCancelled(true);
                            user.sendTranslated(MessageType.NEGATIVE, "The nearby chest is protected by someone else!");
                        }
                        return;
                    }
                }
            }
        }
        else if (placed.getType() == Material.WOODEN_DOOR || placed.getType() == Material.IRON_DOOR_BLOCK)
        {
            Location loc = placed.getLocation();
            Location relativeLoc = new Location(null,0,0,0);
            for (BlockFace blockFace : BlockUtil.CARDINAL_DIRECTIONS)
            {
                if (placed.getType() == placed.getRelative(blockFace).getType())
                {
                    placed.getRelative(blockFace).getLocation(relativeLoc);
                    Lock lock = this.manager.getLockAtLocation(relativeLoc, null, false, false);
                    if (lock != null)
                    {
                        if (!lock.validateTypeAt(relativeLoc))
                        {
                            user.sendTranslated(MessageType.NEUTRAL, "Nearby BlockProtection is not valid!");
                            lock.delete(user);
                        }
                        else
                        {
                            if (!(relativeLoc.getBlock().getState().getData() instanceof Door)) return; // door is above
                            Door botRelative = (Door)relativeLoc.getBlock().getState().getData();
                            if (botRelative.isTopHalf()) return; // door is below
                            Door botDoor = (Door)placed.getState().getData();
                            Door topDoor = new Door(placed.getType(), BlockUtil.getTopDoorDataOnPlace(placed.getType(), loc, event.getPlayer()));
                            Door topRelative = (Door)relativeLoc.getBlock().getRelative(BlockFace.UP).getState().getData();
                            if (botDoor.getFacing() == botRelative.getFacing())// Doors are facing the same direction
                            {
                                if (topDoor.getData() != topRelative.getData()) // This is a doubleDoor!
                                {
                                    if (lock.isOwner(user) || lock.hasAdmin(user) || module.perms().EXPAND_OTHER.isAuthorized(user))
                                    {
                                        this.manager.extendLock(lock, loc); // bot half
                                        this.manager.extendLock(lock, loc.clone().add(0, 1, 0)); // top half
                                        user.sendTranslated(MessageType.POSITIVE, "Protection expanded!");
                                    }
                                    else
                                    {
                                        event.setCancelled(true);
                                        user.sendTranslated(MessageType.NEGATIVE, "The nearby door is protected by someone else!");
                                    }
                                }
                            }
                            // else do not expand protection
                        }
                        return;
                    }
                }
            }
        }
        for (BlockLockerConfiguration blockprotection : this.module.getConfig().blockprotections)
        {
            if (blockprotection.isType(placed.getType()))
            {
                if (!blockprotection.autoProtect) return;
                this.manager.createLock(placed.getType(), placed.getLocation(), user, blockprotection.autoProtectType, null, false);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event)
    {
        if (!this.module.getConfig().protectFromRedstone) return;
        Block block = event.getBlock();
        Lock lock = this.manager.getLockAtLocation(block.getLocation(), null);
        if (lock != null)
        {
            if (lock.hasFlag(BLOCK_REDSTONE))
            {
                event.setNewCurrent(event.getOldCurrent());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event)
    {
        if (!this.module.getConfig().protectFromPistonMove) return;
        Location location = event.getBlock().getLocation();
        for (Block block : event.getBlocks())
        {
            Lock lock = this.manager.getLockAtLocation(block.getLocation(location), null);
            if (lock != null)
            {
                event.setCancelled(true);
                return;
            }
        }
        Lock lock = this.manager.getLockAtLocation(location.getBlock().getRelative(event.getDirection()).getLocation(location), null);
        if (lock != null)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event)
    {
        if (!this.module.getConfig().protectFromPistonMove) return;
        Lock lock = this.manager.getLockAtLocation(event.getRetractLocation(), null);
        if (lock != null)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (!this.module.getConfig().protectFromBlockBreak) return;
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        Lock lock = this.manager.getLockAtLocation(event.getBlock().getLocation(), user);
        if (lock != null)
        {
            lock.handleBlockBreak(event, user);
        }
        else
        {
            // Search for Detachable Blocks
            Location location = new Location(null,0,0,0);
            for (Block block : BlockUtil.getDetachableBlocks(event.getBlock()))
            {
                lock = this.manager.getLockAtLocation(block.getLocation(location), user);
                if (lock != null)
                {
                    lock.handleBlockBreak(event, user);
                    return;
                }
            }
            // Search for Detachable Entities
            if (module.getConfig().protectsDetachableEntities())
            {
                Set<Chunk> chunks = new HashSet<>();
                chunks.add(event.getBlock().getChunk());
                chunks.add(event.getBlock().getRelative(BlockFace.NORTH).getChunk());
                chunks.add(event.getBlock().getRelative(BlockFace.EAST).getChunk());
                chunks.add(event.getBlock().getRelative(BlockFace.SOUTH).getChunk());
                chunks.add(event.getBlock().getRelative(BlockFace.WEST).getChunk());
                Set<Hanging> hangings = new HashSet<>();
                for (Chunk chunk : chunks)
                {
                    for (Entity entity : chunk.getEntities())
                    {
                        if (entity instanceof Hanging)
                        {
                            hangings.add((Hanging)entity);
                        }
                    }
                }
                Location entityLoc = new Location(null,0,0,0);
                for (Hanging hanging : hangings)
                {
                    if (hanging.getLocation(entityLoc).getBlock().getRelative(hanging.getAttachedFace()).equals(event.getBlock()))
                    {
                        lock = this.manager.getLockForEntityUID(hanging.getUniqueId());
                        if (lock != null)
                        {
                            lock.handleBlockBreak(event, user);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(EntityExplodeEvent event)
    {
        if (!this.module.getConfig().protectBlockFromExplosion) return;
        Location location = new Location(null,0,0,0);
        for (Block block : event.blockList())
        {
            Lock lock = this.manager.getLockAtLocation(block.getLocation(location), null);
            if (lock != null)
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        if (!this.module.getConfig().protectBlockFromFire) return;
        Location location = event.getBlock().getLocation();
        Lock lock = this.manager.getLockAtLocation(location, null);
        if (lock != null)
        {
            event.setCancelled(true);
            return;
        }
        for (Block block : BlockUtil.getDetachableBlocks(event.getBlock()))
        {
            lock = this.manager.getLockAtLocation(block.getLocation(location), null);
            if (lock != null)
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHopperItemMove(InventoryMoveItemEvent event)
    {
        if (this.module.getConfig().noProtectFromHopper) return;
        Inventory inventory = event.getSource();
        Lock lock = this.manager.getLockOfInventory(inventory, null);
        if (lock != null)
        {
            InventoryHolder dest = event.getDestination().getHolder();
            if (((dest instanceof Hopper || dest instanceof Dropper) && !lock.hasFlag(HOPPER_OUT))
             || (dest instanceof HopperMinecart && !lock.hasFlag(HOPPER_MINECART_OUT)))
            {
                event.setCancelled(true);
            }
        }
        if (event.isCancelled()) return;
        inventory = event.getDestination();
        lock = this.manager.getLockOfInventory(inventory, null);
        if (lock != null)
        {
            InventoryHolder source = event.getSource().getHolder();
            if (((source instanceof Hopper || source instanceof Dropper) && !lock.hasFlag(HOPPER_IN))
                || (source instanceof HopperMinecart && !lock.hasFlag(HOPPER_MINECART_IN)))
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaterLavaFlow(BlockFromToEvent event)
    {
        if (this.module.getConfig().protectBlocksFromWaterLava && BlockUtil.isNonFluidProofBlock(event.getToBlock().getType()))
        {
            Lock lock = this.manager.getLockAtLocation(event.getToBlock().getLocation(), null);
            if (lock != null)
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) // leash / itemframe / image
    {
        if (event.getCause() == RemoveCause.ENTITY && event instanceof HangingBreakByEntityEvent)
        {
            if (((HangingBreakByEntityEvent)event).getRemover() instanceof Player)
            {
                Lock lock = this.manager.getLockForEntityUID(event.getEntity().getUniqueId());
                User user = this.module.getCore().getUserManager().getExactUser(((Player)((HangingBreakByEntityEvent)event).getRemover()).getName());
                if (module.perms().DENY_HANGING.isAuthorized(user))
                {
                    event.setCancelled(true);
                    return;
                }
                if (lock == null) return;
                if (lock.handleEntityDamage(event, user))
                {
                    lock.delete(user);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTame(EntityTameEvent event)
    {
        for (EntityLockerConfiguration entityProtection : this.module.getConfig().entityProtections)
        {
            if (entityProtection.isType(event.getEntityType()) && entityProtection.autoProtect)
            {
                User user = this.module.getCore().getUserManager().getExactUser(event.getOwner().getName());
                if (this.manager.getLockForEntityUID(event.getEntity().getUniqueId()) == null)
                {
                    this.manager.createLock(event.getEntity(), user, entityProtection.autoProtectType, null, false);
                }
            }
        }
    }
}
