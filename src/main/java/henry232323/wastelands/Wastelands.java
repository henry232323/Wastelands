package henry232323.wastelands;

import net.milkbowl.vault.economy.Economy;
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
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

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
        }

        basePayout = config.getInt("base-payout");
        wastelandsWorld = getServer().getWorld(worldName);
        config.options().copyDefaults(true);
        config.set("wastelands-world", wastelandsWorld);
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
        System.out.println(kent);
        if (kent instanceof Player) {
            Player killer = (Player) kent;
            Player player = event.getEntity();

            PlayerData playerData = PlayerData.load(this, player);
            playerData.addKilled(killer);

            economy.depositPlayer(player, playerData.getPayout());

            playerData.save();

            BountyData bdata = BountyData.load(this, killer);
            bdata.clearBounties(killer);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (command.getName().equalsIgnoreCase("wastelands")) {
                if (args.length != 1) {
                    return false;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    config = getConfig();

                    String worldName = config.getString("wastelands-world");
                    if (worldName == null) {
                        worldName = "world";
                    }

                    basePayout = config.getInt("base-payout");
                    wastelandsWorld = getServer().getWorld(worldName);
                    config.options().copyDefaults(true);
                    config.set("wastelands-world", wastelandsWorld);
                    saveConfig();
                    return true;
                }
            }

            if (command.getName().equalsIgnoreCase("setbounty")) {
                if (!(sender instanceof Player)) {
                    return false;
                }

                Player target = getServer().getPlayer(args[0]);
                float amount = Float.valueOf(args[1]);

                BountyData bdata = BountyData.load(this, target);
                bdata.addBounty((Player) sender, amount);

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
}
