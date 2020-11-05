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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static agents.TestSettings.USE_SERVER_FOR_TEST;

/**
 * Test the individual policy
 */
public class Bruno_2agents_individual_MDP_test {

    private static LabRecruitsTestServer labRecruitsTestServer;

    ArrayList<String> actions = new ArrayList<String>();
    ArrayList<String> existing_buttons = new ArrayList<String>();

    ArrayList<QtableObject_individual> policy_agent0 = new ArrayList<QtableObject_individual>();
    ArrayList<QtableObject_individual> policy_agent1 = new ArrayList<QtableObject_individual>();

    @BeforeAll
    static void start() {
        TestSettings.USE_GRAPHICS = true;
//    static void start(boolean graphics) {
//        // Uncomment this to make the game's graphic visible:
//        TestSettings.USE_GRAPHICS = graphics;
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
//    public Tuple<ArrayList<Vec3>, ArrayList<Vec3>> run(String scenario_filename, String target1, String target2, ArrayList<String> actions, ArrayList<String> existing_buttons) throws InterruptedException, IOException {
    public ArrayList<String[]> run(String scenario_filename, String target1, String target2, ArrayList<String> actions, ArrayList<String> existing_buttons) throws InterruptedException, IOException {


        this.actions = actions;
        this.existing_buttons = existing_buttons;

        // Get the policy for the agents to use
        getPolicy("2agents_" + scenario_filename + "_individual_agent0_Q_time", this.policy_agent0);
        System.out.println();
        getPolicy("2agents_" + scenario_filename + "_individual_agent1_Q_time", this.policy_agent1);


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

        // set up the initial states
        agent0.update();
        State_individual currentState_agent0 = new State_individual(agent0, this.existing_buttons);

        int action_agent0 = getNextActionIndex(this.policy_agent0, currentState_agent0);


        agent1.update();
        State_individual currentState_agent1 = new State_individual(agent1, this.existing_buttons);

        int action_agent1 = getNextActionIndex(this.policy_agent1, currentState_agent1);

        // Set initial goals to agents
        var g0 = doNextAction(action_agent0, agent0);
        agent0.setGoal(g0);
        var g1 = doNextAction(action_agent1, agent1);
        agent1.setGoal(g1);


        long start = System.nanoTime();
        ArrayList<String[]> pressedButtons = new ArrayList<String[]>();

        ArrayList<Vec3> agent0Positions = new ArrayList<Vec3>();
        ArrayList<Vec3> agent1Positions = new ArrayList<Vec3>();


        while (true) {

            addToPos(agent0.getState().worldmodel.position, agent0Positions);
            addToPos(agent1.getState().worldmodel.position, agent1Positions);

            var e1 = agent0.getState().worldmodel.getElement(target1);
            var f1 = agent1.getState().worldmodel.getElement(target1);
            var e2 = agent0.getState().worldmodel.getElement(target2);
            var f2 = agent1.getState().worldmodel.getElement(target2);

            // Check if the target button isOn to end the game
            if (((e1 != null && e1.getBooleanProperty("isOn")) || (f1 != null && f1.getBooleanProperty("isOn"))) &&
                    ((e2 != null && e2.getBooleanProperty("isOn")) || (f2 != null && f2.getBooleanProperty("isOn")))) {

                if (g0.getStatus().success())
                    pressedButtons.add(new String[]{"Agent0", this.actions.get(action_agent0)});
                if (g1.getStatus().success())
                    pressedButtons.add(new String[]{"Agent1", this.actions.get(action_agent1)});

                System.out.println("Objetive completed");
                break;
            }


            if (!g0.getStatus().inProgress() && !g1.getStatus().inProgress()) {

                if (g0.getStatus().success())
                    pressedButtons.add(new String[]{"Agent0", this.actions.get(action_agent0)});
                if (g1.getStatus().success())
                    pressedButtons.add(new String[]{"Agent1", this.actions.get(action_agent1)});


                //Next Action - Agent0
                currentState_agent0 = new State_individual(agent0, this.existing_buttons);
                action_agent0 = getNextActionIndex(this.policy_agent0, currentState_agent0);
                g0 = doNextAction(action_agent0, agent0);
                agent0.setGoal(g0);

                //Next Action - Agent1
                currentState_agent1 = new State_individual(agent1, this.existing_buttons);
                action_agent1 = getNextActionIndex(this.policy_agent1, currentState_agent1);
                g1 = doNextAction(action_agent1, agent1);
                agent1.setGoal(g1);

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

//        for(Vec3 a : agent0Positions)
//            System.out.println((int)a.x + " " + (int)a.y + " " + (int)a.z );
//        System.out.println();
//        for(Vec3 b : agent1Positions)
//            System.out.println((int)b.x + " " + (int)b.y + " " + (int)b.z );

        System.out.println("INDIVIDUAL");
        System.out.println("Time " + totalTime);
        for(String[] _actions : pressedButtons)
            System.out.println(_actions[0] + " " + _actions[1]);

        return pressedButtons;
//        return new Tuple(agent0Positions, agent1Positions);
    }

    public int getNextActionIndex(ArrayList<QtableObject_individual> policy, State_individual state) {
        for (QtableObject_individual qtableObject : policy)
            if (qtableObject.state.checkAllEquals(state))
                return getArgMax_double(qtableObject.actions);

        return 0;
    }

//    public GoalStructure doNextAction(int actionIndex) {
//        String action_object = this.actions.get(actionIndex);
//        if (action_object.equals("null"))
//            return GoalLib.doNothing();
//        else
//            return GoalLib.entityIsInteracted(action_object);
//    }

    public GoalStructure doNextAction(int actionIndex, LabRecruitsTestAgent agent) {
        String action_object = this.actions.get(actionIndex);
        if (action_object.equals("null"))
            return GoalLib.doNothing();
        else
            return GoalLib.entityIsInteracted_Bruno(agent, action_object);
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

    public void getPolicy(String filename, ArrayList<QtableObject_individual> policy) throws IOException {
        // read the object from file
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            Object obj;
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            for (; ; ) {
                obj = in.readObject();
                policy.add((QtableObject_individual) obj);
//                System.out.println(obj.toString());
            }

        } catch (Exception ignored) {
        }

//        System.out.println();
        assert in != null;
        in.close();
    }

    public void addToPos(Vec3 agentPos, ArrayList<Vec3> list) {

        if (agentPos != null) {
            agentPos = new Vec3((int)agentPos.x, (int)agentPos.y, (int)agentPos.z);
            if (list.size() == 0)
                list.add(agentPos);
            boolean equal = false;
//            for (Vec3 temp : list)
//                if (temp.x == agentPos.x && temp.z == agentPos.z) {
//                    equal = true;
//                    break;
//                }

            if (list.get(list.size()-1).x == agentPos.x && list.get(list.size()-1).z == agentPos.z) {
                equal = true;
            }

            if (!equal)
                list.add(agentPos);
        }

    }

    public String prepareStateToAdd(LabRecruitsTestAgent agent0, LabRecruitsTestAgent agent1, State_centralized state) {
        String stringToAdd = "<(";
        Vec3 agent0_pos = agent0.getState().worldmodel.position;
        Vec3 agent1_pos = agent1.getState().worldmodel.position;
        if (agent0_pos != null && agent1_pos != null) {
            stringToAdd += (int) agent0_pos.x + ", " + (int) agent0_pos.y + ", " + (int) agent0_pos.z
                    + "), (" + (int) agent1_pos.x + ", " + (int) agent1_pos.y + ", " + (int) agent1_pos.z
                    + "), [";
            for (int _bs : state.button_states)
                stringToAdd += _bs + ", ";

            stringToAdd = stringToAdd.substring(0, stringToAdd.length() - 2) + "]>";
            return stringToAdd;
        } else
            return null;
    }

}
