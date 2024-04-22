package fr.fluffevent.fluffyteams.models.database;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "fluffy_teams")
public class Team extends DatabaseModel {
  @Transient
  public String sqlCreationQuery = "create table `fluffy_teams` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `name` varchar(255), `display_name` varchar(255), PRIMARY KEY (`id`));";

  @Id
  @GeneratedValue
  public long id;

  @Column(name = "name")
  public String name;

  @Column(name = "display_name")
  public String displayName;
}
