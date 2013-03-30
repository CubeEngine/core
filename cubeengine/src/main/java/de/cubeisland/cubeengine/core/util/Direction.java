package de.cubeisland.cubeengine.core.util;

public enum Direction
{
    N(23),
    NE(68),
    E(113),
    SE(158),
    S(203),
    SW(248),
    W(293),
    NW(338), ;
    private final int dir;

    private Direction(int dir)
    {
        this.dir = dir;
    }

    public static Direction matchDirection(int dir)
    {
        for (Direction direction : values())
        {
            if (dir < direction.dir)
            {
                return direction;
            }
        }
        return Direction.N;
    }
}
