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
package de.cubeisland.cubeengine.guests.prevention;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class PreventionConfiguration extends YamlConfiguration
{
    private final File file;
    private static final String FILE_EXTENTION = ".yml";

    private PreventionConfiguration(File file)
    {
        this.file = file;
    }

    public static PreventionConfiguration get(File dir, Prevention prevention)
    {
        return get(dir, prevention, true);
    }

    public static PreventionConfiguration get(File dir, Prevention prevention, boolean load)
    {
        if (dir == null || prevention == null)
        {
            throw new IllegalArgumentException("dir and prevention both must not be null!");
        }
        dir.mkdirs();
        if (!dir.isDirectory())
        {
            throw new IllegalArgumentException("dir must represent a directory!");
        }

        final PreventionConfiguration config = new PreventionConfiguration(new File(dir, prevention.getName() + FILE_EXTENTION));
        config.options().header(prevention.getConfigHeader());
        if (load)
        {
            try
            {
                config.load();
            }
            catch (FileNotFoundException e)
            {}
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
            catch (InvalidConfigurationException e)
            {
                e.printStackTrace(System.err);
            }
        }
        config.options().copyDefaults(true);
        config.setDefaults(prevention.getDefaultConfig());
        return config;
    }

    public void load() throws FileNotFoundException, IOException, InvalidConfigurationException
    {
        this.load(this.file);
    }

    public void save() throws IOException
    {
        this.save(this.file);
    }

    public boolean safeSave()
    {
        try
        {
            this.save();
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);
        }
        return false;
    }
}
