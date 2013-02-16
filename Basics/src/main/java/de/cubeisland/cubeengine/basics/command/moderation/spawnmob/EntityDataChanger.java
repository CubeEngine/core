package de.cubeisland.cubeengine.basics.command.moderation.spawnmob;

import gnu.trove.set.hash.THashSet;
import org.bukkit.DyeColor;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

public class EntityDataChanger<E,T>
{
    private THashSet<Class> classes = new THashSet<Class>();
    private EntityChanger changer;

    public static final EntityDataChanger<Pig,Boolean> PIGSADDLE =
            new EntityDataChanger<Pig,Boolean>(Pig.class,
                    new EntityChanger<Pig,Boolean>() {
                        @Override
                        public void applyEntity(Pig entity, Boolean value) {
                            entity.setSaddle(value);
                        }
                    });

    public static  final EntityDataChanger<Entity,Boolean> BABY =
            new EntityDataChanger<Entity,Boolean>(Ageable.class,Zombie.class,
                    new EntityChanger<Entity,Boolean>() {
                        @Override
                        public void applyEntity(Entity entity, Boolean value) {
                            if (entity instanceof Zombie)
                            {
                                ((Zombie) entity).setBaby(value);
                            }
                            else if (entity instanceof Ageable)
                            {
                                if (value)
                                    ((Ageable) entity).setBaby();
                                else
                                    ((Ageable) entity).setAdult();
                            }
                        }
                    });
    public static  final EntityDataChanger<Entity,Boolean> ANGRY =
            new EntityDataChanger<Entity,Boolean>(Wolf.class,PigZombie.class,
                    new EntityChanger<Entity,Boolean>() {
                        @Override
                        public void applyEntity(Entity entity, Boolean value) {
                            if (entity instanceof Wolf)
                            {
                                ((Wolf) entity).setAngry(true);
                            }
                            else if (entity instanceof PigZombie)
                            {
                                ((PigZombie) entity).setAngry(true);
                            }
                        }
                    });
    public static  final EntityDataChanger<Creeper,Boolean> POWERED =
            new EntityDataChanger<Creeper,Boolean>(Creeper.class,
                    new EntityChanger<Creeper,Boolean>() {
                        @Override
                        public void applyEntity(Creeper entity, Boolean value) {
                            entity.setPowered(value);
                        }
                    });
    public static  final EntityDataChanger<Entity,Boolean> SITTING =
            new EntityDataChanger<Entity,Boolean>(Wolf.class,Ocelot.class,
                    new EntityChanger<Entity,Boolean>() {
                        @Override
                        public void applyEntity(Entity entity, Boolean value) {
                            if (entity instanceof Wolf)
                            {
                                ((Wolf) entity).setSitting(true);
                            }
                            else if (entity instanceof Ocelot)
                            {
                                ((Ocelot) entity).setSitting(true);
                            }
                        }
                    });

    public static  final EntityDataChanger<Tameable,AnimalTamer> TAME =
            new EntityDataChanger<Tameable,AnimalTamer>(Tameable.class,
                    new EntityChanger<Tameable,AnimalTamer>() {
                        @Override
                        public void applyEntity(Tameable entity, AnimalTamer value) {
                            entity.setTamed(true);
                            if (value != null)
                            {
                                entity.setOwner(value);
                            }
                        }
                    });

    public static  final EntityDataChanger<Sheep,DyeColor> SHEEP_COLOR =
            new EntityDataChanger<Sheep,DyeColor>(Sheep.class,
                    new EntityChanger<Sheep,DyeColor>() {
                        @Override
                        public void applyEntity(Sheep entity, DyeColor value) {
                            entity.setColor(value);
                        }
                    });

    public static  final EntityDataChanger<Slime,Integer> SLIME_SIZE =
            new EntityDataChanger<Slime,Integer>(Slime.class,
                    new EntityChanger<Slime,Integer>() {
                        @Override
                        public void applyEntity(Slime entity, Integer size) {
                            if (size > 0 && size <= 250)
                            {
                                entity.setSize(size);
                            }
                        }
                    });

    public static  final EntityDataChanger<Villager,Villager.Profession> VILLAGER_PROFESSION =
            new EntityDataChanger<Villager,Villager.Profession>(Villager.class,
                    new EntityChanger<Villager,Villager.Profession>() {
                        @Override
                        public void applyEntity(Villager entity, Villager.Profession value) {
                            entity.setProfession(value);
                        }
                    });

    public static final EntityDataChanger<Enderman,ItemStack> ENDERMAN_ITEM =
            new EntityDataChanger<Enderman,ItemStack>(Enderman.class,
                    new EntityChanger<Enderman,ItemStack>() {
                        @Override
                        public void applyEntity(Enderman entity, ItemStack value) {
                            if (value.getType().isBlock())
                            {
                                entity.setCarriedMaterial(value.getData());
                            }
                        }
                    });

    public static final EntityDataChanger<LivingEntity,Integer> HP =
            new EntityDataChanger<LivingEntity,Integer>(LivingEntity.class,
                    new EntityChanger<LivingEntity,Integer>() {
                        @Override
                        public void applyEntity(LivingEntity entity, Integer value) {
                            entity.setMaxHealth(value);
                            entity.setHealth(value);
                        }
                    });

    public static final EntityDataChanger<Zombie,Boolean> VILLAGER_ZOMBIE =
            new EntityDataChanger<Zombie,Boolean>(LivingEntity.class,
                    new EntityChanger<Zombie,Boolean>() {
                        @Override
                        public void applyEntity(Zombie entity, Boolean value) {
                            entity.setVillager(value);
                        }
                    });

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

    //TODO wolf collarColor
    //TODO skeleton type
    //TODO sheared sheeps
    //TODO catType

    private EntityDataChanger(Class clazz, EntityChanger<E,T> changer)
    {
        classes.add(clazz);
        this.changer = changer;
    }

    private EntityDataChanger(Class clazz1, Class clazz2, EntityChanger<E,T> changer)
    {
        classes.add(clazz1);
        classes.add(clazz2);
        this.changer = changer;
    }

    public boolean isAssignable(Entity entity)
    {
        for (Class clazz : classes)
        {
            if (clazz.isAssignableFrom(entity.getClass()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean applyTo(Entity entity, T value)
    {
        if (this.isAssignable(entity))
        {
            this.changer.applyEntity(entity, value);
            return true;
        }
        return false;
    }

    private static abstract class EntityChanger<E,T>
    {
        public abstract void applyEntity(E entity, T value);
    }
}
