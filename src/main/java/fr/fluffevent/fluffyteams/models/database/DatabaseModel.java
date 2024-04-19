package fr.fluffevent.fluffyteams.models.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.fluffevent.fluffyteams.database.DatabaseManager;

public class DatabaseModel {

  public void save() {
    DatabaseManager.getDatabase().upsert(this).execute();
  }

  public String toString() {
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    return gson.toJson(this);
  }
}
