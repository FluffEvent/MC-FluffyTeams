package fr.fluffevent.fluffyteams;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {

  public static String databaseUri = "jdbc:sqlite:%plugin_config_path%/plugins/FluffyTeams/database.db";
  public static String databaseUsername = "";
  public static String databasePassword = "";
  public static String serverName = "default";

  public static void readConfig(JavaPlugin plugin) {
    plugin.saveDefaultConfig();

    FileConfiguration config = plugin.getConfig();

    databaseUri = config.getString("database-uri");
    databaseUsername = config.getString("database-username");
    databasePassword = config.getString("database-password");
    serverName = config.getString("server-name");
  }
}
