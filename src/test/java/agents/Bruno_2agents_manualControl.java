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

public class Bruno_2agents_manualControl{

    private static LabRecruitsTestServer labRecruitsTestServer;

    int max_time = 60;


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
    public void create_policy_manually(String scenario_filename, String target1, String target2) throws InterruptedException, IOException {


        System.out.println(LocalDateTime.now());


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

        long start = System.nanoTime();
        long lasTime = System.nanoTime();
        final double amountOfTicks = 5.0;  // update 5x per second
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;

        while (((System.nanoTime() - start) / 1_000_000_000) < max_time) {
            long now = System.nanoTime();
            delta += (now - lasTime) / ns;
            lasTime = now;
            if (delta >= 1) {
                var e1 = agent0.getState().worldmodel.getElement(target1);
                var f1 = agent1.getState().worldmodel.getElement(target1);
                var e2 = agent0.getState().worldmodel.getElement(target2);
                var f2 = agent1.getState().worldmodel.getElement(target2);


                // Check if the target button isOn to end the game - Tem que estar aqui para o reward ser válido
                if ((e1 != null && e1.getBooleanProperty("isOn") || f1 != null && f1.getBooleanProperty("isOn")) &&
                        (e2 != null && e2.getBooleanProperty("isOn") || f2 != null && f2.getBooleanProperty("isOn"))) {
                    System.out.println("Objetive completed");
                    break;
                }

                try {
                    agent0.update();
                    agent1.update();
                } catch (Exception ignored) {
                }

                delta--;
            }
        }

        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");
    }


}

