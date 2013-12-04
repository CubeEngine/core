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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.scheduler.BukkitScheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.logging.Log;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.task.TaskManager;
import de.cubeisland.engine.stats.annotations.Scheduled;
import de.cubeisland.engine.stats.stat.Stat;
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

    /**
     * Register a statistic.
     * This will make this StatsManager handle the statistic's database connection
     * and register it as a listener.
     *
     * @param statType The stat class to register
     */
    public void register(Class< ? extends Stat> statType)
    {
        try
        {
            Constructor<? extends Stat> constructor = statType.getConstructor(this.getClass());
            Stat stat = constructor.newInstance(this);

            // Get or register the stat id
            if (!statToId.containsKey(stat.getName()))
            {
                synchronized (this.dsl)
                {
                    StatsModel model = this.dsl.newRecord(TABLE_STATS).newStatsModel(stat.getName());
                    model.insert();
                    this.statToId.put(model.getStat(), model.getKey());
                }
            }
            this.stats.put(statToId.get(stat.getName()), stat);

            // Activate hook in the stat
            stat.onActivate();

            // Register Schedulers
            for (Method method : stat.getClass().getMethods())
            {
                if (!method.isAnnotationPresent(Scheduled.class))
                {
                    continue;
                }
                Scheduled annotation = method.getAnnotation(Scheduled.class);
                Runnable wrapper = new MethodRunnableWrapper(this.getModule().getLog(), stat, method);

                Map<String, Long> periods = module.getConfig().periods;
                long period = annotation.period();

                if (!annotation.periodFinal())
                {
                    if (!periods.containsKey(annotation.name()))
                    {
                        periods.put(annotation.name(), period);
                    }
                    period = periods.get(annotation.name());
                }

                TaskManager taskManager = module.getCore().getTaskManager();
                if (annotation.async())
                {
                    taskManager.runAsynchronousTimer(module, wrapper, 1, period);
                }
                else
                {
                    taskManager.runTimer(module, wrapper, 1, period);
                }

                this.module.getLog().debug("Scheduled method {} at interval {}", annotation.name(), period);
            }

        }
        catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex)
        {
            this.module.getLog().error("An error occurred while registering statistic", ex);
        }
    }

    /**
     * Get the module that loaded this StatsManager
     *
     * @return
     */
    public Module getModule()
    {
        return module;
    }

    /**
     * Save an object to the database
     *
     * The object must be able to be parsed through ObjectMapper.writeValue()
     *
     * @param stat The stat that owns this data
     * @param object The data to save
     */
    public void save(Stat stat, Object object)
    {
        synchronized (this.dsl)
        {
            try
            {
                String data = this.jsonMapper.writeValueAsString(object);
                UInteger statID = this.statToId.get(stat.getName());
                StatsDataModel model = this.dsl.newRecord(TABLE_STATSDATA).newStatsData(statID, new Timestamp(System.currentTimeMillis()), data);
                model.insert();
            }
            catch (JsonProcessingException ex)
            {
                log.warn("An error occurred while parsing an object to JSON.", ex);
            }
        }
    }

    private class MethodRunnableWrapper implements Runnable
    {
        private final Log logger;
        private final Object object;
        private final Method method;

        MethodRunnableWrapper(Log logger, Object object, Method method)
        {
            this.logger = logger;
            this.object = object;
            this.method = method;
        }

        public void run()
        {
            try
            {
                method.invoke(object);
            }
            catch (IllegalAccessException | InvocationTargetException ex)
            {
                logger.warn("An error occurred while invoking a scheduled method", ex);
            }
        }
    }
}
