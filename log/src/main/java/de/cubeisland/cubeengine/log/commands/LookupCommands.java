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
package de.cubeisland.cubeengine.log.commands;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAttachment;
import de.cubeisland.cubeengine.log.action.ActionType;
import de.cubeisland.cubeengine.log.action.ActionTypeManager;
import de.cubeisland.cubeengine.log.storage.BlockData;
import de.cubeisland.cubeengine.log.storage.Lookup;
import de.cubeisland.cubeengine.log.storage.QueryParameter;
import de.cubeisland.cubeengine.log.storage.ShowParameter;

public class LookupCommands
{
    private final Log module;
    private ActionTypeManager actionTypeManager;

    public LookupCommands(Log module)
    {
        this.module = module;
        this.actionTypeManager = module.getActionTypeManager();
    }

    private void params(CommandContext context)
    {
        context.sendTranslated("&6Registered ActionTypes:");
        context.sendMessage(this.module.getActionTypeManager().getActionTypesAsString());
        context.sendMessage("");
        context.sendTranslated("&6Lookup&f/&6Rollback&f/&6Restore&f-&6Parameters:");
        context.sendMessage("");
        context.sendTranslated(" &f-&6 action &7<actionType> &flike &3block-break &f(See full list below)");
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
        desc = "Queries a lookup in the database\n    Show availiable parameters with &6/lookup params",
        usage = "[show] [parameters]",
    flags = {
        @Flag(longName = "coordinates", name = "coords"),
        @Flag(longName = "detailed", name = "det"),
        @Flag(longName = "nodate", name = "nd"),
        @Flag(longName = "descending", name = "desc") //sort in descending order (default ascending) //TODO implement
    },
    params = {
        @Param(names = {"action","a"}),// !!must have tabcompleter for all register actionTypes
        @Param(names = {"radius","r"}),//<radius> OR selection|sel OR global|g OR player|p:<radius>
        @Param(names = {"user","player","p"}),
        @Param(names = {"block","b"}),
        @Param(names = {"entity","e"}),
        @Param(names = {"since","time","t"}), // if not given default since 3d
        @Param(names = {"before"}),
        @Param(names = {"world","w","in"}, type = World.class),

        @Param(names = {"limit","pagelimit"},type = Integer.class),
        @Param(names = {"page"},type = Integer.class),
    }, min = 0, max = 1)
    // TODO param to limit query results (default ~10k) ?
    public void lookup(ParameterizedContext context)
    {
        if (context.hasArg(0) && context.getString(0).equalsIgnoreCase("params"))
        {
            this.params(context);
        }
        else if (context.getSender() instanceof User)
        {
            ShowParameter show = new ShowParameter();
            User user = (User)context.getSender();
            LogAttachment attachment = user.attachOrGet(LogAttachment.class,this.module);
            Lookup lookup = attachment.getCommandLookup();
            show.showCoords = context.hasFlag("coords");
            show.showDate = !context.hasFlag("nd");
            show.compress = !context.hasFlag("det");
            if (context.hasParam("limit"))
            {
                Integer limit = context.getParam("limit", null);
                if (limit == null)
                {
                    return;
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
                    if (lookup.queried())
                    {
                        attachment.queueShowParameter(show);
                        lookup.show(user);
                    }
                    else
                    {
                        context.sendTranslated("&cYou have to do a query first!");
                    }
                    return;
                }
                return;
            }
            if (context.hasParam("page"))
            {
                if (lookup.queried())
                {
                    Integer page = context.getParam("page",null);
                    if (page == null)
                    {
                        context.sendTranslated("&cInvalid page!");
                        return;
                    }
                    show.page = page;
                    attachment.queueShowParameter(show);
                    lookup.show(user);
                }
                else
                {
                    context.sendTranslated("&cYou have to do a query first!");
                }
                return;
            }
            attachment.clearLookups(); // TODO only clear cmdlookup
            lookup = attachment.getCommandLookup();
            QueryParameter params = lookup.getQueryParameter();
            if (context.hasParam("action"))
            {
                if (!this.readActions(params, context.getString("action"), user))
                {
                    return;
                }
            }
            if (context.hasParam("radius"))
            {
                String radiusString = context.getString("radius");
                if (radiusString.equalsIgnoreCase("selection")|| radiusString.equalsIgnoreCase("sel"))
                {
                    LogAttachment logAttachment = user.attachOrGet(LogAttachment.class, this.module);
                    if (!logAttachment.applySelection(params))
                    {
                        context.sendTranslated("&cYou have to select a region first!");
                        if (module.hasWorldEdit())
                        {
                            context.sendTranslated("&eUse worldedit to select a cuboid region!");
                        }
                        else
                        {
                            context.sendTranslated("&eUse this selection wand."); // TODO give selectionwand for CE
                            LogCommands.giveSelectionTool(user);
                        }
                        return;
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
                            context.sendTranslated("&cInvalid radius/location selection");
                            context.sendTranslated("&aThe radius parameter can be: <radius> | selection | global | <player>[:<radius>]");
                            return;
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
                            context.sendTranslated("&cInvalid radius/location selection");
                            context.sendTranslated("&aThe radius parameter can be: <radius> | selection | global | <player>[:<radius>]");
                            return;
                        }
                        params.setWorld(radiusUser.getWorld());
                    }
                }
            }
            if (context.hasParam("user"))
            {
                if (!this.readUser(params, context.getString("user"), user))
                {
                    return;
                }
            }
            if (context.hasParam("block"))
            {
                if (!this.readBlocks(params, context.getString("block"), user))
                {
                    return;
                }
            }
            if (context.hasParam("entity"))
            {
                if (!this.readEntities(params, context.getString("entity"), user))
                {
                    return;
                }
            }
            // TODO date too
            try
            {
                if (context.hasParam("since"))
                {
                    long since = StringUtils.convertTimeToMillis(context.getString("since"));
                    params.since(System.currentTimeMillis() - since);
                }
                else
                {
                    params.since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)); // defaulted to last 30 days
                }
                if (context.hasParam("before"))
                {
                    long before = StringUtils.convertTimeToMillis(context.getString("since"));
                    params.before(System.currentTimeMillis() - before);
                }
            }
            catch (ConversionException e)
            {
                context.sendTranslated("&6%s&c is not a valid time value!", context.getString("since"));
                return;
            }
            if (context.hasParam("world"))
            {
                if (context.hasParam("radius"))
                {
                    context.sendTranslated("&cYou cannot define a radius or selection and a world.");
                }
                else
                {
                    World world = user.getServer().getWorld(context.getString("world"));
                    if (world == null)
                    {
                        context.sendTranslated("&cUnkown world: &6%s", context.getString("world"));
                        return;
                    }
                    params.setWorld(world);
                }
            }
            attachment.queueShowParameter(show);
            this.module.getLogManager().fillLookupAndShow(lookup, user);
        }
    }

    @Command(
        desc = "Performs a rollback", usage = "",
        flags = @Flag(longName = "preview", name = "pre"),
        params = {
            @Param(names = {"action","a"}),// !!must have tabcompleter for all register actionTypes
            @Param(names = {"radius","r"}),//<radius> OR selection|sel OR global|g OR player|p:<radius>
            @Param(names = {"user","player","p"}),
            @Param(names = {"block","b"}),
            @Param(names = {"entity","e"}),
            @Param(names = {"since","time","t"}), // if not given default since 3d
            @Param(names = {"before"}),
            @Param(names = {"world","w","in"}, type = World.class),
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
            attachment.clearLookups(); // TODO only clear cmdlookup
            Lookup lookup = attachment.getCommandLookup();
            QueryParameter params = lookup.getQueryParameter();
            if (context.hasParam("action"))
            {
                if (!this.readActions(params, context.getString("action"), user))
                {
                    return;
                }
            }
            if (context.hasParam("radius"))
            {
                String radiusString = context.getString("radius");
                if (radiusString.equalsIgnoreCase("selection")|| radiusString.equalsIgnoreCase("sel"))
                {
                    LogAttachment logAttachment = user.attachOrGet(LogAttachment.class, this.module);
                    if (!logAttachment.applySelection(params))
                    {
                        context.sendTranslated("&cYou have to select a region first!");
                        if (module.hasWorldEdit())
                        {
                            context.sendTranslated("&eUse worldedit to select a cuboid region!");
                        }
                        else
                        {
                            context.sendTranslated("&eUse this selection wand.");
                            LogCommands.giveSelectionTool(user);
                        }
                        return;
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
                            context.sendTranslated("&cInvalid radius/location selection");
                            context.sendTranslated("&aThe radius parameter can be: <radius> | selection | global | <player>[:<radius>]");
                            return;
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
                            context.sendTranslated("&cInvalid radius/location selection");
                            context.sendTranslated("&aThe radius parameter can be: <radius> | selection | global | <player>[:<radius>]");
                            return;
                        }
                        params.setWorld(radiusUser.getWorld());
                    }
                }
            }
            if (context.hasParam("user"))
            {
                if (!this.readUser(params, context.getString("user"), user))
                {
                    return;
                }
            }
            if (context.hasParam("block"))
            {
                if (!this.readBlocks(params, context.getString("block"), user))
                {
                    return;
                }
            }
            if (context.hasParam("entity"))
            {
                if (!this.readEntities(params, context.getString("entity"), user))
                {
                    return;
                }
            }
            // TODO date too
            try
            {
                if (context.hasParam("since"))
                {
                    long since = StringUtils.convertTimeToMillis(context.getString("since"));
                    params.since(System.currentTimeMillis() - since);
                }
                else
                {
                    params.since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30L)); // defaulted to last 30 days
                }
                if (context.hasParam("before"))
                {
                    long before =StringUtils.convertTimeToMillis(context.getString("since"));
                    params.before(System.currentTimeMillis() - before);
                }
            }
            catch (ConversionException e)
            {
                context.sendTranslated("&6%s&c is not a valid time value!", context.getString("since"));
                return;
            }
            if (context.hasParam("world"))
            {
                if (context.hasParam("radius"))
                {
                    context.sendTranslated("&cYou cannot define a radius or selection and a world.");
                }
                else
                {
                    World world = user.getServer().getWorld(context.getString("world"));
                    if (world == null)
                    {
                        context.sendTranslated("&cUnkown world: &6%s", context.getString("world"));
                        return;
                    }
                    params.setWorld(world);
                }
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

    private boolean readUser(QueryParameter params, String userString, User user)
    {
        String[] users = StringUtils.explode(",", userString);
        for (String name : users)
        {
            boolean negate = name.startsWith("!");
            if (negate)
            {
                name = name.substring(1);
            }
            User u = this.module.getCore().getUserManager().getUser(name, false);
            if (u == null)
            {
                user.sendTranslated("&cUser &2%s&c not found!", name);
                return false;
            }
            if (negate)
            {
                params.excludeUser(u.key);
            }
            else
            {
                params.includeUser(u.key);
            }
        }
        return true;
    }

    private boolean readBlocks(QueryParameter params, String block, User user)
    {
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
            BlockData blockData = new BlockData(material, data);
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
