package fr.fluffevent.fluffyteams.runnables.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import fr.fluffevent.fluffyteams.controllers.TeamController;

/**
 * Main command handler for the FluffyTeams plugin.
 * Implements both CommandExecutor for command processing and
 * TabCompleter for tab completion suggestions.
 * Delegates commands to individual subcommand handlers.
 */
public class FluffyTeamsCommand implements CommandExecutor, TabCompleter {

  private final TeamController teamController;
  private final Map<String, SubCommand> subCommands = new HashMap<>();

  /**
   * Initializes the command handler and registers all subcommands.
   */
  public FluffyTeamsCommand() {
    teamController = new TeamController();
    registerSubCommands();
  }

  /**
   * Registers all available subcommands into the subcommands map.
   */
  private void registerSubCommands() {
    subCommands.put("help", new HelpCommand());
    subCommands.put("create", new CreateCommand());
    subCommands.put("delete", new DeleteCommand());
    subCommands.put("add", new AddCommand());
    subCommands.put("remove", new RemoveCommand());
    subCommands.put("list", new ListCommand());
    subCommands.put("spawn", new SpawnCommand());
    subCommands.put("setspawn", new SetSpawnCommand());
  }

  /**
   * Handles command execution by checking permissions and
   * routing to the appropriate subcommand.
   *
   * @param sender  The sender of the command
   * @param command The command being executed
   * @param label   The alias used for the command
   * @param args    The arguments provided with the command
   * @return true if the command was handled, false otherwise
   */
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("fluffyteams.admin")) {
      sender.sendMessage("§cYou don't have permission to use this command.");
      return true;
    }

    if (args.length == 0) {
      subCommands.get("help").execute(sender, args);
      return true;
    }

    String subCommandName = args[0].toLowerCase();
    SubCommand subCommand = subCommands.get(subCommandName);

    if (subCommand != null) {
      try {
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommand.execute(sender, subArgs);
      } catch (IllegalArgumentException e) {
        sender.sendMessage("§4Error: " + e.getMessage());
      }
    } else {
      subCommands.get("help").execute(sender, args);
    }

    return true;
  }

  /**
   * Provides tab completion suggestions based on the current command context.
   *
   * @param sender  The sender of the command
   * @param command The command being tab-completed
   * @param alias   The alias used for the command
   * @param args    The arguments provided so far
   * @return A list of possible completions, or an empty list if none
   */
  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (!sender.hasPermission("fluffyteams.admin")) {
      return Collections.emptyList();
    }

    // First level commands
    if (args.length == 1) {
      return subCommands.keySet().stream()
          .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
          .collect(Collectors.toList());
    }

    // Second level and beyond
    String subCommandName = args[0].toLowerCase();
    SubCommand subCommand = subCommands.get(subCommandName);

    if (subCommand != null) {
      String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
      return subCommand.tabComplete(sender, subArgs);
    }

    return Collections.emptyList();
  }

  /**
   * Interface for subcommands to implement execution and tab completion.
   */
  private interface SubCommand {
    /**
     * Executes the subcommand.
     *
     * @param sender The sender of the command
     * @param args   The arguments for the subcommand
     */
    void execute(CommandSender sender, String[] args);

    /**
     * Provides tab completion for the subcommand.
     *
     * @param sender The sender of the command
     * @param args   The arguments for the subcommand so far
     * @return A list of possible completions
     */
    List<String> tabComplete(CommandSender sender, String[] args);
  }

  /**
   * Shows help information for all available commands.
   */
  private class HelpCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
      sender.sendMessage("§e/fluffyteams help §7- §fShow this help message");
      sender.sendMessage("§e/fluffyteams create <name> <display_name> §7- §fCreate a team");
      sender.sendMessage("§e/fluffyteams delete <name> §7- §fDelete a team");
      sender.sendMessage(
          "§e/fluffyteams add <team> <player> §7- §fAdd a player to a team (works with offline players)");
      sender.sendMessage(
          "§e/fluffyteams remove <player> §7- §fRemove a player from their team (works with offline players)");
      sender.sendMessage("§e/fluffyteams list §7- §fList all teams");
      sender.sendMessage("§e/fluffyteams list <team> §7- §fList all members of a team");
      sender.sendMessage("§e/fluffyteams spawn <team> §7- §fTeleport team to their spawn (* for all teams)");
      sender.sendMessage(
          "§e/fluffyteams setspawn <team> [world] [x] [y] [z] [yaw] [pitch] §7- §fSet a team spawn for the current world (* for all teams)");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
      return Collections.emptyList();
    }
  }

  /**
   * Creates a new team with the specified name and display name.
   */
  private class CreateCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
      if (args.length < 2) {
        sender.sendMessage("§cUsage: /fluffyteams create <name> <display_name>");
        return;
      }

      teamController.create(args[0], args[1]);
      sender.sendMessage("§aTeam created!");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
      // No tab complete suggestions for create command
      return Collections.emptyList();
    }
  }

  /**
   * Deletes an existing team and removes all its members.
   */
  private class DeleteCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
      if (args.length < 1) {
        sender.sendMessage("§cUsage: /fluffyteams delete <name>");
        return;
      }

      teamController.delete(args[0]);
      sender.sendMessage("§aTeam deleted!");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
      if (args.length == 1) {
        return teamController.list().stream()
            .map(t -> t.name)
            .filter(name -> name.startsWith(args[0]))
            .collect(Collectors.toList());
      }
      return Collections.emptyList();
    }
  }

  /**
   * Adds a player to a team. Works with both online and offline players.
   * For offline players, validates their username with the Mojang API.
   */
  private class AddCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
      if (args.length < 2) {
        sender.sendMessage("§cUsage: /fluffyteams add <team> <player>");
        return;
      }

      String teamName = args[0];
      String playerName = args[1];

      // Try to get online player first
      Player player = Bukkit.getPlayer(playerName);

      try {
        if (player != null) {
          // Player is online, use normal method
          teamController.addMember(teamName, player);
          sender.sendMessage("§aPlayer " + playerName + " added to team " + teamName + "!");
        } else {
          // Player is offline, use offline method
          boolean success = teamController.addOfflineMember(teamName, playerName);
          if (success) {
            sender.sendMessage("§aPlayer " + playerName + " added to team " + teamName + "!");
            sender.sendMessage("§e(Mojang-validated player who has never connected to this server)");
          } else {
            sender.sendMessage("§cCouldn't find a valid Minecraft account with name: " + playerName);
            sender.sendMessage("§cThe player name must be a valid Minecraft account registered with Mojang.");
          }
        }
      } catch (IllegalArgumentException e) {
        sender.sendMessage("§c" + e.getMessage());
      }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
      if (args.length == 1) {
        // Suggest teams
        return teamController.list().stream()
            .map(t -> t.name)
            .filter(name -> name.startsWith(args[0]))
            .collect(Collectors.toList());
      } else if (args.length == 2) {
        // Suggest online players
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.startsWith(args[1]))
            .collect(Collectors.toList());
      }
      return Collections.emptyList();
    }
  }

  /**
   * Removes a player from their team. Works with both online and offline players.
   * For offline players, validates their username with the Mojang API.
   */
  private class RemoveCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
      if (args.length < 1) {
        sender.sendMessage("§cUsage: /fluffyteams remove <player>");
        return;
      }

      String playerName = args[0];

      // Try to get online player first
      Player playerToRemove = Bukkit.getPlayer(playerName);

      try {
        if (playerToRemove != null) {
          // Player is online, use normal method
          teamController.removeMember(playerToRemove);
          sender.sendMessage("§aPlayer " + playerName + " removed from team!");
        } else {
          // Player is offline, use offline method
          boolean success = teamController.removeOfflineMember(playerName);
          if (success) {
            sender.sendMessage("§aPlayer " + playerName + " removed from team!");
            sender.sendMessage("§e(Player is currently offline)");
          } else {
            sender.sendMessage("§cCouldn't find a valid player with name: " + playerName);
            sender.sendMessage("§cMake sure the player exists and is in a team.");
          }
        }
      } catch (IllegalArgumentException e) {
        sender.sendMessage("§c" + e.getMessage());
      }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
      if (args.length == 1) {
        // Suggest online players
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.startsWith(args[0]))
            .collect(Collectors.toList());
      }
      return Collections.emptyList();
    }
  }

  /**
   * Lists all teams or lists the members of a specific team.
   */
  private class ListCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
      if (args.length == 0) {
        sender.sendMessage("§eTeams:");
        teamController.list().forEach(t -> sender.sendMessage("§f- " + t.name + " §r§f(" + t.displayName + "§r§f)"));
      } else {
        sender.sendMessage("§eMembers of " + args[0] + ":");
        teamController.listMembers(args[0]).forEach(playerInfo -> {
          String displayName = playerInfo.getUsername();
          String statusInfo = "";

          // Add indicator for players who have never connected
          if (!playerInfo.hasConnectedBefore()) {
            statusInfo = " §7(never connected)";
          }

          sender.sendMessage("§f- " + displayName + statusInfo);
        });
      }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
      if (args.length == 1) {
        return teamController.list().stream()
            .map(t -> t.name)
            .filter(name -> name.startsWith(args[0]))
            .collect(Collectors.toList());
      }
      return Collections.emptyList();
    }
  }

  /**
   * Teleports all online members of a team to their spawn location.
   * Can teleport all teams by using the "*" wildcard.
   */
  private class SpawnCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
      if (args.length < 1) {
        sender.sendMessage("§cUsage: /fluffyteams spawn <team>");
        return;
      }

      teamController.spawn(args[0]);
      sender.sendMessage("§aTeam teleported to spawn!");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
      if (args.length == 1) {
        List<String> suggestions = new ArrayList<>(
            teamController.list().stream()
                .map(t -> t.name)
                .collect(Collectors.toList()));

        // Add the wildcard option
        suggestions.add("*");

        return suggestions.stream()
            .filter(name -> name.startsWith(args[0]))
            .collect(Collectors.toList());
      }
      return Collections.emptyList();
    }
  }

  /**
   * Sets the spawn location for a team.
   * Can set spawn for all teams by using the "*" wildcard.
   * Supports either using the sender's current location or
   * specifying coordinates manually.
   */
  private class SetSpawnCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
      if (args.length < 1) {
        sender.sendMessage("§cUsage: /fluffyteams setspawn <team> [world] [x] [y] [z] [yaw] [pitch]");
        return;
      }

      String teamName = args[0];
      Location location = null;

      if (args.length == 1) {
        if (!(sender instanceof Player)) {
          sender.sendMessage("§cYou must be a player to use this command without coordinates.");
          return;
        }
        Player player = (Player) sender;
        location = player.getLocation();
      }

      if (args.length >= 7) {
        World world = Bukkit.getWorld(args[1]);
        if (world == null) {
          sender.sendMessage("§cWorld not found.");
          return;
        }

        try {
          double x = Double.parseDouble(args[2]);
          double y = Double.parseDouble(args[3]);
          double z = Double.parseDouble(args[4]);
          float yaw = Float.parseFloat(args[5]);
          float pitch = Float.parseFloat(args[6]);

          location = new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
          sender.sendMessage("§cInvalid coordinates format.");
          return;
        }
      }

      if (location == null) {
        sender.sendMessage("§cInvalid arguments.");
        return;
      }

      teamController.setSpawn(teamName, location);
      sender.sendMessage("§aSpawn set!");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
      if (args.length == 1) {
        List<String> suggestions = new ArrayList<>(
            teamController.list().stream()
                .map(t -> t.name)
                .collect(Collectors.toList()));

        // Add the wildcard option
        suggestions.add("*");

        return suggestions.stream()
            .filter(name -> name.startsWith(args[0]))
            .collect(Collectors.toList());
      } else if (args.length == 2) {
        // Suggest worlds
        return Bukkit.getWorlds().stream()
            .map(World::getName)
            .filter(name -> name.startsWith(args[1]))
            .collect(Collectors.toList());
      } else if (sender instanceof Player && args.length >= 3 && args.length <= 7) {
        // For coordinates, suggest the player's current position
        Player player = (Player) sender;
        Location loc = player.getLocation();

        switch (args.length) {
          case 3:
            return List.of(String.valueOf(loc.getBlockX()));
          case 4:
            return List.of(String.valueOf(loc.getBlockY()));
          case 5:
            return List.of(String.valueOf(loc.getBlockZ()));
          case 6:
            return List.of(String.valueOf((int) loc.getYaw()));
          case 7:
            return List.of(String.valueOf((int) loc.getPitch()));
        }
      }
      return Collections.emptyList();
    }
  }
}
