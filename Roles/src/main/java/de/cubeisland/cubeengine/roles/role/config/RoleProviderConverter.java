package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleProviderConverter implements Converter<RoleProvider>
{

    @Override
    public Object toObject(RoleProvider object) throws ConversionException
    {
        Map<String, List<Map<String, List<String>>>> result = new HashMap<String, List<Map<String, List<String>>>>();
        List<Map<String, List<String>>> worlds = new ArrayList<Map<String, List<String>>>();
        result.put(object.mainWorld, worlds);
        for (int worldId : object.getWorlds().keys())
        {
            Map<String, List<String>> world = new HashMap<String, List<String>>();
            worlds.add(world);
            List<String> values = new ArrayList<String>();
            world.put(CubeEngine.getCore().getWorldManager().getWorld(worldId).getName(), values);
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
    public RoleProvider fromObject(Object object) throws ConversionException
    {
        Map<String, List<Map<String, List<String>>>> read = (Map<String, List<Map<String, List<String>>>>) object;
        if (read.isEmpty())
        {
            return null;
        }
        String mainworld = read.keySet().iterator().next();
        RoleProvider provider = new RoleProvider(mainworld);
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
                provider.setWorld(worldName, roles, users);
            }
        }
        return provider;
    }
}
