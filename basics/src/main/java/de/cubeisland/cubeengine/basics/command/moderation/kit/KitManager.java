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
package de.cubeisland.cubeengine.basics.command.moderation.kit;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import gnu.trove.map.hash.THashMap;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;
import java.util.Set;

public class KitManager
{
   private  final Basics module;

    public KitManager(Basics module) {
        this.module = module;
    }

    private THashMap<String, Kit> kitMap = new THashMap<String, Kit>();
    private THashMap<Kit, KitConfiguration> kitConfigMap = new THashMap<Kit, KitConfiguration>();


    public Kit getKit(String name)
    {
        Set<String> match = Match.string().getBestMatches(name.toLowerCase(Locale.ENGLISH), kitMap.keySet(), 2);
        if (match.isEmpty())
        {
            return null;
        }
        return kitMap.get(match.iterator().next());
    }

    public void saveKit(Kit kit)
    {
        KitConfiguration config = kitConfigMap.get(kit);
        if (config == null)
        {
            config = new KitConfiguration();
            kitConfigMap.put(kit, config);
            kitMap.put(kit.getKitName(), kit);
        }
        kit.applyToConfig(config);
        config.save(new File(module.getFolder(), File.separator + "kits" + File.separator + config.kitName + ".yml"));
    }

    public void loadKit(File file)
    {
        try
        {
            KitConfiguration config = Configuration.load(KitConfiguration.class, file);
            config.kitName = StringUtils.stripFileExtension(file.getName());
            Kit kit = config.getKit(module);
            kitConfigMap.put(kit, config);
            kitMap.put(config.kitName.toLowerCase(Locale.ENGLISH), kit);
            if (kit.getPermission() != null)
            {
                this.module.getCore().getPermissionManager().registerPermission(this.module,kit.getPermission());
            }
        }
        catch (Exception ex)
        {
            module.getLog().log(LogLevel.WARNING, "Could not load the kit configuration!", ex);
        }
    }

    public void loadKits()
    {
        File folder = new File(module.getFolder(), "kits");
        folder.mkdir();
        for (File file : folder.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                if (pathname.getName().endsWith(".yml"))
                {
                    return true;
                }
                return false;
            }
        }))
        {
            loadKit(file);
        }
    }

}
