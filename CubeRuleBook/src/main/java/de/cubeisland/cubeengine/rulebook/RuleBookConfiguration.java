/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.rulebook;

import de.cubeisland.cubeengine.core.CubeEngine;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Wolfi
 */

public class RuleBookConfiguration
{
   Rulebook ruleBook;
   Map<String, String> ruleMap = new HashMap<String,String>();
   Map<String, List<String>> convertedRuleMap = new HashMap<String, List<String>>();
   
   public RuleBookConfiguration(Rulebook ruleBook)
   {
       this.ruleBook = ruleBook;

       for(String language : this.ruleBook.getCore().getI18n().getLanguages())
       {
           try 
           {
               loadRules(language);
               convertText(language);
           } 
           catch (Exception e) 
           {
               ruleBook.error("can't read the file " + language + ".txt", e);
           }
       }
   }

    private void loadRules(String language) throws FileNotFoundException, IOException 
    {
        File file = new File(ruleBook.getFolder().getAbsolutePath() + File.separator + language + ".txt");
        if(!file.exists())
        {
            try
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(CubeEngine._(language, "rulebook", "You have to write down your rules here."));
                writer.close();
            } 
            catch(IOException e)
            {
                ruleBook.error("The languagefile \"" + language + "\" could not be created", e);
            }
        }
        
        String text = "";
        String line = null;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while( (line = reader.readLine()) != null)
        {
            text += (line + "\n");
        }
        reader.close();
        
        this.ruleMap.put(language, text);
    }
    
    private void convertText(String language)
    {
        List<String> lines = new ArrayList<String>(); 
        for(String line : getText(language).split("\n"))
        {
            while(line.length() > 255)
            {
                int index = line.substring(0, 254).lastIndexOf(" ");
                lines.add(line.substring(0, index));
                line = line.substring(index + 1);
            }
            lines.add(line);
        }
        
        List<String> convertedText = new ArrayList<String>();
        int line = 0;
        int page = 0;
        convertedText.add("");
        
        for(int i = 0; i < lines.size(); i++)
        {
            if(convertedText.get(page).length() + lines.get(i).length() > 255 || line > 11)
            {
                page++;
                convertedText.add("");
                line = 0;
            }
            if(line == 0)
            {
                convertedText.set(page, lines.get(i));
            }
            else
            {
                convertedText.set(page, convertedText.get(page) + "\n" + lines.get(i));
            }
            line++;
        }
        
        this.convertedRuleMap.put(language, convertedText);
    }
    
    public Collection<String> getLanguages()
    {
        return this.ruleMap.keySet();
    }
    
    public String getText(String language)
    {
        return this.ruleMap.get(language);
    }
    
    public String[] getPages(String language)
    { 
        List<String> pageList = this.convertedRuleMap.get(language);
        if(pageList == null) 
        {
            return null;
        }
        return pageList.toArray(new String[pageList.size()]);
    }
}
