package fr.fluffevent.fluffyteams.database;

import com.dieselpoint.norm.Database;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import fr.fluffevent.fluffyteams.Config;
import fr.fluffevent.fluffyteams.FluffyTeams;
import fr.fluffevent.fluffyteams.models.database.Spawn;
import fr.fluffevent.fluffyteams.models.database.Team;

public class DatabaseManager {

  private static Database database;

  public static void connect() {
    database = new Database();
    database.setJdbcUrl(Config.databaseUri);

    database.setUser(Config.databaseUsername);
    database.setPassword(Config.databasePassword);

    List<Class<?>> classes = new ArrayList<Class<?>>();
    classes.add(Team.class);
    classes.add(Spawn.class);
    createStructure(classes);

    FluffyTeams
        .getInstance()
        .getLogger()
        .info("Connected to database!");
  }

  public static void createStructure(List<Class<?>> classes) {
    Iterator<Class<?>> it = classes.iterator();
    while (it.hasNext()) {
      Class<?> classToCreate = it.next();
      try {
        database.createTable(classToCreate);
      } catch (Exception ex) {
        // TODO handle exeptions
        // PS: it's normal some are thrown if the table already exists
      }
    }
  }

  public static Database getDatabase() {
    return database;
  }
}
