package henry232323.wastelands;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

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

public class BountyData implements Serializable {
    private ArrayList<Pair<UUID, Float>> bounties;
    private String playerUUID;

    transient private Wastelands plugin;

    BountyData(Player player) {
        bounties = new ArrayList<>();
        playerUUID = player.getUniqueId().toString();
    }

    void save() {
        String path = String.format("%s.dat", playerUUID);
        File bountydir = new File(plugin.getDataFolder(), "bounties");
        if (!bountydir.exists()) {
            bountydir.mkdir();
        }
        File file = new File(bountydir, path);

        io.save(this, file);
    }

    static BountyData load(Wastelands plugin, Player player) {
        String path = String.format("%s.dat", player.getUniqueId().toString());
        File bountydir = new File(plugin.getDataFolder(), "bounties");
        if (!bountydir.exists()) {
            bountydir.mkdir();
        }
        File file = new File(bountydir, path);

        BountyData data = (BountyData) io.load(file);
        if (data == null) {
            data = new BountyData(player);
            data.setPlugin(plugin);
            data.save();
        }
        data.setPlugin(plugin);
        return data;
    }

    public Wastelands getPlugin() {
        return plugin;
    }

    void setPlugin(Wastelands plugin) {
        this.plugin = plugin;
    }

    void addBounty(Player player, float amount) {
        EconomyResponse resp = plugin.getEconomy().withdrawPlayer(player, amount);
        if (!resp.transactionSuccess()) {
            player.sendMessage(ChatColor.RED + "You do not have sufficient funds");
            return;
        }
        player.sendMessage(ChatColor.GOLD + "Successfully set bounty");

        Pair<UUID, Float> item = new Pair<>(player.getUniqueId(), amount);
        bounties.add(item);
    }

    void clearBounties(Player killer) {
        if (bounties.size() == 0) {
            return;
        }
        float total = 0.0f;
        for (Pair<UUID, Float> bounty : bounties) {
            total += bounty.getValue();
        }
        bounties.clear();

        Player player = getServer().getPlayer(UUID.fromString(playerUUID));
        plugin.getEconomy().depositPlayer(killer, total);

        String playerName = player.getName();
        String killerName = killer.getName();
        String finalMessage = String.format(ChatColor.GOLD + "%s has claimed the bounties on %s for $%s total", killerName, playerName, total);
        killer.sendMessage(ChatColor.GREEN + String.format("$%s has been added to your account.", total));
        plugin.getServer().broadcastMessage(finalMessage);
        save();
    }

    public ArrayList<Pair<UUID,Float>> getBounties() {
        return bounties;
    }
}
