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

import de.cubeisland.engine.core.config.node.ListNode;
import de.cubeisland.engine.core.config.node.MapNode;
import de.cubeisland.engine.core.config.node.Node;
import de.cubeisland.engine.core.config.node.NullNode;
import de.cubeisland.engine.core.config.node.StringNode;
import de.cubeisland.engine.core.util.convert.ConversionException;
import de.cubeisland.engine.core.util.convert.Converter;
import de.cubeisland.engine.roles.Roles;

public class RoleMirrorConverter implements Converter<RoleMirror>
{
    private Roles module;

    public RoleMirrorConverter(Roles module)
    {
        this.module = module;
    }

    @Override
    public Node toNode(RoleMirror mirror) throws ConversionException
    {
        MapNode resultMap = MapNode.emptyMap();
        resultMap.setNode(new StringNode(mirror.mainWorld), NullNode.emptyNode());
        for (long worldId : mirror.getWorldMirrors().keys())
        {
            String worldName = this.module.getCore().getWorldManager().getWorld(worldId).getName();
            if (mirror.mainWorld.equals(worldName))
            {
                continue;
            }
            ListNode values = ListNode.emptyList();
            resultMap.setNode(StringNode.of(worldName), values);
            if (mirror.getWorldMirrors().get(worldId).getFirst())
            {
                values.addNode(StringNode.of("roles"));
            }
            if (mirror.getWorldMirrors().get(worldId).getSecond())
            {
                values.addNode(StringNode.of("assigned"));
            }
            if (mirror.getWorldMirrors().get(worldId).getThird())
            {
                values.addNode(StringNode.of("users"));
            }
        }
        return resultMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RoleMirror fromNode(Node node) throws ConversionException
    {
        MapNode readMap = (MapNode)node;
        if (readMap.isEmpty())
        {
            return null;
        }
        String mainworld = readMap.getMappedNodes().keySet().iterator().next();
        RoleMirror mirror = new RoleMirror(this.module, mainworld);
        for (Map.Entry<String, Node> worlds : readMap.getMappedNodes().entrySet())
        {
            if (worlds.getKey().equals(mainworld))
            {
                continue;
            }
            ListNode world = ((ListNode)worlds.getValue());
            if (world.isEmpty())
            {
                continue;
            }
            String worldName = worlds.getKey();
            boolean roles = false;
            boolean users = false;
            boolean assigned = false;
            for (Node inList : world.getListedNodes())
            {
                if (inList.unwrap().equals("roles"))
                {
                    roles = true;
                }
                else if (inList.unwrap().equals("users"))
                {
                    users = true;
                }
                else if (inList.unwrap().equals("assigned"))
                {
                    assigned = true;
                }
            }
            mirror.setWorld(worldName, roles, assigned, users);
        }
        return mirror;
    }
}
