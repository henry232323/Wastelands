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

        return leaderboardByKey.indexOf(player.getUniqueId().toString());
    }

    public float[] getRankPercentile(OfflinePlayer player) {
        float[] rp = new float[2];
        HashMap<String, Integer> data = load();
        ArrayList<String> leaderboardByKey = new ArrayList<>(data.keySet());
        leaderboardByKey.sort(Comparator.comparing(data::get));

        int rank = leaderboardByKey.indexOf(player.getUniqueId().toString());
        rp[0] = rank;
        rp[1] = (float) rank / leaderboardByKey.size();
        return rp;
    }

    public int[] getRanks(OfflinePlayer[] players) {
        HashMap<String, Integer> data = load();
        ArrayList<String> leaderboardByKey = new ArrayList<>(data.keySet());
        leaderboardByKey.sort(Comparator.comparing(data::get));

        int[] ranks = new int[players.length];
        for (int i = 0; i < players.length; i++) {
            int prank = leaderboardByKey.indexOf(players[i].getUniqueId().toString());
            ranks[i] = prank;
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
