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
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.MoveItemListener;
import de.cubeisland.engine.log.action.newaction.block.BlockListener;
import de.cubeisland.engine.log.action.newaction.block.entity.EntityBlockListener;
import de.cubeisland.engine.log.action.newaction.block.entity.explosion.ExplodeListener;
import de.cubeisland.engine.log.action.newaction.block.flow.FlowListener;
import de.cubeisland.engine.log.action.newaction.block.ignite.BlockIgniteListener;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockListener;
import de.cubeisland.engine.log.action.newaction.block.player.bucket.PlayerBucketListener;
import de.cubeisland.engine.log.action.newaction.block.player.interact.PlayerBlockInteractListener;
import de.cubeisland.engine.log.action.newaction.death.DeathListener;
import de.cubeisland.engine.log.action.newaction.entity.EntityListener;
import de.cubeisland.engine.log.action.newaction.player.PlayerActionListener;
import de.cubeisland.engine.log.action.newaction.player.entity.PlayerEntityListener;
import de.cubeisland.engine.log.action.newaction.player.entity.hanging.PlayerHangingListener;
import de.cubeisland.engine.log.action.newaction.player.entity.vehicle.PlayerVehicleListener;
import de.cubeisland.engine.log.action.newaction.player.item.PlayerItemListener;
import de.cubeisland.engine.log.action.newaction.player.item.container.ContainerListener;
import gnu.trove.map.hash.TLongObjectHashMap;

public class ActionTypeManager
{
    private final Map<Class<? extends ActionType>, ActionType> registeredActionTypes = new ConcurrentHashMap<>();
    private final Map<String, ActionType> actionTypesByName = new ConcurrentHashMap<>();
    private final TLongObjectHashMap<ActionType> registeredIds = new TLongObjectHashMap<>();
    private final Map<String, ActionTypeCategory> categories = new HashMap<>();
    private final Log module;

    public ActionTypeManager(Log module)
    {
        this.module = module;
        ActionTypeCompleter.manager = this;
        this.registerLogActionTypes();
    }

    public void registerLogActionTypes()
    {
        this.registerListener(new BlockIgniteListener(module)).
            registerListener(new BlockListener(module)).
                registerListener(new ContainerListener(module)).
                registerListener(new DeathListener(module)).
                registerListener(new EntityBlockListener(module)).
                registerListener(new EntityListener(module)).
                registerListener(new ExplodeListener(module)).
                registerListener(new FlowListener(module)).
                registerListener(new MoveItemListener(module)).
                registerListener(new PlayerActionListener(module)).
                registerListener(new PlayerBlockInteractListener(module)).
                registerListener(new PlayerBlockListener(module)).
                registerListener(new PlayerBucketListener(module)).
                registerListener(new PlayerEntityListener(module)).
                registerListener(new PlayerHangingListener(module)).
                registerListener(new PlayerItemListener(module)).
                registerListener(new PlayerVehicleListener(module));
    }

    public ActionTypeManager registerListener(LogListener listener)
    {
        module.getCore().getEventManager().registerListener(module, listener);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <AT extends ActionType> AT getActionType(Class<AT> actionTypeClass)
    {
        return (AT)this.registeredActionTypes.get(actionTypeClass);
    }

    public ActionType getActionType(int id)
    {
        return this.registeredIds.get(id);
    }

    public String getActionTypesAsString()
    {
        TreeSet<String> actionTypes = new TreeSet<>();
        for (ActionType actionType : this.registeredActionTypes.values())
        {
            actionTypes.add(actionType.getName().replace("-", ChatFormat.WHITE + "-" + ChatFormat.GREY));
        }
        return ChatFormat.GREY.toString() + ChatFormat.ITALIC + StringUtils.implode(
            ChatFormat.WHITE.toString() + ", " + ChatFormat.GREY + ChatFormat.ITALIC, actionTypes);
    }

    public Set<ActionType> getActionType(String actionString)
    {

        ActionTypeCategory category = this.categories.get(actionString);
        if (category == null)
        {
            String match = Match.string().matchString(actionString, this.actionTypesByName.keySet());
            if (match == null)
            {
                return null;
            }
            HashSet<ActionType> actionTypes = new HashSet<>();
            actionTypes.add(this.actionTypesByName.get(match));
            return actionTypes;
        }
        else
        {
            return category.getActionTypes();
        }
    }

    public Set<String> getAllActionAndCategoryStrings()
    {
        HashSet<String> strings = new HashSet<>();
        strings.addAll(this.categories.keySet());
        strings.addAll(this.actionTypesByName.keySet());
        return strings;
    }
}
