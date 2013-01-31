package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.CubeEngine;

public class Match {

    private MaterialMatcher materialMatcher;
    private MaterialDataMatcher materialDataMatcher;
    private EnchantMatcher enchantMatcher;
    private ProfessionMatcher professionMatcher;
    private EntityMatcher entityMatcher;
    private StringMatcher stringMatcher;



    public Match() {
        this.materialDataMatcher = new MaterialDataMatcher();
        this.materialMatcher = new MaterialMatcher(materialDataMatcher);
        this.enchantMatcher = new EnchantMatcher();
        this.professionMatcher = new ProfessionMatcher();
        this.entityMatcher = new EntityMatcher();
        this.stringMatcher = new StringMatcher();

    }

    public static MaterialMatcher material()
    {
        return CubeEngine.getCore().getMatcherManager().materialMatcher;
    }

    public static MaterialDataMatcher materialData()
    {
        return CubeEngine.getCore().getMatcherManager().materialDataMatcher;
    }

    public static EnchantMatcher enchant()
    {
        return CubeEngine.getCore().getMatcherManager().enchantMatcher;
    }

    public static ProfessionMatcher profession()
    {
        return CubeEngine.getCore().getMatcherManager().professionMatcher;
    }

    public static EntityMatcher entity()
    {
        return CubeEngine.getCore().getMatcherManager().entityMatcher;
    }

    public static StringMatcher string() {
        return CubeEngine.getCore().getMatcherManager().stringMatcher;
    }
}
