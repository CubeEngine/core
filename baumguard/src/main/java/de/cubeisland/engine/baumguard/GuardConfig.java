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
package de.cubeisland.engine.baumguard;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.core.config.YamlConfiguration;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.Option;

import static de.cubeisland.engine.baumguard.storage.GuardType.PRIVATE;

public class GuardConfig extends YamlConfiguration
{
    //@Option()
    //public int

    public boolean openIronDoorWithClick = false;

    public boolean protectEntityFromEnvironementalDamage = false;

    @Comment("A List of all blocks that can be protected with Baumguard\n" +
                 "use the auto-protect option to automatically create a protection when placing the block\n" +
                 "additionally you can set default flags which will also be automatically applied")
    @Option("protections.blocks")
    public List<BlockGuardConfiguration> blockprotections;

    @Comment("Set this to false if you wish to disable EntityProtection completely")
    @Option("protections.entities-enable")
    public boolean protEntityEnable = true;

    @Comment("A list of all entities that can be protected with Baumguard")
    @Option("protections.entities")
    public List<EntityGuardConfiguration> entityProtections;

    // TODO allow keybooks
    // TODO allow masterkeyBooks
    // limit protection count#
    // TODO globally disable protection from block destruction / left/right-click / Explosion / EntityBreak/Interact etc.
    // TODO protect only when online AND OR only when offline

    // TODO autoclose duration in sec
    @Override
    public void onLoaded(Path loadFrom)
    {
        if (blockprotections == null || blockprotections.isEmpty())
        {
            blockprotections = new ArrayList<>();
            blockprotections.add(new BlockGuardConfiguration(Material.CHEST).autoProtect(PRIVATE));
            blockprotections.add(new BlockGuardConfiguration(Material.TRAPPED_CHEST).autoProtect(PRIVATE));
            blockprotections.add(new BlockGuardConfiguration(Material.FURNACE));
            blockprotections.add(new BlockGuardConfiguration(Material.DISPENSER));
            blockprotections.add(new BlockGuardConfiguration(Material.SIGN_POST));
            blockprotections.add(new BlockGuardConfiguration(Material.WALL_SIGN));
            blockprotections.add(new BlockGuardConfiguration(Material.WOODEN_DOOR));
            blockprotections.add(new BlockGuardConfiguration(Material.IRON_DOOR_BLOCK));
            blockprotections.add(new BlockGuardConfiguration(Material.TRAP_DOOR));
            blockprotections.add(new BlockGuardConfiguration(Material.FENCE_GATE));
        }
        if (protEntityEnable && (entityProtections == null || entityProtections.isEmpty()))
        {
            entityProtections = new ArrayList<>();
            entityProtections.add(new EntityGuardConfiguration(EntityType.HORSE));
        }
    }
}

