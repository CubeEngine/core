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

import java.util.Date;
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
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.LogAttachment;
import de.cubeisland.cubeengine.log.action.ActionType;
import de.cubeisland.cubeengine.log.action.ActionTypeManager;
import de.cubeisland.cubeengine.log.storage.BlockData;
import de.cubeisland.cubeengine.log.storage.Lookup;
import de.cubeisland.cubeengine.log.storage.QueryParameter;

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
        // TODO pagelimit
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
        desc = "Changes regarding blocks", usage = "", flags = {
        @Flag(longName = "coordinates", name = "coords"),
        @Flag(longName = "detailed", name = "det"),
        @Flag(longName = "descending", name = "desc") //sort in descending order (default ascending)
    },
    params = {
        @Param(names = {"action","a"}),// !!must have tabcompleter for all register actionTypes
        @Param(names = {"radius","r"}),//<radius> OR selection|sel OR global|g OR player|p:<radius>
        @Param(names = {"user","player","p"}),
        @Param(names = {"block","b"}),
        @Param(names = {"entity","e"}),
        @Param(names = {"since","time","t"},type = Date.class), // if not given default since 3d
        @Param(names = {"before"},type = Date.class),
        @Param(names = {"world","w","in"}, type = World.class),
        @Param(names = {"limit","pagelimit"},type = Integer.class),

        @Param(names = {"page"},type = Integer.class),
    }, min = 0, max = 1)
    public void lookup(ParameterizedContext context)
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
            User user = (User)context.getSender();
            LogAttachment attachment = user.attachOrGet(LogAttachment.class,this.module);
            Lookup lookup = attachment.getCommandLookup();
            Integer limit;
            if (context.hasParam("limit"))
            {
                limit = context.getParam("limit", null);
                if (limit == null)
                {
                    return;
                }
            }
            else
            {
                limit = lookup.queried() ? lookup.getQueryParameter().getPageLimit() : 10;
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
                    lookup.getQueryParameter().setPageLimit(limit);
                    lookup.show(user, page);
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
            params.setPageLimit(limit);
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
                    // TODO set selection
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
                String[] users = StringUtils.explode(",", context.getString("user"));
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
                        context.sendTranslated("&cUser &2%s&c not found!", name);
                        return;
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
            }
            if (context.hasParam("block"))
            {
                String[] names = StringUtils.explode(",", context.getString("block"));
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
                            context.sendTranslated("&cInvalid BlockData: &6%s", sub);
                            return;
                        }
                        name = name.substring(0,name.indexOf(":"));
                    }
                    Material material = Match.material().material(name);
                    if (material == null)
                    {
                        context.sendTranslated("&cUnkown Material: &6%s", name);
                        return;
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
            }
            if (context.hasParam("entity"))
            {
                String[] names = StringUtils.explode(",", context.getString("entity"));
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
                        context.sendTranslated("&cUnknown EntityType: &6%s", name);
                        return;
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
            }
            // TODO time
            if (context.hasParam("since"))
            {

            }
            if (context.hasParam("before"))
            {

            }
            params.since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30L)); // defaulted to last 30 days
            // TODO time
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
            this.module.getLogManager().fillLookupAndShow(lookup, user);
        }
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
            ActionType actionType = this.actionTypeManager.getActionType(actionString);
            if (actionType == null)
            {
                user.sendTranslated("&cUnkown action-type: &6%s",actionString);
                return false;
            }
            if (negate)
            {
                params.excludeAction(actionType);
            }
            else
            {
                params.includeAction(actionType);
            }
        }
        return true;
    }
}
