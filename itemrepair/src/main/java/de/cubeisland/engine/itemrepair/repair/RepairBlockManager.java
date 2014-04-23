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
package de.cubeisland.engine.itemrepair.repair;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.itemrepair.Itemrepair;
import de.cubeisland.engine.itemrepair.material.RepairItemContainer;
import de.cubeisland.engine.itemrepair.repair.blocks.RepairBlock;
import de.cubeisland.engine.itemrepair.repair.blocks.RepairBlock.RepairBlockInventory;
import de.cubeisland.engine.itemrepair.repair.blocks.RepairBlockConfig;
import de.cubeisland.engine.itemrepair.repair.storage.RepairBlockModel;
import de.cubeisland.engine.itemrepair.repair.storage.RepairBlockPersister;
import org.jooq.DSLContext;

import static de.cubeisland.engine.itemrepair.repair.storage.TableRepairBlock.TABLE_REPAIR_BLOCK;

public class RepairBlockManager implements Listener
{
    private final Map<Material, RepairBlock> repairBlocks;
    private final Map<Block, Material> blockMap;
    private final RepairBlockPersister persister;

    protected final Itemrepair module;
    private final RepairItemContainer itemProvider;

    private final DSLContext dsl;

    public RepairBlockManager(Itemrepair module)
    {
        this.dsl = module.getCore().getDB().getDSL();
        this.module = module;
        this.repairBlocks = new EnumMap<>(Material.class);
        this.itemProvider = new RepairItemContainer(module.getConfig().price.baseMaterials);

        for (Entry<String, RepairBlockConfig> entry : module.getConfig().repairBlockConfigs.entrySet())
        {
            RepairBlock repairBlock = new RepairBlock(module,this,entry.getKey(),entry.getValue());
            this.addRepairBlock(repairBlock);
        }
        this.blockMap = new HashMap<>();
        this.persister = new RepairBlockPersister(module);
        this.module.getCore().getEventManager().registerListener(module, this);
        for (World world : this.module.getCore().getWorldManager().getWorlds())
        {
            this.loadRepairBlocks(this.persister.getAll(world));
        }
    }

    private void loadRepairBlocks(Collection<RepairBlockModel> models)
    {
        for (RepairBlockModel model : models)
        {
            Block block = model.getBlock(this.module.getCore().getWorldManager());
            if (block.getType().name().equals(model.getType()))
            {
                if (this.repairBlocks.containsKey(block.getType()))
                {
                    this.blockMap.put(block,block.getType());
                }
                else
                {
                    this.module.getLog().info("Deleting saved RepairBlock that is no longer a RepairBlock at {}:{}:{} in {}",
                                              block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
                    model.delete();
                }
            }
            else
            {
                this.module.getLog().info("Deleting saved RepairBlock that does not correspond to block at {}:{}:{} in {}",
                                          block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
                model.delete();
            }
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event)
    {
        this.loadRepairBlocks(this.persister.getAll(event.getWorld()));
    }

    /**
     * Adds a repair block
     *
     * @param block the repair block
     * @return fluent interface
     */
    public RepairBlockManager addRepairBlock(RepairBlock block)
    {
        this.module.getCore().getPermissionManager().registerPermission(this.module, block.getPermission());
        this.repairBlocks.put(block.getMaterial(), block);
        this.module.getLog().debug("Added a repair block: {} on ID: {}", block.getName(), block.getMaterial());
        return this;
    }

    /**
     * Returns a repair block by it's materials
     *
     * @param material the material
     * @return the repair block
     */
    public RepairBlock getRepairBlock(Material material)
    {
        return this.repairBlocks.get(material);
    }

    /**
     * Returns the attached repair block of a block
     *
     * @param block the block
     * @return the attached repair block
     */
    public RepairBlock getRepairBlock(Block block)
    {
        Material repairBlockMaterial = this.blockMap.get(block);
        if (repairBlockMaterial != null)
        {
            return this.getRepairBlock(repairBlockMaterial);
        }
        return null;
    }

    /**
     * Checks whether the given block is a repair block
     *
     * @param block the block to check
     * @return true if it is one
     */
    public boolean isRepairBlock(Block block)
    {
        return this.blockMap.containsKey(block);
    }

    /**
     * Attaches a repair block to a block
     *
     * @param block the block to attach to
     * @return true on success
     */
    public boolean attachRepairBlock(Block block)
    {
        Material material = block.getType();
        if (!this.isRepairBlock(block))
        {
            if (this.repairBlocks.containsKey(material))
            {
                this.blockMap.put(block, material);
                this.persister.storeBlock(block, this.dsl.newRecord(TABLE_REPAIR_BLOCK).newRepairBlock(block, this.module.getCore().getWorldManager()));
                return true;
            }
        }
        return false;
    }

    /**
     * Detaches a repair block from a block
     *
     * @param block the block to detach from
     * @return true on success
     */
    public boolean detachRepairBlock(Block block)
    {
        if (this.isRepairBlock(block))
        {
            this.blockMap.remove(block);
            this.persister.deleteByBlock(block);
            return true;
        }
        return false;
    }

    public void removePlayer(final Player player)
    {
        if (player == null)
        {
            return;
        }
        RepairBlockInventory inventory;
        for (RepairBlock repairBlock : this.repairBlocks.values())
        {
            inventory = repairBlock.removeInventory(player);
            if (inventory != null)
            {
                final World world = player.getWorld();
                final Location loc = player.getLocation();
                for (ItemStack stack : inventory.inventory)
                {
                    if (stack != null && stack.getType() != Material.AIR)
                    {
                        world.dropItemNaturally(loc, stack);
                    }
                }
            }
        }
    }

    public RepairItemContainer getItemProvider()
    {
        return itemProvider;
    }
}
