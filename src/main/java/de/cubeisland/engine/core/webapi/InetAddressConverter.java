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
package de.cubeisland.engine.core.webapi;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.cubeisland.engine.converter.ConversionException;
import de.cubeisland.engine.converter.converter.SimpleConverter;
import de.cubeisland.engine.converter.node.Node;
import de.cubeisland.engine.converter.node.StringNode;

public class InetAddressConverter extends SimpleConverter<InetAddress>
{
    @Override
    public Node toNode(InetAddress object) throws ConversionException
    {
        String hostName = object.getHostName();
        return StringNode.of(hostName == null ? object.getHostAddress() : hostName);
    }

    @Override
    public InetAddress fromNode(Node node) throws ConversionException
    {
        try
        {
            return InetAddress.getByName(node.asText());
        }
        catch (UnknownHostException e)
        {
            throw ConversionException.of(this, node, "Unknown Host!", e);
        }
    }
}
