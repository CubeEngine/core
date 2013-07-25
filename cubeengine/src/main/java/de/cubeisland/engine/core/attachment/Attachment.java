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
package de.cubeisland.engine.core.attachment;

import de.cubeisland.engine.core.module.Module;

public abstract class Attachment<T extends AttachmentHolder>
{
    private Module module;
    private T holder;

    public final void attachTo(Module module, T holder)
    {
        this.module = module;
        if (this.holder != null)
        {
            throw new IllegalStateException("This attachment has already been attached!");
        }
        this.holder = holder;
        this.onAttach();
    }

    public Module getModule()
    {
        return this.module;
    }

    public T getHolder()
    {
        return this.holder;
    }

    public void onAttach()
    {}

    public void onDetach()
    {}
}
