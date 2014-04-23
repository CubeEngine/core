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
package de.cubeisland.engine.core.util;

public class Triplet<F, S, T>
{
    private F first;
    private S second;
    private T third;

    public Triplet(F first, S second, T third)
    {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public F getFirst()
    {
        return first;
    }

    public S getSecond()
    {
        return second;
    }

    public T getThird()
    {
        return third;
    }

    public void setFirst(F first)
    {
        this.first = first;
    }

    public void setSecond(S second)
    {
        this.second = second;
    }

    public void setThird(T third)
    {
        this.third = third;
    }
}
