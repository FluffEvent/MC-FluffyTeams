package fr.fluffevent.fluffyteams.models.database;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "fluffy_spawns")
public class Spawn extends DatabaseModel {

  @Id
  @GeneratedValue
  public long id;

  @Column(name = "id")
  public long teamId;

  @Column(name = "world")
  public String world;

  @Column(name = "x")
  public long x;

  @Column(name = "y")
  public long y;

  @Column(name = "z")
  public long z;

  @Column(name = "yaw")
  public long yaw;

  @Column(name = "pitch")
  public long pitch;
}
