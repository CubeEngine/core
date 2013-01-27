package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.node.ListNode;
import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.roles.Roles;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RoleMirrorConverter implements Converter<RoleMirror>
{
    private Roles module;

    public RoleMirrorConverter(Roles module)
    {
        this.module = module;
    }

    @Override
    public Node toNode(RoleMirror object) throws ConversionException
    {
        Map<String, Map<String, List<String>>> result = new LinkedHashMap<String, Map<String, List<String>>>();
        Map<String, List<String>> worlds = new LinkedHashMap<String, List<String>>();
        result.put(object.mainWorld, worlds);
        for (long worldId : object.getWorlds().keys())
        {
            List<String> values = new ArrayList<String>();
            String worldName = CubeEngine.getCore().getWorldManager().getWorld(worldId).getName();
            if (object.mainWorld.equals(worldName))
            {
                continue;
            }
            worlds.put(worldName, values);
            if (object.getWorlds().get(worldId).getLeft())
            {
                values.add("roles");
            }
            if (object.getWorlds().get(worldId).getRight())
            {
                values.add("users");
            }
        }
        return Convert.wrapIntoNode(result); //TODO check if this works
    }

    @Override
    @SuppressWarnings("unchecked")
    public RoleMirror fromNode(Node node) throws ConversionException
    {
        //TODO rework this
        MapNode read = (MapNode)node;
        //Map<String, List<Map<String, List<String>>>> read = (Map<String, List<Map<String, List<String>>>>)object;
        if (read.isEmpty())
        {
            return null;
        }
        String mainworld = read.getMappedNodes().keySet().iterator().next();
        RoleMirror mirror = new RoleMirror(this.module, mainworld);
        MapNode worldsNode = (MapNode)read.getMappedNodes().get(mainworld);
        for (Map.Entry<String,Node> worlds : worldsNode.getMappedNodes().entrySet())
        {
            MapNode world = ((MapNode)worlds.getValue());
            if (world.isEmpty())
            {
                continue;
            }
            String worldName = worlds.getKey();
            ListNode list = (ListNode)worlds.getValue();
            boolean roles = false;
            boolean users = false;
            for (Node inList : list.getListedNodes())
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
