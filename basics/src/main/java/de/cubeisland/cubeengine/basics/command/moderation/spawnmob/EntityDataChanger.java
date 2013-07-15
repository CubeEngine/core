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
package de.cubeisland.cubeengine.basics.command.moderation.spawnmob;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;

import de.cubeisland.cubeengine.core.util.matcher.Match;

import static org.bukkit.entity.Villager.Profession;

public class EntityDataChanger<EntityInterface>
{
    private final Class<EntityInterface> clazz;
    private final EntityChanger<EntityInterface, ?> changer;
    public static Set<EntityDataChanger> entityDataChangers = new HashSet<EntityDataChanger>();

    public static final EntityDataChanger<Pig> PIG_SADDLE =
            new EntityDataChanger<Pig>(Pig.class,
                new BoolEntityChanger<Pig>("saddled")
                {
                    @Override
                    public void applyEntity(Pig entity, String input)
                    {
                        entity.setSaddle(this.getTypeValue(input));
                    }
                });

    public static final EntityDataChanger<Ageable> AGEABLE_BABY =
            new EntityDataChanger<Ageable>(Ageable.class,
                    new BoolEntityChanger<Ageable>("baby") {
                        @Override
                        public void applyEntity(Ageable entity, String input)
                        {
                            if (this.getTypeValue(input))
                            {
                                entity.setBaby();
                            }
                            else
                            {
                                entity.setAdult();
                            }
                        }
                    });

    public static final EntityDataChanger<Zombie> ZOMBIE_BABY =
        new EntityDataChanger<Zombie>(Zombie.class,
            new BoolEntityChanger<Zombie>("baby") {
                @Override
                public void applyEntity(Zombie entity, String input)
                {
                    entity.setBaby(this.getTypeValue(input));
                }
            });

    public static final EntityDataChanger<Zombie> ZOMBIE_VILLAGER =
        new EntityDataChanger<Zombie>(Zombie.class,
          new BoolEntityChanger<Zombie>("villager") {
              @Override
              public void applyEntity(Zombie entity, String input) {
                  entity.setVillager(this.getTypeValue(input));
              }
          });

    public static final EntityDataChanger<Wolf> WOLF_ANGRY =
            new EntityDataChanger<Wolf>(Wolf.class,
                    new BoolEntityChanger<Wolf>("angry") {
                        @Override
                        public void applyEntity(Wolf entity, String input) {
                            entity.setAngry(this.getTypeValue(input));
                        }
                    });

    public static final EntityDataChanger<PigZombie> PIGZOMBIE_ANGRY =
        new EntityDataChanger<PigZombie>(PigZombie.class,
                          new BoolEntityChanger<PigZombie>("angry") {
                              @Override
                              public void applyEntity(PigZombie entity, String input) {
                                  entity.setAngry(this.getTypeValue(input));
                              }
                          });

    public static final EntityDataChanger<Creeper> CREEPER_POWERED =
            new EntityDataChanger<Creeper>(Creeper.class,
                    new BoolEntityChanger<Creeper>("powered", "power", "charged") {
                        @Override
                        public void applyEntity(Creeper entity, String input) {
                            entity.setPowered(this.getTypeValue(input));
                        }
                    });

    public static final EntityDataChanger<Wolf> WOLF_SIT =
            new EntityDataChanger<Wolf>(Wolf.class,
                    new BoolEntityChanger<Wolf>("sitting", "sit") {
                        @Override
                        public void applyEntity(Wolf entity, String input) {
                            entity.setSitting(this.getTypeValue(input));
                        }
                    });

    public static final EntityDataChanger<Ocelot> OCELOT_SIT =
        new EntityDataChanger<Ocelot>(Ocelot.class,
         new BoolEntityChanger<Ocelot>("sitting", "sit") {
             @Override
             public void applyEntity(Ocelot entity, String input) {
                 entity.setSitting(this.getTypeValue(input));
             }
         });

    public static final EntityDataChanger<Skeleton> SKELETON_TYPE =
        new EntityDataChanger<Skeleton>(Skeleton.class,
         new BoolEntityChanger<Skeleton>("wither") {
             @Override
             public void applyEntity(Skeleton entity, String input)
             {
                 if (this.getTypeValue(input)) entity.setSkeletonType(SkeletonType.WITHER);
             }
         });

    public static final EntityDataChanger<Sheep> SHEEP_SHEARED =
        new EntityDataChanger<Sheep>(Sheep.class,
                                             new BoolEntityChanger<Sheep>("sheared") {
                                                 @Override
                                                 public void applyEntity(Sheep entity, String input)
                                                 {
                                                    entity.setSheared(this.getTypeValue(input));
                                                 }
                                             });


