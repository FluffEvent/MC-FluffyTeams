package fr.fluffevent.fluffyteams.database;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dieselpoint.norm.Database;

import fr.fluffevent.fluffyteams.Config;
import fr.fluffevent.fluffyteams.FluffyTeams;
import fr.fluffevent.fluffyteams.models.database.Member;
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
    classes.add(Member.class);
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
      String sqlCreationQuery = null;
      try {
        // Try to access custom table SQL creation if exists
        Constructor<?> ctor = classToCreate.getConstructor();
        Object object = ctor.newInstance(new Object[] {});
        sqlCreationQuery = (String) classToCreate.getDeclaredField("sqlCreationQuery").get(object);
      } catch (Exception ex) {
        // Auto generate the query if missing
        sqlCreationQuery = database.getSqlMaker().getCreateTableSql(classToCreate);
      }

      // Patch to avoid exceptions
      sqlCreationQuery = sqlCreationQuery.replace("create table", "create table if not exists");

      try {
        database.sql(sqlCreationQuery).execute();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  public static Database getDatabase() {
    return database;
  }
}
