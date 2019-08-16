package henry232323.wastelands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
    - People can add $$ to bounty
    - See current bounties for online players
    - Check bounties for a player
  6. Save Structure:
    Wastelands/config.yml
    - Wastelands world name
    - Base payout
    - Increase per unique kill
    - Rank-below decrease
  7. Wastelands/playerdata/UUID-b.dat
    - Current Bounties on that player
  8. Wastelands/playerdata/UUID-k.dat
    - Player level
    - Player kills
  9. Cap level at 35 in Wastelands
 */

public final class Wastelands extends JavaPlugin implements Listener {

    private int basePayout;
    private World wastelandsWorld;
    FileConfiguration config;
    private Economy economy;
    private Leaderboard leaderboard;

    @Override
    public void onEnable() {
        // Plugin startup logic

        File dfolder = getDataFolder();
        if (!dfolder.exists()) {
            dfolder.mkdir();
        }

        getServer().getPluginManager().registerEvents(this, this);

        economy = Vault.setupEconomy(getServer());
        config = getConfig();

        String worldName = config.getString("wastelands-world");
        if (worldName == null) {
            worldName = getServer().getWorlds().get(0).getName();
            config.options().copyDefaults(true);
            wastelandsWorld = getServer().getWorld(worldName);
            config.set("wastelands-world", worldName);
            saveConfig();
        }

        basePayout = config.getInt("base-payout");
        leaderboard = new Leaderboard(this);

        saveConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public World getWastelandsWorld() {
        return wastelandsWorld;
    }

    public int getBasePayout() {
        return basePayout;
    }

    @EventHandler
    public void onExpGain(PlayerExpChangeEvent event) {
        if (event.getPlayer().getWorld().equals(wastelandsWorld)) {
            if (event.getPlayer().getLevel() >= 35 && event.getAmount() > 0) {
                event.setAmount(0);
                event.getPlayer().setLevel(35);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            Player player = event.getEntity();

            if (event.getEntity().getWorld() == wastelandsWorld) {
                PlayerData playerData = PlayerData.load(this, killer);
                playerData.addKilled(player);
                economy.depositPlayer(killer, playerData.getPayout());
                killer.sendMessage(ChatColor.GREEN + String.format("$%s has been added to your account.", playerData.getPayout()));

                playerData.save();
            }


            if (event.getEntity().hasPermission("wastelands.bounty.claim")) {
                BountyData bdata = BountyData.load(this, player);
                bdata.clearBounties(killer);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (command.getName().equalsIgnoreCase("wastelands")) {
                if (args.length == 0) {
                    String version = getDescription().getVersion();
                    sender.sendMessage(ChatColor.GOLD + "Wastelands Version " + version);
                    return true;
                } else if (args.length != 1) {
                    return false;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    config = getConfig();

                    String worldName = config.getString("wastelands-world");
                    if (worldName == null) {
                        worldName = getServer().getWorlds().get(0).getName();
                        config.options().copyDefaults(true);
                        wastelandsWorld = getServer().getWorld(worldName);
                        config.set("wastelands-world", worldName);
                    }

                    saveConfig();

                    basePayout = config.getInt("base-payout");
                    leaderboard = new Leaderboard(this);
                    sender.sendMessage(ChatColor.GOLD + "Wastelands reloaded");
                    return true;
                }
            }

            if (command.getName().equalsIgnoreCase("setbounty")) {
                if (!(sender instanceof Player)) {
                    return false;
                }

                Player target = getServer().getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.DARK_RED + "Player not found.");
                }
                float amount = Float.parseFloat(args[1]);

                BountyData bdata = BountyData.load(this, target);
                boolean success = bdata.addBounty((Player) sender, amount);
                if (!success) {
                    return true;
                }
                bdata.save();
                String playerName = target.getName();
                String killerName = sender.getName();
                getServer().broadcastMessage(String.format(ChatColor.GOLD + "%s has set a bounty on %s for $%s", killerName, playerName, amount));

                return true;
            }

            if (command.getName().equalsIgnoreCase("bounties")) {
                if (args.length == 1) {
                    OfflinePlayer player = getServer().getOfflinePlayer(args[0]);
                    BountyData bdata = BountyData.load(this, player);
                    if (bdata.getBounties().size() > 0) {
                        sender.sendMessage(String.format(ChatColor.GOLD + "Bounties for %s", player.getName()));
                        for (Pair<UUID, Float> bounty : bdata.getBounties()) {
                            UUID hunter = bounty.getKey();
                            String hname = getServer().getOfflinePlayer(hunter).getName();
                            sender.sendMessage(ChatColor.GOLD + String.format("$%s - %s", bounty.getValue(), hname));
                        }
                    } else {
                        sender.sendMessage(String.format(ChatColor.GOLD + "No bounties found for %s", player.getName()));
                    }
                    return true;
                } else if (args.length == 0) {
                    int count = 0;
                    for (Player player : getServer().getOnlinePlayers()) {
                        BountyData bdata = BountyData.load(this, player);
                        if (bdata.getBounties().size() > 0) {
                            sender.sendMessage(String.format(ChatColor.GOLD + "Bounties for %s", player.getName()));
                            for (Pair<UUID, Float> bounty : bdata.getBounties()) {
                                UUID hunter = bounty.getKey();
                                String hname = getServer().getOfflinePlayer(hunter).getName();
                                sender.sendMessage(ChatColor.GOLD + String.format("$%s - %s", bounty.getValue(), hname));
                            }
                            count++;
                        }
                    }

                    if (count == 0) {
                        sender.sendMessage(ChatColor.GOLD + "No bounties found for online players");
                    }
                    return true;
                }
            }
            if (command.getName().equalsIgnoreCase("leaderboard")) {
                ArrayList<OfflinePlayer> top = leaderboard.getTop(10);
                sender.sendMessage("§6Wastelands Leaderboard");
                for (int i = 0; i < top.size(); i++) {
                        sender.sendMessage(String.format("%s %s", i + 1, top.get(i).getName()));
                }

                return true;
            }

            if (command.getName().equalsIgnoreCase("checkrank")) {
                Player player = getServer().getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.DARK_RED + "Player not found.");
                    return true;
                }
                sender.sendMessage("§6Player Data");
                PlayerData target = PlayerData.load(this, player);
                sender.sendMessage("§6Kills: " + target.getKills());
                float[] rp = leaderboard.getRankPercentile(player);
                sender.sendMessage("§6Rank: #" + (int) (rp[0] + 1));
                sender.sendMessage("§6Percentile: Top " + (100 - Math.round(rp[1] * 100)) + "%");

                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Economy getEconomy() {
        return economy;
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
}
