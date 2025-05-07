package fr.fluffevent.fluffyteams.models;

import java.util.UUID;
import org.bukkit.OfflinePlayer;

/**
 * Player information wrapper to handle both online and offline players,
 * including those who have never connected to the server.
 * This class contains player data retrieved from either Bukkit or Mojang API.
 */
public class PlayerInfo {
    private final OfflinePlayer player;
    private final String username;
    private final UUID uuid;
    private final boolean hasConnectedBefore;

    /**
     * Creates a new PlayerInfo object.
     *
     * @param player             The Bukkit OfflinePlayer object
     * @param username           The player's username (from Bukkit or Mojang API)
     * @param uuid               The player's UUID
     * @param hasConnectedBefore Whether the player has connected to the server
     *                           before
     */
    public PlayerInfo(OfflinePlayer player, String username, UUID uuid, boolean hasConnectedBefore) {
        this.player = player;
        this.username = username;
        this.uuid = uuid;
        this.hasConnectedBefore = hasConnectedBefore;
    }

    /**
     * Gets the OfflinePlayer object.
     * Note that this may have limited information for players who have never
     * connected.
     *
     * @return The OfflinePlayer object
     */
    public OfflinePlayer getPlayer() {
        return player;
    }

    /**
     * Gets the player's username.
     * This will be retrieved from Mojang API for players who have never connected.
     *
     * @return The player's username, or "Unknown Player" if not available
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the player's UUID.
     *
     * @return The player's UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Checks if the player has connected to the server before.
     *
     * @return true if the player has connected before, false otherwise
     */
    public boolean hasConnectedBefore() {
        return hasConnectedBefore;
    }
}