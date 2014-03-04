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
package de.cubeisland.engine.log.storage;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LogAttachment;
import de.cubeisland.engine.log.action.ActionType;
import de.cubeisland.engine.log.action.ActionTypeCategory;

public class Lookup implements Cloneable
{
    protected final Log module;

    private QueryParameter queryParameter;
    private QueryResults queryResults;

    private Lookup(Log module)
    {
        this.module = module;
    }

    /**
     * Lookup excluding nothing
     * @return
     */
    public static Lookup general(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(new HashSet<ActionType>(), false); // exclude none
        lookup.queryParameter.since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30));
        return lookup;
    }
    /**
     * Lookup only including container-actions
     */
    public static Lookup container(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(ActionTypeCategory.INVENTORY.getActionTypes(), true); // include inv
        lookup.queryParameter.since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30));
        return lookup;
    }

    /**
     * Lookup only including kill-actions
     */
    public static Lookup kills(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(ActionTypeCategory.KILL.getActionTypes(), true); // include kills
        lookup.queryParameter.since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30));
        return lookup;
    }

    /**
     * Lookup only including player-actions
     */
    public static Lookup player(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(ActionTypeCategory.PLAYER.getActionTypes(), true); // include player
        lookup.queryParameter.since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30));
        return lookup;
    }

    /**
     * Lookup only including block-actions
     */
    public static Lookup block(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(ActionTypeCategory.BLOCK.getActionTypes(), true); // include block
        lookup.queryParameter.since(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30));
        return lookup;
    }

    public void show(User user)
    {
        LogAttachment attachment = user.attachOrGet(LogAttachment.class, this.module);
        attachment.setLastLookup(this);
        this.queryResults.show(user,queryParameter,attachment.getShowParameter());
    }

    public void setQueryResults(QueryResults queryResults)
    {
        this.queryResults = queryResults;
    }

    public QueryParameter getQueryParameter()
    {
        return this.queryParameter;
    }

    @Override
    public Lookup clone()
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = queryParameter.clone();
        return lookup;
    }

    public boolean queried()
    {
        return this.queryResults != null;
    }

    public void rollback(User user, boolean preview)
    {
        LogAttachment attachment = user.attachOrGet(LogAttachment.class, this.module);
        attachment.setLastLookup(this);
        this.queryResults.rollback(attachment, preview);
        if (preview)
        {
            attachment.sendPreview();
        }
    }

    public void redo(User user, boolean preview)
    {
        LogAttachment attachment = user.attachOrGet(LogAttachment.class, this.module);
        attachment.setLastLookup(this);
        this.queryResults.redo(attachment, preview);
        if (preview)
        {
            attachment.sendPreview();
        }
    }
}
