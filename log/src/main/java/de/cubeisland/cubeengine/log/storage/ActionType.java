package de.cubeisland.cubeengine.log.storage;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import gnu.trove.map.hash.TLongObjectHashMap;

public enum ActionType
{
    //BREAK
    BLOCK_BREAK(0x00)
    ,BLOCK_BURN(0x01)
    ,BLOCK_FADE(0x02)
    ,LEAF_DECAY(0x03)
    ,WATER_BREAK(0x04)
    ,LAVA_BREAK(0x05)
    ,ENTITY_BREAK(0x06)
    ,ENDERMAN_PICKUP(0x07)
    ,BUCKET_FILL(0x08)
    ,CROP_TRAMPLE(0x09)
    //EXPLOSIONS
    ,ENTITY_EXPLODE(0x10)
    ,CREEPER_EXPLODE(0x11)
    ,TNT_EXPLODE(0x12)
    ,FIREBALL_EXPLODE(0x13)
    ,ENDERDRAGON_EXPLODE(0x14)
    ,WITHER_EXPLODE(0x15)
    ,TNT_PRIME(0x16)
    //PLACE etc.
    ,BLOCK_PLACE(0x20)
    ,LAVA_BUCKET(0x21)
    ,WATER_BUCKET(0x22)
    ,NATURAL_GROW(0x23)
    ,PLAYER_GROW(0x24)
    ,BLOCK_FORM(0x25) //ice/snow/lava-water
    ,ENDERMAN_PLACE(0x26)
    ,ENTITY_FORM(0x27)//snow-golem snow
    // SPREAD/ IGNITION
    ,FIRE_SPREAD(0x30)
    ,FIREBALL_IGNITE(0x31)
    ,LIGHTER(0x32)
    ,LAVA_IGNITE(0x33)
    ,LIGHTNING(0x34)
    ,BLOCK_SPREAD(0x35)
    ,WATER_FLOW(0x36)
    ,LAVA_FLOW(0x37)
    ,OTHER_IGNITE(0x38)
    //BLOCKCHANGES
    ,BLOCK_SHIFT(0x40) // moved by piston
    ,BLOCK_FALL(0x41)
    ,SIGN_CHANGE(0x42)
    ,SHEEP_EAT(0x43)
    ,BONEMEAL_USE(0x44)
    ,LEVER_USE(0x45)
    ,REPEATER_CHANGE(0x46)
    ,NOTEBLOCK_CHANGE(0x47)
    ,DOOR_USE(0x48)
    ,CAKE_EAT(0x49)
    ,COMPARATOR_CHANGE(0x4A)
    ,WORLDEDIT(0x4B)
    //INTERACTION (stuff that cannot be rolled back)
    ,CONTAINER_ACCESS(0x50)
    ,BUTTON_USE(0x51)
    ,FIREWORK_USE(0x52)
    ,VEHICLE_ENTER(0x53)
    ,VEHICLE_EXIT(0x54)
    ,POTION_SPLASH(0x55)
    ,PLATE_STEP(0x56)
    ,MILK_FILL(0x57)
    ,SOUP_FILL(0x58)
    //ENTITY-PLACE/BREAK
    ,VEHICLE_PLACE(0x60)
    ,HANGING_PLACE(0x61)
    ,VEHICLE_BREAK(0x62)
    ,HANGING_BREAK(0x63) // negative causer -> action-type e.g. BLOCK_BURN -1
    //KILLING
    ,PLAYER_KILL(0x70) // determined by causer ID not saved in DB
    ,ENTITY_KILL(0x71) // determined by causer ID not saved in DB
    ,BOSS_KILL(0x72)
    ,ENVIRONMENT_KILL(0x73) // determined by causer ID not saved in DB
    ,PLAYER_DEATH(0x74)
    ,MONSTER_DEATH(0x75)
    ,ANIMAL_DEATH(0x76)
    ,PET_DEATH(0x77)
    ,NPC_DEATH(0x78)
    ,BOSS_DEATH(0x79)
    ,OTHER_DEATH(0x7A)
    //other entity
    ,MONSTER_EGG_USE(0x80)
    ,NATURAL_SPAWN(0x81)
    ,SPAWNER_SPAWN(0x82)
    ,OTHER_SPAWN(0x83)
    ,ITEM_DROP(0x84)
    ,ITEM_PICKUP(0x85)
    ,XP_PICKUP(0x86)
    ,ENTITY_SHEAR(0x87)
    ,ENTITY_DYE(0x88)
    //chest-transactions
    ,ITEM_INSERT(0x90)
    ,ITEM_REMOVE(0x91)
    ,ITEM_TRANSFER(0x92)

