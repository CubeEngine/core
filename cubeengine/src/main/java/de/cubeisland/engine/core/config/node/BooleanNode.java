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
package de.cubeisland.engine.core.config.node;

public class BooleanNode extends Node<Boolean>
{
    private final boolean bool;

    public BooleanNode(boolean bool)
    {
        this.bool = bool;
    }

    @Override
    public Boolean getValue()
    {
        return this.bool;
    }

    @Override
    public String asText()
    {
        return String.valueOf(bool);
    }

    public static BooleanNode falseNode()
    {
        return new BooleanNode(false);
    }

    public static BooleanNode trueNode()
    {
        return new BooleanNode(true);
    }

    public static BooleanNode of(boolean bool)
    {
        return bool ? trueNode() : falseNode();
    }

    @Override
    public String toString()
    {
        return "BooleanNode=["+bool+"]";
    }
}
