import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationData {
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;

    public boolean isInLocation(double x, double y, double z){
        return ( x >= minX && x <= maxX) && (y >= minY && y <= maxY) && (z >= minZ && z <= maxZ);
    }

    public Location getLoc1(String worldName){
        World world = Bukkit.getWorld(worldName);
        Location location = new Location(world, minX, minY, minZ);
        return location;
    }

    public Location getLoc2(String worldName){
        World world = Bukkit.getWorld(worldName);
        Location location = new Location(world, maxX, maxY, maxZ);
        return location;
    }
}
