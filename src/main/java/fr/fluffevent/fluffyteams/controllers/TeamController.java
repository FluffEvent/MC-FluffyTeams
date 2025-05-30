package fr.fluffevent.fluffyteams.controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.dieselpoint.norm.Database;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import fr.fluffevent.fluffyteams.Config;
import fr.fluffevent.fluffyteams.database.DatabaseManager;
import fr.fluffevent.fluffyteams.models.PlayerInfo;
import fr.fluffevent.fluffyteams.models.database.Member;
import fr.fluffevent.fluffyteams.models.database.Spawn;
import fr.fluffevent.fluffyteams.models.database.Team;

/**
 * Controller class for managing teams, members, and team spawns.
 * Handles all team-related operations including creating/deleting teams,
 * managing team membership, and handling team spawns.
 */
public class TeamController {

    Database db;

    /**
     * Initializes the TeamController with a database connection.
     */
    public TeamController() {
        db = DatabaseManager.getDatabase();
    }

    /**
     * Gets a team by its name.
     *
     * @param name The name of the team to find
     * @return The team if found, null otherwise
     */
    public Team getTeam(String name) {
        List<Team> teams = db.where("name = ?", name).results(Team.class);
        if (teams.isEmpty()) {
            return null;
        }
        return teams.get(0);
    }

    /**
     * Gets a team by its ID.
     *
     * @param id The ID of the team to find
     * @return The team if found, null otherwise
     */
    public Team getTeamFromId(long id) {
        List<Team> teams = db.where("id = ?", id).results(Team.class);
        if (teams.isEmpty()) {
            return null;
        }
        return teams.get(0);
    }

    /**
     * Gets the team that a player belongs to.
     *
     * @param player The player to check
     * @return The team the player belongs to, or null if the player isn't in a team
     */
    public Team getMemberTeam(Player player) {
        Member member = getMember(player);
        if (member == null) {
            return null;
        }
        return getTeamFromId(member.teamId);
    }

    /**
     * Gets the member record for a player.
     *
     * @param player The player to check
     * @return The member record if found, null otherwise
     */
    public Member getMember(Player player) {
        List<Member> members = db.where("player_uuid = ?", player.getUniqueId().toString())
                .results(Member.class);
        if (members.isEmpty()) {
            return null;
        }
        return members.get(0);
    }

