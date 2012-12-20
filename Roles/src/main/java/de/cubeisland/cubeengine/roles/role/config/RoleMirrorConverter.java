package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.roles.Roles;
import java.util.ArrayList;
import java.util.HashMap;
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
    public Object toObject(RoleMirror object) throws ConversionException
    {
        Map<String, List<Map<String, List<String>>>> result = new HashMap<String, List<Map<String, List<String>>>>();
        List<Map<String, List<String>>> worlds = new ArrayList<Map<String, List<String>>>();
        result.put(object.mainWorld, worlds);
        for (long worldId : object.getWorlds().keys())
        {
            List<String> values = new ArrayList<String>();
            String worldName = CubeEngine.getCore().getWorldManager().getWorld(worldId).getName();
            if (object.mainWorld.equals(worldName))
            {
                continue;
            }
            Map<String, List<String>> world = new HashMap<String, List<String>>();
            worlds.add(world);
            world.put(worldName, values);
            if (object.getWorlds().get(worldId).getLeft())
            {
                values.add("roles");
            }
            if (object.getWorlds().get(worldId).getRight())
            {
                values.add("users");
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RoleMirror fromObject(Object object) throws ConversionException
    {
        Map<String, List<Map<String, List<String>>>> read = (Map<String, List<Map<String, List<String>>>>) object;
        if (read.isEmpty())
        {
            return null;
        }
        String mainworld = read.keySet().iterator().next();
        RoleMirror mirror = new RoleMirror(this.module, mainworld);
        if (read.get(mainworld) != null)
        {
            for (Map<String, List<String>> world : read.get(mainworld))
            {
                if (world.isEmpty())
                {
                    continue;
                }
                String worldName = world.keySet().iterator().next();
                if (world.get(worldName) == null)
                {
                    continue;
                }
                boolean roles = world.get(worldName).contains("roles");
                boolean users = world.get(worldName).contains("users");
                mirror.setWorld(worldName, roles, users);
            }
        }
        return mirror;
    }
}
