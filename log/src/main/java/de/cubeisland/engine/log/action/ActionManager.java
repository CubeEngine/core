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
package de.cubeisland.engine.log.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;

import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.block.ListenerBlock;
import de.cubeisland.engine.log.action.block.entity.ListenerEntityBlock;
import de.cubeisland.engine.log.action.block.entity.explosion.ListenerExplode;
import de.cubeisland.engine.log.action.block.flow.ListenerFlow;
import de.cubeisland.engine.log.action.block.ignite.ListenerBlockIgnite;
import de.cubeisland.engine.log.action.block.player.ListenerPlayerBlock;
import de.cubeisland.engine.log.action.block.player.bucket.ListenerBucket;
import de.cubeisland.engine.log.action.block.player.interact.ListenerPlayerBlockInteract;
import de.cubeisland.engine.log.action.block.player.worldedit.ActionWorldEdit;
import de.cubeisland.engine.log.action.death.ListenerDeath;
import de.cubeisland.engine.log.action.entityspawn.ListenerEntitySpawn;
import de.cubeisland.engine.log.action.hanging.ListenerHanging;
import de.cubeisland.engine.log.action.player.PlayerActionListener;
import de.cubeisland.engine.log.action.player.entity.ListenerPlayerEntity;
import de.cubeisland.engine.log.action.player.item.ListenerItem;
import de.cubeisland.engine.log.action.player.item.container.ListenerContainerItem;
import de.cubeisland.engine.log.action.vehicle.ListenerVehicle;

public class ActionManager
{
    // Map Category -> Category
    private final Map<String, ActionCategory> categories = new HashMap<>();
    private Map<String, List<Class<? extends BaseAction>>> actionNames = new LinkedHashMap<>();
    // TODO:
    // Map Category-Name -> List<Class>

    // e.g searching for "water" yields water-break, water-flow, water-form, bucket-water

    private final Map<Class<? extends BaseAction>, BaseAction> actions = new HashMap<>();
    private final Log module;

    public ActionManager(Log module)
    {
        this.module = module;
        ActionTypeCompleter.manager = this;
        this.registerLogActionTypes();
    }

    public void registerLogActionTypes()
    {
        this.registerListener(new ListenerBlockIgnite(module)).
            registerListener(new ListenerBlock(module)).
                registerListener(new ListenerContainerItem(module)).
                registerListener(new ListenerDeath(module)).
                registerListener(new ListenerEntityBlock(module)).
                registerListener(new ListenerEntitySpawn(module)).
                registerListener(new ListenerExplode(module)).
                registerListener(new ListenerFlow(module)).
                registerListener(new ListenerItemMove(module)).
                registerListener(new PlayerActionListener(module)).
                registerListener(new ListenerPlayerBlockInteract(module)).
                registerListener(new ListenerPlayerBlock(module)).
                registerListener(new ListenerBucket(module)).
                registerListener(new ListenerPlayerEntity(module)).
                registerListener(new ListenerHanging(module)).
                registerListener(new ListenerItem(module)).
                registerListener(new ListenerVehicle(module));
        if (module.hasWorldEdit())
        {
            this.registerAction(ActionWorldEdit.class);
        }
    }

    public ActionManager registerListener(LogListener listener)
    {
        module.getCore().getEventManager().registerListener(module, listener);
        for (Class<? extends BaseAction> actionClass : listener.getActions())
        {
            registerAction(actionClass);
        }
        return this;
    }

    public void registerAction(Class<? extends BaseAction> actionClass)
    {
        try
        {
            BaseAction action = actionClass.newInstance();
            this.actions.put(actionClass, action);
            for (ActionCategory category : action.getCategories())
            {
                category.addAction(actionClass);
                this.categories.put(category.name, category);
                String name = category.name + "-" + action.getName();
                List<Class<? extends BaseAction>> list = this.actionNames.get(name);
                if (list == null)
                {
                    list = new ArrayList<>();
                    this.actionNames.put(name, list);
                }
                list.add(actionClass);
            }
        }
        catch (ReflectiveOperationException e)
        {
            throw new IllegalArgumentException("Could not instantiate action", e);
        }
    }


    public Set<String> getAllActionAndCategoryStrings()
    {
        HashSet<String> strings = new HashSet<>();
        strings.addAll(this.categories.keySet());
        strings.addAll(this.actionNames.keySet());
        return strings;
    }

    public boolean isActive(Class<? extends BaseAction> clazz, LoggingConfiguration config)
    {
        BaseAction action = this.actions.get(clazz);
        if (action == null)
        {
            module.getLog().error("Action is not registered! {}", clazz.getName());
            try
            {
                action = clazz.newInstance();
            }
            catch (ReflectiveOperationException e)
            {
                throw new IllegalArgumentException("Could not instantiate action " + clazz.getName(), e);
            }
        }
        return action.isActive(config);
    }

    public String getActionTypesAsString()
    {
        String delim = ChatColor.GRAY + ", " + ChatColor.YELLOW;
        return ChatColor.YELLOW + StringUtils.implode(delim, this.getAllActionAndCategoryStrings());
    }

    public List<Class<? extends BaseAction>> getAction(String actionName)
    {
        return this.actionNames.get(actionName);
    }
}
