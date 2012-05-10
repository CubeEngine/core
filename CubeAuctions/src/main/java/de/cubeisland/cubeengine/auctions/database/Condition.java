package de.cubeisland.cubeengine.auctions.database;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author CodeInfection
 */
public class Condition
{
    public final String condition;
    public final List<Object> params;
    
    public Condition(String condition, Object... params)
    {
        if (condition == null)
        {
            throw new IllegalArgumentException("condition must not be null!");
        }
        this.condition = condition;
        this.params = Arrays.asList(params);
    }
}
