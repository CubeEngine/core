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
package org.cubeengine.libcube;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "cubeengine-core", name = "LibCube", version = "1.0.0",
        description = "Core Library for CubeEngine plugins",
        url = "http://cubeengine.org",
        authors = {"Anselm 'Faithcaio' Brehme", "Phillip Schichtel"})
public class LibCube
{
    private Module guiceModule;

    public LibCube()
    {
        this.guiceModule = new CubeEngineGuiceModule();
    }

    public Module getGuiceModule()
    {
        return guiceModule;
    }

    private class CubeEngineGuiceModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            // TODO
        }
    }
}
