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
package de.cubeisland.engine.stats;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.core.logging.Log;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.stats.storage.StatsDataModel;
import de.cubeisland.engine.stats.storage.StatsModel;
import de.cubeisland.engine.stats.storage.TableStats;
import de.cubeisland.engine.stats.storage.TableStatsData;
import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import static de.cubeisland.engine.stats.storage.TableStats.TABLE_STATS;
import static de.cubeisland.engine.stats.storage.TableStatsData.TABLE_STATSDATA;

public class StatsManager
{
    private final Stats module;
    private final Database database;
    private final DSLContext dsl;
    private final Log log;

    private final ObjectMapper jsonMapper;
    private final Map<String, UInteger> statToId;
    private final Map<UInteger, Stat> stats;

    public StatsManager(Stats stats)
    {
        this.module = stats;
        this.database = stats.getCore().getDB();
        this.dsl = database.getDSL();
        this.log = stats.getLog();

        database.registerTable(TableStats.initTable(database));
        database.registerTable(TableStatsData.initTable(database));

        this.jsonMapper = new ObjectMapper();
        this.statToId = new HashMap<>();
        this.stats = new HashMap<>();

        for (StatsModel stat : dsl.selectFrom(TableStats.TABLE_STATS).fetch())
        {
            statToId.put(stat.getStat(), stat.getKey());
        }
    }

    public void register(Stat stat)
    {
        if (!statToId.containsKey(stat.getName()))
        {
            StatsModel model = this.dsl.newRecord(TABLE_STATS).newStatsModel(stat.getName());
            model.insert();
            this.statToId.put(model.getStat(), model.getKey());
        }
        this.stats.put(statToId.get(stat.getName()), stat);
        stat.init(this);
        stat.onActivate();
    }

    public Module getModule()
    {
        return module;
    }

    public void save(Stat stat, Object object)
    {
        try
        {
            String data = this.jsonMapper.writeValueAsString(object);
            UInteger statID = this.statToId.get(stat.getName());
            StatsDataModel model = this.dsl.newRecord(TABLE_STATSDATA).newStatsData(statID, data);
            model.insert();
        }
        catch (JsonProcessingException ex)
        {
            log.warn("An error occurred while parsing an object to JSON.", ex);
        }
    }
}
