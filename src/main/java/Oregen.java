import com.sun.org.apache.xpath.internal.operations.Or;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Oregen implements Listener {
    public LocationData locations;
    private UUID starter;

    public final String nameTag;
    private Random random = new Random();

    public Oregen(HashMap<Material, Integer> probability, UUID starter, String nameTag, Location loc1, Location loc2){
        Bukkit.getPluginManager().registerEvents(this, OregenCustom.oregenCustom);

        this.nameTag = nameTag;
        this.starter = starter;

        LocationData locationData = new LocationData();
        locationData.minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        locationData.maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        locationData.minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        locationData.maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        locationData.minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        locationData.maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        locations = locationData;

        firstGenerate(probability);
    }

    public void delete(){
        BlockBreakEvent.getHandlerList().unregister(this);
    }

    public void firstGenerate(HashMap<Material, Integer> probability){

        World world = Bukkit.getWorld("skyblock");

        for(int x = locations.minX; x <= locations.maxX; x++){
            for(int y = locations.minY; y <= locations.maxY; y++){
                for(int z = locations.minZ; z <= locations.maxZ; z++){
                    Location location = new Location(world, x, y, z);
                    generateNewBlockImmediately(location, probability);
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        Location location = e.getBlock().getLocation();
        if(location.getWorld().getName().equalsIgnoreCase("skyblock")){
            if(locations.isInLocation(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ())){
                generateNewBlock(location, OregenCustom.oregenCustom.getOregenManager().getMaterialMapActived(e.getPlayer().getUniqueId()));
            }
        }
    }

    public void generateNewBlock(Location location, HashMap<Material, Integer> probability){
        Bukkit.getScheduler().scheduleSyncDelayedTask(OregenCustom.oregenCustom, new Runnable() {
            @Override
            public void run() {
                Material material = getRandomMaterial(probability);
                location.getBlock().setType(material);
            }
        }, 15L);

    }

    public void generateNewBlockImmediately(Location location, HashMap<Material, Integer> probability){
        Material material = getRandomMaterial(probability);
        Bukkit.getLogger().info(location.getBlock().getType().name());
        location.getBlock().setType(material);

    }

    public Material getRandomMaterial(HashMap<Material, Integer> probability){
        int randomInteger = random.nextInt(100) + 1;

        int accumulated = 0;
        Bukkit.getLogger().info(randomInteger + "");
        for(Material material : probability.keySet()){
            accumulated += probability.get(material);

            if(randomInteger <= accumulated){
                return material;
            }
        }
        return Material.COBBLESTONE;
    }
}
