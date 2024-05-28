package fr.fluffevent.fluffyteams.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import fr.fluffevent.fluffyteams.controllers.TeamController;
import fr.fluffevent.fluffyteams.models.database.Member;
import fr.fluffevent.fluffyteams.models.database.Team;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;

public class PlayerRespawnListener implements Listener {

    TeamController teamController = null;

    public PlayerRespawnListener() {
        teamController = new TeamController();
    }

    // Check if user is in a group that matches a team name, if so add it
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        Team playerTeam = teamController.getMemberTeam(player);

        if (playerTeam == null) {
            return;
        }

        Location spawnLocation = teamController.getSpawnLocation(playerTeam.name);

        if (spawnLocation == null) {
            return;
        }

        event.setRespawnLocation(spawnLocation);
    }
}
