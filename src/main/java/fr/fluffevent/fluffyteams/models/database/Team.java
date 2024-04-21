package fr.fluffevent.fluffyteams.models.database;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "fluffy_teams")
public class Team extends DatabaseModel {

  @Id
  @GeneratedValue
  public long id;

  @Column(name = "name")
  public String name;

  @Column(name = "display_name")
  public String displayName;
}
