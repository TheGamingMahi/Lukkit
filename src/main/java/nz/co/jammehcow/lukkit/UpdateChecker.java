package nz.co.jammehcow.lukkit;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import java.util.ArrayList;

public class UpdateChecker {
    private static final String GITHUB_ORG = "artex-development";
    
    public static void checkForUpdates(String pluginVersion) {
        try {
            HttpResponse<JsonNode> res = Unirest.get("https://api.github.com/repos/" + GITHUB_ORG + "/Lukkit/releases/latest").asJson();
            String tagName = res.getBody().getObject().getString("tag_name").replace("v", "");

            if (isOutOfDate(pluginVersion.split("-")[0], tagName)) {
                LukkitContainer.getInstance().getLogger().info("A new version of Lukkit has been released: " + tagName);
                LukkitContainer.getInstance().getLogger().info("You can download it from https://www.spigotmc.org/resources/lukkit.32599/ or https://github.com/jammehcow/Lukkit/releases");
            } else {
                LukkitContainer.getInstance().getLogger().info("You're up to date with the latest version of Lukkit.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static boolean isOutOfDate(String current, String remote) {
        ArrayList<Integer> currentVersion = new ArrayList<>(3);
        ArrayList<Integer> remoteVersion = new ArrayList<>(3);
        currentVersion.addAll(getIntegers(current.split("\\.")));
        remoteVersion.addAll(getIntegers(remote.split("\\.")));
        for (int i = 0; i < currentVersion.size(); i++) {
            if (currentVersion.get(i).compareTo(remoteVersion.get(i)) < 0) return true;
            else if (currentVersion.get(i).compareTo(remoteVersion.get(i)) > 0) return false;
        }
        return false;
    }

    private static ArrayList<Integer> getIntegers(String[] numbers) {
        ArrayList<Integer> ints = new ArrayList<>();
        for (String number : numbers) ints.add(Integer.parseInt(number));
        return ints;
    }
}
