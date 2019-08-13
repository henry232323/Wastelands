package henry232323.wastelands;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class PlayerData implements Serializable {
    private ArrayList<UUID> killed;
    private int kills;
    private int rankPoints;
    private int payout;
    private String playerUUID;

    transient private Wastelands plugin;

    private PlayerData(Wastelands plugin, Player player) {
        killed = new ArrayList<>();
        kills = 0;
        setPlugin(plugin);
        payout = plugin.getBasePayout();
        playerUUID = player.getUniqueId().toString();
    }

    public void save() {
        String path = String.format("%s.dat", playerUUID);
        File ranksdir = new File(plugin.getDataFolder(), "ranks");
        if (!ranksdir.exists()) {
            ranksdir.mkdir();
        }
        File file = new File(ranksdir, path);
        io.save(this, file);
    }

    static PlayerData load(Wastelands plugin, Player player) {
        String path = String.format("%s.dat", player.getUniqueId().toString());
        File playerdir = new File(plugin.getDataFolder(), "ranks");
        if (!playerdir.exists()) {
            playerdir.mkdir();
        }
        File file = new File(playerdir, path);
        PlayerData data = (PlayerData) io.load(file);
        if (data == null) {
            data = new PlayerData(plugin, player);
            data.save();
        }
        data.setPlugin(plugin);
        return data;
    }

    void setPlugin(Wastelands plugin) {
        this.plugin = plugin;
    }

    public void addKilled(Player killedPlayer) {
        UUID uuid = killedPlayer.getUniqueId();
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(UUID.fromString(playerUUID));
        if (!killed.contains(uuid)) {
            killed.add(uuid);
        }

        int oldRank = rankPoints;

        PlayerData killedData = PlayerData.load(plugin, killedPlayer);
        int eRank = killedData.rankPoints;
        int oldERank = eRank;

        if (rankPoints >= eRank) {
            rankPoints += Math.floor((eRank*5)-((rankPoints/10)*5)+10);
        } else {
            rankPoints += Math.floor((eRank*10)+(rankPoints * (eRank-rankPoints)));
        }

        kills += 1;
        save();

        if (eRank > 5) {
            killedData.rankPoints -=  Math.floor(eRank*((Math.random() * 1.6) + 1.35));
            killedData.save();
        }

        Leaderboard leaderboard = plugin.getLeaderboard();
        HashMap<String, Integer> scores = leaderboard.load();
        scores.put(playerUUID, rankPoints);
        scores.put(killedData.playerUUID, killedData.rankPoints);

        int newPercentile = percentToRank((float) rankPoints/scores.size());
        int newEPercentile = percentToRank((float) killedData.rankPoints/scores.size());
        int oldPercentile = percentToRank((float) oldRank/scores.size());
        int oldEPercentile = percentToRank((float) oldERank/scores.size());

        if (newPercentile != oldPercentile || kills == 1) {
            String rankname = String.format("Wastelands%s", numeral(newPercentile));
            String command = String.format("manuadd %s %s %s", player.getName(), rankname, "Wastelands");
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        }

        if (newEPercentile != oldEPercentile) {
            String rankname = String.format("Wastelands%s", numeral(newEPercentile));
            String command = String.format("manuadd %s %s %s", killedPlayer.getName(), rankname, "Wastelands");
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
        }

        leaderboard.save(scores);
    }

    private int percentToRank(float percent) {
        if (percent > .9) {
            return 3;
        }
        if (percent > .5) {
            return 2;
        }
        return 1;
    }

    private String numeral(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            s.append("I");
        }

        return s.toString();
    }

    public int getPayout() {
        return payout;
    }

    public int getKills() {
        return kills;
    }
}
