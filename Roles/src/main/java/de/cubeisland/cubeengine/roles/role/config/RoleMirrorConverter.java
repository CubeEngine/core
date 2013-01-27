package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.node.*;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.roles.Roles;

import java.util.Map;

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
        for (long worldId : mirror.getWorlds().keys())
        {
            String worldName = CubeEngine.getCore().getWorldManager().getWorld(worldId).getName();
            if (mirror.mainWorld.equals(worldName))
            {
                continue;
            }
            ListNode values = ListNode.emptyList();
            resultMap.setNode(StringNode.of(worldName), values);
            if (mirror.getWorlds().get(worldId).getLeft())
            {
                values.addNode(StringNode.of("roles"));
            }
            if (mirror.getWorlds().get(worldId).getRight())
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
        for (Map.Entry<String,Node> worlds : readMap.getMappedNodes().entrySet())
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
            for (Node inList : world.getListedNodes())
            {
                if (inList.unwrap().equals("roles"))
                {
                    roles =  true;
                }else if (inList.unwrap().equals("users"))
                {
                    users =  true;
                }
            }
            mirror.setWorld(worldName, roles, users);
        }
        return mirror;
    }
}
