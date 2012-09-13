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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Wolfi
 */

public class RuleBookConfiguration
{
   Rulebook ruleBook;
   Map<String, String> ruleMap = new HashMap<String,String>();
   
   public RuleBookConfiguration(Rulebook ruleBook)
   {
       this.ruleBook = ruleBook;

       for(String language : this.ruleBook.getCore().getI18n().getLanguages())
       {
           try 
           {
               loadRules(language);
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
    
    public Collection<String> getLanguages()
    {
        return this.ruleMap.keySet();
    }
    
    public String getText(String language)
    {
        return this.ruleMap.get(language);
    }
}
