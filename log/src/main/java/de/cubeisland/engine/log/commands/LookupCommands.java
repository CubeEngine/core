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
package de.cubeisland.engine.log.commands;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.core.command.HelpContext;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.parameterized.completer.MaterialListCompleter;
import de.cubeisland.engine.core.command.parameterized.completer.PlayerListCompleter;
import de.cubeisland.engine.core.command.parameterized.completer.WorldCompleter;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.TimeConversionException;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.ActionManager;
import de.cubeisland.engine.log.action.ActionTypeCompleter;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.ActionBlock.BlockSection;
import de.cubeisland.engine.log.storage.Lookup;
import de.cubeisland.engine.log.storage.QueryParameter;
import de.cubeisland.engine.log.storage.ShowParameter;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class LookupCommands
{
    private final Log module;
    private final ActionManager actionManager;

    public LookupCommands(Log module)
    {
        this.module = module;
        this.actionManager = module.getActionManager();
    }

    private void params(ParameterizedContext context)
    {
        if (context.hasParam("params"))
        {
            String param = context.getString("params");
            context.sendMessage("NOT YET DONE");
            // TODO show description
            return;
        }
        context.sendTranslated(NEUTRAL, "Registered ActionTypes:"); //TODO colors
        context.sendMessage(this.module.getActionManager().getActionTypesAsString());
        context.sendMessage("");
        context.sendTranslated(NEUTRAL, "Lookup/Rollback/Redo-Parameters:");
        context.sendMessage("");
        context.sendTranslated(NEUTRAL, " - action <actionType> like a block-break (See full list above)");
        context.sendTranslated(NEUTRAL, " - radius <radius> or sel, global, player:<radius>");
        context.sendTranslated(NEUTRAL, " - player <users> like p Faithcaio ");
        context.sendTranslated(NEUTRAL, " - entity <entities> like e sheep");
        context.sendTranslated(NEUTRAL, " - block <blocks> like b stone");
        context.sendTranslated(NEUTRAL, " - since <time> default is 3 days");
        context.sendTranslated(NEUTRAL, " - before <time>");
        context.sendTranslated(NEUTRAL, " - world <world> default is your current world");

        context.sendMessage("");
        context
            .sendTranslated(NEUTRAL, "Use {text:!} to exclude the parameters instead of including them.");
    }

    @Command(
        desc = "Queries a lookup in the database\n    " + "Show availiable parameters with /lookup params",
        indexed = @Grouped(req = false, value = @Indexed(label = "params")),
        flags = {
            @Flag(longName = "coordinates", name = "coords"),
            @Flag(longName = "detailed", name = "det"),
            @Flag(longName = "nodate", name = "nd"),
            @Flag(longName = "descending", name = "desc")},
        params = {
            @Param(names = {"action", "a"}, completer = ActionTypeCompleter.class),
            @Param(names = {"radius", "r"}),//<radius> OR selection|sel OR global|g OR player|p:<radius>
            @Param(names = {"user", "player", "p"}, completer = PlayerListCompleter.class),
            @Param(names = {"block", "b"}, completer = MaterialListCompleter.class),
            @Param(names = {"entity", "e"}),
            @Param(names = {"since", "time", "t"}), // if not given default since 3d
            @Param(names = {"before"}),
            @Param(names = {"world", "w", "in"}, type = World.class, completer = WorldCompleter.class),
            @Param(names = {"limit", "pagelimit"}, type = Integer.class),
            @Param(names = {"page"}, type = Integer.class),
            @Param(names = "params", completer = ActionTypeCompleter.class)})
    // TODO param for filter / chat / command / signtexts
    public void lookup(ParameterizedContext context)
    {
        if ((context.hasArg(0) && "params".equalsIgnoreCase(context.<String>getArg(0))) || context.hasParam("params"))
        {
            this.params(context);
        }
        else if (context.getSender() instanceof User)
        {
            if (context.getParams().isEmpty())
            {
                try
                {
                    // TODO show all selected params of last lookup
                    context.getCommand().help(new HelpContext(context));
                }
                catch (Exception e)
                {
                    throw new IllegalStateException(e);
                }
                return;
            }
            User user = (User)context.getSender();
            LogAttachment attachment = user.attachOrGet(LogAttachment.class, this.module);
            ShowParameter show = attachment.getLastShowParameter(); // gets last OR new Showparameter
            Lookup lookup = attachment.getLastLookup();
            if (!this.fillShowOptions(attachment, lookup, show, context)) // /lookup show / page <page>
            {
                return;
            }
            lookup = attachment.createNewCommandLookup();
            QueryParameter params = lookup.getQueryParameter();
            if (!(this.readActions(params, context.getString("action"), user) && this
                .readRadius(params, context.getString("radius"), user) && this
                .readUser(params, context.getString("user"), user) && this
                .readBlocks(params, context.getString("block"), user) && this
                .readEntities(params, context.getString("entity"), user) && this
                .readWorld(params, context.getString("world"), context.hasParam("radius"), user) && this
                .readTimeSince(params, context.getString("since"), user) && this
                .readTimeBefore(params, context.getString("before"), user)))
            {
                return;
            }
            attachment.queueShowParameter(show);
            this.module.getLogManager().fillLookupAndShow(lookup, user);
        }
        else
        {
            // TODO implement me
            System.out.println("Not implemented yet");
        }
    }

    @Command(desc = "Performs a rollback",
        flags = @Flag(longName = "preview", name = "pre"),
        indexed = @Grouped(req = false, value = @Indexed(label = "!params")),
        params = {
            @Param(names = {"action", "a"}, completer = ActionTypeCompleter.class), @Param(names = {"radius", "r"}),//<radius> OR selection|sel OR global|g OR player|p:<radius>
            @Param(names = {"user", "player", "p"}, completer = PlayerListCompleter.class), @Param(names = {"block", "b"}, completer = MaterialListCompleter.class), @Param(names = {"entity", "e"}), @Param(names = {"since", "time", "t"}), // if not given default since 3d
            @Param(names = {"before"}), @Param(names = {"world", "w", "in"}, type = World.class, completer = WorldCompleter.class),
        })
    public void rollback(ParameterizedContext context)
    {
        if (context.hasArg(0))
        {
            if ("params".equalsIgnoreCase(context.<String>getArg(0)))
            {
                this.params(context);
            }
        }
        else if (context.getSender() instanceof User)
        {
            if (!context.hasParams())
            {
                context.sendTranslated(NEGATIVE, "You need to define parameters to rollback!");
                return;
            }
            User user = (User)context.getSender();
            LogAttachment attachment = user.attachOrGet(LogAttachment.class, this.module);
            Lookup lookup = attachment.createNewCommandLookup();
            QueryParameter params = lookup.getQueryParameter();
            if (!(this.readActions(params, context.getString("action"), user) && this
                .readRadius(params, context.getString("radius"), user) && this
                .readUser(params, context.getString("user"), user) && this
                .readBlocks(params, context.getString("block"), user) && this
                .readEntities(params, context.getString("entity"), user) && this
                .readWorld(params, context.getString("world"), context.hasParam("radius"), user) && this
                .readTimeSince(params, context.getString("since"), user) && this
                .readTimeBefore(params, context.getString("before"), user)))
            {
                return;
            }
            if (context.hasFlag("pre"))
            {
                this.module.getLogManager().fillLookupAndPreviewRollback(lookup, user);
            }
            else
            {
                this.module.getLogManager().fillLookupAndRollback(lookup, user);
            }
        }
        else
        {
            // TODO implement me
            System.out.println("Not implemented yet");
        }
    }

    @Command(
        desc = "Performs a rollback",
        indexed = @Grouped(req = false, value = @Indexed(label = "params")),
        flags = @Flag(longName = "preview", name = "pre"),
        params = {
            @Param(names = {"action", "a"}, completer = ActionTypeCompleter.class), @Param(names = {"radius", "r"}),//<radius> OR selection|sel OR global|g OR player|p:<radius>
            @Param(names = {"user", "player", "p"}, completer = PlayerListCompleter.class), @Param(names = {"block", "b"}, completer = MaterialListCompleter.class), @Param(names = {"entity", "e"}), @Param(names = {"since", "time", "t"}), // if not given default since 3d
            @Param(names = {"before"}), @Param(names = {"world", "w", "in"}, type = World.class, completer = WorldCompleter.class)})
    public void redo(ParameterizedContext context)
    {
        if (context.hasArg(0))
        {
            if ("params".equalsIgnoreCase(context.<String>getArg(0)))
            {
                this.params(context);
            }
        }
        else if (context.getSender() instanceof User)
        {
            if (!context.hasParams())
            {
                context.sendTranslated(NEGATIVE, "You need to define parameters to redo!");
                return;
            }
            User user = (User)context.getSender();
            LogAttachment attachment = user.attachOrGet(LogAttachment.class, this.module);
            Lookup lookup = attachment.createNewCommandLookup();
            QueryParameter params = lookup.getQueryParameter();
            if (!(this.readActions(params, context.getString("action"), user) && this
                .readRadius(params, context.getString("radius"), user) && this
                .readUser(params, context.getString("user"), user) && this
                .readBlocks(params, context.getString("block"), user) && this
                .readEntities(params, context.getString("entity"), user) && this
                .readWorld(params, context.getString("world"), context.hasParam("radius"), user) && this
                .readTimeSince(params, context.getString("since"), user) && this
                .readTimeBefore(params, context.getString("before"), user)))
            {
                return;
            }
            if (context.hasFlag("pre"))
            {
                this.module.getLogManager().fillLookupAndPreviewRedo(lookup, user);
            }
            else
            {
                this.module.getLogManager().fillLookupAndRedo(lookup, user);
            }
        }
        else
        {
            // TODO implement me
            System.out.println("Not implemented yet");
        }
    }

    private boolean readTimeBefore(QueryParameter params, String beforeString, User user)
    {
        try
        { // TODO date too
            if (beforeString == null)
            {
                return true;
            }
            long before = StringUtils.convertTimeToMillis(beforeString);
            params.before(new Date(System.currentTimeMillis() - before));
            return true;
        }
        catch (TimeConversionException e)
        {
            user.sendTranslated(NEGATIVE, "{input#time} is not a valid time value!", beforeString);
            return false;
        }
    }

    private boolean readTimeSince(QueryParameter params, String sinceString, User user)
    {
        try
        {
            if (sinceString != null)
            { // TODO date too
                long since = StringUtils.convertTimeToMillis(sinceString);
                params.since(new Date(System.currentTimeMillis() - since));
            }
            else
            {
                params.since(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30))); // defaulted to last 30 days
            }
            return true;
        }
        catch (TimeConversionException e)
        {
            user.sendTranslated(NEGATIVE, "{input#time} is not a valid time value!", sinceString);
            return false;
        }
    }

    private boolean readWorld(QueryParameter params, String worldString, boolean hasRadius, User user)
    {
        if (worldString == null)
        {
            return true;
        }
        if (hasRadius)
        {
            user.sendTranslated(NEGATIVE, "You cannot define a radius or selection and a world.");
            return false;
        }
        World world = user.getServer().getWorld(worldString);
        if (world == null)
        {
            user.sendTranslated(NEGATIVE, "Unknown world: {input#world}", worldString);
            return false;
        }
        params.setWorld(world);
        return true;
    }

    private boolean readRadius(QueryParameter params, String radiusString, User user)
    {
        if (radiusString == null)
        {
            return true;
        }
        if (radiusString.equalsIgnoreCase("selection") || radiusString.equalsIgnoreCase("sel"))
        {
            LogAttachment logAttachment = user.attachOrGet(LogAttachment.class, this.module);
            if (!logAttachment.applySelection(params))
            {
                user.sendTranslated(NEGATIVE, "You have to select a region first!");
                if (module.hasWorldEdit())
                {
                    user.sendTranslated(NEUTRAL, "Use worldedit to select a cuboid region!");
                }
                else
                {
                    user.sendTranslated(NEUTRAL, "Use this selection wand.");
                    LogCommands.giveSelectionTool(user);
                }
                return false;
            }
        }
        else if (radiusString.equalsIgnoreCase("global") || radiusString.equalsIgnoreCase("g"))
        {
            params.setWorld(user.getWorld());
        }
        else
        {
            User radiusUser = null;
            Integer radius;
            if (radiusString.contains(":"))
            {
                radiusUser = this.module.getCore().getUserManager()
                                        .findUser(radiusString.substring(0, radiusString.indexOf(":")));
                if (radiusUser == null)
                {
                    user.sendTranslated(NEGATIVE, "Invalid radius/location selection");
                    user.sendTranslated(POSITIVE, "The radius parameter can be: <radius> | selection | global | <player>[:<radius>]");
                    return false;
                }
                radiusString = radiusString.substring(radiusString.indexOf(":") + 1);
            }
            try
            {
                radius = Integer.parseInt(radiusString);
                if (radiusUser == null)
                {
                    radiusUser = user;
                }
                params.setLocationRadius(radiusUser.getLocation(), radius);
            }
            catch (NumberFormatException ex)
            {
                radiusUser = this.module.getCore().getUserManager().findUser(radiusString);
                if (radiusUser == null)
                {
                    user.sendTranslated(NEGATIVE, "Invalid radius/location selection");
                    user.sendTranslated(POSITIVE, "The radius parameter can be: <radius> | selection | global | <player>[:<radius>]");
                    return false;
                }
                params.setWorld(radiusUser.getWorld());
            }
        }
        return true;
    }

    private boolean fillShowOptions(LogAttachment attachment, Lookup lookup, ShowParameter show, ParameterizedContext context)
    {
        show.showCoords = context.hasFlag("coords");
        show.showDate = !context.hasFlag("nd");
        show.compress = !context.hasFlag("det");
        show.reverseOrder = !context.hasFlag("desc");
        if (context.hasParam("limit"))
        {
            Integer limit = context.getParam("limit", null);
            if (limit == null)
            {
                return false;
            }
            if (limit > 100)
            {
                context.sendTranslated(NEUTRAL, "Your page-limit is to high! Showing 100 logs per page.");
                limit = 100;
            }
            show.pagelimit = limit;
        }
        if (context.hasArg(0))
        {
            if ("show".equalsIgnoreCase(context.<String>getArg(0)))
            {
                if (lookup != null && lookup.queried())
                {
                    attachment.queueShowParameter(show);
                    lookup.show(attachment.getHolder());
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "You have to do a query first!");
                }
                return false;
            }
            return false;
        }
        if (context.hasParam("page"))
        {
            if (lookup != null && lookup.queried())
            {
                Integer page = context.getParam("page", null);
                if (page == null)
                {
                    context.sendTranslated(NEGATIVE, "Invalid page!");
                    return false;
                }
                show.page = page;
                attachment.queueShowParameter(show);
                lookup.show(attachment.getHolder());
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You have to do a query first!");
            }
            return false;
        }
        return true;
    }

    private boolean readUser(QueryParameter params, String userString, User sender)
    {
        if (userString == null)
        {
            return true;
        }
        String[] users = StringUtils.explode(",", userString);
        for (String name : users)
        {
            boolean negate = name.startsWith("!");
            if (negate)
            {
                name = name.substring(1);
            }
            User user = this.module.getCore().getUserManager().findExactUser(name);
            if (user == null)
            {
                sender.sendTranslated(NEGATIVE, "User {user} not found!", name);
                return false;
            }
            if (negate)
            {
                params.excludeUser(user.getUniqueId());
            }
            else
            {
                params.includeUser(user.getUniqueId());
            }
        }
        return true;
    }

    private boolean readBlocks(QueryParameter params, String block, User user)
    {
        if (block == null)
        {
            return true;
        }
        String[] names = StringUtils.explode(",", block);
        for (String name : names)
        {
            boolean negate = name.startsWith("!");
            if (negate)
            {
                name = name.substring(1);
            }
            Byte data = null;
            if (name.contains(":"))
            {
                String sub = name.substring(name.indexOf(":") + 1);
                try
                {
                    data = Byte.parseByte(sub);
                }
                catch (NumberFormatException ex)
                {
                    user.sendTranslated(NEGATIVE, "Invalid BlockData: {name#block}", sub);
                    return false;
                }
                name = name.substring(0, name.indexOf(":"));
            }
            Material material = Match.material().material(name);
            if (material == null)
            {
                user.sendTranslated(NEGATIVE, "Unknown Material: {name#material}", name);
                return false;
            }
            BlockSection blockData = new BlockSection(material);
            blockData.data = data == null ? 0 : data;
            if (negate)
            {
                params.excludeBlock(blockData);
            }
            else
            {
                params.includeBlock(blockData);
            }
        }
        return true;
    }

    private boolean readEntities(QueryParameter params, String entity, User user)
    {
        if (entity == null)
        {
            return true;
        }
        String[] names = StringUtils.explode(",", entity);
        for (String name : names)
        {
            boolean negate = name.startsWith("!");
            if (negate)
            {
                name = name.substring(1);
            }
            EntityType entityType = Match.entity().living(name);
            if (entityType == null)
            {
                user.sendTranslated(NEGATIVE, "Unknown EntityType: {name#entity}", name);
                return false;
            }
            if (negate)
            {
                params.excludeEntity(entityType);
            }
            else
            {
                params.includeEntity(entityType);
            }
        }
        return true;
    }


    private boolean readActions(QueryParameter params, String input, User user)
    {
        if (input == null)
        {
            return true;
        }
        String[] inputs = StringUtils.explode(",", input);
        for (String actionString : inputs)
        {
            boolean negate = actionString.startsWith("!");
            if (negate)
            {
                actionString = actionString.substring(1);
            }
            List<Class<? extends BaseAction>> actions = this.actionManager.getAction(actionString);
            if (actions == null)
            {
                user.sendTranslated(NEGATIVE, "Unknown action-type: {name#action}", actionString);
                return false;
            }
            if (negate)
            {
                for (Class<? extends BaseAction> action : actions)
                {
                    params.excludeAction(action);
                }
            }
            else
            {
                for (Class<? extends BaseAction> action : actions)
                {
                    params.includeAction(action);
                }
            }
        }
        return true;
    }
}

