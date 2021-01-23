import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class OregenCustom extends JavaPlugin {
    public static String prefix = ChatColor.GRAY + "[" + ChatColor.YELLOW + "N" + ChatColor.GRAY + "] ";
    public static OregenCustom oregenCustom;
    private OregenManager oregenManager;
    private IslandPlugin islandPlugin;

    @Override
    public void onEnable(){
        if(!getDataFolder().exists()){
            getDataFolder().mkdir();
        }

        File gen = new File(getDataFolder(), "generator.yml");
        if(!gen.exists()){
            saveResource("generator.yml", false);
        }

        File perm = new File(getDataFolder(), "permission.yml");
        if(!perm.exists()){
            saveResource("permission.yml", false);
        }

        File ore = new File(getDataFolder(), "oregen.yml");
        if(!ore.exists()){
            saveResource("oregen.yml", false);
        }

        oregenCustom = this;
        islandPlugin = (IslandPlugin) Bukkit.getPluginManager().getPlugin("islandPlugin");
        oregenManager = new OregenManager();
        Bukkit.getPluginManager().registerEvents(oregenManager, this);
        getCommand("cb").setExecutor(oregenManager);
    }

    @Override
    public void onDisable(){
        oregenManager.save();
    }

    public OregenManager getOregenManager(){
        return oregenManager;
    }

    public IslandPlugin getIslandPlugin(){
        return islandPlugin;
    }


}
