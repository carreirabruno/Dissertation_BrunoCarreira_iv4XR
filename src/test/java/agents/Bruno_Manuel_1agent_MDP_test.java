package agents;

import agents.tactics.GoalLib;
import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import helperclasses.datastructures.Tuple;
import helperclasses.datastructures.Vec3;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import world.BeliefState;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static agents.TestSettings.USE_SERVER_FOR_TEST;

public class Bruno_Manuel_1agent_MDP_test {

    private static LabRecruitsTestServer labRecruitsTestServer;

    ArrayList<String> actions = new ArrayList<String>();
    ArrayList<String> existing_buttons = new ArrayList<String>();

    ArrayList<QtableObject> policy_agent = new ArrayList<QtableObject>();


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
     * Test that the agent can test this scenario
     */
    @Test
//    public Tuple<ArrayList<Vec3>, ArrayList<Vec3>> run(String scenario_filename, String target1, String target2) throws InterruptedException, IOException {
    public void run() throws InterruptedException, IOException {

        // Set the possible actions
//        this.actions.add("null");
        this.actions.add("forward");
        this.actions.add("backward");
        this.actions.add("left");
        this.actions.add("right");

        this.existing_buttons.add("button0");
        this.existing_buttons.add("button1");


        String scenario_filename = "bruno_manuel_scenario1";
        boolean buttons = true;

        // Get the policy for the agents to use
        getPolicy("Manuel_1agent_" + scenario_filename, this.policy_agent);
        if(buttons)
            getPolicy("Manuel_1agent_" + scenario_filename, this.policy_agent);
        else
            getPolicy("Manuel_1agent_" + scenario_filename + "noButtons", this.policy_agent);


        var environment = new LabRecruitsEnvironment(new EnvironmentConfig(scenario_filename));

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

        // set up the initial states
        agent0.update();
        State currentState_agent0 = new State(agent0, this.existing_buttons, buttons);

        int action_agent0 = getNextActionIndex(this.policy_agent, currentState_agent0);

        // Set initial goals to agents
        var g0 = doNextAction(agent0, action_agent0);
        agent0.setGoal(g0);

        long start = System.nanoTime();
//        ArrayList<String[]> pressedButtons = new ArrayList<String[]>();
//
//        ArrayList<Vec3> agent0Positions = new ArrayList<Vec3>();
//        ArrayList<Vec3> agent1Positions = new ArrayList<Vec3>();


        while (true) {

//            addToPos(agent0.getState().worldmodel.position, agent0Positions);
//            addToPos(agent1.getState().worldmodel.position, agent1Positions);

            var goalButton = agent0.getState().worldmodel.getElement("button1");

            // Check if the target button isOn to end the game - Tem que estar aqui para o reward ser válido
            if (goalButton != null && goalButton.getBooleanProperty("isOn")) {

//                if (g0.getStatus().success())
//                    pressedButtons.add(new String[] {"Agent0", this.actions.get(action_agent0)});

                System.out.println("Objetive completed");
                break;
            }


            if (!g0.getStatus().inProgress()) {

                System.out.println(currentState_agent0.toString());

//                if (g0.getStatus().success())
//                    pressedButtons.add(new String[] {"Agent0", this.actions.get(action_agent0)});


                //Next Action - Agent0
                currentState_agent0 = new State(agent0, this.existing_buttons, buttons);
                action_agent0 = getNextActionIndex(this.policy_agent, currentState_agent0);
                g0 = doNextAction(agent0, action_agent0);
                agent0.setGoal(g0);

            }

            try {
                agent0.update();
                agent1.update();
            } catch (Exception ignored) {
            }

        }


        long finish = System.nanoTime();
        long totalTime = (finish - start) / 1_000_000_000;

        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

        System.out.println("Time " + totalTime);

//        return new Tuple(agent0Positions, agent1Positions);
    }

    public int getNextActionIndex(ArrayList<QtableObject> policy, State state) {
        for (QtableObject qtableObject : policy)
            if (qtableObject.state.checkAllEquals(state))
                return getArgMax_double(qtableObject.actions);

        return 0;
    }

    public GoalStructure doNextAction(LabRecruitsTestAgent agent, int actionIndex) {
        String action_object = this.actions.get(actionIndex);
        if (action_object.equals("null"))
            return GoalLib.doNothing();
        else if (action_object.equals("forward"))
            return GoalLib.ForwardAndInteract(agent, this.existing_buttons);
        else if (action_object.equals("backward"))
            return GoalLib.BackwardAndInteract(agent, this.existing_buttons);
        else if (action_object.equals("left"))
            return GoalLib.LeftAndInteract(agent, this.existing_buttons);
        else if (action_object.equals("right"))
            return GoalLib.RightAndInteract(agent, this.existing_buttons);
        else
            return null;
    }

    public int getArgMax_double(double[] array) {
        double max = array[0];
        int maxIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] >= max) {
                max = array[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    public void getPolicy(String filename, ArrayList<QtableObject> policy) throws IOException {
        // read the object from file
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            Object obj;
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            for (; ; ) {
                obj = in.readObject();
                policy.add((QtableObject) obj);
                System.out.println(obj.toString());
            }

        } catch (Exception ignored) {
        }

//        System.out.println();
        assert in != null;
        in.close();
    }

    public void addToPos(Vec3 agentPos, ArrayList<Vec3> list) {
        if (agentPos != null) {
            if (list.size() == 0)
                list.add(agentPos);
            boolean equal = false;
            for (Vec3 temp : list)
                if (temp.equals(agentPos) || (temp.x == agentPos.x && temp.z == agentPos.z)) {
                    equal = true;
                    break;
                }
            if (!equal)
                list.add(agentPos);
        }

    }


}