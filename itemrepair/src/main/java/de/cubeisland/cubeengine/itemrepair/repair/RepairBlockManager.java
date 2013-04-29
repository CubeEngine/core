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
package de.cubeisland.cubeengine.itemrepair.repair;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.itemrepair.Itemrepair;
import de.cubeisland.cubeengine.itemrepair.material.RepairItemContainer;
import de.cubeisland.cubeengine.itemrepair.repair.blocks.RepairBlock;
import de.cubeisland.cubeengine.itemrepair.repair.blocks.RepairBlockConfig;
import de.cubeisland.cubeengine.itemrepair.repair.storage.RepairBlockModel;
import de.cubeisland.cubeengine.itemrepair.repair.storage.RepairBlockPersister;

public class RepairBlockManager
{
    private Map<Material, RepairBlock> repairBlocks;
    private Map<Block, Material> blockMap;
    private RepairBlockPersister persister;
    private final Permission parentPermission;

    protected final Itemrepair module;
    private RepairItemContainer itemProvider;

    public RepairBlockManager(Itemrepair module)
    {
        this.module = module;
        this.repairBlocks = new EnumMap<Material, RepairBlock>(Material.class);
        this.itemProvider = new RepairItemContainer(module.getConfig().baseMaterials);

        for (Entry<String, RepairBlockConfig> entry : module.getConfig().repairBlockConfigs.entrySet())
        {
            RepairBlock repairBlock = new RepairBlock(module,this,entry.getKey(),entry.getValue());
            this.repairBlocks.put(repairBlock.getMaterial(),repairBlock);
        }

        this.blockMap = new HashMap<Block, Material>();

        this.persister = new RepairBlockPersister(module);


        this.loadBlocks();

        this.parentPermission =  this.module.getBasePermission().createChild("allblocks");
        // TODO register perm
    }

    /**
     * Loads the blocks from a persister
     *
     * @return fluent interface
     */
    public RepairBlockManager loadBlocks()
    {
        for (RepairBlockModel model : this.persister.getAll())
        {
            Block block = model.getBlock(this.module.getCore().getWorldManager());
            if (block.getType().name().equals(model.type))
            {
                if (this.repairBlocks.containsKey(block.getType()))
                {
                    this.blockMap.put(block,block.getType());
                }
                else
                {
                    this.module.getLog().warning("Deleting saved RepairBlock that is no longer a RepairBlock at " +
                             + block.getX() + ":" + block.getY() + ":" + block.getZ() + " in " + block.getWorld().getName());
                    // TODO delete
                }
            }
            else
            {
                this.module.getLog().warning("Deleting saved RepairBlock that does not correspond to block at " +
                             + block.getX() + ":" + block.getY() + ":" + block.getZ() + " in " + block.getWorld().getName());
                // TODO delete
            }
        }
        return this;
    }

    /**
     * Adds a repair block
     *
     * @param block the repair block
     * @return fluent interface
     */
    public RepairBlockManager addRepairBlock(RepairBlock block)
    {
        this.parentPermission.attach(block.getPermission());
        // TODO register perm
        this.repairBlocks.put(block.getMaterial(), block);
        this.module.getLog().log(LogLevel.DEBUG, "Added a repair block: " + block.getName() + " on ID: " + block
            .getMaterial());
        return this;
    }

    /**
     * Returns a repair block by its material's ID
     *
     * @param materialId the material ID
     * @return the repair block
     */
    public RepairBlock getRepairBlock(int materialId)
    {
        return this.getRepairBlock(Material.getMaterial(materialId));
    }

    /**
     * Returns a repair block by its material's name
     *
     * @param materialName the name of the material
     * @return the repair block
     */
    public RepairBlock getRepairBlock(String materialName)
    {
        return this.getRepairBlock(Material.getMaterial(materialName));
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
                this.persister.store(new RepairBlockModel(block, this.module.getCore().getWorldManager()));
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
        Inventory inventory;
        for (RepairBlock repairBlock : this.repairBlocks.values())
        {
            inventory = repairBlock.removeInventory(player);
            if (inventory != null)
            {
                final World world = player.getWorld();
                final Location loc = player.getLocation();
                for (ItemStack stack : inventory)
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
