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
package jsonprosessing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import de.cubeisland.engine.reflect.Reflector;

/**
 * Created by Tim on 26.09.2015.
 */
public class RefelctImport
{

    public void load()
    {
//        Create the Factory
        Reflector factory = new Reflector();
        Modules modules;
        InputStream in = null;
        try
        {
            in = new FileInputStream("launcher/src/main/resources/structure.json");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        modules = factory.load(Modules.class, inputStreamReader);
        ArrayList<DisplayedModule> displayedModules = modules.buildModules();
        return;
    }
}