    /**
     * Gets the spawn record for a team on the current server.
     *
     * @param teamName The name of the team
     * @return The spawn record if found, null otherwise
     * @throws IllegalArgumentException if the team doesn't exist
     */
    public Spawn getSpawn(String teamName) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new IllegalArgumentException("Team " + teamName + " not found");
        }

        List<Spawn> spawns = db.where("team_id = ? AND server = ?", team.id, Config.serverName).results(Spawn.class);
        if (spawns.isEmpty()) {
            return null;
        }
        return spawns.get(0);
    }

    /**
     * Gets the spawn location for a team on the current server.
     *
     * @param teamName The name of the team
     * @return The location of the spawn if set, null otherwise
     */
    public Location getSpawnLocation(String teamName) {
        Spawn spawn = getSpawn(teamName);
        if (spawn == null) {
            return null;
        }

        World world = Bukkit.getWorld(spawn.world);
        return new Location(world, spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch);
    }

    /**
     * Creates a new team.
     *
     * @param name        The name of the team (used for commands and permissions)
     * @param displayName The display name of the team (supports color codes with &)
     */
    public void create(String name, String displayName) {
        Team team = new Team();
        team.name = name;
        team.displayName = ChatColor.translateAlternateColorCodes('&', displayName);

        db.insert(team);
    }

    /**
     * Deletes a team and removes all its members.
     *
     * @param teamName The name of the team to delete
     * @throws IllegalArgumentException if the team doesn't exist
     */
    public void delete(String teamName) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new IllegalArgumentException("Team " + teamName + " not found");
        }

        // Get all members before deleting
        List<PlayerInfo> members = listMembers(teamName);

        // Remove all members
        for (PlayerInfo playerInfo : members) {
            OfflinePlayer player = playerInfo.getPlayer();
            if (player.isOnline()) {
                removeMember(player.getPlayer());
            } else {
                removeOfflineMember(playerInfo.getUsername());
            }
        }

        db.delete(team);
    }

    /**
     * Adds an online player to a team.
     *
     * @param teamName The name of the team to add the player to
     * @param player   The player to add
     * @throws IllegalArgumentException if the team doesn't exist or if the player
     *                                  is already in a team
     */
    public void addMember(String teamName, Player player) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new IllegalArgumentException("Team " + teamName + " not found");
        }

        if (getMember(player) != null) {
            throw new IllegalArgumentException("Player " + player.getName() + " is already in a team");
        }

        Member member = new Member();
        member.teamId = team.id;
        member.playerUuid = player.getUniqueId().toString();

        db.insert(member);

        Server server = Bukkit.getServer();
        if (server.getPluginManager().isPluginEnabled("LuckPerms")) {
            String lpCommand = "lp user " + player.getUniqueId() + " parent add " + team.name;
            Bukkit.getServer().dispatchCommand(server.getConsoleSender(), lpCommand);
        }
    }

    /**
     * Add a player to a team by their username, even if they have never connected
     * before. Uses Mojang API to validate username and retrieve the official UUID.
     *
     * @param teamName Name of the team to add the player to
     * @param username Minecraft username of the player (must be a valid Minecraft
     *                 account)
     * @return true if the player was successfully added, false if the player
     *         doesn't exist in Mojang's database
     * @throws IllegalArgumentException if the team doesn't exist or if the player
     *                                  is already in a team
     */
    public boolean addOfflineMember(String teamName, String username) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new IllegalArgumentException("Team " + teamName + " not found");
        }

        // Validate the username against Mojang API
        UUID playerUUID = fetchUUIDFromMojang(username);

        // If UUID couldn't be retrieved, the player doesn't exist
        if (playerUUID == null) {
            return false;
        }

        String uuid = playerUUID.toString();

        // Check if player is already in a team
        List<Member> existingMembers = db.where("player_uuid = ?", uuid).results(Member.class);
        if (!existingMembers.isEmpty()) {
            throw new IllegalArgumentException("Player " + username + " is already in a team");
        }

        // Add player to team
        Member member = new Member();
        member.teamId = team.id;
        member.playerUuid = uuid;

        db.insert(member);

        // Add to LuckPerms if available - use UUID instead of username
        Server server = Bukkit.getServer();
        if (server.getPluginManager().isPluginEnabled("LuckPerms")) {
            String lpCommand = "lp user " + uuid + " parent add " + team.name;
            Bukkit.getServer().dispatchCommand(server.getConsoleSender(), lpCommand);
        }

        return true;
    }

    /**
     * Fetch a player's UUID from Mojang API by username.
     * Makes a direct HTTP call to Mojang's profile API to verify the username
     * exists
     * and to retrieve the official UUID.
     *
     * @param username The Minecraft username to look up
     * @return The UUID if the player exists, null if the player doesn't exist or if
     *         an error occurred
     */
    private UUID fetchUUIDFromMojang(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();

            // 200 means player exists, 204 means player doesn't exist
            if (status == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON response
                JsonObject json = new Gson().fromJson(response.toString(), JsonObject.class);
                String id = json.get("id").getAsString();

                // Mojang API returns UUID without hyphens, we need to add them
                String formattedUUID = id.replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5");

                return UUID.fromString(formattedUUID);
            }

            return null;
        } catch (Exception e) {
            // Log the error but don't crash the plugin
            Bukkit.getLogger().log(Level.WARNING, "Failed to fetch UUID for username: " + username, e);
            return null;
        }
    }

    /**
     * Removes an online player from their team.
     *
     * @param player The player to remove
     * @throws IllegalArgumentException if the player is not in a team
     */
    public void removeMember(Player player) {
        Member member = getMember(player);

        if (member == null) {
            throw new IllegalArgumentException("Player " + player.getName() + " not found in any team ");
        }

        Team team = getTeamFromId(member.teamId);

        db.delete(member);

        Server server = Bukkit.getServer();
        if (server.getPluginManager().isPluginEnabled("LuckPerms")) {
            String lpCommand = "lp user " + player.getUniqueId() + " parent remove " + team.name;
            Bukkit.getServer().dispatchCommand(server.getConsoleSender(), lpCommand);
        }
    }

    /**
     * Removes a player from their team by username, even if offline.
     * First validates the username against Mojang API, then falls back to checking
     * for players who have connected before using Bukkit.
     *
     * @param username The Minecraft username of the player to remove
     * @return true if the player was successfully removed from their team,
     *         false if they weren't in a team, don't exist, or couldn't be found
     */
    public boolean removeOfflineMember(String username) {
        // Try to get the real UUID from Mojang
        UUID playerUUID = fetchUUIDFromMojang(username);

        // If couldn't get UUID from Mojang, try with Bukkit's offline player (for
        // players who have connected before)
        if (playerUUID == null) {
            @SuppressWarnings("deprecation")
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
            if (offlinePlayer.hasPlayedBefore()) {
                playerUUID = offlinePlayer.getUniqueId();
            } else {
                return false;
            }
        }

        String uuid = playerUUID.toString();

        // Find the member record
        List<Member> members = db.where("player_uuid = ?", uuid).results(Member.class);
        if (members.isEmpty()) {
            return false;
        }

        Member member = members.get(0);
        Team team = getTeamFromId(member.teamId);

        db.delete(member);

        // Remove from LuckPerms if available - use UUID instead of username
        Server server = Bukkit.getServer();
        if (server.getPluginManager().isPluginEnabled("LuckPerms")) {
            String lpCommand = "lp user " + uuid + " parent remove " + team.name;
            Bukkit.getServer().dispatchCommand(server.getConsoleSender(), lpCommand);
        }

        return true;
    }

    /**
     * Lists all teams in the system.
     *
     * @return A list of all teams
     */
    public List<Team> list() {
        return db.results(Team.class);
    }

    /**
     * Lists all members of a team.
     *
     * @param teamName The name of the team
     * @return A list of PlayerInfo objects for all team members
     */
    public List<PlayerInfo> listMembers(String teamName) {
        Team team = getTeam(teamName);
        if (team == null) {
            throw new IllegalArgumentException("Team " + teamName + " not found");
        }

        List<Member> members = db.where("team_id = ?", team.id).results(Member.class);
        List<PlayerInfo> result = new ArrayList<>();

        for (Member member : members) {
            UUID uuid = UUID.fromString(member.playerUuid);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String username = offlinePlayer.getName();
            boolean hasConnectedBefore = offlinePlayer.hasPlayedBefore();

            // If player has never connected, try to fetch username from Mojang API
            if (username == null || username.isEmpty()) {
                username = fetchUsernameFromMojang(uuid);
            }

            result.add(new PlayerInfo(offlinePlayer, username, uuid, hasConnectedBefore));
        }

        return result;
    }

    /**
     * Fetch a player's username from Mojang API by UUID.
     *
     * @param uuid The UUID to look up
     * @return The username if found, "Unknown Player" otherwise
     */
    private String fetchUsernameFromMojang(UUID uuid) {
        try {
            // Convert UUID to Mojang's format (no hyphens)
            String uuidStr = uuid.toString().replace("-", "");
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();

            if (status == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON response
                JsonObject json = new Gson().fromJson(response.toString(), JsonObject.class);
                return json.get("name").getAsString();
            }

            return "Unknown Player";
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to fetch username for UUID: " + uuid, e);
            return "Unknown Player";
        }
    }

    /**
     * Teleports all online members of a team to their spawn location.
     * If teamName is "*", teleports all teams to their respective spawns.
     *
     * @param teamName The name of the team, or "*" for all teams
     * @throws IllegalArgumentException if no spawn is set for the team
     */
    public void spawn(String teamName) {
        if (teamName.equals("*")) {
            this.list().forEach(t -> spawn(t.name));
            return;
        }

        Location spawnLocation = getSpawnLocation(teamName);

        if (spawnLocation == null) {
            throw new IllegalArgumentException("No spawn set for team " + teamName);
        }

        listMembers(teamName).forEach(playerInfo -> {
            if (playerInfo.getPlayer().isOnline()) {
                playerInfo.getPlayer().getPlayer().teleport(spawnLocation);
            }
        });
    }

    /**
     * Sets the spawn location for a team.
     * If teamName is "*", sets the spawn for all teams to the same location.
     *
     * @param teamName The name of the team, or "*" for all teams
     * @param location The location to set as the spawn
     * @throws IllegalArgumentException if the team doesn't exist
     */
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

        spawn.server = Config.serverName;
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
