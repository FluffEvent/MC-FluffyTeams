package fr.fluffevent.fluffyteams.controllers;

import java.util.List;
import java.util.UUID;

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

    public Team getTeam(String name) {
        List<Team> teams = db.where("name = ?", name).results(Team.class);
        if (teams.isEmpty()) {
            return null;
        }
        return teams.get(0);
    }

    public Member getMember(String teamName, Player player) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new IllegalArgumentException("Team " + teamName + " not found");
        }

        List<Member> members = db.where("team_id = ? AND player_uuid = ?", team.id, player.getUniqueId())
                .results(Member.class);
        if (members.isEmpty()) {
            return null;
        }
        return members.get(0);
    }

    public Spawn getSpawn(String teamName) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new IllegalArgumentException("Team " + teamName + " not found");
        }

        List<Spawn> spawns = db.where("team_id = ?", team.id).results(Spawn.class);
        if (spawns.isEmpty()) {
            return null;
        }
        return spawns.get(0);
    }

    public void create(String name, String displayName) {
        Team team = new Team();
        team.name = name;
        team.displayName = ChatColor.translateAlternateColorCodes('&', displayName);

        db.insert(team);
    }

    public void delete(String teamName) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new IllegalArgumentException("Team " + teamName + " not found");
        }
        db.delete(team);
    }

    public void addMember(String teamName, Player player) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new IllegalArgumentException("Team " + teamName + " not found");
        }

        Member member = new Member();
        member.teamId = team.id;
        member.playerUuid = player.getUniqueId().toString();

        db.insert(member);
    }

    public void removeMember(String teamName, Player player) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new IllegalArgumentException("Team " + teamName + " not found");
        }

        Member member = getMember(teamName, player);

        if (member == null) {
            throw new IllegalArgumentException("Player " + player.getName() + " not found in team " + teamName);
        }

        db.delete(member);
    }

    public List<Team> list() {
        return db.results(Team.class);
    }

    public List<OfflinePlayer> listMembers(String teamName) {
        Team team = getTeam(teamName);
        List<Member> members = db.where("team_id = ?", team.id).results(Member.class);

        return members.stream().map(m -> Bukkit.getOfflinePlayer(UUID.fromString(m.playerUuid))).toList();
    }

    public void spawn(String teamName) {
        if (teamName.equals("*")) {
            this.list().forEach(t -> spawn(t.name));
            return;
        }

        Spawn spawn = getSpawn(teamName);

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

    public void setSpawn(String teamName, Location location) {
        if (teamName.equals("*")) {
            this.list().forEach(t -> setSpawn(t.name, location));
            return;
        }

        Team team = getTeam(teamName);

        if (team == null) {
            throw new IllegalArgumentException("Team " + teamName + " not found");
        }

        Spawn spawn = getSpawn(teamName);

        if (spawn == null) {
            spawn = new Spawn();
            spawn.teamId = team.id;
        }

        spawn.world = location.getWorld().getName();
        spawn.x = location.getX();
        spawn.y = location.getY();
        spawn.z = location.getZ();
        spawn.yaw = location.getYaw();
        spawn.pitch = location.getPitch();

        if (spawn.id == 0) {
            db.insert(spawn);
        } else {
            db.update(spawn);
        }
    }
}
