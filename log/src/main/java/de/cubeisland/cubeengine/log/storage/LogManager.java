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
package de.cubeisland.cubeengine.log.storage;

import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import de.cubeisland.cubeengine.core.bukkit.EventManager;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.storage.database.querybuilder.SelectBuilder;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LoggingConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LogManager
{
    public final ObjectMapper mapper;
    private final Log module;

    private LoggingConfiguration globalConfig;
    private Map<World, LoggingConfiguration> worldConfigs = new HashMap<World, LoggingConfiguration>();

    private final QueryManager queryManager;

    public LogManager(Log module)
    {
        this.module = module;
        this.mapper = new ObjectMapper();
        File file = new File(module.getFolder(), "worlds");
        file.mkdir();
        this.globalConfig = Configuration.load(LoggingConfiguration.class, new File(module.getFolder(), "globalconfig.yml"));
        for (World world : Bukkit.getServer().getWorlds())
        {
            file = new File(module.getFolder(), "worlds" + File.separator + world.getName());
            file.mkdir();
            this.worldConfigs.put(world, (LoggingConfiguration)globalConfig.loadChild(new File(file, "config.yml")));
        }


        final EventManager em = module.getCore().getEventManager();

        this.queryManager = new QueryManager(module);


    }

    private void buildWorldAndLocation(SelectBuilder builder, World world, Location loc1, Location loc2)
    {
        if (world != null)
        {
            builder.field("world_id").isEqual().value(this.module.getCore().getWorldManager().getWorldId(world));
            if (loc1 != null)
            {
                if (loc2 == null) // single location
                {
                    builder.and().field("x").isEqual().value(loc1.getBlockX())
                            .and().field("y").isEqual().value(loc1.getBlockY())
                            .and().field("z").isEqual().value(loc1.getBlockZ());
                }
                else
                // range of locations
                {
                    builder.and().field("x").between(loc1.getBlockX(), loc2.getBlockX())
                            .and().field("y").between(loc1.getBlockY(), loc2.getBlockY())
                            .and().field("z").between(loc1.getBlockZ(), loc2.getBlockZ());
                }
            }
            builder.and();
        }
    }

    private void buildDates(SelectBuilder builder, Timestamp fromDate, Timestamp toDate)
    {
        builder.beginSub().field("date").between(fromDate, toDate).endSub();
    }

    public void disable()
    {
        this.queryManager.disable();
    }

    public int getQueueSize() {
        return this.queryManager.queuedLogs.size();
    }

    public void fillLookupAndShow(Lookup lookup, User user)
    {
        this.queryManager.prepareLookupQuery(lookup.clone(), user);
    }

    public LoggingConfiguration getConfig(World world) {
        if (world == null) return globalConfig;
        return this.worldConfigs.get(world);
    }

    public void queueLog(QueuedLog log)
    {
        this.queryManager.queueLog(log);
    }

    public QueryManager getQueryManager()
    {
        return queryManager;
    }
}