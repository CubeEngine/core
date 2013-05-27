package de.cubeisland.cubeengine.log;

import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

import de.cubeisland.cubeengine.core.user.User;

public class Preview
{
    private Queue<BlockState> states = new LinkedList<BlockState>();

    public void add(BlockState state)
    {
        states.add(state);
    }

    public void send(User user)
    {
        // test limit preview changes to 1k
        Location location = new Location(null, 0, 0, 0);
        for (int i = 0 ; i < 1000; i++)
        {
            if (states.isEmpty()) return;
            BlockState poll = states.poll();
            poll.getLocation(location);
            user.sendBlockChange(location, poll.getType(), poll.getRawData());
        }
    }
}
