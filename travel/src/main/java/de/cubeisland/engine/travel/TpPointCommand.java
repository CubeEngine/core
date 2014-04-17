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
package de.cubeisland.engine.travel;

import java.util.Set;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.command.exception.InvalidArgumentException;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;

public class TpPointCommand extends ContainerCommand
{
    protected InviteManager iManager;

    public TpPointCommand(Travel module, String name, String desc)
    {
        super(module, name, desc);
        iManager = module.getInviteManager();
    }

    protected User getUser(CommandContext context, int i)
    {
        User user;
        if (context.hasArg(i))
        {
            user = context.getUser(i);
            if (user == null)
            {
                throw new InvalidArgumentException(context.getSender().getTranslation(NEGATIVE, "Player {user} not found!", context.getString(i)));
            }
        }
        else if (context.isSender(User.class))
        {
            user = (User)context.getSender();
        }
        else
        {
            throw new IncorrectUsageException(context.getSender().getTranslation(NEGATIVE, "You need to provide a owner"));
        }
        return user;
    }

    protected User getUser(ParameterizedContext context, String owner)
    {
        User user;
        if (context.hasParam(owner))
        {
            user = context.getUser(owner);
            if (user == null)
            {
                throw new InvalidArgumentException(context.getSender().getTranslation(NEGATIVE,
                                                                                      "Player {user} not found!",
                                                                                      context.getString(owner)));
            }
        }
        else if (context.isSender(User.class))
        {
            user = (User)context.getSender();
        }
        else
        {
            throw new IncorrectUsageException(context.getSender().getTranslation(NEGATIVE, "You need to provide a owner"));
        }
        return user;
    }

    protected void showList(ParameterizedContext context, User user, Set<? extends TeleportPoint> points)
    {
        for (TeleportPoint point : points)
        {
            if (point.isPublic())
            {
                if (user != null && point.isOwner(user))
                {
                    context.sendTranslated(NEUTRAL, "  {name#tppoint} ({text:public})", point.getName());
                }
                else
                {
                    context.sendTranslated(NEUTRAL, "  {user}:{name#tppoint} ({text:public})", point.getOwnerName(), point.getName());
                }
            }
            else
            {
                if (user != null && point.isOwner(user))
                {
                    context.sendTranslated(NEUTRAL, "  {name#tppoint} ({text:private})", point.getName());
                }
                else
                {
                    context.sendTranslated(NEUTRAL, "  {user}:{name#tppoint} ({text:private})", point.getOwnerName(), point.getName());
                }
            }
        }
    }
}
