package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.i18n.geoip.LookupService;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import de.cubeisland.cubeengine.core.persistence.filesystem.Resource;
import gnu.trove.map.hash.THashMap;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

/**
 *
 * @author Phillip Schichtel
 */
public class I18n
{
    private final LookupService lookupService;
    private final FileManager fileManager;
    private final Map<String, String> languageMap;

    public I18n(FileManager fileManager)
    {
        this.fileManager = fileManager;
        try
        {
            this.lookupService = new LookupService(this.fileManager.getResource(getClass(), Resource.GEOIP_DATABASE.getPath()));
        }
        catch (IOException e)
        {
            throw new RuntimeException("CubeCore failed to load the GeoIP database!", e);
        }

        this.languageMap = new THashMap<String, String>();
    }

    public String locateAddress(InetAddress address)
    {
        return this.lookupService.getCountry(address).getCode();
    }
}
