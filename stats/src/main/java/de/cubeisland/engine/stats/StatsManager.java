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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cubeisland.engine.configuration.annotations.Comment;
import de.cubeisland.engine.configuration.annotations.Name;
import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.core.logging.Log;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.task.TaskManager;
import de.cubeisland.engine.stats.annotations.Configured;
import de.cubeisland.engine.stats.annotations.Scheduled;
import de.cubeisland.engine.stats.configuration.DynamicSection;
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
    private final ConverterManager converterManager;

    private final ObjectMapper jsonMapper;
    private final Map<String, UInteger> statToId;
    private final Map<UInteger, Stat> stats;
    private final Map<String, Set<Integer>> tasks;

    public StatsManager(Stats stats, ConverterManager converterManager)
    {
        this.module = stats;
        this.database = stats.getCore().getDB();
        this.dsl = database.getDSL();
        this.log = stats.getLog();
        this.converterManager = converterManager;

        database.registerTable(TableStats.initTable(database));
        database.registerTable(TableStatsData.initTable(database));

        this.jsonMapper = new ObjectMapper();
        this.statToId = new HashMap<>();
        this.stats = new HashMap<>();
        this.tasks = new HashMap<>();

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
            // Get the Constructor, and construct a new instance of the Stat.
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

            // Load configured fields

            for (Field field : statType.getFields())
            {
                if (!field.isAnnotationPresent(Configured.class))
                {
                    return;
                }
                String name = field.getName();
                if (field.isAnnotationPresent(Name.class))
                {
                    Name nameAnnotation = field.getAnnotation(Name.class);
                    name = nameAnnotation.value();
                }

                if (!module.getConfig().statConfigs.containsKey(stat.getName()))
                {
                    module.getConfig().statConfigs.put(stat.getName(), new DynamicSection(converterManager));
                }
                DynamicSection section = module.getConfig().statConfigs.get(stat.getName());
                if (section.hasKey(name))
                {
                    field.set(stat, section.get(name));
                    continue;
                }

                String[] comment = {};
                if (field.isAnnotationPresent(Comment.class))
                {
                    Comment commentAnnotation = field.getAnnotation(Comment.class);
                    comment = commentAnnotation.value();
                }
                section.put(name, field.get(stat), comment);
            }

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

                long interval;
                if (annotation.periodFinal())
                {
                    interval = annotation.interval();
                }
                else
                {
                    if (!module.getConfig().statConfigs.containsKey(stat.getName()))
                    {
                        module.getConfig().statConfigs.put(stat.getName(), new DynamicSection(converterManager));
                    }
                    DynamicSection section = module.getConfig().statConfigs.get(stat.getName());
                    if (!section.hasKey("tasks"))
                    {
                        section.put("tasks", new DynamicSection(converterManager), "Intervals for the tasks this statistic schedules");
                    }
                    DynamicSection tasks = (DynamicSection)section.get("tasks", DynamicSection.class);
                    if (!tasks.hasKey(annotation.name()))
                    {
                        tasks.put(annotation.name(), annotation.interval(), annotation.comment());
                    }
                    interval = (Long)tasks.get(annotation.name(), Long.class);
                }

                if (!this.tasks.containsKey(stat.getName()))
                {
                    this.tasks.put(stat.getName(), new HashSet<Integer>());
                }
                Set<Integer> tasks = this.tasks.get(stat.getName());
                Runnable wrapper = new ScheduledMethod(this.getModule().getLog(), stat, method);
                TaskManager taskManager = module.getCore().getTaskManager();
                if (annotation.async())
                {
                    tasks.add(taskManager.runAsynchronousTimer(module, wrapper, 1, interval));
                }
                else
                {
                    tasks.add(taskManager.runTimer(module, wrapper, 1, interval));
                }

                this.module.getLog().debug("Scheduled method {} at interval {}", annotation.name(), interval);
            }

        }
        catch (ReflectiveOperationException | ConversionException ex)
        {
            this.module.getLog().error("An error occurred while registering statistic", ex);
        }
    }

    public void disableStat(String stat)
    {
        if (this.tasks.containsKey(stat))
        {
            for (Integer id : tasks.get(stat))
            {
                this.module.getCore().getTaskManager().cancelTask(this.module, id);
            }
        }
        this.stats.get(this.statToId.get(stat)).onDeactivate();
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

    private static final class ScheduledMethod implements Runnable
    {
        private final Log logger;
        private final Object object;
        private final Method method;

        ScheduledMethod(Log logger, Object object, Method method)
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
