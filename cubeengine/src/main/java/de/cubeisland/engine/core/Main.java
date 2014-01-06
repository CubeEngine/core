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
package de.cubeisland.engine.core;

import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

public class Main
{
    public static void main(String[] args)
    {
        if (GraphicsEnvironment.isHeadless())
        {
            System.out.println("This is a Minecraft plugin and not a standalone program!");
        }
        else
        {
            JOptionPane.showConfirmDialog(null, "This is a Minecraft plugin and not a standalone program!", CubeEngine.class.getSimpleName(), JOptionPane.DEFAULT_OPTION);
        }
    }
}
