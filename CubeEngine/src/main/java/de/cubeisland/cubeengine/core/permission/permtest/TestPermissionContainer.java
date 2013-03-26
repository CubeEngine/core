package de.cubeisland.cubeengine.core.permission.permtest;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.permission.PermissionContainer;

public class TestPermissionContainer extends PermissionContainer
{
    // module.registerPermissions(new TestPermissionContainer.getPermissions())
    public TestPermissionContainer(Module module)
    {
        super(module);
        ALLADMINSTUFF.attach(USER_PREVENT).attach(FEATURE_FORADMIN);
        this.registerAllPermissions();
    }

    /*
    note: prepend cubeengine. to all of the perms below
    Children do generate * permissions
    test.* :
        test.alluserstuff
        test.alladminstuff
        test.user.cando
        test.user.prevent
        test.feature.foruser
        test.feature.foradmin

    Permission-bundles do not but can group multiple permissions together
    test.alluserstuff :
        test.user.cando
        test.feature.foruser
    test.alladminstuff
        test.user.prevent
        test.feature.foradmin

     */

    //this permission will not be registered because created with createAbstract...
    //but will exist as 'test.*' permission
    //because other use TEST as parent
    private static final Permission TEST = BASEPERM.createAbstractChild("test");

    public static final Permission ALLUSERSTUFF = TEST.createChild("alluserstuff");
    public static final Permission ALLADMINSTUFF = TEST.createChild("alladminstuff");

    public static final Permission USER_CANDO = TEST.createChild("user.cando").attachTo(ALLUSERSTUFF);
    public static final Permission FEATURE_FORUSER = TEST.createChild("feature.foruser").attachTo(ALLUSERSTUFF);

    public static final Permission USER_PREVENT = TEST.createChild("user.prevent");
    public static final Permission FEATURE_FORADMIN = TEST.createChild("feature.foradmin");

    // will not be included in any * perms
    public static final Permission DETACHED_PERM = TEST.createNew("detached");


    // --------------------------------------------------------------------
    // adding dynamic permissions later on

    public static final Permission TRAVEL = BASEPERM.createAbstractChild("travel"); // module permission
    public static final Permission WARP = TRAVEL.createAbstractChild("warp"); // container for dynamically created warps
    public static final Permission AWARP = WARP.createChild("awarp");

    public void dynamiclyAddingAPermissionFromOutside()
    {
        String newWarpName = "deepMine";
        Permission perm = WARP.createChild(newWarpName);
        perm.getPermission(); // == travel.warp.deepMine
        //registerPermission(perm);
    }

}
