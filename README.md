# FluffyTeams

A Minecraft team management plugin for Spigot/Paper servers that allows administrators to create and manage teams with custom names and permissions.

## Features

- **Team Management**: Create, delete, and list teams with custom names and display names
- **Player Management**: Add or remove players to/from teams
- **Offline Player Support**: Add players who have never connected to your server (validated through Mojang API)
- **Team Spawns**: Set spawn points for each team and teleport team members to their spawns
- **Multi-server Support**: Configure different spawn points for the same team across multiple servers
- **LuckPerms Integration**: Automatically adds/removes team permission groups when players join/leave teams
- **Tab Completion**: Full tab completion support for all commands

## Real-World Application

This plugin was originally developed for and used during the Fluff Event, where it managed team checkpoints in jump/parkour maps. Command blocks could interact with the plugin to set team-specific checkpoints, allowing different teams to have their own progression through challenge maps.

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your server's `plugins` folder
3. Start or restart your server
4. Configure the plugin using `plugins/FluffyTeams/config.yml`

## Configuration

The default configuration file will be generated on first run:

```yaml
# Database connection string (defaults to SQLite)
database-uri: "jdbc:sqlite:plugins/FluffyTeams/database.db"
database-username: ""
database-password: ""

# Server name (used for multi-server setups)
server-name: "default"

# Override player bed respawn location with team spawn
override-bed-respawn-location: false
```

## Commands

All commands are accessible through `/fluffyteams` or the shorthand `/ft`

### Team Management
- `/ft help` - Display help information
- `/ft create <name> <display_name>` - Create a new team
  - Example: `/ft create red &cRed Team`
- `/ft delete <name>` - Delete a team and remove all its members
- `/ft list` - List all teams
- `/ft list <team>` - List all members of a specific team

### Player Management
- `/ft add <team> <player>` - Add a player to a team
- `/ft remove <player>` - Remove a player from their team

### Spawn Management
- `/ft spawn <team>` - Teleport all members of a team to their spawn point
  - Use `*` to teleport all teams to their respective spawns
- `/ft setspawn <team>` - Set the team spawn to your current location
  - Use `*` to set the same spawn for all teams
- `/ft setspawn <team> <world> <x> <y> <z> <yaw> <pitch>` - Set team spawn with specific coordinates

## Permissions

- `fluffyteams.admin` - Access to all FluffyTeams commands

## LuckPerms Integration

When a player is added to a team, they are automatically given the parent group matching the team name in LuckPerms.
For example, if a player is added to the "red" team, they will be given the "red" parent group in LuckPerms.

## Example Use Cases

### Parkour/Jump Maps
- Use command blocks with `/ft setspawn <team>` to create team-specific checkpoints
- Use `/ft spawn <team>` to teleport them back to their team's latest or next checkpoint
- Track progress separately for each team in competition events

### Minigames
- Create teams for various minigames with different permissions and spawn points
- Use LuckPerms integration to give teams specific abilities based on their team

### RPG Servers
- Establish different factions or guilds using teams
- Set team base locations using the spawn system
- Manage offline players and allow adding members who haven't joined yet

## Support

For issues, feature requests, or questions, please open an issue on the GitHub repository.
