import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.dieselpoint.norm.Database;
import com.dieselpoint.norm.Query;

import fr.fluffevent.fluffyteams.controllers.TeamController;
import fr.fluffevent.fluffyteams.database.DatabaseManager;
import fr.fluffevent.fluffyteams.models.database.Member;
import fr.fluffevent.fluffyteams.models.database.Spawn;
import fr.fluffevent.fluffyteams.models.database.Team;

/**
 * Unit tests for TeamController, using simplified database mocking
 *
 * This test class uses MockitoJUnitRunner.Silent to avoid errors about
 * unnecessary stubbings.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TeamControllerTest {

    @Mock
    private Database mockDb;

    @Mock
    private Query mockQuery;

    @Mock
    private Server mockServer;

    @Mock
    private Player mockPlayer;

    @Mock
    private World mockWorld;

    @Mock
    private PluginManager mockPluginManager;

    @Mock
    private Location mockLocation;

    private TeamController teamController;
    private UUID playerUUID;

    @Before
    public void setUp() throws Exception {
        // Setup fixed UUID for consistency
        playerUUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

        // Configure basic mocks using lenient mode to avoid unnecessary stubbing errors
        lenient().when(mockPlayer.getUniqueId()).thenReturn(playerUUID);
        lenient().when(mockPlayer.getName()).thenReturn("TestPlayer");

        // Setup database and query mocking
        lenient().when(mockDb.where(anyString(), any())).thenReturn(mockQuery);
        lenient().when(mockDb.where(anyString(), any(), any())).thenReturn(mockQuery);

        // Setup location mock
        lenient().when(mockLocation.getWorld()).thenReturn(mockWorld);
        lenient().when(mockWorld.getName()).thenReturn("testworld");
        lenient().when(mockLocation.getX()).thenReturn(100.0);
        lenient().when(mockLocation.getY()).thenReturn(64.0);
        lenient().when(mockLocation.getZ()).thenReturn(200.0);
        lenient().when(mockLocation.getYaw()).thenReturn(90.0f);
        lenient().when(mockLocation.getPitch()).thenReturn(0.0f);

        // Create controller with our mocked components
        teamController = new TeamController();

        // Replace the database in controller via reflection
        Field dbField = TeamController.class.getDeclaredField("db");
        dbField.setAccessible(true);
        dbField.set(teamController, mockDb);
    }

    @Test
    public void testGetTeam() {
        // Setup test data
        Team expectedTeam = new Team();
        expectedTeam.id = 1;
        expectedTeam.name = "TestTeam";
        expectedTeam.displayName = "Test Team";

        List<Team> teamList = new ArrayList<>();
        teamList.add(expectedTeam);

        // Setup mock behavior
        when(mockQuery.results(Team.class)).thenReturn(teamList);

        // Mock the static methods for both Bukkit and DatabaseManager
        try (var bukkitMock = mockStatic(Bukkit.class);
                var dbManagerMock = mockStatic(DatabaseManager.class)) {

            // Setup the static mocks
            bukkitMock.when(Bukkit::getServer).thenReturn(mockServer);
            when(mockServer.getPluginManager()).thenReturn(mockPluginManager);
            dbManagerMock.when(DatabaseManager::getDatabase).thenReturn(mockDb);

            // Execute
            Team result = teamController.getTeam("TestTeam");

            // Verify
            assertNotNull("Team should not be null", result);
            assertEquals("Team name should match", "TestTeam", result.name);
            assertEquals("Team ID should match", 1, result.id);
        }
    }

    @Test
    public void testCreateTeam() {
        // Mock the static methods for both Bukkit and DatabaseManager
        try (var bukkitMock = mockStatic(Bukkit.class);
                var dbManagerMock = mockStatic(DatabaseManager.class)) {

            // Setup the static mocks
            bukkitMock.when(Bukkit::getServer).thenReturn(mockServer);
            when(mockServer.getPluginManager()).thenReturn(mockPluginManager);
            dbManagerMock.when(DatabaseManager::getDatabase).thenReturn(mockDb);

            // Execute
            teamController.create("TestTeam", "&cTest Team");

            // Verify
            verify(mockDb).insert(argThat(team -> ((Team) team).name.equals("TestTeam") &&
                    ((Team) team).displayName.equals(ChatColor.RED + "Test Team")));
        }
    }

    @Test
    public void testAddMember() {
        // Setup test data
        Team team = new Team();
        team.id = 1;
        team.name = "TestTeam";

        List<Team> teamList = new ArrayList<>();
        teamList.add(team);

        List<Member> emptyMemberList = new ArrayList<>();

        // Setup mock behavior
        when(mockQuery.results(Team.class)).thenReturn(teamList);
        when(mockQuery.results(Member.class)).thenReturn(emptyMemberList);
        when(mockPluginManager.isPluginEnabled("LuckPerms")).thenReturn(true);

        // Mock the static methods
        try (var bukkitMock = mockStatic(Bukkit.class);
                var dbManagerMock = mockStatic(DatabaseManager.class)) {

            // Setup the static mocks
            bukkitMock.when(Bukkit::getServer).thenReturn(mockServer);
            when(mockServer.getPluginManager()).thenReturn(mockPluginManager);
            dbManagerMock.when(DatabaseManager::getDatabase).thenReturn(mockDb);

            // Execute
            teamController.addMember("TestTeam", mockPlayer);

            // Verify member was added to database
            verify(mockDb).insert(argThat(member -> ((Member) member).teamId == 1 &&
                    ((Member) member).playerUuid.equals(playerUUID.toString())));

            // Verify LuckPerms command was executed
            verify(mockServer).dispatchCommand(any(), contains("lp user " + playerUUID + " parent add TestTeam"));
        }
    }

    @Test
    public void testSetSpawn() {
        // Setup test data
        Team team = new Team();
        team.id = 1;
        team.name = "TestTeam";

        List<Team> teamList = new ArrayList<>();
        teamList.add(team);

        List<Spawn> emptySpawnList = new ArrayList<>();

        // Setup mock behavior
        when(mockQuery.results(Team.class)).thenReturn(teamList);
        when(mockQuery.results(Spawn.class)).thenReturn(emptySpawnList);

        // Mock the static methods
        try (var bukkitMock = mockStatic(Bukkit.class);
                var dbManagerMock = mockStatic(DatabaseManager.class)) {

            // Setup the static mocks
            bukkitMock.when(Bukkit::getServer).thenReturn(mockServer);
            dbManagerMock.when(DatabaseManager::getDatabase).thenReturn(mockDb);

            // Execute
            teamController.setSpawn("TestTeam", mockLocation);

            // Verify
            verify(mockDb).insert(argThat(spawn -> {
                Spawn s = (Spawn) spawn;
                return s.teamId == 1 &&
                        s.world.equals("testworld") &&
                        s.x == 100.0 &&
                        s.y == 64.0 &&
                        s.z == 200.0 &&
                        s.yaw == 90.0f &&
                        s.pitch == 0.0f;
            }));
        }
    }
}
