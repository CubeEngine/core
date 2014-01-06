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
package de.cubeisland.engine.roles.config;

import java.util.Map;
import java.util.Map.Entry;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.configuration.convert.Converter;
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.configuration.node.ListNode;
import de.cubeisland.engine.configuration.node.MapNode;
import de.cubeisland.engine.configuration.node.Node;
import de.cubeisland.engine.configuration.node.NullNode;
import de.cubeisland.engine.configuration.node.StringNode;
import de.cubeisland.engine.core.util.Triplet;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.roles.Roles;

public class MirrorConfigConverter implements Converter<MirrorConfig>
{
    private Roles module;

    public MirrorConfigConverter(Roles module)
    {
        this.module = module;
    }

    @Override
    public Node toNode(MirrorConfig mirror, ConverterManager manager) throws ConversionException
    {
        MapNode resultMap = MapNode.emptyMap();
        resultMap.setNode(new StringNode(mirror.mainWorld.getName()), NullNode.emptyNode());
        for (Entry<ConfigWorld, Triplet<Boolean, Boolean, Boolean>> entry : mirror.mirrors.entrySet())
        {
            if (!entry.getKey().getName().equals(mirror.mainWorld.getName()))
            {
                ListNode values = ListNode.emptyList();
                resultMap.setNode(StringNode.of(entry.getKey().getName()), values);
                if (entry.getValue().getFirst())
                {
                    values.addNode(StringNode.of("roles"));
                }
                if (entry.getValue().getSecond())
                {
                    values.addNode(StringNode.of("assigned"));
                }
                if (entry.getValue().getThird())
                {
                    values.addNode(StringNode.of("users"));
                }
            }
        }
        return resultMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MirrorConfig fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        MapNode readMap = (MapNode)node;
        if (readMap.isEmpty())
        {
            return null;
        }

        MirrorConfig mirror = new MirrorConfig(module, new ConfigWorld(module.getCore().getWorldManager(),
                                       readMap.getMappedNodes().keySet().iterator().next()));
        for (Map.Entry<String, Node> worlds : readMap.getMappedNodes().entrySet())
        {
            if (worlds.getKey().equals(mirror.mainWorld.getName()))
            {
                continue;
            }
            ListNode worldList = ((ListNode)worlds.getValue());
            if (worldList.isEmpty())
            {
                continue;
            }
            String worldName = worlds.getKey();
            boolean roles = false;
            boolean users = false;
            boolean assigned = false;
            for (Node inList : worldList.getListedNodes())
            {
                if (inList.asText().equals("roles"))
                {
                    roles = true;
                }
                else if (inList.asText().equals("users"))
                {
                    users = true;
                }
                else if (inList.asText().equals("assigned"))
                {
                    assigned = true;
                }
            }
            ConfigWorld world = new ConfigWorld(module.getCore().getWorldManager(), worldName);
            mirror.setWorld(world, new Triplet<>(roles, assigned, users));
        }
        return mirror;
    }
}