    public static final EntityDataChanger<Ocelot> OCELOT_TYPE =
        new EntityDataChanger<Ocelot>(Ocelot.class,
              new MappedEntityChanger<Ocelot, Type>() {
                  @Override
                  void fillValues()
                  {
                      this.map.put("black", Type.BLACK_CAT);
                      this.map.put("red", Type.RED_CAT);
                      this.map.put("orange", Type.RED_CAT);
                      this.map.put("white", Type.SIAMESE_CAT);
                      this.map.put("siamese", Type.SIAMESE_CAT);
                  }

                  @Override
                  public void applyEntity(Ocelot entity, String input)
                  {
                      Type catType = this.getTypeValue(input);
                      if (catType != null) entity.setCatType(catType);
                  }
              });

    public static  final EntityDataChanger<Colorable> SHEEP_COLOR =
            new EntityDataChanger<Colorable>(Colorable.class,
                    new EntityChanger<Colorable, DyeColor>() {
                        @Override
                        public void applyEntity(Colorable entity, String input)
                        {
                            DyeColor color = this.getTypeValue(input);
                            if (color != null) entity.setColor(color);
                        }
                        @Override
                        public DyeColor getTypeValue(String input)
                        {
                            return Match.materialData().colorData(input);
                        }
                    });

    public static final EntityDataChanger<Wolf> WOLF_COLLAR =
        new EntityDataChanger<Wolf>(Wolf.class,
                   new EntityChanger<Wolf, DyeColor>() {
                         @Override
                         public void applyEntity(Wolf entity, String input) {
                             DyeColor color = this.getTypeValue(input);
                             if (color != null) entity.setCollarColor(color);
                         }
                         @Override
                         public DyeColor getTypeValue(String input)
                         {
                             return Match.materialData().colorData(input);
                         }
                     });

    public static  final EntityDataChanger<Villager> VILLAGER_PROFESSION =
            new EntityDataChanger<Villager>(Villager.class,
                    new EntityChanger<Villager, Profession>() {
                        @Override
                        public void applyEntity(Villager entity, String input)
                        {
                            Profession typeValue = this.getTypeValue(input);
                            if (typeValue != null) entity.setProfession(typeValue);
                        }

                        @Override
                        public Profession getTypeValue(String input)
                        {
                            return Match.profession().profession(input);
                        }
                    });

    public static final EntityDataChanger<Enderman> ENDERMAN_ITEM =
            new EntityDataChanger<Enderman>(Enderman.class,
                    new EntityChanger<Enderman, ItemStack>() {
                        @Override
                        public void applyEntity(Enderman entity, String value) {
                            ItemStack typeValue = this.getTypeValue(value);
                            if (typeValue != null && typeValue.getType().isBlock())
                            {
                                entity.setCarriedMaterial(typeValue.getData());
                            }
                        }

                        @Override
                        public ItemStack getTypeValue(String input)
                        {
                            return Match.material().itemStack(input);
                        }
                    });

    public static final EntityDataChanger<Slime> SLIME_SIZE =
        new EntityDataChanger<Slime>(Slime.class,
                                             new EntityChanger<Slime,Integer>() {
                                                 @Override
                                                 public void applyEntity(Slime entity, String input) {
                                                     Integer typeValue = this.getTypeValue(input);
                                                     if (typeValue != null) entity.setSize(typeValue);
                                                 }

                                                 @Override
                                                 public Integer getTypeValue(String input)
                                                 {
                                                     // TODO tiny small big 0 2 4
                                                     try
                                                     {
                                                         Integer parsed = Integer.parseInt(input);
                                                         return (parsed > 0 && parsed <= 250) ? parsed : null;
                                                     }
                                                     catch (NumberFormatException ex)
                                                     {
                                                         return null;
                                                     }
                                                 }
                                             });

    public static final EntityDataChanger<LivingEntity> HP =
            new EntityDataChanger<LivingEntity>(LivingEntity.class,
                    new EntityChanger<LivingEntity, Integer>() {
                        @Override
                        public void applyEntity(LivingEntity entity, String input)
                        {
                            Integer typeValue = this.getTypeValue(input);
                            if (typeValue != null)
                            {
                                entity.setMaxHealth(typeValue);
                                entity.setHealth(typeValue);
                            }
                        }

                        @Override
                        public Integer getTypeValue(String input)
                        {
                            if (input.endsWith("hp"))
                            {
                                try
                                {
                                    return Integer.parseInt(input.substring(0,input.length()-2));
                                }
                                catch (NumberFormatException ex)
                                {}
                            }
                            return null;
                        }
                    });

