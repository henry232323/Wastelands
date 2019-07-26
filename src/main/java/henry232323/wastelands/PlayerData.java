package henry232323.wastelands;

import org.bukkit.entity.Player;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/*
TODO:
  1. Track kills
  2. More money for each unique kill
  3. Decrease rank when getting killed by a high level
  4. Rank based off top n%
  5. Bounties
    - /bounty (playername) ($ amount)
    - bounty sends a broadcast with $$ amount
    - People can add  $$ to bounty
    - See current bounties for online players
    - Check bounties for a player

  Save Structure:
    Wastelands/config.yml
    Wastelands/playerdata/UUID-b.dat
    - Current Bounties on that player

    Wastelands/playerdata/UUID-k.dat
    - Player level
    - Player kills
 */

public class PlayerData implements Serializable {
    private ArrayList<UUID> killed;
    private int kills;
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

    public Wastelands getPlugin() {
        return plugin;
    }

    public void setPlugin(Wastelands plugin) {
        this.plugin = plugin;
    }

    public void addKilled(Player player) {
        UUID uuid = player.getUniqueId();
        if (!killed.contains(uuid)) {
            killed.add(uuid);
        }

        kills += 1;
    }

    public int getPayout() {
        return payout;
    }

    public int getKills() {
        return kills;
    }
}
