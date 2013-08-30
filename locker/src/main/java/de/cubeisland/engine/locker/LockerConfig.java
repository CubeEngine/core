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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.core.config.YamlConfiguration;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.Option;

import static de.cubeisland.engine.locker.storage.LockType.PRIVATE;

public class LockerConfig extends YamlConfiguration
{
    //@Option()
    //public int

    @Option("settings.open-iron-door-with-click")
    public boolean openIronDoorWithClick = false;

    @Comment("If set to true protected living entities will receive no damage from environment in addition to damage done by players")
    @Option("settings.protect.living-from-environment")
    public boolean protectEntityFromEnvironementalDamage = true;

    @Comment("If set to true protected vehicles will not break when receiving damage from environment in addition to the player-protection")
    @Option("settings.protect.vehicle-from-environment")
    public boolean protectVehicleFromEnvironmental = true;

    @Option("settings.protect.blocks-from-water-and-lava")
    public boolean protectBlocksFromWaterLava = true;

    @Comment("If set to true protected doors will auto-close after the configured time")
    @Option("settings.auto-close.enable")
    public boolean autoCloseEnable = true;

    @Comment("Doors will auto-close after this set amount of seconds.")
    @Option("settings.auto-close.time")
    public int autoCloseSeconds = 3;

    @Option("settings.key-books.allow-single")
    public boolean allowKeyBooks = true;
    // TODO allow masterKeyBooks

    @Comment("A List of all blocks that can be protected with Locker\n" +
                 "use the auto-protect option to automatically create a protection when placing the block\n" +
                 "additionally you can set default flags which will also be automatically applied")
    @Option("protections.blocks")
    public List<BlockLockerConfiguration> blockprotections;

    @Comment("Set this to false if you wish to disable EntityProtection completely")
    @Option("protections.entities-enable")
    public boolean protEntityEnable = true;

    @Comment("A list of all entities that can be protected with Locker")
    @Option("protections.entities")
    public List<EntityLockerConfiguration> entityProtections;

    // limit protection count#
    // TODO globally disable protection from block destruction / left/right-click / Explosion / EntityBreak/Interact etc.
    // TODO protect only when online AND OR only when offline

    @Override
    public void onLoaded(Path loadFrom)
    {
        if (blockprotections == null || blockprotections.isEmpty())
        {
            blockprotections = new ArrayList<>();
            blockprotections.add(new BlockLockerConfiguration(Material.CHEST).autoProtect(PRIVATE));
            blockprotections.add(new BlockLockerConfiguration(Material.TRAPPED_CHEST).autoProtect(PRIVATE));
            blockprotections.add(new BlockLockerConfiguration(Material.FURNACE));
            blockprotections.add(new BlockLockerConfiguration(Material.DISPENSER));
            blockprotections.add(new BlockLockerConfiguration(Material.SIGN_POST));
            blockprotections.add(new BlockLockerConfiguration(Material.WALL_SIGN));
            blockprotections.add(new BlockLockerConfiguration(Material.WOODEN_DOOR));
            blockprotections.add(new BlockLockerConfiguration(Material.IRON_DOOR_BLOCK));
            blockprotections.add(new BlockLockerConfiguration(Material.TRAP_DOOR));
            blockprotections.add(new BlockLockerConfiguration(Material.FENCE_GATE));
        }
        if (protEntityEnable && (entityProtections == null || entityProtections.isEmpty()))
        {
            entityProtections = new ArrayList<>();
            entityProtections.add(new EntityLockerConfiguration(EntityType.HORSE));
        }
    }
}

