package agents;

import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import world.BeliefState;

import java.io.IOException;
import java.time.LocalDateTime;


import static agents.TestSettings.USE_SERVER_FOR_TEST;

public class Bruno_2agents_manualControl {

    private static LabRecruitsTestServer labRecruitsTestServer;

    int max_time = 60000;


    @BeforeAll
    static void start() throws Exception {
        // Uncomment this to make the game's graphic visible:
        TestSettings.USE_GRAPHICS = true;
        String labRecruitsExeRootDir = System.getProperty("user.dir");
        labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitsExeRootDir);
    }

    @AfterAll
    static void close() {
        if (USE_SERVER_FOR_TEST) labRecruitsTestServer.close();
    }

    /**
     * Test that the agent can train in this scenario
     */
    @Test
    public void create_policy_manually(String scenario_filename) throws InterruptedException{


//        var environment = new LabRecruitsEnvironment(new EnvironmentConfig("bruno_" + scenario_filename).replaceAgentMovementSpeed(0.53f));
        var environment = new LabRecruitsEnvironment(new EnvironmentConfig("bruno_" + scenario_filename));

        // Create the agents
        var agent0 = new LabRecruitsTestAgent("agent0")
                .attachState(new BeliefState())
                .attachEnvironment(environment);
        agent0.setSamplingInterval(0);

        var agent1 = new LabRecruitsTestAgent("agent1")
                .attachState(new BeliefState())
                .attachEnvironment(environment);
        agent1.setSamplingInterval(0);

        // press play in Unity
        if (!environment.startSimulation())
            throw new InterruptedException("Unity refuses to start the Simulation!");


        while (true) {

            try {
                agent0.update();
                agent1.update();
            } catch (Exception ignored) {
            }

        }
    }

}

