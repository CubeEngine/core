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
package de.cubeisland.engine.core.util;

import java.util.Locale;

public class Version implements Comparable<Version>
{
    public final static Version ZERO = new Version(0);
    public final static Version ONE = new Version(1);

    private final int major;
    private final int minor;
    private final int patch;
    private final String qualifier;
    private final int buildNumber;

    public Version(int major)
    {
        this(major, 0);
    }

    public Version(int major, int minor)
    {
        this(major, minor, 0);
    }

    public Version(int major, int minor, int patch)
    {
        this(major, minor, patch, null);
    }

    public Version(int major, int minor, int patch, String qualifier)
    {
        this(major, minor, patch, qualifier, 0);
    }

    public Version(int major, int minor, int patch, String qualifier, int buildNumber)
    {
        if (buildNumber < 0)
        {
            throw new IllegalArgumentException("The build number must be greater or equal than 0");
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.qualifier = qualifier == null ? null : qualifier.toUpperCase(Locale.US);
        this.buildNumber = buildNumber;
    }

    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public int getPatch()
    {
        return patch;
    }

    public String getQualifier()
    {
        return qualifier;
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public boolean isRelease()
    {
        return this.getQualifier() == null;
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
        int majorDiff = this.getMajor() - other.getMajor();
        if (majorDiff != 0)
        {
            return majorDiff;
        }
        int minorDiff = this.getMinor() - other.getMinor();
        if (minorDiff != 0)
        {
            return minorDiff;
        }
        int patchDiff = this.getPatch() - other.getPatch();
        if (patchDiff != 0)
        {
            return patchDiff;
        }
        if (this.getQualifier() == null && other.getQualifier() != null)
        {
            return 1;
        }
        if (other.getQualifier() == null && this.getQualifier() != null)
        {
            return -1;
        }

        if ((this.getQualifier() == null && other.getQualifier() == null) || this.getQualifier().equalsIgnoreCase(other.getQualifier()))
        {
            return this.getBuildNumber() - other.getBuildNumber();
        }

        return this.getQualifier().compareToIgnoreCase(other.getQualifier());
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
        result = 31 * result + patch;
        result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
        result = 31 * result + buildNumber;
        return result;
    }

    @Override
    public String toString()
    {
        String version = this.getMajor() + "." + this.getMinor() + "." + this.getPatch();
        if (this.getBuildNumber() > 0)
        {
            if (this.getQualifier() != null)
            {
                version += "-" + this.getQualifier().toUpperCase(Locale.US);
            }
            version += "-" + this.getBuildNumber();
        }
        else if (this.getQualifier() != null)
        {
            version += "-" + this.getQualifier().toUpperCase(Locale.US);
        }
        return version;
    }

    /**
     * This method parses a string into a Version object.
     * A Version instance will be returned in <b>any</b> case!
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
        int patch = 0;
        String qualifier = null;
        int buildNumber = 0;

        if (!string.isEmpty())
        {
            int dashIndex = string.lastIndexOf('-');
            if (dashIndex > -1)
            {
                qualifier = string.substring(dashIndex + 1);
                string = string.substring(0, dashIndex);
            }
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
                patch = readNumber(parts[2]);
            }
        }

        if (minor == 0 && patch == 0 && qualifier == null && buildNumber == 0)
        {
            switch (major)
            {
                case 0:
                    return ZERO;
                case 1:
                    return ONE;
            }
        }

        return new Version(major, minor, patch, qualifier, buildNumber);
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
