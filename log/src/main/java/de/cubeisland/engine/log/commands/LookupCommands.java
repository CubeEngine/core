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

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
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
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.TimeConversionException;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.ActionType;
import de.cubeisland.engine.log.action.ActionTypeCompleter;
import de.cubeisland.engine.log.action.ActionTypeManager;
import de.cubeisland.engine.log.storage.ImmutableBlockData;
import de.cubeisland.engine.log.storage.Lookup;
import de.cubeisland.engine.log.storage.QueryParameter;
import de.cubeisland.engine.log.storage.ShowParameter;

public class LookupCommands
{
    private final Log module;
    private ActionTypeManager actionTypeManager;

    public LookupCommands(Log module)
    {
        this.module = module;
        this.actionTypeManager = module.getActionTypeManager();
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
        context.sendTranslated("&6Registered ActionTypes:");
        context.sendMessage(this.module.getActionTypeManager().getActionTypesAsString());
        context.sendMessage("");
        context.sendTranslated("&6Lookup&f/&6Rollback&f/&6Restore&f-&6Parameters:");
        context.sendMessage("");
        context.sendTranslated(" &f-&6 action &7<actionType> &flike &3a block-break &f(See full list above)");
        context.sendTranslated(" &f-&6 radius &7<radius>&f or &3sel&f, &3global&f, &3player:<radius>");
        context.sendTranslated(" &f-&6 player &7<users>&f like &3p Faithcaio ");
        context.sendTranslated(" &f-&6 entity &7<entities>&f like &3e sheep");
        context.sendTranslated(" &f-&6 block &7<blocks>&f like &3b stone &for &3b 1");
        context.sendTranslated(" &f-&6 since &7<time>&f default is 3 days");
        context.sendTranslated(" &f-&6 before &7<time>");
        context.sendTranslated(" &f-&6 world &7<world>&f default is your current world");

        context.sendMessage("");
        context.sendTranslated("Use &6!&f to exclude the parameters instead of including them.");
    }

    /**
     * Returns the Selection or null if nothing is selected
     *
     * @param context
     * @return
     */
    //TODO change return to a selection See WE how they did it
    private Location getSelection(ParameterizedContext context)
    {
        if (!context.hasFlag("sel"))
        {
            throw new IllegalStateException("Did not choose selection!");
        }
        return null;
    }

