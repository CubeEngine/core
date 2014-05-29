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
package de.cubeisland.engine.core.command;

import java.util.List;
import java.util.Set;

import static de.cubeisland.engine.core.util.StringUtils.explode;

public final class AliasCommand extends CubeCommand
{
    private static final String[] NO_ADDITION = new String[0];
    private final CubeCommand target;
    private final String[] prefix;
    private final String[] suffix;

    public AliasCommand(CubeCommand target, String name, Set<String> aliases, String prefix, String suffix)
    {
        super(target.getModule(), name, target.getDescription(), target.getContextFactory(), target.getPermission(), target.isCheckperm());
        this.setAliases(aliases);
        this.target = target;
        this.prefix = (prefix == null || prefix.isEmpty() ? NO_ADDITION : explode(" ", prefix));
        this.suffix = (suffix == null || suffix.isEmpty() ? NO_ADDITION : explode(" ", suffix));
    }

    @Override
    protected void addHelp()
    {
        // Help is already given by the target
    }

    public CubeCommand getTarget()
    {
        return this.target;
    }

    public String[] getPrefix()
    {
        return this.prefix;
    }

    public String[] getSuffix()
    {
        return this.suffix;
    }

    @Override
    public CommandResult run(CubeContext context)
    {
        return this.target.run(context);
    }

    @Override
    public List<String> tabComplete(CubeContext context)
    {
        return this.target.tabComplete(context);
    }

    @Override
    public Set<CubeCommand> getChildren()
    {
        return target.getChildren();
    }

    @Override
    public boolean hasChildren()
    {
        return target.hasChildren();
    }

    @Override
    public boolean hasChild(String name)
    {
        return target.hasChild(name);
    }

    @Override
    public void addChild(CubeCommand command)
    {
        target.addChild(command);
    }

    @Override
    public CubeCommand getChild(String name)
    {
        return target.getChild(name);
    }

    @Override
    public void removeChild(String name)
    {
        target.removeChild(name);
    }

    @Override
    public boolean isAsynchronous()
    {
        return target.isAsynchronous();
    }

    @Override
    public boolean isLoggable()
    {
        return target.isLoggable();
    }

    @Override
    public void setAsynchronous(boolean asynchronous)
    {
        target.setAsynchronous(asynchronous);
    }

    @Override
    public void setLoggable(boolean state)
    {
        target.setLoggable(state);
    }
}
