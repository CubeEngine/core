package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.persistence.filesystem.ModuleConfiguration;
import de.cubeisland.cubeengine.core.persistence.filesystem.Option;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Faithcaio
 */
public class FlyConfiguration extends ModuleConfiguration
{
    @Option("debug")
    public boolean debugMode = false;
    @Option("mode.flycommand")
    public boolean flycommand = true; //if false fly command does not work
    @Option("mode.flyfeather")
    public boolean flyfeather = true; //if false feather fly does not work
    //TODO remove this test
    @Option("regions.use-scheduler")
    public boolean use_scheduler = true;
    @Option("regions.sql.use")
    public boolean sql_use = false;
    @Option("regions.sql.dsn")
    public String sql_dsn = "jdbc:mysql://localhost/worldguard";
    @Option("regions.sql.username")
    public String sql_username = "worldguard";
    @Option("regions.sql.password")
    public String sql_password = "worldguard";
    @Option("regions.enable")
    public boolean enable = true;
    @Option("regions.invincibility-removes-mobs")
    public boolean invincibility_removes_mobs = true;
    @Option("regions.high-frequency-flags")
    public boolean high_frequency_flags = true;
    @Option("regions.wand")
    public Integer wand = 287;
    @Option("regions.max-claim-volume")
    public Integer max_claim_volume = 30000;
    @Option("regions.claim-only-inside-existing-regions")
    public boolean claim_only_inside_existing_regions = false;
    @Option("regions.max-region-count-per-player")
    public HashMap<String, Integer> max_region_count_per_player = new HashMap<String, Integer>()
    {
        
        {
            put("default", 7);
        }
    };
    @Option("auto-invincible")
    public boolean auto_invincible = false;
    @Option("use-player-move-event")
    public boolean use_player_move_event = true;
    @Option("op-permissions")
    public boolean op_permissions = true;
    @Option("protection.item-durability")
    public boolean item_dura = true;
    @Option("protection.remove-infinite-stacks")
    public boolean rem_inf_stacks = true;
    @Option("protection.disable-xp-orb-drops")
    public boolean dis_xp_drop = true;
    @Option("protection.disable-obsidian-generators")
    public boolean dis_obsi_gen = true;
    @Option("simulation.sponge.enable")
    public boolean sponge_enable = true;
    @Option("simulation.sponge.radius")
    public Integer sponge_rad = 3;
    @Option("simulation.sponge.redstone")
    public boolean sponge_redst = true;
    @Option("default.pumpkin-scuba")
    public boolean pumpkin_scuba = false;
    @Option("default.disable-health-regain")
    public boolean disable_health_regain = false;
    @Option("physics.no-physics-gravel")
    public boolean no_physics_gravel = false;
    @Option("physics.no-physics-sand")
    public boolean no_physics_sand = false;
    @Option("physics.allow-portal-anywhere")
    public boolean allow_portal_anywhere = false;
    @Option("physics.disable-water-damage-blocks")
    public List<String> disable_water_damage_blocks = new ArrayList<String>();
    @Option("ignition.block-tnt")
    public boolean block_tnt = false;
    @Option("ignition.block-tnt-block-damage")
    public boolean block_tnt_block_damage = false;
    @Option("ignition.block-lighter")
    public boolean block_lighter = false;
    @Option("fire.disable-lava-fire-spread")
    public boolean disable_lava_fire_spread = false;
    @Option("fire.disable-all-fire-spread")
    public boolean disable_all_fire_spread = false;
    @Option("fire.disable-fire-spread-blocks")
    public List<String> disable_fire_spread_blocks = new ArrayList<String>();
    @Option("fire.lava-spread-blocks")
    public List<String> lava_spread_blocks = new ArrayList<String>();
    @Option("blacklist.use-as-whitelist")
    public boolean use_as_whitelist = false;
    @Option("blacklist.logging.console.enable")
    public boolean log_console = true;
    @Option("blacklist.logging.database.enable")
    public boolean log_db = false;
    @Option("blacklist.logging.database.dsn")
    public String log_db_dsn = "jdbc:mysql://localhost:3306/minecraft";
    @Option("blacklist.logging.database.user")
    public String log_db_user = "root";
    @Option("blacklist.logging.database.pass")
    public String log_db_pass = "";
    @Option("blacklist.logging.database.table")
    public String log_db_table = "blacklist_events";
    @Option("blacklist.logging.file.enable")
    public boolean log_file = false;
    @Option("blacklist.logging.file.path")
    public String log_file_path = "worldguard/logs/%Y-%m-%d.log";
    @Option("blacklist.logging.file.open-files")
    public Integer log_file_open_files = 10;
    @Option("summary-on-start")
    public boolean summary_on_start = false;

    public FlyConfiguration(Module module)
    {
        super(module);
    }
}
