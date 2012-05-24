package de.cubeisland.cubeengine.core.test;

import de.cubeisland.cubeengine.core.util.math.*;
import junit.framework.TestCase;
import org.junit.Test;

public class MathTest extends TestCase
{
    
    @Test
    public void testMath()
    {
        //2D Vector testing...
        Vector2 v21 = new Vector2(4,-2);
        Vector2 v22 = new Vector2(2,4);
        Vector2 v23 = new Vector2(4,8);
        Vector2 v24 = new Vector2(6,2);
        Vector2 v25 = new Vector2(0,5);
        Vector2 v26 = new Vector2(3,1);
        Vector2 v27 = new Vector2(1.5,2.5);
        assertTrue( v21.x == 4);
        assertTrue( v21.y == -2);
        assertTrue( v21.isOrthogonal(v22));
        assertTrue( !v21.isParallel(v22));
        assertTrue( v23.isParallel(v22));
        assertTrue( v22.dot(v23) == 2*4+4*8);
        assertTrue( v21.add(v22).equals(v24));
        assertTrue( v24.substract(v22).equals(v21));
        assertTrue( v22.multiply(2).equals(v23));
        assertTrue( v23.multiply(0.5).equals(v22));
        assertTrue( v23.divide(2).equals(v22));
        assertTrue( v22.divide(0.5).equals(v23));
        assertTrue( v21.squaredLength() == 20);
        assertTrue( v25.length() == 5);
        assertTrue( v22.distanceVector(v24).equals(v21));
        assertTrue( v22.squaredDistance(v24) == 20);
        assertTrue( v25.distance(v26) == 5);
        assertTrue( v21.crossAngle(v22) == 90*Math.PI/180);
        assertTrue( v21.normalize().length() == v22.normalize().length());
        assertTrue( v21.midpoint(v22).equals(v26));
        assertTrue( v27.toString().equals("(1.5|2.5)"));
    }
}
