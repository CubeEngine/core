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
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.basics.BasicsPerm;

public class EntityRemoval
{
    private Permission perm;
    public final Class<?>[] interfaces;

    private EntityRemoval(Permission perm, Class<?>... interfaces)
    {
        this.perm = perm;
        this.interfaces = interfaces;
    }

    public boolean doesMatch(Entity entity)
    {
        if (interfaces.length == 0) return this.extra(entity);
        for (Class<?> anInterface : interfaces)
        {
            if (entity.getClass().isAssignableFrom(anInterface))
            {
                return this.extra(entity);
            }
        }
        return false;
    }

    public boolean isAllowed(Permissible permissible)
    {
        return this.perm.isAuthorized(permissible);
    }

    /**
     * Override this to check extra information
     *
     * @param entity
     * @return
     */
    public boolean extra(Entity entity)
    {
        return true;
    }

    static final Map<String, EntityRemoval> GROUPED_ENTITY_REMOVAL = new HashMap<String, EntityRemoval>()
    {
        {
            this.put("pet", new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_PET, Tameable.class)
            {
                @Override
                public boolean extra(Entity entity)
                {
                    return ((Tameable)entity).isTamed();
                }
            });
            this.put("golem", new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_GOLEM, Golem.class));
            this.put("animal", new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_ANIMAL, Animals.class)
            {
                @Override
                public boolean extra(Entity entity)
                {
                    return !(entity instanceof Tameable && ((Tameable)entity).isTamed());
                }
            });
            this.put("other", new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_OTHER, Ambient.class, WaterMob.class));
            this.put("boss", new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_BOSS, EnderDragon.class, Wither.class));
            this.put("monster", new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER, Monster.class, Slime.class, Ghast.class));
        }
    };

    static final Map<EntityType, EntityRemoval> DIRECT_ENTITY_REMOVAL = new HashMap<EntityType, EntityRemoval>()
    {
        {
            this.put(EntityType.CREEPER, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.SKELETON, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.SPIDER, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.GIANT, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_BOSS));
            this.put(EntityType.ZOMBIE, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.SLIME, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.GHAST, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.PIG_ZOMBIE, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.ENDERMAN, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.CAVE_SPIDER, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.SILVERFISH, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.BLAZE, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.MAGMA_CUBE, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.ENDER_DRAGON, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_BOSS));
            this.put(EntityType.WITHER, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_BOSS));
            this.put(EntityType.BAT, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_OTHER));
            this.put(EntityType.WITCH, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_MONSTER));
            this.put(EntityType.PIG, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_ANIMAL));
            this.put(EntityType.SHEEP, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_ANIMAL));
            this.put(EntityType.COW, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_ANIMAL));
            this.put(EntityType.CHICKEN, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_ANIMAL));
            this.put(EntityType.SQUID, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_OTHER));
            this.put(EntityType.WOLF, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_ANIMAL)
            {
                @Override
                public boolean extra(Entity entity)
                {
                    return !((Wolf)entity).isTamed();
                }
            });
            this.put(EntityType.MUSHROOM_COW, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_ANIMAL));
            this.put(EntityType.SNOWMAN, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_GOLEM));
            this.put(EntityType.OCELOT, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_ANIMAL)
            {
                @Override
                public boolean extra(Entity entity)
                {
                    return !((Ocelot)entity).isTamed();
                }
            });
            this.put(EntityType.IRON_GOLEM, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_GOLEM));
            this.put(EntityType.HORSE, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_ANIMAL)
            {
                @Override
                public boolean extra(Entity entity)
                {
                    return !((Horse)entity).isTamed();
                }
            });
            this.put(EntityType.VILLAGER, new EntityRemoval(BasicsPerm.COMMAND_BUTCHER_FLAG_NPC));
        }
    };
}
