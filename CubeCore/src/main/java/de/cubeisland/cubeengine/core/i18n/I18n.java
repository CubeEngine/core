package de.cubeisland.cubeengine.core.i18n;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.i18n.geoip.LookupService;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileManager;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.util.Map;

/**
 *
 * @author Phillip Schichtel
 */
public class I18n
{
    public static final String SOURCE_LANGUAGE = "en_US";
    private final LookupService lookupService;
    private final Map<String, String> languageMap;
    private final Map<String, Language> translationMap;

    public I18n(FileManager fileManager)
    {
        try
        {
            this.lookupService = new LookupService(fileManager.getResourceFile(CoreResource.GEOIP_DATABASE));
        }
        catch (IOException e)
        {
            throw new RuntimeException("CubeCore failed to load the GeoIP database!", e);
        }

        this.languageMap = new THashMap<String, String>();
        this.translationMap = new THashMap<String, Language>();

        this.loadLanguages(fileManager.getLanguageDir());
    }

    private void loadLanguages(File languageDir)
    {
        JsonParser parser = new JsonParser();
        FileFilter jsonFileFilter = new FileExtentionFilter("json");
        JsonObject root;
        File messageDir;
        Language language;
        for (File file : languageDir.listFiles(jsonFileFilter))
        {
            try
            {
                String name = file.getName();
                name = name.substring(0, name.lastIndexOf("."));
                
                language = new Language(name);

                root = parser.parse(new FileReader(file)).getAsJsonObject();

                for (JsonElement elem : root.getAsJsonArray("language"))
                {
                    if (elem.isJsonPrimitive())
                    {
                        
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
            catch (IllegalStateException e)
            {
                e.printStackTrace(System.err);
            }
        }
    }

    public String locateAddress(InetAddress address)
    {
        return this.lookupService.getCountry(address).getCode();
    }

    public String translate(String language, String category, String message, Object... params)
    {
        String translation = null;
        Language lang = this.translationMap.get(language);
        if (lang != null)
        {
            translation = lang.getTranslation(category, message);
        }
        return String.format(translation == null ? message : translation, params);
    }
}