    ,ITEM_CHANGE_IN_CONTAINER(0x93) // this ID is not used in the database
    //misc
    ,PLAYER_COMMAND(0xA0)
    //removed: ,CONSOLE_COMMAND(0xA1)
    ,PLAYER_CHAT(0xA2)
    ,PLAYER_JOIN(0xA3)
    ,PLAYER_QUIT(0xA4)
    ,PLAYER_TELEPORT(0xA5)
    ,ENCHANT_ITEM(0xA6)
    ,CRAFT_ITEM(0xA7);

    public final int value;
    public final String name;
    public final boolean noRollback;

    private static TLongObjectHashMap<ActionType> actions = new TLongObjectHashMap<ActionType>();

    static
    {
        for (ActionType actionType : ActionType.values())
        {
            actions.put(actionType.value,actionType);
        }
    }

    private ActionType(int value)
    {
        this.value = value;
        this.name = this.name().toLowerCase().replace("_","-");
        this.noRollback = false; // TODO
    }

    public static final Collection<ActionType> LOOKUP_BLOCK =
        EnumSet.of(BLOCK_BREAK, BLOCK_BURN, BLOCK_FADE, LEAF_DECAY, WATER_BREAK, LAVA_BREAK, ENTITY_BREAK,
                 ENDERMAN_PICKUP, BUCKET_FILL, CROP_TRAMPLE , ENTITY_EXPLODE, CREEPER_EXPLODE, TNT_EXPLODE,
                 FIREBALL_EXPLODE, ENDERDRAGON_EXPLODE, WITHER_EXPLODE, TNT_PRIME , BLOCK_PLACE,
                 LAVA_BUCKET, WATER_BUCKET, NATURAL_GROW, PLAYER_GROW, BLOCK_FORM , ENDERMAN_PLACE,
                 ENTITY_FORM , FIRE_SPREAD, FIREBALL_IGNITE, LIGHTER, LAVA_IGNITE, LIGHTNING, BLOCK_SPREAD,
                 WATER_FLOW, LAVA_FLOW, OTHER_IGNITE  , BLOCK_SHIFT , BLOCK_FALL, SIGN_CHANGE, SHEEP_EAT,
                 BONEMEAL_USE, LEVER_USE, REPEATER_CHANGE, NOTEBLOCK_CHANGE, DOOR_USE, CAKE_EAT,
                 COMPARATOR_CHANGE, WORLDEDIT, HANGING_BREAK, HANGING_PLACE);
    public static final Collection<ActionType> LOOKUP_PLAYER =
        EnumSet.of(BLOCK_BREAK, BUCKET_FILL, CROP_TRAMPLE, CREEPER_EXPLODE, TNT_PRIME, BLOCK_PLACE,
                LAVA_BUCKET, WATER_BUCKET, PLAYER_GROW, LIGHTER, BLOCK_FALL, BONEMEAL_USE, LEVER_USE,
                REPEATER_CHANGE, NOTEBLOCK_CHANGE, DOOR_USE, CAKE_EAT, COMPARATOR_CHANGE, WORLDEDIT,
                CONTAINER_ACCESS, BUTTON_USE, FIREWORK_USE, VEHICLE_ENTER, VEHICLE_EXIT, POTION_SPLASH,
                PLATE_STEP, MILK_FILL, SOUP_FILL, VEHICLE_PLACE, HANGING_PLACE, VEHICLE_BREAK,
                HANGING_BREAK, PLAYER_DEATH, MONSTER_DEATH, ANIMAL_DEATH, PET_DEATH, NPC_DEATH,
                BOSS_DEATH, OTHER_DEATH, MONSTER_EGG_USE, OTHER_SPAWN, ITEM_DROP, ITEM_PICKUP,
                XP_PICKUP, ENTITY_SHEAR, ENTITY_DYE, ITEM_INSERT, ITEM_REMOVE, PLAYER_COMMAND,
                PLAYER_CHAT, PLAYER_JOIN, PLAYER_QUIT, PLAYER_TELEPORT, ENCHANT_ITEM, CRAFT_ITEM);
    public static final Collection<ActionType> LOOKUP_KILLS =
        EnumSet.of(MONSTER_DEATH, ANIMAL_DEATH, PET_DEATH, NPC_DEATH, BOSS_DEATH, OTHER_DEATH);
    public static final Collection<ActionType> LOOKUP_CONTAINER =
        EnumSet.of(ITEM_INSERT, ITEM_REMOVE, ITEM_TRANSFER, CONTAINER_ACCESS);

    public static ActionType getById(int actionID)
    {
        return actions.get(actionID);
    }
}
