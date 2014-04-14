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
package de.cubeisland.engine.basics.command.general;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.storage.IgnoreList;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import org.jooq.DSLContext;

import static de.cubeisland.engine.basics.storage.TableIgnorelist.TABLE_IGNORE_LIST;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class IgnoreCommands
{
    private final Basics module;
    private final UserManager um;
    private final DSLContext dsl;

    public IgnoreCommands(Basics basics)
    {
        this.module = basics;
        this.um = basics.getCore().getUserManager();
        this.dsl = module.getCore().getDB().getDSL();
    }

    private boolean addIgnore(User user, User ignored)
    {
        if (checkIgnored(user, ignored))
        {
            return false;
        }
        IgnoreList ignoreList = this.dsl.newRecord(TABLE_IGNORE_LIST).newIgnore(user, ignored);
        ignoreList.insert();
        return true;
    }

    private boolean removeIgnore(User user, User ignored)
    {
        if (checkIgnored(user, ignored))
        {
            this.dsl.delete(TABLE_IGNORE_LIST).
                where(TABLE_IGNORE_LIST.KEY.eq(user.getEntity().getKey())).
                and(TABLE_IGNORE_LIST.IGNORE.eq(ignored.getEntity().getKey())).execute();
            return true;
        }
        return true;
    }

    public boolean checkIgnored(User user, User ignored)
    {
        IgnoreList ignore =
            this.dsl.selectFrom(TABLE_IGNORE_LIST).
                where(TABLE_IGNORE_LIST.KEY.eq(user.getEntity().getKey())).
                and(TABLE_IGNORE_LIST.IGNORE.eq(ignored.getEntity().getKey())).fetchOneInto(TABLE_IGNORE_LIST);
        return ignore != null;
    }

    @Command(desc = "Ignores all messages from players",
             indexed = @Grouped(@Indexed("players...")))
    public void ignore(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            String[] userNames = StringUtils.explode(",", context.getString(0));
            List<String> added = new ArrayList<>();
            for (String name : userNames)
            {
                User user = this.um.findUser(name);
                if (user == null)
                {
                    context.sendTranslated(NEGATIVE, "User {user} not found!", name);
                }
                else if (!this.addIgnore(sender, user))
                {
                    if (module.perms().COMMAND_IGNORE_PREVENT.isAuthorized(user))
                    {
                        context.sendTranslated(NEGATIVE, "You are not allowed to ignore {user}!", user);
                        continue;
                    }
                    context.sendTranslated(NEGATIVE, "{user} is already on your ignore list!", user);
                }
                else
                {
                    added.add(name);
                }
            }
            context.sendTranslated(POSITIVE, "You added {user#list} to your ignore list!",
                                   StringUtils.implode(ChatFormat.WHITE + ", " + ChatFormat.DARK_GREEN, added));
            return;
        }
        int rand1 = new Random().nextInt(6)+1;
        int rand2 = new Random().nextInt(6-rand1+1)+1;
        context.sendTranslated(NEUTRAL, "Ignore ({text:8+:color=WHITE}): {integer#random} + {integer#random} = {integer#sum} -> {text:failed:color=RED}",
                               rand1, rand2, rand1 + rand2);
    }

    @Command(desc = "Stops ignoring all messages from a player",
             indexed = @Grouped(@Indexed("players...")))
    public void unignore(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            User sender = (User)context.getSender();
            String[] userNames = StringUtils.explode(",", context.getString(0));
            List<String> added = new ArrayList<>();
            for (String name : userNames)
            {
                User user = this.um.findUser(name);
                if (user == null)
                {
                    context.sendTranslated(NEGATIVE, "User {user} not found!", name);
                }
                else if (!this.removeIgnore(sender, user))
                {
                    context.sendTranslated(NEGATIVE, "You haven't ignored {user}!", user);
                }
                else
                {
                    added.add(name);
                }
            }
            context.sendTranslated(POSITIVE, "You removed {user#list} from your ignore list!",
                                   StringUtils.implode(ChatFormat.WHITE + ", " + ChatFormat.DARK_GREEN, added));
            return;
        }
        context.sendTranslated(NEGATIVE, "Congratulations! You are now looking at this text!");
    }
}
