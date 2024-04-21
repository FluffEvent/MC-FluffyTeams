package fr.fluffevent.fluffyteams.controllers;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.dieselpoint.norm.Database;

import fr.fluffevent.fluffyteams.database.DatabaseManager;
import fr.fluffevent.fluffyteams.models.database.Member;
import fr.fluffevent.fluffyteams.models.database.Spawn;
import fr.fluffevent.fluffyteams.models.database.Team;

public class TeamController {

    Database db;

    public TeamController() {
        db = DatabaseManager.getDatabase();
    }

    public void create(String name, String displayName) {
        Team team = new Team();
        team.name = name;
        team.displayName = ChatColor.translateAlternateColorCodes('&', displayName);

        db.insert(team);
    }

    public void delete(String name) {
        Team team = db.where("name = ?", name).results(Team.class).get(0);
        db.delete(team);
    }

    public void addMember(String teamName, Player player) {
        Team team = db.where("name = ?", teamName).results(Team.class).get(0);

        Member member = new Member();
        member.teamId = team.id;
        member.playerUuid = player.getUniqueId();

        db.insert(member);
    }

    public void removeMember(String teamName, Player player) {
        Team team = db.where("name = ?", teamName).results(Team.class).get(0);

        Member member = db.where("team_id = ? AND player_uuid = ?", team.id, player.getUniqueId()).results(Member.class)
                .get(0);

        db.delete(member);
    }

    public List<Team> list() {
        return db.results(Team.class);
    }

    public List<OfflinePlayer> listMembers(String teamName) {
        Team team = db.where("name = ?", teamName).results(Team.class).get(0);
        List<Member> members = db.where("team_id = ?", team.id).results(Member.class);

        return members.stream().map(m -> Bukkit.getOfflinePlayer(m.playerUuid)).toList();
    }

    public void spawn(String teamName) {
        if (teamName.equals("*")) {
            this.list().forEach(t -> spawn(t.name));
        } else {
            Team team = db.where("name = ?", teamName).results(Team.class).get(0);
            Spawn spawn = db.where("team_id = ?", team.id).results(Spawn.class).get(0);

            if (spawn == null) {
                throw new IllegalArgumentException("No spawn set for team " + teamName);
            }

            World world = Bukkit.getWorld(spawn.world);
            Location location = new Location(world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch);

            listMembers(teamName).forEach(p -> {
                if (p.isOnline()) {
                    p.getPlayer().teleport(location);
                }
            });
        }
    }

    public void setSpawn(String teamName, Location location) {
        Team team = db.where("name = ?", teamName).results(Team.class).get(0);

        Spawn spawn = db.where("team_id = ?", team.id).results(Spawn.class).get(0);

        if (spawn == null) {
            spawn = new Spawn();
            spawn.teamId = team.id;
        }

        spawn.world = location.getWorld().getName();
        spawn.x = (long) location.getX();
        spawn.y = (long) location.getY();
        spawn.z = (long) location.getZ();
        spawn.yaw = (long) location.getYaw();
        spawn.pitch = (long) location.getPitch();

        if (spawn.id == 0) {
            db.insert(spawn);
        } else {
            db.update(spawn);
        }
    }
}
