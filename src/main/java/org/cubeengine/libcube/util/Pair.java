/*
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
package org.cubeengine.libcube.util;

import java.util.Objects;

public class Pair<L, R>
{
    private L left;
    private R right;

    public Pair(L left, R right)
    {
        this.left = left;
        this.right = right;
    }

    public L getLeft()
    {
        return this.left;
    }

    public R getRight()
    {
        return this.right;
    }

    public void setLeft(L l)
    {
        this.left = l;
    }

    public void setRight(R r)
    {
        this.right = r;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final Pair<?, ?> pair = (Pair<?, ?>)o;
        return Objects.equals(left, pair.left) && Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(left, right);
    }

    @Override
    public String toString()
    {
        return "Pair(" + left + ", " + right + ')';
    }
}
