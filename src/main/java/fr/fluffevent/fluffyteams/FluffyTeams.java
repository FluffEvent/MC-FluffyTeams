package fr.fluffevent.fluffyteams;

import java.io.File;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import fr.fluffevent.fluffyteams.listeners.PlayerLoginListener;
import fr.fluffevent.fluffyteams.listeners.PlayerRespawnListener;
import fr.fluffevent.fluffyteams.database.DatabaseManager;
import fr.fluffevent.fluffyteams.runnables.commands.FluffyTeamsCommand;

/**
 * FluffyTeams - A Minecraft team management plugin.
 *
 * This plugin provides functionality for creating and managing teams,
 * including assigning players to teams, setting team spawns, and
 * teleporting team members to their spawn points.
 *
 * Features:
 * - Create and manage teams with custom names and display names
 * - Add/remove players to/from teams (works with offline players)
 * - Set spawn points for each team
 * - Teleport team members to their spawn points
 * - Integration with LuckPerms for team-based permissions
 */
public class FluffyTeams extends JavaPlugin {

  /**
   * Default constructor required by Bukkit.
   */
  public FluffyTeams() {
    super();
  }

  /**
   * Alternative constructor for MockBukkit testing.
   *
   * @param loader      The plugin loader
   * @param description The plugin description
   * @param dataFolder  The plugin data folder
   * @param file        The plugin file
   */
  protected FluffyTeams(
      JavaPluginLoader loader,
      PluginDescriptionFile description,
      File dataFolder,
      File file) {
    super(loader, description, dataFolder, file);
  }

  private static FluffyTeams instance;

  /**
   * Gets the singleton instance of the plugin.
   *
   * @return The plugin instance
   */
  public static FluffyTeams getInstance() {
    return instance;
  }

  /**
   * Called when the plugin is enabled.
   * Sets up the configuration, database connection, event listeners, and
   * commands.
   */
  @Override
  public void onEnable() {
    instance = this;

    // Setup
    Config.readConfig(this);
    DatabaseManager.connect();

    // Game listeners
    PluginManager pluginManager = this.getServer().getPluginManager();
    pluginManager.registerEvents(new PlayerLoginListener(), instance);
    pluginManager.registerEvents(new PlayerRespawnListener(), instance);

    // Commands
    FluffyTeamsCommand fluffyTeamsCommand = new FluffyTeamsCommand();
    this.getCommand("fluffyteams").setExecutor(fluffyTeamsCommand);
    this.getCommand("fluffyteams").setTabCompleter(fluffyTeamsCommand);
  }

  /**
   * Called when the plugin is disabled.
   * Cancels all tasks and performs cleanup.
   */
  @Override
  public void onDisable() {
    getServer().getScheduler().cancelTasks(this);
  }
}
