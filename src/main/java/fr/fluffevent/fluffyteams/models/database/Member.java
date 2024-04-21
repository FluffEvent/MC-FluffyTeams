package fr.fluffevent.fluffyteams.models.database;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "fluffy_members")
public class Member extends DatabaseModel {

  @Id
  @GeneratedValue
  public long id;

  @Column(name = "id")
  public long teamId;

  @Column(name = "player_uuid")
  public UUID playerUuid;
}
