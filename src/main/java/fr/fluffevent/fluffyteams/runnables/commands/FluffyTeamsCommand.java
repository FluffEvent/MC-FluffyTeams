package fr.fluffevent.fluffyteams.runnables.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.fluffevent.fluffyteams.controllers.TeamController;

public class FluffyTeamsCommand implements CommandExecutor {

  TeamController teamController;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("fluffyteams.admin")) {
      sender.sendMessage("§cYou don't have permission to use this command.");
      return true;
    }

    if (teamController == null) {
      teamController = new TeamController();
    }

    if (args.length == 0) {
      help(sender);
      return true;
    }

    try {
      switch (args[0]) {
        case "help":
          help(sender);
          break;
        case "create":
          if (args.length < 3) {
            sender.sendMessage("§cUsage: /fluffyteams create <name> <display_name>");
            return true;
          }
          create(sender, args[1], args[2]);
          break;
        case "delete":
          if (args.length < 2) {
            sender.sendMessage("§cUsage: /fluffyteams delete <name>");
            return true;
          }
          delete(sender, args[1]);
          break;
        case "add":
          if (args.length < 3) {
            sender.sendMessage("§cUsage: /fluffyteams add <team> <player>");
            return true;
          }
          Player player = Bukkit.getPlayer(args[2]);
          if (player == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
          }
          add(sender, args[1], player);
          break;
        case "remove":
          if (args.length < 2) {
            sender.sendMessage("§cUsage: /fluffyteams remove <player>");
            return true;
          }
          Player playerToRemove = Bukkit.getPlayer(args[1]);
          if (playerToRemove == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
          }
          remove(sender, playerToRemove);
          break;
        case "list":
          if (args.length == 1) {
            list(sender);
          } else {
            list(sender, args[1]);
          }
          break;
        case "spawn":
          if (args.length < 2) {
            sender.sendMessage("§cUsage: /fluffyteams spawn <team>");
            return true;
          }
          spawn(sender, args[1]);
          break;
        case "setspawn":
          if (args.length < 2) {
            sender.sendMessage("§cUsage: /fluffyteams setspawn <team> [world] [x] [y] [z] [yaw] [pitch]");
            return true;
          }
          setSpawn(sender, args);
          break;
        default:
          help(sender);
          break;
      }
    } catch (IllegalArgumentException e) {
      sender.sendMessage("§4Error: " + e.getMessage());
    }

    return true;
  }

  private void help(CommandSender sender) {
    sender.sendMessage("§e/fluffyteams help §7- §fShow this help message");
    sender.sendMessage("§e/fluffyteams create <name> <display_name> §7- §fCreate a team");
    sender.sendMessage("§e/fluffyteams delete <name> §7- §fDelete a team");
    sender.sendMessage("§e/fluffyteams add <team> <player> §7- §fAdd a player to a team");
    sender.sendMessage("§e/fluffyteams remove <player> §7- §fRemove a player from their team");
    sender.sendMessage("§e/fluffyteams list §7- §fList all teams");
    sender.sendMessage("§e/fluffyteams list <team> §7- §fList all members of a team");
    sender.sendMessage("§e/fluffyteams spawn <team> §7- §fTeleport team to their spawn (* for all teams)");
    sender.sendMessage(
        "§e/fluffyteams setspawn <team> [world] [x] [y] [z] [yaw] [pitch] §7- §fSet a team spawn for the current world (* for all teams)");
  }

  private void create(CommandSender sender, String name, String displayName) {
    teamController.create(name, displayName);
    sender.sendMessage("§aTeam created!");
  }

  private void delete(CommandSender sender, String name) {
    teamController.delete(name);
    sender.sendMessage("§aTeam deleted!");
  }

  private void add(CommandSender sender, String teamName, Player player) {
    teamController.addMember(teamName, player);
    sender.sendMessage("§aPlayer added to team!");
  }

  private void remove(CommandSender sender, Player player) {
    teamController.removeMember(player);
    sender.sendMessage("§aPlayer removed from team!");
  }

  private void list(CommandSender sender) {
    sender.sendMessage("§eTeams:");
    teamController.list().forEach(t -> sender.sendMessage("§f- " + t.name + " §r§f(" + t.displayName + "§r§f)"));
  }

  private void list(CommandSender sender, String teamName) {
    sender.sendMessage("§eMembers of " + teamName + ":");
    teamController.listMembers(teamName).forEach(p -> sender.sendMessage("§f- " + p.getName()));
  }

  private void spawn(CommandSender sender, String teamName) {
    teamController.spawn(teamName);
    sender.sendMessage("§aTeam teleported to spawn!");
  }

  private void setSpawn(CommandSender sender, String[] args) {
    String teamName = args[1];

    Location location = null;

    if (args.length == 2) {
      if (!(sender instanceof Player)) {
        sender.sendMessage("§cYou must be a player to use this command.");
        return;
      }
      Player player = (Player) sender;
      location = player.getLocation();
    }

    if (args.length >= 8) {
      World world = Bukkit.getWorld(args[2]);
      if (world == null) {
        sender.sendMessage("§cWorld not found.");
        return;
      }

      double x = Double.parseDouble(args[3]);
      double y = Double.parseDouble(args[4]);
      double z = Double.parseDouble(args[5]);
      float yaw = Float.parseFloat(args[6]);
      float pitch = Float.parseFloat(args[7]);

      location = new Location(world, x, y, z, yaw, pitch);
    }

    if (location == null) {
      sender.sendMessage("§cInvalid arguments.");
      return;
    }

    teamController.setSpawn(teamName, location);
    sender.sendMessage("§aSpawn set!");
  }
}
