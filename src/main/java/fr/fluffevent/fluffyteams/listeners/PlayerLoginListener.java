package fr.fluffevent.fluffyteams.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import fr.fluffevent.fluffyteams.controllers.TeamController;
import fr.fluffevent.fluffyteams.models.database.Member;
import fr.fluffevent.fluffyteams.models.database.Team;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;

public class PlayerLoginListener implements Listener {

    LuckPerms luckperms = null;
    TeamController teamController = null;

    public PlayerLoginListener() {
        teamController = new TeamController();
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager()
                    .getRegistration(LuckPerms.class);
            if (provider != null) {
                luckperms = provider.getProvider();
            }
        }

        if (luckperms == null) {
            Bukkit.getLogger().warning("LuckPerms not found, disabling log-in detection.");
        }
    }

    // Check if user is in a group that matches a team name, if so add it
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (luckperms == null) {
            return;
        }

        Player player = event.getPlayer();

        User user = luckperms.getUserManager().getUser(player.getUniqueId());

        if (user == null) {
            return;
        }

        String primaryGroup = user.getPrimaryGroup();

        Team luckPermsTeam = teamController.getTeam(primaryGroup);

        Member currentTeamMember = teamController.getMember(player);

        if (luckPermsTeam != null && currentTeamMember == null) {
            teamController.addMember(primaryGroup, player);
            Bukkit.getLogger().info("Added " + player.getName() + " to team " + primaryGroup);
        }
    }
}