    @Command(
        desc = "Queries a lookup in the database\n    " +
            "Show availiable parameters with &6/lookup params",
        usage = "[page <page>] [parameters]",
    flags = {
        @Flag(longName = "coordinates", name = "coords"),
        @Flag(longName = "detailed", name = "det"),
        @Flag(longName = "nodate", name = "nd"),
        @Flag(longName = "descending", name = "desc")
    },
    params = {
        @Param(names = {"action","a"}, completer = ActionTypeCompleter.class),
        @Param(names = {"radius","r"}),//<radius> OR selection|sel OR global|g OR player|p:<radius>
        @Param(names = {"user","player","p"}, completer = PlayerListCompleter.class),
        @Param(names = {"block","b"}, completer = MaterialListCompleter.class),
        @Param(names = {"entity","e"}),
        @Param(names = {"since","time","t"}), // if not given default since 3d
        @Param(names = {"before"}),
        @Param(names = {"world","w","in"}, type = World.class, completer = WorldCompleter.class),

        @Param(names = {"limit","pagelimit"},type = Integer.class),
        @Param(names = {"page"},type = Integer.class),

        @Param(names = "params", completer = ActionTypeCompleter.class)
    }, min = 0, max = 1)
    // TODO param for filter / chat / command / signtexts
    public void lookup(ParameterizedContext context)
    {
        if ((context.hasArg(0) && context.getString(0).equalsIgnoreCase("params"))
            || context.hasParam("params"))
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
            LogAttachment attachment = user.attachOrGet(LogAttachment.class,this.module);
            ShowParameter show = attachment.getLastShowParameter(); // gets last OR new Showparameter
            Lookup lookup = attachment.getLastLookup();
            if (!this.fillShowOptions(attachment, lookup, show, context)) // /lookup show / page <page>
            {
                return;
            }
            lookup = attachment.createNewCommandLookup();
            QueryParameter params = lookup.getQueryParameter();
            if (! (this.readActions(params, context.getString("action"), user)
            && this.readRadius(params, context.getString("radius"), user)
            && this.readUser(params, context.getString("user"), user)
            && this.readBlocks(params, context.getString("block"), user)
            && this.readEntities(params, context.getString("entity"), user)
            && this.readWorld(params, context.getString("world"), context.hasParam("radius"), user)
            && this.readTimeSince(params, context.getString("since"), user)
            && this.readTimeBefore(params, context.getString("before"), user)))
            {
                return;
            }
            attachment.queueShowParameter(show);
            this.module.getLogManager().fillLookupAndShow(lookup, user);
        }
    }

    @Command(
        desc = "Performs a rollback", usage = "",
        flags = @Flag(longName = "preview", name = "pre"),
        params = {
            @Param(names = {"action","a"}, completer = ActionTypeCompleter.class),
            @Param(names = {"radius","r"}),//<radius> OR selection|sel OR global|g OR player|p:<radius>
            @Param(names = {"user","player","p"}, completer = PlayerListCompleter.class),
            @Param(names = {"block","b"}, completer = MaterialListCompleter.class),
            @Param(names = {"entity","e"}),
            @Param(names = {"since","time","t"}), // if not given default since 3d
            @Param(names = {"before"}),
            @Param(names = {"world","w","in"}, type = World.class, completer = WorldCompleter.class),
        }, min = 0, max = 1)
    public void rollback(ParameterizedContext context)
    {
        if (context.hasArg(0))
        {
            if (context.getString(0).equalsIgnoreCase("params"))
            {
                this.params(context);
            }
        }
        else if (context.getSender() instanceof User)
        {
            if (!context.hasParams())
            {
                context.sendTranslated("&cYou need to define parameters to rollback!");
                return;
            }
            User user = (User)context.getSender();
            LogAttachment attachment = user.attachOrGet(LogAttachment.class,this.module);
            Lookup lookup = attachment.createNewCommandLookup();
            QueryParameter params = lookup.getQueryParameter();
            if (! (this.readActions(params, context.getString("action"), user)
                && this.readRadius(params, context.getString("radius"), user)
                && this.readUser(params, context.getString("user"), user)
                && this.readBlocks(params, context.getString("block"), user)
                && this.readEntities(params, context.getString("entity"), user)
                && this.readWorld(params, context.getString("world"), context.hasParam("radius"), user)
                && this.readTimeSince(params, context.getString("since"), user)
                && this.readTimeBefore(params, context.getString("before"), user)))
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
    }

    @Command(
        desc = "Performs a rollback", usage = "",
        flags = @Flag(longName = "preview", name = "pre"),
        params = {
            @Param(names = {"action","a"}, completer = ActionTypeCompleter.class),
            @Param(names = {"radius","r"}),//<radius> OR selection|sel OR global|g OR player|p:<radius>
            @Param(names = {"user","player","p"}, completer = PlayerListCompleter.class),
            @Param(names = {"block","b"}, completer = MaterialListCompleter.class),
            @Param(names = {"entity","e"}),
            @Param(names = {"since","time","t"}), // if not given default since 3d
            @Param(names = {"before"}),
            @Param(names = {"world","w","in"}, type = World.class, completer = WorldCompleter.class),
        }, min = 0, max = 1)
    public void redo(ParameterizedContext context)
    {
        if (context.hasArg(0))
        {
            if (context.getString(0).equalsIgnoreCase("params"))
            {
                this.params(context);
            }
        }
        else if (context.getSender() instanceof User)
        {
            if (!context.hasParams())
            {
                context.sendTranslated("&cYou need to define parameters to redo!");
                return;
            }
            User user = (User)context.getSender();
            LogAttachment attachment = user.attachOrGet(LogAttachment.class,this.module);
            Lookup lookup = attachment.createNewCommandLookup();
            QueryParameter params = lookup.getQueryParameter();
            if (! (this.readActions(params, context.getString("action"), user)
                && this.readRadius(params, context.getString("radius"), user)
                && this.readUser(params, context.getString("user"), user)
                && this.readBlocks(params, context.getString("block"), user)
                && this.readEntities(params, context.getString("entity"), user)
                && this.readWorld(params, context.getString("world"), context.hasParam("radius"), user)
                && this.readTimeSince(params, context.getString("since"), user)
                && this.readTimeBefore(params, context.getString("before"), user)))
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
    }

    private boolean readTimeBefore(QueryParameter params, String beforeString, User user)
    {
        try
        { // TODO date too
            if (beforeString == null) return true;
            long before = StringUtils.convertTimeToMillis(beforeString);
            params.before(System.currentTimeMillis() - before);
            return true;
        }
        catch (TimeConversionException e)
        {
            user.sendTranslated("&6%s&c is not a valid time value!", beforeString);
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
                params.since(System.currentTimeMillis() - since);
            }
            else
            {
                params.since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)); // defaulted to last 30 days
            }
            return true;
        }
        catch (TimeConversionException e)
        {
            user.sendTranslated("&6%s&c is not a valid time value!", sinceString);
            return false;
        }
    }

    private boolean readWorld(QueryParameter params, String worldString, boolean hasRadius, User user)
    {
        if (worldString == null) return true;
        if (hasRadius)
        {
            user.sendTranslated("&cYou cannot define a radius or selection and a world.");
            return false;
        }
        World world = user.getServer().getWorld(worldString);
        if (world == null)
        {
            user.sendTranslated("&cUnkown world: &6%s", worldString);
            return false;
        }
        params.setWorld(world);
        return true;
    }

    private boolean readRadius(QueryParameter params, String radiusString, User user)
    {
        if (radiusString == null) return true;
        if (radiusString.equalsIgnoreCase("selection")|| radiusString.equalsIgnoreCase("sel"))
        {
            LogAttachment logAttachment = user.attachOrGet(LogAttachment.class, this.module);
            if (!logAttachment.applySelection(params))
            {
                user.sendTranslated("&cYou have to select a region first!");
                if (module.hasWorldEdit())
                {
                    user.sendTranslated("&eUse worldedit to select a cuboid region!");
                }
                else
                {
                    user.sendTranslated("&eUse this selection wand.");
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
                radiusUser = this.module.getCore().getUserManager().findUser(radiusString.substring(0,radiusString.indexOf(":")));
                if (radiusUser == null)
                {
                    user.sendTranslated("&cInvalid radius/location selection");
                    user.sendTranslated("&aThe radius parameter can be: <radius> | selection | global | <player>[:<radius>]");
                    return false;
                }
                radiusString = radiusString.substring(radiusString.indexOf(":")+1);
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
                    user.sendTranslated("&cInvalid radius/location selection");
                    user.sendTranslated("&aThe radius parameter can be: <radius> | selection | global | <player>[:<radius>]");
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
                context.sendTranslated("&eYour page-limit is to high! Showing 100 logs per page.");
                limit = 100;
            }
            show.pagelimit = limit;
        }
        if (context.hasArg(0))
        {
            if (context.getString(0).equalsIgnoreCase("show"))
            {
                if (lookup != null && lookup.queried())
                {
                    attachment.queueShowParameter(show);
                    lookup.show(attachment.getHolder());
                }
                else
                {
                    context.sendTranslated("&cYou have to do a query first!");
                }
                return false;
            }
            return false;
        }
        if (context.hasParam("page"))
        {
            if (lookup != null && lookup.queried())
            {
                Integer page = context.getParam("page",null);
                if (page == null)
                {
                    context.sendTranslated("&cInvalid page!");
                    return false;
                }
                show.page = page;
                attachment.queueShowParameter(show);
                lookup.show(attachment.getHolder());
            }
            else
            {
                context.sendTranslated("&cYou have to do a query first!");
            }
            return false;
        }
        return true;
    }

    private boolean readUser(QueryParameter params, String userString, User sender)
    {
        if (userString == null) return true;
        String[] users = StringUtils.explode(",", userString);
        for (String name : users)
        {
            boolean negate = name.startsWith("!");
            if (negate)
            {
                name = name.substring(1);
            }
            User user = this.module.getCore().getUserManager().getUser(name, false);
            if (user == null)
            {
                sender.sendTranslated("&cUser &2%s&c not found!", name);
                return false;
            }
            if (negate)
            {
                params.excludeUser(user.getId());
            }
            else
            {
                params.includeUser(user.getId());
            }
        }
        return true;
    }

    private boolean readBlocks(QueryParameter params, String block, User user)
    {
        if (block == null) return true;
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
                String sub = name.substring(name.indexOf(":")+1);
                try
                {
                    data = Byte.parseByte(sub);
                }
                catch (NumberFormatException ex)
                {
                    user.sendTranslated("&cInvalid BlockData: &6%s", sub);
                    return false;
                }
                name = name.substring(0,name.indexOf(":"));
            }
            Material material = Match.material().material(name);
            if (material == null)
            {
                user.sendTranslated("&cUnknown Material: &6%s", name);
                return false;
            }
            ImmutableBlockData blockData = new ImmutableBlockData(material, data);
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
        if (entity == null) return true;
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
                user.sendTranslated("&cUnknown EntityType: &6%s", name);
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
        if (input == null) return true;
        String[] inputs = StringUtils.explode(",", input);
        for (String actionString : inputs)
        {
            boolean negate = actionString.startsWith("!");
            if (negate)
            {
                actionString = actionString.substring(1);
            }
            Set<ActionType> actionTypes = this.actionTypeManager.getActionType(actionString);
            if (actionTypes == null)
            {
                user.sendTranslated("&cUnkown action-type: &6%s",actionString);
                return false;
            }
            if (negate)
            {
                for (ActionType actionType : actionTypes)
                {
                    params.excludeAction(actionType);
                }
            }
            else
            {
                for (ActionType actionType : actionTypes)
                {
                    params.includeAction(actionType);
                }
            }
        }
        return true;
    }
}

