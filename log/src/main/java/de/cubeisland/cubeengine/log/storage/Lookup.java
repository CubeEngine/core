package de.cubeisland.cubeengine.log.storage;

import java.util.HashSet;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.ActionType;
import de.cubeisland.cubeengine.log.action.ActionType.Category;


public class Lookup implements Cloneable
{
    private final Log module;

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
        return lookup;
    }
    /**
     * Lookup only including container-actions
     */
    public static Lookup container(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(Category.INVENTORY.getActionTypes(), true); // include inv
        return lookup;
    }

    /**
     * Lookup only including kill-actions
     */
    public static Lookup kills(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(Category.KILL.getActionTypes(), true); // include kils
        return lookup;
    }

    /**
     * Lookup only including player-actions
     */
    public static Lookup player(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(Category.PLAYER.getActionTypes(), true); // include player
        return lookup;
    }

    /**
     * Lookup only including block-actions
     */
    public static Lookup block(Log module)
    {
        Lookup lookup = new Lookup(module);
        lookup.queryParameter = new QueryParameter(module);
        lookup.queryParameter.setActions(Category.BLOCK.getActionTypes(), true); // include block
        return lookup;
    }

    public void show(User user)
    {
        this.queryResults.show(user,queryParameter);
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
}
