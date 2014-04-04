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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.newaction.BaseAction;
import de.cubeisland.engine.log.action.newaction.ListenerItemMove;
import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.block.ListenerBlock;
import de.cubeisland.engine.log.action.newaction.block.entity.ListenerEntityBlock;
import de.cubeisland.engine.log.action.newaction.block.entity.explosion.ListenerExplode;
import de.cubeisland.engine.log.action.newaction.block.flow.ListenerFlow;
import de.cubeisland.engine.log.action.newaction.block.ignite.ListenerBlockIgnite;
import de.cubeisland.engine.log.action.newaction.block.player.ListenerPlayerBlock;
import de.cubeisland.engine.log.action.newaction.block.player.bucket.ListenerBucket;
import de.cubeisland.engine.log.action.newaction.block.player.interact.ListenerPlayerBlockInteract;
import de.cubeisland.engine.log.action.newaction.death.ListenerDeath;
import de.cubeisland.engine.log.action.newaction.entityspawn.ListenerEntitySpawn;
import de.cubeisland.engine.log.action.newaction.player.PlayerActionListener;
import de.cubeisland.engine.log.action.newaction.player.entity.ListenerPlayerEntity;
import de.cubeisland.engine.log.action.newaction.hanging.ListenerHanging;
import de.cubeisland.engine.log.action.newaction.player.item.ListenerItem;
import de.cubeisland.engine.log.action.newaction.player.item.container.ListenerContainerItem;
import de.cubeisland.engine.log.action.newaction.vehicle.ListenerVehicle;

public class ActionManager
{
    // Map Category -> Category
    private final Map<String, ActionCategory> categories = new HashMap<>();
    // TODO:
    // Map Category-Name -> List<Class>
    // Map Name -> List<Class>

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
    }

    public ActionManager registerListener(LogListener listener)
    {
        module.getCore().getEventManager().registerListener(module, listener);
        for (Class<? extends BaseAction> actionClass : listener.getActions())
        {
            try
            {
                BaseAction action = actionClass.newInstance();
                this.actions.put(actionClass, action);
                action.getCategory().addAction(actionClass);
            }
            catch (ReflectiveOperationException e)
            {
                throw new IllegalArgumentException("Could not instantiate action", e);
            }
        }
        return this;
    }


    public Set<String> getAllActionAndCategoryStrings()
    {
        HashSet<String> strings = new HashSet<>();
        strings.addAll(this.categories.keySet());
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
                throw new IllegalArgumentException("Could not instantiate action", e);
            }
        }
        return action.isActive(config);
    }
}
