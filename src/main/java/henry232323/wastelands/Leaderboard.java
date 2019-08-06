package henry232323.wastelands;

import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class Leaderboard implements Serializable {
    private transient Wastelands plugin;

    Leaderboard (Wastelands plugin) {
        this.plugin = plugin;
    }

    public HashMap<String, Integer> load() {
        File file = new File(plugin.getDataFolder(), "leaderboard.dat");
        HashMap<String, Integer> data = (HashMap<String, Integer>) io.load(file);
        if (data == null) {
            return new HashMap<>();
        }
        return data;
    }

    public void save(HashMap<String, Integer> data) {
        File file = new File(plugin.getDataFolder(), "leaderboard.dat");
        io.save(data, file);
    }

    public int getRank(OfflinePlayer player) {
        HashMap<String, Integer> data = load();
        ArrayList<String> leaderboardByKey = new ArrayList<>(data.keySet());
        Collections.sort(leaderboardByKey);

        int rank = leaderboardByKey.indexOf(player.getUniqueId().toString());
        return (int) Math.floor((float) rank/leaderboardByKey.size() * 20) + 1;
    }

    public int[] getRanks(OfflinePlayer[] players) {
        HashMap<String, Integer> data = load();
        ArrayList<String> leaderboardByKey = new ArrayList<>(data.keySet());
        Collections.sort(leaderboardByKey);

        int[] ranks = new int[players.length];
        for (int i = 0; i < players.length; i++) {
            int prank = leaderboardByKey.indexOf(players[i].getUniqueId().toString());
            ranks[i] = (int) Math.floor((float) prank/leaderboardByKey.size() * 20) + 1;
        }

        return ranks;
    }

    public ArrayList<OfflinePlayer> getTop(int n) {
        HashMap<String, Integer> data = load();
        ArrayList<String> leaderboardByKey = new ArrayList<>(data.keySet());
        leaderboardByKey.sort(Comparator.comparing(data::get));

        ArrayList<OfflinePlayer> players = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            try {
                players.add(plugin.getServer().getOfflinePlayer(UUID.fromString(leaderboardByKey.get(i))));
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        return players;
    }
}
