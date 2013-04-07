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
package de.cubeisland.cubeengine.core.util;

import java.util.Locale;

public class Version implements Comparable<Version>
{
    public static Version ZERO = new Version(0);
    public static Version ONE = new Version(1);

    private final int major;
    private final int minor;
    private final int bugfix;
    private final String suffix;

    public Version(int major)
    {
        this(major, 0);
    }

    public Version(int major, int minor)
    {
        this(major, minor, 0);
    }

    public Version(int major, int minor, int bugfix)
    {
        this(major, minor, bugfix, null);
    }

    public Version(int major, int minor, int bugfix, String suffix)
    {
        this.major = major;
        this.minor = minor;
        this.bugfix = bugfix;
        this.suffix = suffix == null ? null : suffix.toUpperCase(Locale.US);
    }

    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public int getBugfix()
    {
        return bugfix;
    }

    public String getSuffix()
    {
        return suffix;
    }

    public boolean isRelease()
    {
        return this.getSuffix() == null;
    }

    public boolean isNewerThan(Version version)
    {
        if (version == null)
        {
            throw new NullPointerException("The version must not be null!");
        }
        return this.compareTo(version) > 0;
    }

    public boolean isOlderThan(Version version)
    {
        if (version == null)
        {
            throw new NullPointerException("The version must not be null!");
        }
        return this.compareTo(version) < 0;
    }

    @Override
    public int compareTo(Version other)
    {
        int major = this.getMajor() - other.getMajor();
        if (major != 0)
        {
            return major;
        }
        int minor = this.getMinor() - other.getMinor();
        if (minor != 0)
        {
            return minor;
        }
        int bugfix = this.getBugfix() - other.getBugfix();
        if (bugfix != 0)
        {
            return bugfix;
        }
        if (this.getSuffix() == null && other.getSuffix() != null)
        {
            return 1;
        }
        if (other.getSuffix() == null && this.getSuffix() != null)
        {
            return -1;
        }
        if (this.getSuffix() == null && other.getSuffix() == null)
        {
            return 0;
        }

        return this.getSuffix().compareToIgnoreCase(other.getSuffix());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Version))
        {
            return false;
        }

        return this.compareTo((Version)o) == 0;
    }

    @Override
    public int hashCode()
    {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + bugfix;
        result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        String version = this.getMajor() + "." + this.getMinor() + "." + this.getBugfix();
        if (this.getSuffix() != null)
        {
            version += "-" + this.getSuffix().toUpperCase(Locale.US);
        }
        return version;
    }

    /**
     * This method parses a string into a Version object.
     * A Version instance will be returend in <b>any</b> case!
     *
     * @param string the string to parse
     * @return a Version representation of the input string
     */
    public static Version fromString(String string)
    {
        if (string == null)
        {
            return ZERO;
        }
        string = string.trim().toUpperCase(Locale.US);
        if (string.startsWith("V"))
        {
            string = string.substring(1);
        }

        int major = 0;
        int minor = 0;
        int bugfix = 0;
        String suffix = null;

        if (!string.isEmpty())
        {
            int dashIndex = string.lastIndexOf('-');
            if (dashIndex > -1)
            {
                suffix = string.substring(dashIndex + 1);
                string = string.substring(0, dashIndex);
            }
            if (dashIndex > 0)
            {
                string = string.replace('-', '.').replace(',', '.').replace('_', '.').replace('/', '.').replace('\\', '.');
                String[] parts = string.split("\\.");
                if (parts.length > 0)
                {
                    major = readNumber(parts[0]);
                }
                if (parts.length > 1)
                {
                    minor = readNumber(parts[1]);
                }
                if (parts.length > 2)
                {
                    bugfix = readNumber(parts[2]);
                }
            }
        }

        return new Version(major, minor, bugfix, suffix);
    }

    private static int readNumber(String string)
    {
        try
        {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException ignored)
        {
            return 0;
        }
    }
}
