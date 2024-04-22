package fr.fluffevent.fluffyteams.models.database;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "fluffy_spawns")
public class Spawn extends DatabaseModel {
  @Transient
  public String sqlCreationQuery = "create table `fluffy_spawns` (`id` bigint(20) NOT NULL AUTO_INCREMENT, `team_id` bigint(20), `server` varchar(255), `world` varchar(255), `x` double, `y` double, `z` double, `yaw` float, `pitch` float, PRIMARY KEY (`id`));";

  @Id
  @GeneratedValue
  public long id;

  @Column(name = "team_id")
  public long teamId;

  @Column(name = "server")
  public String server;

  @Column(name = "world")
  public String world;

  @Column(name = "x")
  public double x;

  @Column(name = "y")
  public double y;

  @Column(name = "z")
  public double z;

  @Column(name = "yaw")
  public float yaw;

  @Column(name = "pitch")
  public float pitch;
}
