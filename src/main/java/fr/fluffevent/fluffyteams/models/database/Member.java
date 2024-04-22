package fr.fluffevent.fluffyteams.models.database;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "fluffy_members")
public class Member extends DatabaseModel {
  @Transient
  public String sqlCreationQuery = "create table `fluffy_members` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `team_id` bigint(20), `player_uuid` varchar(255), PRIMARY KEY (`id`));";

  @Id
  @GeneratedValue
  public long id;

  @Column(name = "team_id")
  public long teamId;

  @Column(name = "player_uuid")
  public String playerUuid;
}
