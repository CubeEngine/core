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
package de.cubeisland.engine.log.action.block.player.interact;

import java.util.concurrent.TimeUnit;

import org.bukkit.material.Diode;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.player.ActionPlayerBlock;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.USE;

/**
 * Represents a player changing the delay of a repeater
 */
public class UseRepeater extends ActionPlayerBlock
{
    public UseRepeater()
    {
        super("repeater", USE);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof UseRepeater && this.player.equals(((ActionPlayerBlock)action).player)
            && TimeUnit.MINUTES.toMillis(2) > Math.abs(this.date.getTime() - action.date.getTime());
    }

    @Override
    public String translateAction(User user)
    {
        int oldTicks = this.oldBlock.as(Diode.class).getDelay();
        int newTicks = this.newBlock.as(Diode.class).getDelay();
        if (this.hasAttached())
        {
            UseRepeater action = (UseRepeater)this.getAttached().get(this.getAttached().size() - 1);
            newTicks = action.newBlock.as(Diode.class).getDelay();
        }
        if (oldTicks == newTicks)
        {
            return user.getTranslation(POSITIVE, "{user} fiddled around with the repeater but did not change anything",
                                       this.player.name);
        }
        return user.getTranslation(POSITIVE, "{user} set the repeater to {amount} ticks delay", this.player.name,
                                   newTicks);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.use.repeater;
    }
}
