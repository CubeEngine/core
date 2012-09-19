package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.filesystem.Resource;
import gnu.trove.map.hash.TShortObjectHashMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Anselm Brehme
 */
public class EntityMatcher
{
    private static EntityMatcher instance = null;

    public EntityMatcher()
    {
        TShortObjectHashMap<List<String>> entityList = this.readEntities();
        for (short id : entityList.keys())
        {
            try
            {
                EntityType.fromId(id).registerName(entityList.get(id));
            }
            catch (NullPointerException e)
            {
                CubeEngine.getLogger().log(Level.WARNING, "Unknown Entity ID: " + id + " " + entityList.get(id).get(0));
            }
        }
    }

    public static EntityMatcher get()
    {
        if (instance == null)
        {
            instance = new EntityMatcher();
        }
        return instance;
    }

    public EntityType matchEntity(String name)
    {
        Map<String, EntityType> entities = EntityType.getNameSets();
        String s = name.toLowerCase(Locale.ENGLISH);
        EntityType entity = entities.get(s);
        try
        {
            short entityId = Short.parseShort(s);
            return EntityType.fromId(entityId);
        }
        catch (NumberFormatException e)
        {
        }
        if (entity == null)
        {
            if (s.length() < 4)
            {
                return null;
            }
            String t_key = StringUtils.matchString(name, entities.keySet());
            if (t_key != null)
            {
                return entities.get(t_key);
            }
        }
        return entity;
    }

    public EntityType matchMob(String s)
    {
        EntityType type = this.matchEntity(s);
        if (type.isAlive())
        {
            return type;
        }
        return null;
    }

    public EntityType matchSpawnEggMobs(String s)
    {
        EntityType type = this.matchMob(s);
        if (type.canBeSpawnedBySpawnEgg())
        {
            return type;
        }
        return null;
    }

    public EntityType matchMonster(String s)
    {
        EntityType type = this.matchEntity(s);
        if (type.isMonster())
        {
            return type;
        }
        return null;
    }

    public EntityType matchFriendlyMob(String s)
    {
        EntityType type = this.matchEntity(s);
        if (type.isFriendly())
        {
            return type;
        }
        return null;
    }

    public EntityType matchProjectile(String s)
    {
        EntityType type = this.matchEntity(s);
        if (type.isProjectile())
        {
            return type;
        }
        return null;
    }

    private TShortObjectHashMap<List<String>> readEntities()
    {
        try
        {
            File file = new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ENTITIES.getTarget());
            List<String> input = FileUtil.getFileAsStringList(file);

            TShortObjectHashMap<List<String>> entityList = new TShortObjectHashMap<List<String>>();
            ArrayList<String> names = new ArrayList<String>();
            for (String line : input)
            {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                {
                    continue;
                }
                if (line.endsWith(":"))
                {
                    short id = Short.parseShort(line.substring(0, line.length() - 1));
                    names = new ArrayList<String>();
                    entityList.put(id, names);
                }
                else
                {
                    names.add(line);
                }
            }
            // update
            boolean updated = false;
            Resource resource = CubeEngine.getFileManager().getSourceOf(file);
            String source = resource.getSource();
            if (!source.startsWith("/"))
            {
                source = "/" + source;
            }
            List<String> jarinput = FileUtil.getReaderAsStringList(new InputStreamReader(resource.getClass().getResourceAsStream(source)));
            for (String line : jarinput)
            {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                {
                    continue;
                }
                if (line.endsWith(":"))
                {

                    short id = Short.parseShort(line.substring(0, line.length() - 1));
                    names = new ArrayList<String>();
                    if (entityList.get(id) == null)
                    {
                        entityList.put(id, names);
                        updated = true;
                    }
                }
                else
                {
                    names.add(line);
                }
            }
            if (updated)
            {
                CubeEngine.getLogger().log(Level.FINER, "Updated entities.txt");
                StringBuilder sb = new StringBuilder();
                for (short key : entityList.keys())
                {
                    sb.append(key).append(":").append("\n");
                    List<String> entitynames = entityList.get(key);
                    for (String entityname : entitynames)
                    {
                        sb.append("    ").append(entityname).append("\n");
                    }
                }
                FileUtil.saveFile(sb.toString(), file);
            }
            return entityList;
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalStateException("enchantments.txt is corrupted!", ex);
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Error while reading enchantments.txt", ex);
        }
    }
}