package henry232323.wastelands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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

  Save Structure:
    Wastelands/config.yml
    - Wastelands world name
    - Base payout
    - Increase per unique kill
    - Rank-below decrease

    Wastelands/playerdata/UUID-b.dat
    - Current Bounties on that player

    Wastelands/playerdata/UUID-k.dat
    - Player level
    - Player kills
 */

public final class Wastelands extends JavaPlugin implements Listener {

    private int basePayout;
    private World wastelandsWorld;
    FileConfiguration config;
    private Economy economy;

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
            worldName = "world";
            config.options().copyDefaults(true);
            wastelandsWorld = getServer().getWorld(worldName);
            config.set("wastelands-world", worldName);
            saveConfig();
        }

        basePayout = config.getInt("base-payout");

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
    public void onPlayerDeath(PlayerDeathEvent event) {
        LivingEntity kent = event.getEntity().getKiller();
        if (kent != null) {
            Player killer = (Player) kent;
            Player player = event.getEntity();

            PlayerData playerData = PlayerData.load(this, killer);
            playerData.addKilled(player);

            economy.depositPlayer(killer, playerData.getPayout());
            killer.sendMessage(ChatColor.GREEN + String.format("$%s has been added to your account.", playerData.getPayout()));

            playerData.save();

            BountyData bdata = BountyData.load(this, player);
            bdata.clearBounties(killer);
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
                }
                else if (args.length != 1) {
                    return false;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    config = getConfig();

                    String worldName = config.getString("wastelands-world");
                    if (worldName == null) {
                        worldName = "world";
                        config.options().copyDefaults(true);
                        wastelandsWorld = getServer().getWorld(worldName);
                        config.set("wastelands-world", worldName);
                        saveConfig();
                    }

                    basePayout = config.getInt("base-payout");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "Wastelands reloaded");
            }

            if (command.getName().equalsIgnoreCase("setbounty")) {
                if (!(sender instanceof Player)) {
                    return false;
                }

                Player target = getServer().getPlayer(args[0]);
                float amount = Float.valueOf(args[1]);

                BountyData bdata = BountyData.load(this, target);
                bdata.addBounty((Player) sender, amount);
                bdata.save();

                return true;
            }

            if (command.getName().equalsIgnoreCase("bounties")) {
                if (args.length == 1) {
                    Player player = getServer().getPlayer(args[0]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.DARK_RED
                                 + "Player not found.");
                        return true;
                    }
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
                } else if (args.length == 0){
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

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Economy getEconomy() {
        return economy;
    }
}
