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
package de.cubeisland.engine.basics.command.moderation;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Monster;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;

import de.cubeisland.engine.basics.Basics;

public class EntityRemovals
{
    public EntityRemovals(Basics module)
    {
        GROUPED_ENTITY_REMOVAL.put("pet", new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_PET, Tameable.class)
        {
            @Override
            public boolean extra(Entity entity)
            {
                return ((Tameable)entity).isTamed();
            }
        });
        GROUPED_ENTITY_REMOVAL.put("golem", new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_GOLEM, Golem.class));
        GROUPED_ENTITY_REMOVAL.put("animal", new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_ANIMAL, Animals.class)
        {
            @Override
            public boolean extra(Entity entity)
            {
                return !(entity instanceof Tameable && ((Tameable)entity).isTamed());
            }
        });
        GROUPED_ENTITY_REMOVAL.put("other", new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_OTHER, Ambient.class, WaterMob.class));
        GROUPED_ENTITY_REMOVAL.put("boss", new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_BOSS, EnderDragon.class, Wither.class));
        GROUPED_ENTITY_REMOVAL.put("monster", new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, Monster.class, Slime.class, Ghast.class));


        DIRECT_ENTITY_REMOVAL.put(EntityType.CREEPER, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, Creeper.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.SKELETON, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, Skeleton.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.SPIDER, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, Spider.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.GIANT, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_BOSS, Giant.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.ZOMBIE, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, Zombie.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.SLIME, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, Slime.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.GHAST, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, Ghast.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.PIG_ZOMBIE, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, PigZombie.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.ENDERMAN, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, Enderman.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.CAVE_SPIDER, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, CaveSpider.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.SILVERFISH, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, Silverfish.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.BLAZE, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, Blaze.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.MAGMA_CUBE, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, MagmaCube.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.ENDER_DRAGON, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_BOSS, EnderDragon.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.WITHER, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_BOSS, Wither.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.BAT, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_OTHER, Bat.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.WITCH, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_MONSTER, Witch.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.PIG, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_ANIMAL, Pig.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.SHEEP, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_ANIMAL, Sheep.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.COW, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_ANIMAL, Cow.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.CHICKEN, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_ANIMAL, Chicken.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.SQUID, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_OTHER, Squid.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.WOLF, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_ANIMAL, Wolf.class)
        {
            @Override
            public boolean extra(Entity entity)
            {
                return !((Wolf)entity).isTamed();
            }
        });
        DIRECT_ENTITY_REMOVAL.put(EntityType.MUSHROOM_COW, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_ANIMAL, MushroomCow.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.SNOWMAN, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_GOLEM, Snowman.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.OCELOT, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_ANIMAL, Ocelot.class)
        {
            @Override
            public boolean extra(Entity entity)
            {
                return !((Ocelot)entity).isTamed();
            }
        });
        DIRECT_ENTITY_REMOVAL.put(EntityType.IRON_GOLEM, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_GOLEM, IronGolem.class));
        DIRECT_ENTITY_REMOVAL.put(EntityType.HORSE, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_ANIMAL, Horse.class)
        {
            @Override
            public boolean extra(Entity entity)
            {
                return !((Horse)entity).isTamed();
            }
        });
        DIRECT_ENTITY_REMOVAL.put(EntityType.VILLAGER, new EntityRemoval(module.perms().COMMAND_BUTCHER_FLAG_NPC, Villager.class));
    }

    final Map<String, EntityRemoval> GROUPED_ENTITY_REMOVAL = new HashMap<>();
    final Map<EntityType, EntityRemoval> DIRECT_ENTITY_REMOVAL = new HashMap<>();
}
