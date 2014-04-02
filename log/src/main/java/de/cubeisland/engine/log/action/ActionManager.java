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
import de.cubeisland.engine.log.action.newaction.BaseAction;
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
import de.cubeisland.engine.log.action.newaction.entityspawn.EntitySpawnListener;
import de.cubeisland.engine.log.action.newaction.player.PlayerActionListener;
import de.cubeisland.engine.log.action.newaction.player.entity.PlayerEntityListener;
import de.cubeisland.engine.log.action.newaction.player.entity.hanging.PlayerHangingListener;
import de.cubeisland.engine.log.action.newaction.player.entity.vehicle.PlayerVehicleListener;
import de.cubeisland.engine.log.action.newaction.player.item.PlayerItemListener;
import de.cubeisland.engine.log.action.newaction.player.item.container.ContainerListener;

public class ActionManager
{
    private final Map<String, ActionCategory> categories = new HashMap<>();
    private final Log module;

    public ActionManager(Log module)
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
                registerListener(new EntitySpawnListener(module)).
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

    public ActionManager registerListener(LogListener listener)
    {
        module.getCore().getEventManager().registerListener(module, listener);
        for (Class<? extends BaseAction> actionClass : listener.getActions())
        {

        }

        return this;
    }


    public Set<String> getAllActionAndCategoryStrings()
    {
        HashSet<String> strings = new HashSet<>();
        strings.addAll(this.categories.keySet());
        return strings;
    }
}
