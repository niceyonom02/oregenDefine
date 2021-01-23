import com.sun.org.apache.xpath.internal.operations.Or;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class OregenManager implements Listener, CommandExecutor {
    Inventory upgradeMenu;
    String upgradeTitle = "업그레이드";
    HashMap<UUID, Location> pos1List = new HashMap<>();
    HashMap<UUID, Location> pos2List = new HashMap<>();
    ArrayList<UUID> activePos1 = new ArrayList<>();
    ArrayList<UUID> activePos2 = new ArrayList<>();
    HashMap<UUID, ArrayList<Oregen>> oregens = new HashMap<>();
    HashMap<UUID, ArrayList<Permission>> userPermissions = new HashMap<>();
    HashMap<UUID, Permission> activedPermissions = new HashMap<>();
    HashMap<Permission, HashMap<Material, Integer>> probabilityMap = new HashMap<>();
    ArrayList<Permission> permissionObjects = new ArrayList<>();

    public OregenManager(){
        loadProbabilityMap();
        loadUserPermissions();
        loadActivedPermissions();
        loadOregens();

        setUpgradeMenu();
    }

    public Permission getPermissionWithPermissionName(String name){
        for(Permission permission : permissionObjects){
            if(permission.permission.equalsIgnoreCase(name)){
                return permission;
            }
        }
        return null;
    }

    public void setUpgradeMenu(){
        upgradeMenu = Bukkit.createInventory(null, 9, upgradeTitle);
    }

    public void loadProbabilityMap(){
        File generatorFile = new File(OregenCustom.oregenCustom.getDataFolder(), "generator.yml");
        YamlConfiguration generatorConfig = YamlConfiguration.loadConfiguration(generatorFile);

        if(generatorConfig.getConfigurationSection("generator") != null){
            for(String permission : generatorConfig.getConfigurationSection("generator").getKeys(false)){
                Bukkit.getLogger().info(permission);

                HashMap<Material, Integer> probability = new HashMap<>();
                if(generatorConfig.getConfigurationSection("generator." + permission) != null){
                    String code = generatorConfig.getString("generator." + permission + ".code");
                    Bukkit.getLogger().info(code);

                    Bukkit.getLogger().info(generatorConfig.getConfigurationSection("generator." + permission + ".materials") == null ? "null" : "not null");
                    for(String materialName : generatorConfig.getConfigurationSection("generator." + permission + ".materials").getKeys(false)){
                        int percentage = generatorConfig.getInt("generator." + permission + ".materials." + materialName);
                        Material material = Material.getMaterial(materialName);
                        probability.put(material, percentage);
                    }
                    Permission permission1 = new Permission();
                    permission1.code = code;
                    permission1.price = generatorConfig.getInt("generator." + permission + ".price");
                    permission1.permission = permission;
                    if(generatorConfig.getList("generator." + permission + ".requiredPermission") != null){
                        permission1.requiredPermission = (ArrayList<String>) generatorConfig.getList("generator." + permission + ".requiredPermission") ;
                    } else{
                        permission1.requiredPermission = new ArrayList<>();
                    }
                    permission1.upgradeSlot = generatorConfig.getInt("generator." + permission + ".slot");
                    permissionObjects.add(permission1);

                    probabilityMap.put(permission1, probability);
                }
            }
        }
        for(Permission permission : permissionObjects){
            Bukkit.getLogger().info(permission.toString());
        }
    }

    public void loadUserPermissions(){
        File permissionFile = new File(OregenCustom.oregenCustom.getDataFolder(), "permission.yml");
        YamlConfiguration permissionConfig = YamlConfiguration.loadConfiguration(permissionFile);

        if(permissionConfig.getConfigurationSection("users") != null){
            for(String uuidString : permissionConfig.getConfigurationSection("users").getKeys(false)){
                ArrayList<String> userperms = (ArrayList<String>) permissionConfig.getList("users." + uuidString + ".permission");

                ArrayList<Permission> permissionObs = new ArrayList<>();
                for(String pe : userperms){
                    if(getPermissionWithPermissionName(pe) != null){
                        permissionObs.add(getPermissionWithPermissionName(pe));
                    }
                }
                UUID uuid = UUID.fromString(uuidString);
                userPermissions.put(uuid, permissionObs);
            }
        }
    }

    public void saveUserPermission(){
        File permissionFile = new File(OregenCustom.oregenCustom.getDataFolder(), "permission.yml");
        YamlConfiguration permissionConfig = YamlConfiguration.loadConfiguration(permissionFile);

        permissionConfig.set("users", "");
        for(UUID uuid : userPermissions.keySet()){
            ArrayList<String> permStrings = new ArrayList<>();
            for(Permission permission : userPermissions.get(uuid)){

                permStrings.add(permission.permission);
            }

            permissionConfig.set("users." + uuid + ".permission", permStrings);
        }

        try{
            permissionConfig.save(permissionFile);
        }catch (Exception e){

        }
    }

    public void loadActivedPermissions(){
        File permissionFile = new File(OregenCustom.oregenCustom.getDataFolder(), "permission.yml");
        YamlConfiguration permissionConfig = YamlConfiguration.loadConfiguration(permissionFile);

        if(permissionConfig.getConfigurationSection("actived") != null){
            for(String uuidString : permissionConfig.getConfigurationSection("actived").getKeys(false)){
                String actived = permissionConfig.getString("actived." + uuidString);
                UUID uuid = UUID.fromString(uuidString);
                activedPermissions.put(uuid, getPermissionWithPermissionName(actived));
            }
        }
    }

    public void saveActivedPermission(){
        File permissionFile = new File(OregenCustom.oregenCustom.getDataFolder(), "permission.yml");
        YamlConfiguration permissionConfig = YamlConfiguration.loadConfiguration(permissionFile);

        permissionConfig.set("actived", "");
        for(UUID uuid : activedPermissions.keySet()){
            permissionConfig.set("actived." + uuid.toString(), activedPermissions.get(uuid).permission);
        }

        try{
            permissionConfig.save(permissionFile);
        }catch (Exception e){
        }
    }

    public void loadOregens(){
        File oregenFile = new File(OregenCustom.oregenCustom.getDataFolder(), "oregen.yml");
        YamlConfiguration oregenConfig = YamlConfiguration.loadConfiguration(oregenFile);

        if(oregenConfig.getConfigurationSection("oregen") != null){
            for(String uuidString : oregenConfig.getConfigurationSection("oregen").getKeys(false)){
                ArrayList<Oregen> oregenIndividual = new ArrayList<>();

                if(oregenConfig.getConfigurationSection("oregen." + uuidString) != null){
                    for(String oregenName : oregenConfig.getConfigurationSection("oregen." + uuidString).getKeys(false)){
                        Location loc1 = (Location) oregenConfig.get("oregen." + uuidString + "." + oregenName + ".loc1");
                        Location loc2 = (Location) oregenConfig.get("oregen." + uuidString + "." + oregenName + ".loc2");

                        Permission activatedPermission = activedPermissions.get(UUID.fromString(uuidString));
                        HashMap<Material, Integer> map = probabilityMap.get(activatedPermission);
                        Oregen oregen = new Oregen(map, UUID.fromString(uuidString), oregenName, loc1, loc2);
                        oregenIndividual.add(oregen);
                    }
                }

                oregens.put(UUID.fromString(uuidString), oregenIndividual);
            }
        }
    }

    public void saveOregen(){
        File oregenFile = new File(OregenCustom.oregenCustom.getDataFolder(), "oregen.yml");
        YamlConfiguration oregenConfig = YamlConfiguration.loadConfiguration(oregenFile);

        oregenConfig.set("oregen", "");
        for(UUID uuid : oregens.keySet()){
            for(Oregen oregen : oregens.get(uuid)){
                oregenConfig.set("oregen." + uuid.toString() + "." + oregen.nameTag + ".loc1", oregen.locations.getLoc1("skyblock"));
                oregenConfig.set("oregen." + uuid.toString() + "." + oregen.nameTag + ".loc2", oregen.locations.getLoc2("skyblock"));
            }
        }

        try{
            oregenConfig.save(oregenFile);
        }catch (Exception e){

        }
    }


    public void save(){
        saveOregen();
        saveActivedPermission();
        saveUserPermission();
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(PlayerInteractEvent e){
        if(e.getHand() == EquipmentSlot.OFF_HAND){
            if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
                if(e.getClickedBlock() != null && e.getClickedBlock().getType() != Material.AIR){
                    if(e.getClickedBlock().getLocation().getWorld().getName().equalsIgnoreCase("skyblock")){
                        if(activePos1.contains(e.getPlayer().getUniqueId())){
                            if(isInMyIsland(e.getPlayer(), e.getClickedBlock().getLocation())){
                                pos1List.put(e.getPlayer().getUniqueId(), e.getClickedBlock().getLocation());
                                e.getPlayer().sendMessage(OregenCustom.prefix + "포지션1을 지정하였습니다!");
                            } else{
                                e.getPlayer().sendMessage(OregenCustom.prefix + "자신의 섬만 지정할 수 있습니다!");
                            }
                        } else if(activePos2.contains(e.getPlayer().getUniqueId())){
                            if(isInMyIsland(e.getPlayer(), e.getClickedBlock().getLocation())){
                                pos2List.put(e.getPlayer().getUniqueId(), e.getClickedBlock().getLocation());
                                e.getPlayer().sendMessage(OregenCustom.prefix + "포지션 2를 지정하였습니다!");
                            } else{
                                e.getPlayer().sendMessage(OregenCustom.prefix + "자신의 섬만 지정할 수 있습니다!");
                            }
                        }
                    }
                }
            }
        }

    }

    @EventHandler
    public void upgradeClicked(PlayerInteractEvent e){
        if(e.getHand() == EquipmentSlot.HAND){
            if(e.getItem() != null){
                if(e.getItem().getType() != Material.AIR){
                    if(e.getItem().hasItemMeta()){
                        if(e.getItem().getItemMeta().hasDisplayName()){
                            String itemName = e.getItem().getItemMeta().getDisplayName();

                            for(Permission permission : permissionObjects){
                                if(itemName.equalsIgnoreCase(permission.code)){
                                    e.setCancelled(true);
                                    if(userPermissions.get(e.getPlayer().getUniqueId()).contains(permission)){
                                        e.getPlayer().sendMessage(OregenCustom.prefix + "이미 보유하고 있습니다!");
                                        return;
                                    }
                                    if(hasRequiredPermissions(e.getPlayer(), permission)){
                                        if(isPlayerhasEnoughItem(e.getPlayer(), e.getItem(), 1)){
                                            removePlayerItem(e.getPlayer(), e.getItem(), 1);
                                            userPermissions.get(e.getPlayer().getUniqueId()).add(permission);
                                            e.getPlayer().sendMessage(OregenCustom.prefix + "생성기를 추가하였습니다!");
                                        } else{
                                            e.getPlayer().sendMessage(OregenCustom.prefix + "권한이 부족합니다!");
                                        }
                                        break;
                                    } else{
                                        e.getPlayer().sendMessage(OregenCustom.prefix + "이 생성기를 사용하기 위한 권한이 부족합니다!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isPlayerhasEnoughItem(Player player, ItemStack itemStack, int minimumCount){
        int count = 0;
        for(int i = 0; i < player.getInventory().getSize(); i++){
            ItemStack targetItem = player.getInventory().getItem(i);

            if(targetItem != null){
                if(targetItem.isSimilar(itemStack)){
                    count += targetItem.getAmount();
                }
            }
        }

        Bukkit.getLogger().info(count + "");
        return count >= minimumCount;
    }

    private void removePlayerItem(Player player, ItemStack itemStack, int count){
        int removedCount = 0;
        for(int i = 0; i< player.getInventory().getSize();i++){
            ItemStack targetItem = player.getInventory().getItem(i);

            if(targetItem != null){
                if(targetItem.isSimilar(itemStack)){

                    if(removedCount + targetItem.getAmount() < count){
                        removedCount += targetItem.getAmount();
                        player.getInventory().setItem(i, null);
                    } else if(removedCount + targetItem.getAmount() == count){
                        player.getInventory().setItem(i, null);
                        break;
                    } else{
                        targetItem.setAmount((removedCount + targetItem.getAmount()) - count);
                        break;
                    }

                }
            }
        }
    }

    public boolean hasRequiredPermissions(Player player, Permission permission){
        for(String required : permission.requiredPermission){
            boolean found = false;
            for(Permission userHave : userPermissions.get(player.getUniqueId())){
                if(userHave.permission.equalsIgnoreCase(required)){
                    found = true;
                    break;
                }
            }
            if(!found) return false;
        }
        return true;
    }

    public boolean isInMyIsland(Player player, Location location){
        if(IslandPlugin.islandManager.getBelongedIsland(player) != null){
            if(IslandPlugin.islandManager.getBelongedIsland(player).region.isLocationInRegion(location)){
                return true;
            } else{
                return false;
            }
        } else{
            return false;
        }
    }

    public void help(Player player){
        player.sendMessage(OregenCustom.prefix + "광물 생성기 도움말");
        player.sendMessage(OregenCustom.prefix + "/cb pos1");
        player.sendMessage(OregenCustom.prefix + "/cb pos2");
        player.sendMessage(OregenCustom.prefix + "/cb list");
        player.sendMessage(OregenCustom.prefix + "/cb cancel");
        player.sendMessage(OregenCustom.prefix + "/cb list");
        player.sendMessage(OregenCustom.prefix + "/cb delete [이름]");
        player.sendMessage(OregenCustom.prefix + "/cb define [이름]");
        player.sendMessage(OregenCustom.prefix + "/cb perms");
        player.sendMessage(OregenCustom.prefix + "/cb change [바꿀 코드]");
    }

    public HashMap<Material, Integer> getProbability(Player player){
        if(probabilityMap.containsKey(activedPermissions.get(player.getUniqueId()))){
            return probabilityMap.get(activedPermissions.get(player.getUniqueId()));
        }
        return null;
    }

    public HashMap<Material, Integer> getMaterialMapActived(UUID uuid){
        if(activedPermissions.get(uuid) != null){
            return probabilityMap.get(activedPermissions.get(uuid));
        }


        return probabilityMap.get("default");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(!userPermissions.containsKey(e.getPlayer().getUniqueId())){
            ArrayList<Permission> perms = new ArrayList<>();
            perms.add(getPermissionWithPermissionName("default"));
            userPermissions.put(e.getPlayer().getUniqueId(), perms);
        }

        if(!activedPermissions.containsKey(e.getPlayer().getUniqueId())){
            activedPermissions.put(e.getPlayer().getUniqueId(), getPermissionWithPermissionName("default"));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] strings) {
        if(!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if(label.equalsIgnoreCase("cb")){
            if(strings.length > 0){
                switch (strings[0]){
                    case "change":
                        if(strings.length > 1){
                            for(Permission permission : permissionObjects){
                                if(strings[1].equalsIgnoreCase(permission.code)){
                                    if(activedPermissions.get(player.getUniqueId()).code.equalsIgnoreCase(permission.code)){
                                        player.sendMessage(OregenCustom.prefix + "이미 해당 펄미션이 적용중입니다!");
                                    } else{
                                        activedPermissions.put(player.getUniqueId(), permission);
                                        player.sendMessage(OregenCustom.prefix + "적용되었습니다!");
                                    }
                                }
                            }
                        } else{
                            help(player);
                        }
                        break;
                    case "perms":
                        if(activedPermissions.get(player.getUniqueId()) != null){
                            player.sendMessage(OregenCustom.prefix + "현재 펄미션: " + activedPermissions.get(player.getUniqueId()).code);
                            for(Permission permission : userPermissions.get(player.getUniqueId())){
                                if(permission.permission.equalsIgnoreCase(activedPermissions.get(player.getUniqueId()).permission)) continue;
                                player.sendMessage(OregenCustom.prefix + permission.code);

                            }
                        } else{
                            player.sendMessage(OregenCustom.prefix + "보유중인 펄미션이 없습니다!");
                        }
                        break;
                    case "pos1":
                        if(activePos2.contains(player.getUniqueId())){
                            activePos2.remove(player.getUniqueId());
                        }

                        activePos1.add(player.getUniqueId());
                        player.sendMessage(OregenCustom.prefix + "블럭을 클릭하여 포지션 1을 선택해주세요");
                        break;
                    case "pos2":
                        if(activePos1.contains(player.getUniqueId())){
                            activePos1.remove(player.getUniqueId());
                        }

                        activePos2.add(player.getUniqueId());
                        player.sendMessage(OregenCustom.prefix + "블럭을 클릭하여 포지션 2를 선택해주세요");
                        break;
                    case "list":
                        if(oregens.containsKey(player.getUniqueId()) && !oregens.get(player.getUniqueId()).isEmpty()){
                            for(Oregen oregen : oregens.get(player.getUniqueId())){
                                player.sendMessage(oregen.nameTag);
                            }
                        } else{
                            player.sendMessage(OregenCustom.prefix + "보유중인 생성기가 없습니다!");
                        }
                        break;
                    case "help":
                        help(player);
                        break;
                    case "cancel":
                        activePos1.remove(player.getUniqueId());
                        activePos2.remove(player.getUniqueId());
                        pos1List.remove(player.getUniqueId());
                        pos2List.remove(player.getUniqueId());
                        player.sendMessage(OregenCustom.prefix + "작업을 취소하였습니다");
                        break;
                    case "define":
                        if(strings.length >1){
                            if(pos1List.containsKey(player.getUniqueId()) && pos2List.containsKey(player.getUniqueId())){
                                if(oregens.get(player.getUniqueId()) != null){
                                    for(Oregen oregen : oregens.get(player.getUniqueId())){
                                        if(oregen.nameTag.equalsIgnoreCase(strings[1])){
                                            player.sendMessage(OregenCustom.prefix + "이미 존재하는 이름입니다!");
                                            return false;
                                        }
                                    }
                                }

                                if(!userPermissions.containsKey(player.getUniqueId())){
                                    ArrayList<Permission> temp = new ArrayList<>();
                                    temp.add(getPermissionWithPermissionName("default"));

                                    userPermissions.put(player.getUniqueId(), temp);
                                    activedPermissions.put(player.getUniqueId(),getPermissionWithPermissionName("default"));
                                }

                                HashMap<Material, Integer> probability = getProbability(player);

                                Oregen oregen = new Oregen(probability, player.getUniqueId(), strings[1], pos1List.get(player.getUniqueId()), pos2List.get(player.getUniqueId()));
                                if(oregens.containsKey(player.getUniqueId())){
                                    oregens.get(player.getUniqueId()).add(oregen);
                                } else{
                                    ArrayList<Oregen> or = new ArrayList<>();
                                    or.add(oregen);
                                    oregens.put(player.getUniqueId(), or);

                                }
                                activePos1.remove(player.getUniqueId());
                                activePos2.remove(player.getUniqueId());
                                pos1List.remove(player.getUniqueId());
                                pos2List.remove(player.getUniqueId());
                                player.sendMessage(OregenCustom.prefix + strings[1] + "생성기가 등록되었습니다");
                            } else{
                                player.sendMessage(OregenCustom.prefix + "먼저 구역을 지정해주세요");
                            }
                        } else{
                            help(player);
                        }
                        break;
                    case "delete":
                        if(strings.length >1){
                            if(oregens.containsKey(player.getUniqueId()) && !oregens.get(player.getUniqueId()).isEmpty()){
                                Oregen todelete = null;
                                for(Oregen oregen : oregens.get(player.getUniqueId())){
                                    if(oregen.nameTag.equalsIgnoreCase(strings[1])){
                                        oregen.delete();
                                        todelete = oregen;
                                        break;
                                    }
                                }

                                if(todelete != null){
                                    oregens.get(player.getUniqueId()).remove(todelete);
                                    player.sendMessage(OregenCustom.prefix + "해당 생성기를 삭제하였습니다");
                                } else{
                                    player.sendMessage(OregenCustom.prefix + "해당하는 이름의 생성기가 없습니다");
                                }
                            } else{
                                player.sendMessage(OregenCustom.prefix + "보유중인 생성기가 없습니다");
                            }
                        } else{
                            help(player);
                        }
                        break;
                    default:
                        help(player);
                }
            } else{
                help(player);
            }
        }
        return false;
    }
}
