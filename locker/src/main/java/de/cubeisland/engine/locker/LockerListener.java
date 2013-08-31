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

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;
import org.bukkit.material.Openable;
import org.bukkit.util.Vector;

import de.cubeisland.engine.locker.storage.Lock;
import de.cubeisland.engine.locker.storage.LockManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.core.util.ChatFormat;

import static de.cubeisland.engine.locker.storage.ProtectionFlags.*;

public class LockerListener implements Listener
{
    private LockManager manager;
    private de.cubeisland.engine.locker.Locker module;

    public LockerListener(de.cubeisland.engine.locker.Locker module, LockManager manager)
    {
        this.module = module;
        this.manager = manager;
        this.module.getCore().getEventManager().registerListener(module, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.useInteractedBlock().equals(Result.DENY)) return;
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        Location location = event.getClickedBlock().getLocation();
        Lock lock = this.manager.getLockAtLocation(location);
        if (lock != null && !lock.isValidType())
        {
            user.sendTranslated("&eExisting BlockProtection is not valid!");
            lock.delete(user);
            return;
        }
        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof InventoryHolder)
        {
            if (LockerPerm.DENY_CONTAINER.isAuthorized(user))
            {
                user.sendTranslated("&cStrong magic prevents you from accessing any inventory!");
                event.setCancelled(true);
                return;
            }
            if (this.handleAccess(lock, user, null, event)) return;
            lock.handleInventoryOpen(event, null, user);
        }
        else if (event.getClickedBlock().getState().getData() instanceof Openable)
        {
            if (LockerPerm.DENY_DOOR.isAuthorized(user))
            {
                user.sendTranslated("&cStrong magic prevents you from accessing any door!");
                event.setCancelled(true);
                return;
            }
            if (this.handleAccess(lock, user, location, event))
            {
                if (lock == null) return;
                lock.doorUse(user, location);
                return;
            }
            lock.handleBlockDoorUse(event, user, location);
        }
        if (event.isCancelled()) event.setUseInteractedBlock(Result.DENY);
    }

    private boolean handleAccess(Lock lock, User user, Location soundLocation, Cancellable event)
    {
        if (lock == null) return true;
        if (lock.isOwner(user)) return true;
        Boolean keyBookUsed = this.checkForKeyBook(lock, user, soundLocation);
        if (keyBookUsed == null)
        {
            event.setCancelled(true);
            return false;
        }
        return keyBookUsed || this.checkForUnlocked(lock, user);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        Entity entity = event.getRightClicked();
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (LockerPerm.DENY_ENTITY.isAuthorized(user))
        {
            user.sendTranslated("&cStrong magic prevents you from reaching this Entity!");
            event.setCancelled(true);
            return;
        }
        Lock lock = this.manager.getLockForEntityUID(entity.getUniqueId());
        if (lock == null) return;
        if (this.handleAccess(lock, user, null, event)) return;
        if (entity instanceof StorageMinecart || entity instanceof HopperMinecart
            || (entity.getType() == EntityType.HORSE && entity instanceof InventoryHolder && event.getPlayer().isSneaking()))
        {
            lock.handleInventoryOpen(event, null, user);
        }
        else
        {
            lock.handleEntityInteract(event, entity, user);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        if (!(event.getPlayer() instanceof Player)) return;
        Location holderLoc = new Location(null, 0,0,0);
        Lock lock = this.getLockOfInventory(event.getInventory(), holderLoc);
        if (lock != null)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            if (this.handleAccess(lock, user, holderLoc, event)) return;
            lock.handleInventoryOpen(event, event.getInventory(), user);
        }
    }

    /**
     * Returns the lock for given inventory it exists, also sets the location to the holders location if not null
     *
     * @param inventory
     * @param holderLoc a location object to hold the LockLocation
     * @return the lock for given inventory
     */
    public Lock getLockOfInventory(Inventory inventory, Location holderLoc)
    {
        InventoryHolder holder = inventory.getHolder();
        Lock lock;
        if (holderLoc == null)
        {
            holderLoc = new Location(null, 0, 0, 0);
        }
        if (holder instanceof Entity)
        {
            lock = this.manager.getLockForEntityUID(((Entity)holder).getUniqueId());
            ((Entity)holder).getLocation(holderLoc);
        }
        else
        {
            Location lockLoc;
            if (holder instanceof BlockState)
            {
                lockLoc = ((BlockState)holder).getLocation(holderLoc);
            }
            else if (holder instanceof DoubleChest)
            {
                lockLoc = ((BlockState)((DoubleChest)holder).getRightSide()).getLocation(holderLoc);
            }
            else return null;
            lock = this.manager.getLockAtLocation(lockLoc);
        }
        return lock;
    }

    private boolean checkForUnlocked(Lock lock, User user)
    {
        LockerAttachment lockerAttachment = user.get(LockerAttachment.class);
        return lockerAttachment != null && lockerAttachment.hasUnlocked(lock);
    }

    /**
     * Returns true if the chest could open
     * <p>null if the chest cannot be opened with the KeyBook
     * <p>false if the user has no KeyBook in hand
     *
     * @param lock
     * @param user
     * @param effectLocation
     * @return
     */
    private Boolean checkForKeyBook(Lock lock, User user, Location effectLocation)
    {
        if (user.getItemInHand().getType() == Material.ENCHANTED_BOOK && user.getItemInHand().getItemMeta().getDisplayName().contains("KeyBook"))
        {
            String keyBookName = user.getItemInHand().getItemMeta().getDisplayName();
            try
            {
                long id = Long.valueOf(keyBookName.substring(keyBookName.indexOf('#')+1, keyBookName.length()));
                if (lock.getId().equals(id)) // Id matches ?
                {
                    // Validate book
                    if (keyBookName.startsWith(lock.getColorPass()))
                    {
                        if (effectLocation != null) user.sendTranslated("&aAs you approach with your KeyBook the magic lock disappears!");
                        user.playSound(effectLocation, Sound.PISTON_EXTEND, 1, 2);
                        user.playSound(effectLocation, Sound.PISTON_EXTEND, 1, (float)1.5);
                        if (effectLocation != null) lock.notifyKeyUsage(user);
                        return true;
                    }
                    else
                    {
                        user.sendTranslated("&cYou try to open the container with your KeyBook\n" +
                                                "but forcefully get pushed away!");
                        ItemStack itemInHand = user.getItemInHand();
                        ItemMeta itemMeta = itemInHand.getItemMeta();
                        itemMeta.setDisplayName(ChatFormat.parseFormats("&4Broken KeyBook"));
                        itemMeta.setLore(Arrays.asList(ChatFormat.parseFormats(user.translate("&eThis KeyBook")),
                                                       ChatFormat.parseFormats(user.translate("&elooks old and")),
                                                       ChatFormat.parseFormats(user.translate("&eused up. It")),
                                                       ChatFormat.parseFormats(user.translate("&ewont let you")),
                                                       ChatFormat.parseFormats(user.translate("&eopen any containers!"))));
                        itemInHand.setItemMeta(itemMeta);
                        itemInHand.setType(Material.PAPER);
                        user.updateInventory();
                        user.playSound(effectLocation, Sound.GHAST_SCREAM, 1, 1);
                        final Vector userDirection = user.getLocation().getDirection();
                        user.damage(1);
                        user.setVelocity(userDirection.multiply(-3));
                        return null;
                    }
                }
                else
                {
                    user.sendTranslated("&eYou try to open the container with your KeyBook but nothing happens!");
                    user.playSound(effectLocation, Sound.BLAZE_HIT, 1, 1);
                    user.playSound(effectLocation, Sound.BLAZE_HIT, 1, (float)0.8);
                    return null;
                }
            }
            catch (NumberFormatException|IndexOutOfBoundsException ignore) // invalid book / we do not care
            {}
        }
        return false;
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
            LivingEntity shooter = ((Projectile)event.getDamager()).getShooter();
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
        // no need to check if allowed to kill as this would have caused an DamageEvent before / this is only to cleanup database a bit
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

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event)
    {
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
                    Lock lock = this.manager.getLockAtLocation(relativeLoc);
                    if (lock != null)
                    {
                        if (lock.isValidType())
                        {
                            user.sendTranslated("&eNearby BlockProtection is not valid!");
                            lock.delete(user);
                        }
                        else if (lock.isOwner(user) || lock.hasAdmin(user)) // TODO perm
                        {
                            this.manager.extendLock(lock, event.getBlockPlaced().getLocation());
                            user.sendTranslated("&aProtection expanded!");
                        }
                        else
                        {
                            event.setCancelled(true);
                            user.sendTranslated("&cThe nearby chest is protected by someone else!");
                        }
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
                    Lock lock = this.manager.getLockAtLocation(relativeLoc);
                    if (lock != null)
                    {
                        if (lock.isValidType())
                        {
                            user.sendTranslated("&eNearby BlockProtection is not valid!");
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
                                    if (lock.isOwner(user) || lock.hasAdmin(user)) // TODO perm
                                    {
                                        this.manager.extendLock(lock, loc); // bot half
                                        this.manager.extendLock(lock, loc.clone().add(0, 1, 0)); // top half
                                        user.sendTranslated("&aProtection expanded!");
                                    }
                                    else
                                    {
                                        event.setCancelled(true);
                                        user.sendTranslated("&cThe nearby door is protected by someone else!");
                                    }
                                }
                            }
                            // else do not expand protection
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event)
    {
        Block block = event.getBlock();
        Lock lock = this.manager.getLockAtLocation(block.getLocation());
        if (lock != null)
        {
            if (lock.isValidType())
            {
                if (lock.hasFlag(BLOCK_REDSTONE))
                {
                    event.setNewCurrent(0);
                }
                return;
            }
            lock.delete(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event)
    {
        Location location = event.getBlock().getLocation();
        for (Block block : event.getBlocks())
        {
            Lock lock = this.manager.getLockAtLocation(block.getLocation(location));
            if (lock != null)
            {
                if (lock.isValidType())
                {
                    event.setCancelled(true);
                    return;
                }
                lock.delete(null);
            }
        }
        Lock lock = this.manager.getLockAtLocation(location.getBlock().getRelative(event.getDirection())
                                                           .getLocation(location));
        if (lock != null)
        {
            if (lock.isValidType())
            {
                event.setCancelled(true);
                return;
            }
            lock.delete(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event)
    {
        Lock lock = this.manager.getLockAtLocation(event.getRetractLocation());
        if (lock != null)
        {
            if (lock.isValidType())
            {
                event.setCancelled(true);
                return;
            }
            lock.delete(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Lock lock = this.manager.getLockAtLocation(event.getBlock().getLocation());
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (lock != null)
        {
            if (lock.isValidType())
            {
                user.sendTranslated("&eExisting BlockProtection is not valid!");
                lock.delete(user);
            }
            else
            {
                lock.handleBlockBreak(event, user);
            }
        }
        else
        {
            Location location = new Location(null,0,0,0);
            for (Block block : BlockUtil.getDetachableBlocks(event.getBlock()))
            {
                lock = this.manager.getLockAtLocation(block.getLocation(location));
                if (lock != null)
                {
                    if (lock.isValidType())
                    {
                        user.sendTranslated("&eExisting BlockProtection is not valid!");
                        lock.delete(user);
                    }
                    else
                    {
                        lock.handleBlockBreak(event, user);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(EntityExplodeEvent event)
    {
        // TODO allow explode flag
        Location location = new Location(null,0,0,0);
        for (Block block : event.blockList())
        {
            Lock lock = this.manager.getLockAtLocation(block.getLocation(location));
            if (lock != null)
            {
                if (lock.isValidType())
                {
                    event.setCancelled(true);
                    return;
                }
                lock.delete(null);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        // TODO allow burn flag
        Location location = event.getBlock().getLocation();
        Lock lock = this.manager.getLockAtLocation(location);
        if (lock != null)
        {
            if (lock.isValidType())
            {
                event.setCancelled(true);
                return;
            }
            lock.delete(null);
        }
        for (Block block : BlockUtil.getDetachableBlocks(event.getBlock()))
        {
            lock = this.manager.getLockAtLocation(block.getLocation(location));
            if (lock != null)
            {
                if (lock.isValidType())
                {
                    event.setCancelled(true);
                    return;
                }
                lock.delete(null);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHopperItemMove(InventoryMoveItemEvent event)
    {
        Inventory inventory = event.getSource();
        Lock lock = this.getLockOfInventory(inventory, null);
        if (lock != null)
        {
            InventoryHolder dest = event.getDestination().getHolder();
            if ((dest instanceof Hopper && lock.hasFlag(BLOCK_HOPPER_OUT))
             || (dest instanceof HopperMinecart && lock.hasFlag(BLOCK_HOPPER_MINECART_OUT)))
            {
                event.setCancelled(true);
            }
        }
        if (event.isCancelled()) return;
        inventory = event.getDestination();
        lock = this.getLockOfInventory(inventory, null);
        if (lock != null && lock.hasFlag(BLOCK_HOPPER_ANY_IN))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWaterLavaFlow(BlockFromToEvent event)
    {
        if (this.module.getConfig().protectBlocksFromWaterLava && BlockUtil.isNonFluidProofBlock(event.getToBlock().getType()))
        {
            Lock lock = this.manager.getLockAtLocation(event.getToBlock().getLocation());
            if (lock != null)
            {
                if (lock.isValidType())
                {
                    event.setCancelled(true);
                    return;
                }
                lock.delete(null);
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
                if (LockerPerm.DENY_HANGING.isAuthorized(user))
                {
                    event.setCancelled(true);
                    return;
                }
                if (lock == null) return;
                if (lock.isOwner(user))
                {
                    lock.delete(user);
                    return;
                }
            }
        }
        event.setCancelled(true);
    }

    // TODO auto-protect
    // TODO expand protections for hangings/attachables
}
