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
package de.cubeisland.engine.cguard;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;
import org.bukkit.material.Openable;
import org.bukkit.util.Vector;

import de.cubeisland.engine.cguard.storage.Guard;
import de.cubeisland.engine.cguard.storage.GuardManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.core.util.ChatFormat;

import static de.cubeisland.engine.cguard.storage.ProtectionFlags.REDSTONE;

public class GuardListener implements Listener
{
    private GuardManager manager;
    private Cguard module;

    public GuardListener(Cguard module, GuardManager manager)
    {
        this.module = module;
        this.manager = manager;
        this.module.getCore().getEventManager().registerListener(module, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.useInteractedBlock().equals(Result.DENY)) return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        Location location = event.getClickedBlock().getLocation();
        Guard guard = this.manager.getGuardAtLocation(location);
        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof InventoryHolder)
        {
            // TODO deny permission protects ALL containers
            if (this.handleAccess(guard, user, null, event)) return;
            guard.handleInventoryOpen(event, null, user);
        }
        else if (event.getClickedBlock().getState().getData() instanceof Openable)
        {
            // TODO deny permission protects ALL doors
            if (this.handleAccess(guard, user, location, event))
            {
                if (guard == null) return;
                // TODO open doubledoor
                // TODO autoclose later
                return;
            }
            guard.handleBlockDoorUse(event, user);
        }
        if (event.isCancelled()) event.setUseInteractedBlock(Result.DENY);
    }

    private boolean handleAccess(Guard guard, User user, Location soundLocation, Cancellable event)
    {
        if (guard == null) return true;
        if (guard.isOwner(user)) return true;
        Boolean keyBookUsed = this.checkForKeyBook(guard, user, soundLocation);
        if (keyBookUsed == null)
        {
            event.setCancelled(true);
            return false;
        }
        return keyBookUsed || this.checkForUnlocked(guard, user);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        Entity entity = event.getRightClicked();
        // TODO check for deny flag
        Guard guard = this.manager.getGuardForEntityUID(entity.getUniqueId());
        if (guard == null) return;
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (this.handleAccess(guard, user, null, event)) return;
        if (entity instanceof StorageMinecart || entity instanceof HopperMinecart
            || (entity.getType().equals(EntityType.HORSE) && entity instanceof InventoryHolder && event.getPlayer().isSneaking()))
        {
            guard.handleInventoryOpen(event, null, user);
        }
        else
        {
            guard.handleEntityInteract(event, entity, user);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        if (!(event.getPlayer() instanceof Player)) return;
        Guard guard;
        InventoryHolder holder = event.getInventory().getHolder();
        Location holderLoc;
        if (holder instanceof Entity)
        {
            guard = this.manager.getGuardForEntityUID(((Entity)holder).getUniqueId());
            holderLoc = ((Entity)holder).getLocation();
        }
        else
        {
            Location location;
            if (holder instanceof BlockState)
            {
                location = ((BlockState)holder).getLocation();
                holderLoc = ((BlockState)holder).getLocation();
            }
            else if (holder instanceof DoubleChest)
            {
                location = ((BlockState)((DoubleChest)holder).getRightSide()).getLocation();
                holderLoc = ((DoubleChest)holder).getLocation();
            }
            else return;
            guard = this.manager.getGuardAtLocation(location);
        }
        if (guard != null)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            if (this.handleAccess(guard, user, holderLoc, event)) return;
            guard.handleInventoryOpen(event, event.getInventory(), user);
        }

    }

    private boolean checkForUnlocked(Guard guard, User user)
    {
        GuardAttachment guardAttachment = user.get(GuardAttachment.class);
        return guardAttachment != null && guardAttachment.hasUnlocked(guard);
    }

    /**
     * Returns true if the chest could open
     * <p>null if the chest cannot be opened with the KeyBook
     * <p>false if the user has no KeyBook in hand
     *
     * @param guard
     * @param user
     * @param effectLocation
     * @return
     */
    private Boolean checkForKeyBook(Guard guard, User user, Location effectLocation)
    {
        if (user.getItemInHand().getType().equals(Material.ENCHANTED_BOOK) && user.getItemInHand().getItemMeta().getDisplayName().contains("KeyBook"))
        {
            String keyBookName = user.getItemInHand().getItemMeta().getDisplayName();
            try
            {
                long id = Long.valueOf(keyBookName.substring(keyBookName.indexOf('#')+1, keyBookName.length()));
                if (guard.getId().equals(id)) // Id matches ?
                {
                    // Validate book
                    if (keyBookName.startsWith(guard.getColorPass()))
                    {
                        if (effectLocation != null) user.sendTranslated("&aAs you approach with your KeyBook the magic lock disappears!");
                        user.playSound(effectLocation, Sound.PISTON_EXTEND, 1, 2);
                        user.playSound(effectLocation, Sound.PISTON_EXTEND, 1, (float)1.5);
                        if (effectLocation != null) guard.notifyKeyUsage(user);
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

    public void onPlayerDamageEntity(EntityDamageByEntityEvent event)
    {
        // TODO
        if (event.getDamager() instanceof Player)
        {
            Entity entity = event.getEntity();
            Guard guard = this.manager.getGuardForEntityUID(entity.getUniqueId());
            if (guard == null) return;
            User user = this.module.getCore().getUserManager().getExactUser(((Player)event.getDamager()).getName());
            if (guard.isOwner(user)) return;
            if (this.checkForUnlocked(guard, user)) return;
            guard.handleEntityDamage(event, user);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event)
    {
        if (event instanceof EntityDamageByEntityEvent)
        {
            this.onPlayerDamageEntity((EntityDamageByEntityEvent)event);
        }
        else
        {
            Entity entity = event.getEntity();
            Guard guard = this.manager.getGuardForEntityUID(entity.getUniqueId());
            if (guard == null) return;
            event.setCancelled(true);
            // TODO config
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event)
    {
        // no need to check if allowed to kill as this would have caused an DamageEvent before / this is only to cleanup database a bit
        Guard guard = this.manager.getGuardForEntityUID(event.getEntity().getUniqueId());
        if (guard == null) return;
        EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();
        User user = null;
        if (lastDamage != null && lastDamage instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)lastDamage).getDamager() instanceof Player)
        {
            user = this.module.getCore().getUserManager().getExactUser(((Player)((EntityDamageByEntityEvent)lastDamage).getDamager()).getName());
        }
        guard.handleEntityDestroy(user);
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleBreak(VehicleDestroyEvent event)
    {
        Guard guard = this.manager.getGuardForEntityUID(event.getVehicle().getUniqueId());
        if (guard == null) return;
        if (event.getAttacker() == null)
        {
            // TODO configure if lava can destroy or else
            event.setCancelled(true);
            return;
        }
        User user = this.module.getCore().getUserManager().getExactUser(((Player)event.getAttacker()).getName());
        if (guard.isOwner(user))
        {
            guard.handleEntityDestroy(user);
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event)
    {
        Block placed = event.getBlockPlaced();
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (placed.getType().equals(Material.CHEST) || placed.getType().equals(Material.TRAPPED_CHEST))
        {
            Location relativeLoc = new Location(null,0,0,0);
            for (BlockFace blockFace : BlockUtil.CARDINAL_DIRECTIONS)
            {
                if (placed.getType().equals(placed.getRelative(blockFace).getType())) // bindable chest
                {
                    placed.getRelative(blockFace).getLocation(relativeLoc);
                    Guard guard = this.manager.getGuardAtLocation(relativeLoc);
                    if (guard != null)
                    {
                        if (guard.isOwner(user))
                        {
                            this.manager.extendGuard(guard, event.getBlockPlaced().getLocation());
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
        else if (placed.getType().equals(Material.WOODEN_DOOR) || placed.getType().equals(Material.IRON_DOOR_BLOCK))
        {
            Location loc = placed.getLocation();
            Location relativeLoc = new Location(null,0,0,0);
            for (BlockFace blockFace : BlockUtil.CARDINAL_DIRECTIONS)
            {
                if (placed.getType().equals(placed.getRelative(blockFace).getType()))
                {
                    placed.getRelative(blockFace).getLocation(relativeLoc);
                    Guard guard = this.manager.getGuardAtLocation(relativeLoc);
                    if (guard != null)
                    {
                        if (!(relativeLoc.getBlock().getState().getData() instanceof Door)) return; // door is above
                        Door botRelative = (Door)relativeLoc.getBlock().getState().getData();
                        if (botRelative.isTopHalf()) return; // door is below
                        Door botDoor = (Door)placed.getState().getData();
                        Door topDoor = new Door(placed.getType(), BlockUtil.getTopDoorDataOnPlace(placed.getType(), loc, event.getPlayer()));
                        Door topRelative = (Door)relativeLoc.getBlock().getRelative(BlockFace.UP).getState().getData();
                        if (botDoor.getFacing().equals(botRelative.getFacing()))// Doors are facing the same direction
                        {
                            if (topDoor.getData() != topRelative.getData()) // This is a doubleDoor!
                            {
                                if (guard.isOwner(user))
                                {
                                    this.manager.extendGuard(guard, loc); // bot half
                                    this.manager.extendGuard(guard, loc.clone().add(0,1,0)); // top half
                                    this.manager.extendGuard(guard, loc.clone().add(0,-1,0)); // floor
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

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event)
    {
        Block block = event.getBlock();
        Guard guard = this.manager.getGuardAtLocation(block.getLocation());
        if (guard != null)
        {
            if (guard.hasFlag(REDSTONE))
            {
                event.setNewCurrent(0);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event)
    {
        Location location = new Location(null,0,0,0);
        for (Block block : event.getBlocks())
        {
            Guard guard = this.manager.getGuardAtLocation(block.getLocation(location));
            if (guard != null)
            {
                event.setCancelled(true);
                return;
            }
        }
        Guard guard = this.manager.getGuardAtLocation(location.getBlock().getRelative(event.getDirection()).getLocation(location));
        if (guard != null)
        {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event)
    {
        Guard guard = this.manager.getGuardAtLocation(event.getRetractLocation());
        if (guard != null)
        {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Guard guard = this.manager.getGuardAtLocation(event.getBlock().getLocation());
        if (guard != null)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            guard.handleBlockBreak(event, user);
        }
        else
        {
            Location location = new Location(null,0,0,0);
            for (Block block : BlockUtil.getDetachableBlocks(event.getBlock()))
            {
                guard = this.manager.getGuardAtLocation(block.getLocation(location));
                if (guard != null)
                {
                    User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
                    guard.handleBlockBreak(event, user);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(EntityExplodeEvent event)
    {
        Location location = new Location(null,0,0,0);
        for (Block block : event.blockList())
        {
            Guard guard = this.manager.getGuardAtLocation(block.getLocation(location));
            if (guard != null)
            {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        Location location = new Location(null,0,0,0);
        for (Block block : BlockUtil.getDetachableBlocks(event.getBlock()))
        {
            Guard guard = this.manager.getGuardAtLocation(block.getLocation(location));
            if (guard != null)
            {
                event.setCancelled(true);
                return;
            }
        }
    }
}
