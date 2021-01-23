import java.util.ArrayList;

public class Permission {
    public String permission;
    public String code;
    public ArrayList<String> requiredPermission = new ArrayList<>();
    public int upgradeSlot;
    public int price;

    @Override
    public String toString() {
        return "Permission{" +
                "permission='" + permission + '\'' +
                ", code='" + code + '\'' +
                ", requiredPermission=" + requiredPermission +
                ", upgradeSlot=" + upgradeSlot +
                ", price=" + price +
                '}';
    }
}
