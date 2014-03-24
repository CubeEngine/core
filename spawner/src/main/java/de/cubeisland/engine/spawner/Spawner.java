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
package de.cubeisland.engine.spawner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.permission.PermissionManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.formatter.MessageType;

import static org.bukkit.entity.EntityType.*;

/**
 * A module to gather monster spawners with sliktouch and reactivate them using spawneggs
 */
public class Spawner extends Module implements Listener
{
    private ItemStack spawnerItem;
    private Permission eggPerms;
    private Map<EntityType, Permission> perms = new HashMap<>();

    @Override
    public void onEnable()
    {
        this.eggPerms = getBasePermission().childWildcard("egg");
        PermissionManager permMan = this.getCore().getPermissionManager();
        permMan.registerPermission(this, this.eggPerms);
        this.initPerms();
        this.spawnerItem = new ItemStack(Material.MOB_SPAWNER, 1);

        ItemMeta meta = spawnerItem.getItemMeta();
        meta.setLore(Arrays.asList(ChatFormat.parseFormats("&5&7&a&e&r"))); // pssht i am not here
        spawnerItem.setItemMeta(meta);
        this.getCore().getEventManager().registerListener(this, this);
    }

    private void initPerms()
    {
        this.initPerm(CREEPER, SKELETON, SPIDER, ZOMBIE, SLIME, GHAST,
                      PIG_ZOMBIE, ENDERMAN, CAVE_SPIDER, SILVERFISH,
                      BLAZE, MAGMA_CUBE, WITCH, BAT, PIG, SHEEP, COW,
                      CHICKEN, SQUID, WOLF, MUSHROOM_COW, OCELOT,
                      HORSE, VILLAGER);
    }

    private void initPerm(EntityType... types)
    {
        for (EntityType type : types)
        {
            Permission child = eggPerms.child(type.name().toLowerCase().replace("_", "-"));
            this.perms.put(type, child);
            this.getCore().getPermissionManager().registerPermission(this, child);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (event.getPlayer().getItemInHand() != null &&
            event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) &&
            event.getBlock().getType() == Material.MOB_SPAWNER)
        {
            User user = this.getCore().getUserManager().getExactUser(event.getPlayer().getName());

            ItemStack clone = spawnerItem.clone();
            ItemMeta itemMeta = clone.getItemMeta();
            itemMeta.setDisplayName(user.translate(MessageType.NONE, "Inactive Monster Spawner"));
            clone.setItemMeta(itemMeta);
            event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), clone);

            user.sendTranslated(MessageType.POSITIVE, "Dropped inactive Monster Spawner!");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.getBlockPlaced().getType() == Material.MOB_SPAWNER)
        {
            if (event.getPlayer().getItemInHand().isSimilar(spawnerItem))
            {
                CreatureSpawner spawner = (CreatureSpawner)event.getBlock().getState();
                spawner.setSpawnedType(EntityType.SNOWBALL);
                User user = this.getCore().getUserManager().getExactUser(event.getPlayer().getName());
                user.sendTranslated(MessageType.POSITIVE, "Inactive Monster Spawner placed!");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
         && event.getClickedBlock().getType() == Material.MOB_SPAWNER
         && event.getPlayer().getItemInHand().getType() == Material.MONSTER_EGG)
        {
            event.setCancelled(true);
            User user = this.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            CreatureSpawner state = (CreatureSpawner)event.getClickedBlock().getState();
            if (state.getSpawnedType() == EntityType.SNOWBALL)
            {
                this.handleInteract(user, state, (SpawnEgg)event.getPlayer().getItemInHand().getData());
                return;
            }
            user.sendTranslated(MessageType.NEGATIVE, "You can only change inactive Monster Spawner!");
        }
    }

    private void handleInteract(User user, CreatureSpawner spawner, SpawnEgg egg)
    {
        Permission perm = this.perms.get(egg.getSpawnedType());
        if (perm == null && !this.eggPerms.isAuthorized(user))
        {
            user.sendTranslated(MessageType.NEGATIVE, "Invalid SpawnEgg!");
        }
        else if (perm != null && !perm.isAuthorized(user))
        {
            user.sendTranslated(MessageType.NEGATIVE, "You are not allowed to change Monster Spawner to this EntityType!");
        }
        else
        {
            spawner.setSpawnedType(egg.getSpawnedType());
            spawner.update();
            if (user.getGameMode() != GameMode.CREATIVE)
            {
                if (user.getItemInHand().getAmount() - 1 == 0)
                {
                    user.setItemInHand(null);
                }
                else
                {
                    user.getItemInHand().setAmount(user.getItemInHand().getAmount() - 1);
                }
            }
            user.sendTranslated(MessageType.POSITIVE, "Monster Spawner activated!");
        }
    }
}
