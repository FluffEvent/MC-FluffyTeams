package fr.fluffevent.fluffyteams;

import java.io.File;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import fr.fluffevent.fluffyteams.database.DatabaseManager;
import fr.fluffevent.fluffyteams.runnables.commands.FluffyTeamsCommand;

public class FluffyTeams extends JavaPlugin {

  public FluffyTeams() {
    super();
  }

  protected FluffyTeams(
      JavaPluginLoader loader,
      PluginDescriptionFile description,
      File dataFolder,
      File file) {
    super(loader, description, dataFolder, file);
  }

  private static FluffyTeams instance;

  public static FluffyTeams getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    instance = this;

    // Setup
    Config.readConfig(this);
    DatabaseManager.connect();

    // Game listeners
    // PluginManager pluginManager = this.getServer().getPluginManager();
    // pluginManager.disablePlugin(this); // Remove this once your listeners are
    // ready

    // Commands
    this.getCommand("fluffyteams").setExecutor(new FluffyTeamsCommand());
  }

  @Override
  public void onDisable() {
    getServer().getScheduler().cancelTasks(this);
  }
}
