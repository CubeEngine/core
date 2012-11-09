package de.cubeisland.cubeengine.core.bukkit;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.NetHandler;
import net.minecraft.server.Packet;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PacketHackTest
{
    @Test
    public void testImplementedPacketMethods()
    {
        Method[] nhMethods = readMethods(NetHandler.class);
        Method[] ceMethods = readMethods(CubeEngineNetServerHandler.class);
        
        System.out.println("nh methods: " + nhMethods.length);
        System.out.println("ce methods: " + ceMethods.length);
        
        assertTrue("Not all packet methods are implemented.", nhMethods.length == ceMethods.length);
    }
    
    private static Method[] readMethods(final Class clazz)
    {
        List<Method> methods = new ArrayList<Method>();
        
        for (Method method : clazz.getDeclaredMethods())
        {
            if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers()) || Modifier.isAbstract(method.getModifiers()))
            {
                continue;
            }
            
            if (method.getParameterTypes().length != 1 || !Packet.class.isAssignableFrom(method.getParameterTypes()[0]))
            {
                continue;
            }
            
            methods.add(method);
        }
        
        return methods.toArray(new Method[methods.size()]);
    }
}