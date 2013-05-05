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
package de.cubeisland.cubeengine.core.config.codec;

import java.io.IOException;
import java.io.OutputStream;

import de.cubeisland.cubeengine.core.config.Configuration;

import org.spout.nbt.stream.NBTOutputStream;

public class NBTCodecContainer extends CodecContainer<NBTCodecContainer,NBTCodec>
{
    public NBTCodecContainer(NBTCodec codec)
    {
        super(codec);
    }

    public NBTCodecContainer(NBTCodecContainer superContainer, String parentPath)
    {
        super(superContainer, parentPath);
    }

    @Override
    protected void writeConfigToStream(OutputStream stream, Configuration config) throws IOException
    {
        NBTOutputStream nbtOutputStream = new NBTOutputStream(stream, false);
        nbtOutputStream.writeTag(this.codec.convertMap(this));
        nbtOutputStream.flush();
        nbtOutputStream.close();
    }
}
