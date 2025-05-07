package fr.fluffevent.fluffyteams;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Configuration manager for the FluffyTeams plugin.
 * Handles loading and storing configuration values from config.yml.
 */
public class Config {

  /**
   * Database connection URI.
   * Default is SQLite but can be configured to use other databases.
   */
  public static String databaseUri = "jdbc:sqlite:%plugin_config_path%/plugins/FluffyTeams/database.db";

  /**
   * Database username for authentication.
   * Not required for SQLite but needed for databases like MySQL.
   */
  public static String databaseUsername = "";

  /**
   * Database password for authentication.
   * Not required for SQLite but needed for databases like MySQL.
   */
  public static String databasePassword = "";

  /**
   * Name of the current server instance.
   * Used to manage team spawns across multiple servers.
   */
  public static String serverName = "default";

  /**
   * Whether to override the player's bed respawn location with team spawn.
   * If true, players will always respawn at their team spawn instead of their
   * beds.
   */
  public static boolean overrideBedRespawnLocation = false;

  /**
   * Loads configuration values from config.yml.
   * Creates the default configuration file if it doesn't exist.
   *
   * @param plugin The JavaPlugin instance to get configuration from
   */
  public static void readConfig(JavaPlugin plugin) {
    plugin.saveDefaultConfig();

    FileConfiguration config = plugin.getConfig();

    databaseUri = config.getString("database-uri");
    databaseUsername = config.getString("database-username");
    databasePassword = config.getString("database-password");
    serverName = config.getString("server-name");
    overrideBedRespawnLocation = config.getBoolean("override-bed-respawn-location", false);
  }
}
