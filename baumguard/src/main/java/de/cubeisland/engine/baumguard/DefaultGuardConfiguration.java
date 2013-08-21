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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.baumguard.storage.GuardType;
import de.cubeisland.engine.baumguard.storage.ProtectedType;
import de.cubeisland.engine.baumguard.storage.ProtectionFlags;
import de.cubeisland.engine.core.config.node.BooleanNode;
import de.cubeisland.engine.core.config.node.ListNode;
import de.cubeisland.engine.core.config.node.MapNode;
import de.cubeisland.engine.core.config.node.Node;
import de.cubeisland.engine.core.config.node.NullNode;
import de.cubeisland.engine.core.config.node.StringNode;
import de.cubeisland.engine.core.util.convert.ConversionException;
import de.cubeisland.engine.core.util.convert.Converter;

/**
 * Example:
 * B_DOOR:
 *   auto-protect: PRIVATE
 *   flags:
 *      - BLOCK_REDSTONE
 *      - AUTOCLOSE
 */
public class DefaultGuardConfiguration
{
    protected final ProtectedType protectedType;
    protected boolean autoProtect = false;
    protected GuardType autoProtectType = GuardType.PRIVATE; // defaults to private
    protected List<ProtectionFlags> defaultFlags; // TODO validate if possible
    private final Material material;
    private final EntityType entityType;
    private boolean enable = true;

    private DefaultGuardConfiguration(ProtectedType protectedType, Material material, EntityType entityType)
    {
        this.protectedType = protectedType;
        this.material = material;
        this.entityType = entityType;
    }

    public DefaultGuardConfiguration(Material material)
    {
        this(ProtectedType.getProtectedType(material), material, null);
    }

    public DefaultGuardConfiguration(EntityType entityType)
    {
        this(ProtectedType.getProtectedType(entityType), null, entityType);
    }

    public String getTitle()
    {
        if (material == null)
        {
            return "E_" + entityType.name();
        }
        else
        {
            return "B_" + material.name();
        }
    }

    public DefaultGuardConfiguration autoProtect(GuardType type)
    {
        this.autoProtectType = type;
        this.autoProtect = type != null;
        return this;
    }

    public static class DefaultGuardConfigConverter implements Converter<DefaultGuardConfiguration>
    {
        @Override
        public Node toNode(DefaultGuardConfiguration object) throws ConversionException
        {
            MapNode root = MapNode.emptyMap();
            MapNode config = MapNode.emptyMap();
            if (!object.enable)
            {
                config.setNode(StringNode.of("enable"), BooleanNode.falseNode());
            }
            if (object.autoProtect)
            {
                config.setNode(StringNode.of("auto-protect"), StringNode.of(object.autoProtectType.name()));
                if (object.defaultFlags != null && !object.defaultFlags.isEmpty())
                {
                    ListNode flags = ListNode.emptyList();
                    for (ProtectionFlags defaultFlag : object.defaultFlags)
                    {
                        flags.addNode(StringNode.of(defaultFlag.name()));
                    }
                    config.setNode(StringNode.of("default-flags"), flags);
                }
            }
            if (config.isEmpty())
            {
                return StringNode.of(object.getTitle());
            }
            root.setNode(StringNode.of(object.getTitle()), config);
            return root;
        }

        private DefaultGuardConfiguration fromString(String s) throws ConversionException
        {
            if (s.startsWith("B_"))
            {
                s = s.substring(2);
                Material material = Material.getMaterial(s);
                // TODO from id
                return new DefaultGuardConfiguration(material);
            }
            else if (s.startsWith("E_"))
            {
                s = s.substring(2);
                EntityType entityType = EntityType.valueOf(s);
                // TODO from id
                return new DefaultGuardConfiguration(entityType);
            }
            else throw new ConversionException("Illegal GuardType! BlockTypes must start with \"B_\" and EntityTypes with \"E_\"!");
        }

        @Override
        public DefaultGuardConfiguration fromNode(Node node) throws ConversionException
        {
            if (node instanceof NullNode) return null;
            DefaultGuardConfiguration configuration;
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
                    if (entry.getKey().equals("auto-protect"))
                    {
                        configuration.autoProtect = true;
                        configuration.autoProtectType = GuardType.valueOf(entry.getValue().unwrap());
                    }
                    if (entry.getKey().equals("default-flags"))
                    {
                        ListNode list = (ListNode)entry.getValue();
                        configuration.defaultFlags = new ArrayList<>();
                        for (Node listedNode : list.getListedNodes())
                        {
                            configuration.defaultFlags.add(ProtectionFlags.valueOf(listedNode.unwrap()));
                        }
                    }
                }
            }
            return configuration;
        }
    }
}