    // TODO set tame_owner

    public static final EntityDataChanger<Tameable> TAMEABLE =
        new EntityDataChanger<Tameable>(Tameable.class,
                                                    new BoolEntityChanger<Tameable>("tamed") {
                                                        @Override
                                                        public void applyEntity(Tameable entity, String value) {
                                                            entity.setTamed(this.getTypeValue(value));
                                                        }
                                                    });


// TODO equipment
/*

    public static final EntityDataChanger<LivingEntity,ItemStack> EQUIP_ITEMINHAND =
            new EntityDataChanger<LivingEntity,ItemStack>(LivingEntity.class,
                    new EntityChanger<LivingEntity,ItemStack>() {
                        @Override
                        public void applyEntity(LivingEntity entity, ItemStack value) {
                            entity.getEquipment().setItemInHand(value);
                        }
                    });

    public static final EntityDataChanger<LivingEntity,ItemStack> EQUIP_HELMET =
            new EntityDataChanger<LivingEntity,ItemStack>(LivingEntity.class,
                    new EntityChanger<LivingEntity,ItemStack>() {
                        @Override
                        public void applyEntity(LivingEntity entity, ItemStack value) {
                            entity.getEquipment().setHelmet(value);
                        }
                    });

    public static final EntityDataChanger<LivingEntity,ItemStack> EQUIP_CHESTPLATE =
            new EntityDataChanger<LivingEntity,ItemStack>(LivingEntity.class,
                    new EntityChanger<LivingEntity,ItemStack>() {
                        @Override
                        public void applyEntity(LivingEntity entity, ItemStack value) {
                            entity.getEquipment().setChestplate(value);
                        }
                    });

    public static final EntityDataChanger<LivingEntity,ItemStack> EQUIP_LEGGINGS =
            new EntityDataChanger<LivingEntity,ItemStack>(LivingEntity.class,
                    new EntityChanger<LivingEntity,ItemStack>() {
                        @Override
                        public void applyEntity(LivingEntity entity, ItemStack value) {
                            entity.getEquipment().setLeggings(value);
                        }
                    });
    public static final EntityDataChanger<LivingEntity,ItemStack> EQUIP_BOOTS =
            new EntityDataChanger<LivingEntity,ItemStack>(LivingEntity.class,
                    new EntityChanger<LivingEntity,ItemStack>() {
                        @Override
                        public void applyEntity(LivingEntity entity, ItemStack value) {
                            entity.getEquipment().setBoots(value);
                        }
                    });
//*/


    private EntityDataChanger(Class<EntityInterface> clazz, EntityChanger<EntityInterface, ?> changer)
    {
        this.clazz = clazz;
        this.changer = changer;
        entityDataChangers.add(this);
    }

    @SuppressWarnings("unchecked")
    public boolean applyTo(Entity entity, String value)
    {
        if (canApply(entity))
        {
            this.changer.applyEntity((EntityInterface)entity, value);
            return true;
        }
        return false;
    }

    public boolean canApply(Entity entity)
    {
        return clazz.isAssignableFrom(entity.getClass());
    }

    private static abstract class EntityChanger<E,T>
    {
        public abstract void applyEntity(E entity, String input);
        public abstract T getTypeValue(String input);
    }

    private static abstract class BoolEntityChanger<E> extends EntityChanger<E, Boolean>
    {
        private List<String> names;
        protected BoolEntityChanger(String... names)
        {
            this.names = Arrays.asList(names);
        }

        @Override
        public Boolean getTypeValue(String input)
        {
            return Match.string().matchString(input, this.names) != null;
        }
    }

    private static abstract class ColorEntityChanger<E> extends EntityChanger<E, DyeColor>
    {
        @Override
        public DyeColor getTypeValue(String input)
        {
            return Match.materialData().colorData(input);
        }
    }

    private static abstract class MappedEntityChanger<E, T> extends EntityChanger<E, T>
    {
        protected Map<String, T> map = new HashMap<String, T>();

        protected MappedEntityChanger()
        {
            this.fillValues();
        }

        @Override
        public T getTypeValue(String input)
        {
            String match = Match.string().matchString(input, map.keySet());
            return match != null ? map.get(match) : null;
        }

        abstract void fillValues();
    }
}
