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
package de.cubeisland.engine.log.action.logaction.block.interaction;

import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.engine.log.action.logaction.ActionTypeContainer;
import de.cubeisland.engine.log.action.logaction.block.player.BlockPlace;
import de.cubeisland.engine.log.action.logaction.interact.FireworkUse;
import de.cubeisland.engine.log.action.logaction.interact.MonsterEggUse;
import de.cubeisland.engine.log.action.logaction.interact.VehiclePlace;

/**
 * Container-ActionType for interaction
 * <p>Events: {@link PlayerInteractEvent}</p>
 * <p>External Actions:
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.ContainerAccess.ContainerAccess},
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.DoorUse.DoorUse},
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.LeverUse.LeverUse},
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.ComparatorChange.ComparatorChange},
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.ButtonUse.ButtonUse},
 * {@link BlockPlace} for CocoaPods,
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.BonemealUse},
 * {@link VehiclePlace} preplanned,
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.TntPrime.TntPrime},
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.CakeEat.CakeEat},
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.NoteBlockChange.NoteBlockChange},
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.RepeaterChange.RepeaterChange},
 * {@link MonsterEggUse},
 * {@link FireworkUse},
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.CropTrample.CropTrample},
 * {@link de.cubeisland.engine.log.action.newaction.block.player.interact.PlateStep.PlateStep}
 */
public class RightClickActionType extends ActionTypeContainer
{
    public RightClickActionType()
    {
        super("RIGHT_CLICK");
    }


}
