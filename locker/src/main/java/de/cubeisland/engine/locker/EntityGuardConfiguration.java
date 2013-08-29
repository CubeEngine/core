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

import java.util.Map.Entry;

import org.bukkit.entity.EntityType;

import de.cubeisland.engine.locker.storage.ProtectedType;
import de.cubeisland.engine.core.config.node.BooleanNode;
import de.cubeisland.engine.core.config.node.MapNode;
import de.cubeisland.engine.core.config.node.Node;
import de.cubeisland.engine.core.config.node.NullNode;
import de.cubeisland.engine.core.config.node.StringNode;
import de.cubeisland.engine.core.util.convert.ConversionException;
import de.cubeisland.engine.core.util.convert.Converter;
import de.cubeisland.engine.core.util.matcher.Match;

public class EntityGuardConfiguration
{
    protected final ProtectedType protectedType;
    private final EntityType entityType;
    private boolean enable = true;

    public EntityGuardConfiguration(EntityType entityType)
    {
        this.protectedType = ProtectedType.getProtectedType(entityType);
        this.entityType = entityType;
    }

    public String getTitle()
    {
        return entityType.name();
    }

    public boolean isType(EntityType type)
    {
        return this.entityType.equals(type);
    }

    public static class EntityGuardConfigConverter implements Converter<EntityGuardConfiguration>
    {
        @Override
        public Node toNode(EntityGuardConfiguration object) throws ConversionException
        {
            MapNode root = MapNode.emptyMap();
            MapNode config = MapNode.emptyMap();
            if (!object.enable)
            {
                config.setNode(StringNode.of("enable"), BooleanNode.falseNode());
            }
            if (config.isEmpty())
            {
                return StringNode.of(object.getTitle());
            }
            root.setNode(StringNode.of(object.getTitle()), config);
            return root;
        }

        private EntityGuardConfiguration fromString(String s) throws ConversionException
        {
            EntityType entityType;
            try
            {
                entityType = EntityType.valueOf(s);
            }
            catch (IllegalArgumentException ignore)
            {
                try
                {
                    entityType = EntityType.fromId(Integer.valueOf(s));
                }
                catch (NumberFormatException ignoreToo)
                {
                    entityType = Match.entity().any(s);
                }
            }
            if (entityType == null)
            {
                throw new ConversionException(s + " is not a valid EntityType!");
            }
            return new EntityGuardConfiguration(entityType);
        }

        @Override
        public EntityGuardConfiguration fromNode(Node node) throws ConversionException
        {
            if (node instanceof NullNode) return null;
            EntityGuardConfiguration configuration;
            if (node instanceof StringNode)
            {
                configuration = fromString(node.unwrap());
            }
            else
            {
                MapNode root = (MapNode)node;
                if (root.isEmpty()) return null;
                String next = root.getOriginalKey(root.getMappedNodes().keySet().iterator().next());
                MapNode config = (MapNode)root.getExactNode(next);
                configuration = fromString(next);
                for (Entry<String, Node> entry : config.getMappedNodes().entrySet())
                {
                    if (entry.getKey().equals("enable"))
                    {
                        configuration.enable = ((BooleanNode)entry.getValue()).getValue();
                    }
                }
            }
            return configuration;
        }
    }
}
