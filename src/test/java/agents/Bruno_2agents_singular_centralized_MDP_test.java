package agents;

import agents.tactics.GoalLib;
import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
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


public class Bruno_2agents_singular_centralized_MDP_test {

    private static LabRecruitsTestServer labRecruitsTestServer = null;

    ArrayList<String[]> actions = new ArrayList<String[]>();
    ArrayList<String> existing_buttons = new ArrayList<String>();

    ArrayList<QtableObject_centralized> policy = new ArrayList<QtableObject_centralized>();

    ArrayList<String> actionPolicyList = new ArrayList<String>();

    @BeforeAll
    static void start() {
        // Uncomment this to make the game's graphic visible:
        TestSettings.USE_GRAPHICS = true;
        String labRecruitsExeRootDir = System.getProperty("user.dir");
        labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitsExeRootDir);
    }

    @AfterAll
    static void close() {
        if (labRecruitsTestServer != null) labRecruitsTestServer.close();
    }

    /**
     * Test that the agent can test this scenario
     */
    @Test
//    public Tuple<ArrayList<Vec3>, ArrayList<Vec3>> run(String scenario_filename, String target1, String target2, ArrayList<String[]> actions, ArrayList<String> existing_buttons) throws InterruptedException, IOException {
//    public ArrayList<String[]> run(String scenario_filename, String target1, String target2, ArrayList<String[]> actions, ArrayList<String> existing_buttons) throws InterruptedException, IOException {
    public void run(String scenario_filename, String target1, String target2, ArrayList<String[]> actions, ArrayList<String> existing_buttons) throws InterruptedException, IOException {

        this.actions = actions;
        this.existing_buttons = existing_buttons;

        // Get the policy for the agents to use
        getPolicy("2agents_" + scenario_filename + "_centralized_agents_Q_time");

        var environment = new LabRecruitsEnvironment(new EnvironmentConfig("bruno_" + scenario_filename));

        // Create the agent
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

        State_centralized currentState = new State_centralized(agent0, agent1, this.existing_buttons);
        int action = getNextActionIndex(currentState);
//        int action = 0;

        // Set initial goals to agents
        var g0 = doNextAction(action, 0, agent0);
        agent0.setGoal(g0);
        var g1 = doNextAction(action, 1, agent1);
        agent1.setGoal(g1);

//        while (agent0.getState().worldmodel.position == null && agent1.getState().worldmodel.position == null) {
//            agent0.update();
//            agent1.update();
//        }
//        State_centralized currentState = new State_centralized(agent0, agent1, this.existing_buttons);

        long start = System.nanoTime();

        boolean agent0First = true;
        boolean agent1First = true;

        while (true) {

            var e1 = agent0.getState().worldmodel.getElement(target1);
            var f1 = agent1.getState().worldmodel.getElement(target1);
            var e2 = agent0.getState().worldmodel.getElement(target2);
            var f2 = agent1.getState().worldmodel.getElement(target2);

            if (g0.getStatus().success() && agent0First) {
                actionPolicyList.add("agent0 pressed " + this.actions.get(action)[0]);
                agent0First = false;
            }
            if (g1.getStatus().success() && agent1First) {
                actionPolicyList.add("agent1 pressed " + this.actions.get(action)[1]);
                agent1First = false;
            }

            // Check if the target button isOn to end the game
            if ((e1 != null && e1.getBooleanProperty("isOn") || f1 != null && f1.getBooleanProperty("isOn")) &&
                    (e2 != null && e2.getBooleanProperty("isOn") || f2 != null && f2.getBooleanProperty("isOn"))) {
                System.out.println("Objetive completed");
                break;
            }

            String stringToAdd = prepareStateToAdd(agent0, agent1, new State_centralized(agent0, agent1, this.existing_buttons));
            if (stringToAdd != null)
                actionPolicyList.add(stringToAdd);

            if (!g0.getStatus().inProgress() && !g1.getStatus().inProgress()) {

                agent0First = true;
                agent1First = true;

                currentState = new State_centralized(agent0, agent1, this.existing_buttons);
                System.out.println("second " + currentState.toString());
                // Set up the next action - agent0
                action = getNextActionIndex(currentState);
                g0 = doNextAction(action, 0, agent0);
                agent0.setGoal(g0);
                g0.getStatus().resetToInProgress();
                // Set up the next action - agent1
                g1 = doNextAction(action, 1, agent1);
                agent1.setGoal(g1);
                g1.getStatus().resetToInProgress();

            }

            try {
                agent0.update();
                agent1.update();
            } catch (Exception wtf) {
            }

        }


        long finish = System.nanoTime();
        long totalTime = (finish - start) / 1_000_000_000;

        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

        System.out.println("CENTRALIZED");
//        ArrayList<String> temp_actionPolicyList = new ArrayList<String>();
        for (String _a : actionPolicyList)
//            if(temp_actionPolicyList.isEmpty() || !temp_actionPolicyList.get(temp_actionPolicyList.size()-1).equals(_a)){
//                temp_actionPolicyList.add(_a);
            System.out.println(_a);
//            }

//        return pressedButtons;
//        return new Tuple(agent0Positions, agent1Positions);

    }

    public int getNextActionIndex(State_centralized state) {

        for (QtableObject_centralized qtableObject : this.policy)
            if (qtableObject.state.checkAllEquals(state))
                return getArgMax_double(qtableObject.actions);
//        System.out.println(state.toString());
//        System.out.println();
//        for (QtableObject_centralized qtableObject : this.policy)
//            System.out.println(qtableObject.state.toString());
        return -1;
    }

//    public GoalStructure doNextAction(int actionIndex, int agent) {
//        String[] action_object = this.actions.get(actionIndex);
//        if (action_object[agent].equals("null"))
//            return GoalLib.doNothing();
//        else
//            return GoalLib.entityIsInteracted(action_object[agent]);
//    }

    public GoalStructure doNextAction(int actionIndex, int agentID, LabRecruitsTestAgent agent) {
        String[] action_object = this.actions.get(actionIndex);
        if (action_object[agentID].equals("null"))
            return GoalLib.doNothing();
        else
            return GoalLib.entityIsInteracted_Bruno(agent, action_object[agentID]);
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

    public void getPolicy(String filename) throws IOException {
        // read the object from file
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            Object obj;
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            for (; ; ) {
                obj = in.readObject();
                this.policy.add((QtableObject_centralized) obj);
//                System.out.println(obj.toString());
            }

        } catch (Exception ignored) {
//            System.out.println(i.toString());
        }

        assert in != null;

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
