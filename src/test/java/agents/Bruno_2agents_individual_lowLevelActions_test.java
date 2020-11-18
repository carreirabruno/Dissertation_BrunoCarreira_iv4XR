package agents;

import agents.tactics.GoalLib;
import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import world.BeliefState;
import world.LabEntity;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class Bruno_2agents_individual_lowLevelActions_test {

    private static LabRecruitsTestServer labRecruitsTestServer = null;

    String[] actions;
    String[] targetButtons;
    int[] doorsState;
    ArrayList<String[]> initialMapMatrix;
    ArrayList<String[]> mapMatrix;
    ArrayList<String[]> connectionButtonsDoors;

    ArrayList<IndividualQTableObj> QTableAgent0;
    ArrayList<IndividualQTableObj> QTableAgent1;

    ArrayList<String> targetButtonsAlreadyClicked;
    ArrayList<String> existingButtons;


    @BeforeAll
    static void start(boolean useLabRecruits) {
        if (useLabRecruits) {
            // Uncomment this to make the game's graphic visible:
            TestSettings.USE_GRAPHICS = true;
            String labRecruitsExeRootDir = System.getProperty("user.dir");
            labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitsExeRootDir);
        }
    }

    @AfterAll
    static void close(boolean useLabRecruits) {
        if (useLabRecruits) {
            if (labRecruitsTestServer != null) labRecruitsTestServer.close();
        }
    }

    /**
     * Test that the agent can train in this scenario
     */
    @Test
    public void test(String scenario_filename, String[] targetButtons, boolean useLabRecruits) throws InterruptedException, IOException {

        this.initialMapMatrix = new ArrayList<String[]>();
        this.connectionButtonsDoors = new ArrayList<String[]>();
//        this.QTable = getPolicy("centralizedLowLevelActions_" + scenario_filename);
        this.QTableAgent0 = getPolicy("individualHash_" + scenario_filename + "_agent0");
        this.QTableAgent1 = getPolicy("individualHash_" + scenario_filename + "_agent1");

        verifyQable(this.QTableAgent0);
        verifyQable(this.QTableAgent1);

        setUpScenarioMatrix(scenario_filename);

        this.targetButtons = targetButtons;
        this.targetButtonsAlreadyClicked = new ArrayList<String>();
        this.existingButtons = getAllExistingButtons();
        this.actions = new String[]{"Nothing", "Up", "Down", "Left", "Right", "Press"};

        if (useLabRecruits) {

//            var environment = new LabRecruitsEnvironment(new EnvironmentConfig("bruno_" + scenario_filename));
//
//            // Create the agent
//            var agent0 = new LabRecruitsTestAgent("agent0")
//                    .attachState(new BeliefState())
//                    .attachEnvironment(environment);
//            agent0.setSamplingInterval(0);
//
//            var agent1 = new LabRecruitsTestAgent("agent1")
//                    .attachState(new BeliefState())
//                    .attachEnvironment(environment);
//            agent1.setSamplingInterval(0);
//
//            // press play in Unity
//            if (!environment.startSimulation())
//                throw new InterruptedException("Unity refuses to start the Simulation!");
//
//            // set up the initial state
//            while (agent0.getState().worldmodel.position == null || agent1.getState().worldmodel.position == null) {
//                var actionAgent0 = 0;
//                var g0 = doNextAction(actionAgent0, agent0);
//                agent0.setGoal(g0);
//                var actionAgent1 = 0;
//                var g1 = doNextAction(actionAgent1, agent1);
//                agent1.setGoal(g1);
//
//                agent0.update();
//                agent1.update();
//            }
//
//            //Tem que ser assim porque se fizer isto no objeto CentralizedState criar problemas...
//            int[] posAgent0 = new int[]{(int) Math.round(agent0.getState().worldmodel.position.z), (int) Math.round(agent0.getState().worldmodel.position.x)};
//            int[] posAgent1 = new int[]{(int) Math.round(agent1.getState().worldmodel.position.z), (int) Math.round(agent1.getState().worldmodel.position.x)};
//
//            CentralizedState currentState = new CentralizedState(posAgent0, posAgent1, countApperancesOfWordOnInitialMap("button"));
//            System.out.println(currentState.toString());
//            // Set initial goals to agents
//            var actionAgent0 = chooseAction(currentState, 0);
//            var g0 = doNextAction(actionAgent0, agent0);
//            agent0.setGoal(g0);
//            var actionAgent1 = chooseAction(currentState, 1);
//            var g1 = doNextAction(actionAgent1, agent1);
//            agent1.setGoal(g1);
//
//            CentralizedState tempState = new CentralizedState(currentState);
//
//            while (!checkIfEndendLabRecruits(currentState, agent0, agent1)) {
//
//                posAgent0 = new int[]{(int) Math.round(agent0.getState().worldmodel.position.z), (int) Math.round(agent0.getState().worldmodel.position.x)};
//                posAgent1 = new int[]{(int) Math.round(agent1.getState().worldmodel.position.z), (int) Math.round(agent1.getState().worldmodel.position.x)};
//
//                tempState = new CentralizedState(posAgent0, posAgent1, countApperancesOfWordOnInitialMap("button"));
//
//                if (!currentState.equalsTo(tempState) && !g0.getStatus().inProgress() && !g1.getStatus().inProgress()) {
//                    currentState = new CentralizedState(posAgent0, posAgent1, countApperancesOfWordOnInitialMap("button"));
//                    currentState = new CentralizedState(checkCurrentStateButtonsState(currentState, agent0, agent1));
//
//                    // Set up the next action - agent0
//                    actionAgent0 = chooseAction(currentState, 0);
//                    g0 = doNextAction(actionAgent0, agent0);
//                    agent0.setGoal(g0);
//                    g0.getStatus().resetToInProgress();
//
//                    // Set up the next action - agent1
//                    actionAgent1 = chooseAction(currentState, 1);
//                    g1 = doNextAction(actionAgent1, agent1);
//                    agent1.setGoal(g1);
//                    g1.getStatus().resetToInProgress();
//
//                }
//
//                try {
//                    agent0.update();
//                    agent1.update();
//                } catch (Exception ignored) {
//                }
//
//            }
//
//            if (!environment.close())
//                throw new InterruptedException("Unity refuses to close the Simulation!");
        }
        else {

            resetMapMatrix();
            this.doorsState = new int[countApperancesOfWordOnInitialMap("door")];

            IndividualState currentStateAgent0 = new IndividualState(findTruePosInCurrentMapMatrix("agent0"), countApperancesOfWordOnInitialMap("button"));
            IndividualState currentStateAgent1 = new IndividualState(findTruePosInCurrentMapMatrix("agent1"), countApperancesOfWordOnInitialMap("button"));

            int actionAgent0;
            int actionAgent1;
            RewardRewardStateStateObject rewardRewardStateStateObject;


            int step = 0;

            while ((!EndendMatrix(currentStateAgent0, currentStateAgent1))) {

                step++;
                //action Agent0
                actionAgent0 = chooseAction(currentStateAgent0, this.QTableAgent0);

                //action Agent1
                actionAgent1 = chooseAction(currentStateAgent1, this.QTableAgent1);

                printInvertedMapMatrix();
                System.out.println(currentStateAgent0.toString());
                System.out.println("Agent0, " + this.actions[actionAgent0]);
                System.out.println(currentStateAgent1.toString());
                System.out.println("Agent1, " + this.actions[actionAgent1]);
                System.out.println("-------------------------------");

                //Act on map, get rewards and nextState
                rewardRewardStateStateObject = new RewardRewardStateStateObject(actOnMap(currentStateAgent0, actionAgent0, currentStateAgent1, actionAgent1));
                currentStateAgent0 = rewardRewardStateStateObject.stateAgent0;
                currentStateAgent1 = rewardRewardStateStateObject.stateAgent1;

                //Prints to understand whats is happening
//            System.out.println(currentState.toString());
//            System.out.println(Arrays.toString(this.doorsState));
//            System.out.println(nextState.toString());
//            System.out.println();
//            printInvertedMapMatrix();

//            printQTableObj(currentState);
//            System.out.println(getQValueQTable(currentState, -1) + "   asasa" );
//            System.out.println(getQValueQTable(currentState, -1));


                //Check if the target buttons have been clicked

            }
            System.out.println("Steps = " + step);
        }
    }

    void setUpScenarioMatrix(String scenario_filename) {
        String csvFile = "D:/GitHub/Tese_iv4XR_Pessoal/src/test/resources/levels/bruno_" + scenario_filename + ".csv";
//        String csvFile = "C:\\Users\\Beatriz Carreirer\\Documents\\Bruno_Programas\\GitHub\\Tese_iv4XR_Pessoal\\src\\test\\resources\\levels\\bruno_" + scenario_filename + ".csv";

        String line = "";
        String cvsSplitBy = ",";

        boolean MappingMatrix = false;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                // Separate using a comma
                String[] splitedLine = line.split(cvsSplitBy);

                String tempLine = "";
                for (String symbol : splitedLine) {

                    // Can start creating the map matrix
                    if (symbol.equals("|w")) {
                        MappingMatrix = true;
                        symbol = symbol.substring(1);
                    }

                    // Create the map
                    if (MappingMatrix) {
                        if (symbol.contains("door"))
                            symbol = symbol.substring(symbol.indexOf("door"));
                        if (symbol.contains("button"))
                            symbol = symbol.substring(symbol.indexOf("button"));
                        if (symbol.contains("agent"))
                            symbol = symbol.substring(symbol.indexOf("agent"));
                    }
                    tempLine += symbol + ",";
                }
                if (MappingMatrix) {
                    this.initialMapMatrix.add(tempLine.split(cvsSplitBy));
                } else
                    this.connectionButtonsDoors.add(tempLine.split(cvsSplitBy));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int[] findTruePosInInitialMapMatrix(String word) {
        int numberOfRows = this.initialMapMatrix.size();  //this is for the Z
        int numberOfColumns = this.initialMapMatrix.get(0).length;   //this is for the X

        for (int z = 0; z < numberOfRows; z++)
            for (int x = 0; x < numberOfColumns; x++)
                if (this.initialMapMatrix.get(z)[x].equals(word))
                    return new int[]{z, x};

        return null;
    }

    int[] findTruePosInCurrentMapMatrix(String word) {
        int numberOfRows = this.mapMatrix.size();  //this is for the Z
        int numberOfColumns = this.mapMatrix.get(0).length;   //this is for the X

        for (int z = 0; z < numberOfRows; z++)
            for (int x = 0; x < numberOfColumns; x++)
                if (this.mapMatrix.get(z)[x].equals(word))
                    return new int[]{z, x};

        return null;
    }

    void printInitialMapMatrix() {
        for (String[] a : this.initialMapMatrix) {
            for (String b : a)
                System.out.print(" " + b + " ");
            System.out.println();
        }
        System.out.println();
    }

    void printMapMatrix() {
        for (String[] a : this.mapMatrix) {
            for (String b : a)
                System.out.print(" " + b + " ");
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Prints the map as inverted so its easier to see as a print
     */
    void printInvertedMapMatrix() {
        for (int z = this.mapMatrix.size() - 1; z >= 0; z--) {
            for (int x = this.mapMatrix.get(z).length - 1; x >= 0; x--) {
                String temp = new String(this.mapMatrix.get(z)[x]);
                temp = temp.replace("gent", "");
                temp = temp.replace("utton", "");
                System.out.print(" " + temp + " ");
            }
            System.out.println();
        }
    }

    void printConnectionsButtonsDoors() {
        for (String[] a : connectionButtonsDoors) {
            for (String b : a)
                System.out.print(" " + b + " ");
            System.out.println();
        }
        System.out.println();
    }

    void printQTable(ArrayList<IndividualQTableObj> table) {
        for (IndividualQTableObj obj : table)
            System.out.println(obj.toString());
        System.out.println();
    }

    int countApperancesOfWordOnInitialMap(String word) {
        int count = 0;
        for (String[] a : this.initialMapMatrix)
            for (String b : a)
                if (b.contains(word))
                    count++;

        return count;
    }

    int chooseAction(IndividualState state, ArrayList<IndividualQTableObj> table) {
        for (IndividualQTableObj obj : table)
            if (obj.state.equalsTo(state))
                return obj.maxAction();
        return -1;
    }

    public GoalStructure doNextAction(int action, LabRecruitsTestAgent agent) {
        String action_object = this.actions[action];
        if (this.actions[action].equals("Nothing"))
            return GoalLib.doNothing();
        else if (this.actions[action].equals("Up")) {
//            System.out.println(this.actions[action] + " " + agent.getId());
//            Vec3 agent_pos = new Vec3((int) agent.getState().worldmodel.position.x, (int) agent.getState().worldmodel.position.y, (int) agent.getState().worldmodel.position.z);
//            System.out.println(new Vec3((int) agent_pos.x, (int) agent_pos.y, (int) agent_pos.z + 1).toString());
//            System.exit(123);
            return GoalLib.Forward(agent);
        } else if (this.actions[action].equals("Down")) {
//            System.out.println(this.actions[action] + " " + agent.getId());
            return GoalLib.Backward(agent);
        } else if (this.actions[action].equals("Left")) {
//            System.out.println(this.actions[action] + " " + agent.getId());
            return GoalLib.Left(agent);
        } else if (this.actions[action].equals("Right")) {
//            System.out.println(this.actions[action] + " " + agent.getId());
            return GoalLib.Right(agent);
        } else if (this.actions[action].equals("Press")) {
//            System.out.println(this.actions[action] + " " + agent.getId());
            return GoalLib.Press(agent, this.existingButtons);
        }
        return null;

    }

    RewardRewardStateStateObject actOnMap(IndividualState currentStateAgent0, int actionAgent0, IndividualState currentStateAgent1, int actionAgent1) {
        int[] newPos;
        //Agent0
        IndividualState nextStateAgent0 = new IndividualState(currentStateAgent0);
        int rewardAgent0 = 0;

        if (this.actions[actionAgent0].equals("Press")) {
            if (this.mapMatrix.get(nextStateAgent0.agentPos[0])[nextStateAgent0.agentPos[1]].contains("button")) {
                String buttonToClick = new String(this.mapMatrix.get(nextStateAgent0.agentPos[0])[nextStateAgent0.agentPos[1]].substring(0, 7));
                nextStateAgent0.changeButtonState(Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)));
                rewardAgent0 = getRewardFromPressingButton(buttonToClick);
                openCloseDoor(buttonToClick);

            } else
                rewardAgent0--;
        } else {
            newPos = new int[]{nextStateAgent0.agentPos[0], nextStateAgent0.agentPos[1]};

            if (this.actions[actionAgent0].equals("Up"))
                newPos = new int[]{nextStateAgent0.agentPos[0] + 1, nextStateAgent0.agentPos[1]};
            else if (this.actions[actionAgent0].equals("Down"))
                newPos = new int[]{nextStateAgent0.agentPos[0] - 1, nextStateAgent0.agentPos[1]};
            else if (this.actions[actionAgent0].equals("Left"))
                newPos = new int[]{nextStateAgent0.agentPos[0], nextStateAgent0.agentPos[1] + 1};
            else if (this.actions[actionAgent0].equals("Right"))
                newPos = new int[]{nextStateAgent0.agentPos[0], nextStateAgent0.agentPos[1] - 1};
            if (checkCanMove(newPos[0], newPos[1])) {
                nextStateAgent0.changeAgentPos(newPos[0], newPos[1]);
                changeMapMatrixAgentPositions("agent0", currentStateAgent0.agentPos[0], currentStateAgent0.agentPos[1], newPos[0], newPos[1]);
            }
            rewardAgent0--;
        }

        //Agent1
        IndividualState nextStateAgent1 = new IndividualState(currentStateAgent1);
        nextStateAgent1.copyAllButtonsState(nextStateAgent0.buttonsState);
        int rewardAgent1 = 0;

        if (this.actions[actionAgent1].equals("Press")) {
            if (mapMatrix.get(nextStateAgent1.agentPos[0])[nextStateAgent1.agentPos[1]].contains("button")) {
                String buttonToClick = new String(this.mapMatrix.get(nextStateAgent1.agentPos[0])[nextStateAgent1.agentPos[1]].substring(0, 7));
                nextStateAgent1.changeButtonState(Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)));
                rewardAgent1 = getRewardFromPressingButton(buttonToClick);
                openCloseDoor(buttonToClick);

            } else
                rewardAgent1--;
        } else {
            newPos = new int[]{nextStateAgent1.agentPos[0], nextStateAgent1.agentPos[1]};
            if (this.actions[actionAgent1].equals("Up"))
                newPos = new int[]{nextStateAgent1.agentPos[0] + 1, nextStateAgent1.agentPos[1]};
            else if (this.actions[actionAgent1].equals("Down"))
                newPos = new int[]{nextStateAgent1.agentPos[0] - 1, nextStateAgent1.agentPos[1]};
            else if (this.actions[actionAgent1].equals("Left"))
                newPos = new int[]{nextStateAgent1.agentPos[0], nextStateAgent1.agentPos[1] + 1};
            else if (this.actions[actionAgent1].equals("Right"))
                newPos = new int[]{nextStateAgent1.agentPos[0], nextStateAgent1.agentPos[1] - 1};
            if (checkCanMove(newPos[0], newPos[1])) {
                nextStateAgent1.changeAgentPos(newPos[0], newPos[1]);

                changeMapMatrixAgentPositions("agent1", currentStateAgent1.agentPos[0], currentStateAgent1.agentPos[1], newPos[0], newPos[1]);
            }
            rewardAgent1--;
        }

        nextStateAgent0.copyAllButtonsState(nextStateAgent1.buttonsState);

        return new RewardRewardStateStateObject(rewardAgent0, rewardAgent1, nextStateAgent0, nextStateAgent1);
    }

    boolean checkCanMove(int z, int x) {
        return !mapMatrix.get(z)[x].equals("w") && !mapMatrix.get(z)[x].contains("door") && !mapMatrix.get(z)[x].contains("agent");
    }

    boolean EndendMatrix(IndividualState stateAgent0, IndividualState stateAgent1) {
        int count = targetButtons.length;
        for (String button : targetButtons) {
            if ((stateAgent0.buttonsState[Integer.parseInt(new String(button.substring(button.length() - 1))) - 1]) == 1 && (stateAgent1.buttonsState[Integer.parseInt(new String(button.substring(button.length() - 1))) - 1]) == 1)
                count--;
        }
        return count == 0;
    }

    boolean checkIfEndendLabRecruits(CentralizedState state, LabRecruitsTestAgent agent0, LabRecruitsTestAgent agent1) {
        ArrayList<LabEntity> targetButtonsEntities = new ArrayList<LabEntity>();
        for (String targetBtn : this.targetButtons) {
            if (agent0.getState().worldmodel.getElement(targetBtn) != null)
                targetButtonsEntities.add(agent0.getState().worldmodel.getElement(targetBtn));
            else if (agent1.getState().worldmodel.getElement(targetBtn) != null)
                targetButtonsEntities.add(agent1.getState().worldmodel.getElement(targetBtn));
            else
                targetButtonsEntities.add(null);
        }

        for (LabEntity entity : targetButtonsEntities)
            if (entity == null || !entity.getBooleanProperty("isOn"))
                return false;

        return true;
    }

    int getRewardFromPressingButton(String buttonPressed) {
//        if(state.buttonsState[Integer.parseInt(buttonPressed.substring(buttonPressed.length() - 1)) - 1] == 1) {  // Se o agent ligou
//            for (String targetBtn : targetButtons) {
//                if (targetBtn.equals(buttonPressed)) {
//                    if (!AlreadyClicked(buttonPressed))
//                        return 100;
//                    else
//                        return -10;
//                }
//            }
//        }
//        return -1;


        for (String targetBtn : targetButtons) {
            if (targetBtn.equals(buttonPressed)) {
                if (!AlreadyClicked(buttonPressed))
                    return 1000;
//                else if(AlreadyClicked(buttonPressed) && state.buttonsState[Integer.parseInt(buttonPressed.substring(buttonPressed.length() - 1)) - 1] == 1)
//                    return 10;
            }
        }
        return -1;

    }

    void openCloseDoor(String button) {
        for (String[] connection : this.connectionButtonsDoors)
            if (connection[0].equals(button))
                for (int i = 1; i < connection.length; i++)
                    this.doorsState[Integer.parseInt(new String(connection[i].substring(connection[i].length() - 1))) - 1] ^= 1;

        for (int i = 0; i < this.doorsState.length; i++) {
            int[] doorPos = findTruePosInInitialMapMatrix("door" + (i + 1));
            if (this.doorsState[i] == 0)    //Close de door
                this.mapMatrix.get(doorPos[0])[doorPos[1]] = "door" + (i + 1);
            else if (this.doorsState[i] == 1)   //Open the door
                this.mapMatrix.get(doorPos[0])[doorPos[1]] = "f";   //Open the door
        }

    }

    void changeMapMatrixAgentPositions(String agent, int oldX, int oldZ, int newX, int newZ) {
        if (this.mapMatrix.get(newX)[newZ].equals("f") && !this.mapMatrix.get(oldX)[oldZ].contains("button")) {
            this.mapMatrix.get(oldX)[oldZ] = "f";
            this.mapMatrix.get(newX)[newZ] = agent;
        } else if (this.mapMatrix.get(newX)[newZ].equals("f") && this.mapMatrix.get(oldX)[oldZ].contains("button")) {
            this.mapMatrix.get(oldX)[oldZ] = this.mapMatrix.get(oldX)[oldZ].substring(0, 7);
            this.mapMatrix.get(newX)[newZ] = agent;
        } else if (this.mapMatrix.get(newX)[newZ].contains("button") && !this.mapMatrix.get(oldX)[oldZ].contains("button")) {
            this.mapMatrix.get(oldX)[oldZ] = "f";
            this.mapMatrix.get(newX)[newZ] = this.mapMatrix.get(newX)[newZ] + "|" + agent;
        } else if (this.mapMatrix.get(newX)[newZ].contains("button") && this.mapMatrix.get(oldX)[oldZ].contains("button")) {
            this.mapMatrix.get(oldX)[oldZ] = this.mapMatrix.get(oldX)[oldZ].substring(0, 7);
            this.mapMatrix.get(newX)[newZ] = this.mapMatrix.get(newX)[newZ] + "|" + agent;
        }
    }

    void resetMapMatrix() {
        this.mapMatrix = new ArrayList<String[]>();
        for (String[] a : this.initialMapMatrix)
            this.mapMatrix.add((String[]) a.clone());
    }

    boolean objExistsInQTable(IndividualQTableObj obj, ArrayList<IndividualQTableObj> table) {
        for (IndividualQTableObj temp : table)
            if (temp.equalsTo(obj))
                return true;
        return false;
    }

    float getQValueQTable(IndividualState state, int action, ArrayList<IndividualQTableObj> table) {
        for (IndividualQTableObj temp : table) {
            if (temp.state.equalsTo(state)) {
                if (action != -1)
                    return temp.actionsQValues[action];
                else
                    return temp.maxActionQValue();
            }
        }
        return -1;
    }

    ArrayList<IndividualQTableObj> getPolicy(String filename) throws IOException {
        ArrayList<IndividualQTableObj> policy = new ArrayList<IndividualQTableObj>();
        // read the object from file
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            Object obj;
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            for (; ; ) {
                obj = in.readObject();
                policy.add((IndividualQTableObj) obj);
            }

        } catch (Exception ignored) {
        }


        assert in != null;
        in.close();


        return policy;
    }

    boolean AlreadyClicked(String buttonPressed) {
        if (this.targetButtonsAlreadyClicked.size() == 0) {
        } else {
            for (String btn : this.targetButtonsAlreadyClicked)
                if (btn.equals(buttonPressed))
                    return true;
        }
        this.targetButtonsAlreadyClicked.add(buttonPressed);
        return false;
    }

    void printQTableObj(IndividualState obj, ArrayList<IndividualQTableObj> table) {
        for (IndividualQTableObj temp : table)
            if (temp.state.equalsTo(obj))
                System.out.println("aaa " + temp.toString());
    }

    void verifyQable(ArrayList<IndividualQTableObj> table) {

        for (IndividualQTableObj a : table) {
            int count = 0;
            for (IndividualQTableObj b : table)
                if (a.equalsTo(b))
                    count++;
            if (count > 1)
                System.out.println("HEYHEYHEY " + a.toString());
        }

    }

    void printBestActionCurrentState(IndividualState state, ArrayList<IndividualQTableObj> table) {
        for (IndividualQTableObj obj : table)
            if (obj.state.equalsTo(state))
                System.out.println("Best Action = " + this.actions[obj.maxAction()]);
    }

    ArrayList<String> getAllExistingButtons(){
        ArrayList<String> temp = new ArrayList<String>();
        for (String[] a : this.initialMapMatrix)
            for (String b : a)
                if (b.contains("button"))
                    temp.add(b);
        return temp;
    }

    CentralizedState checkCurrentStateButtonsState(CentralizedState state, LabRecruitsTestAgent agent0, LabRecruitsTestAgent agent1){
        CentralizedState tempState = new CentralizedState(state);
        for(String btn: this.existingButtons){
            int buttonNumber = Integer.parseInt(btn.replace("button",""));
            var e0 = (LabEntity) agent0.getState().worldmodel.getElement(btn);
            var e1 = (LabEntity) agent1.getState().worldmodel.getElement(btn);
            if(((e0 != null && e0.getBooleanProperty("isOn")) || (e1 != null && e1.getBooleanProperty("isOn"))) && tempState.buttonsState[buttonNumber-1] == 0)
                tempState.changeButtonState(buttonNumber);
            else if(((e0 != null && !e0.getBooleanProperty("isOn")) || (e1 != null && !e1.getBooleanProperty("isOn"))) && tempState.buttonsState[buttonNumber-1] == 1)
                tempState.changeButtonState(buttonNumber);
        }
        return tempState;
    }

}
//class CentralizedQtableObject implements Serializable {
//
//    CentralizedState state;
//    double[] actions;
//
//    public CentralizedQtableObject(CentralizedState state) {
//        this.state = state;
//        this.actions = new double[(state.button_states.size() + 1) * (state.button_states.size() + 1)];
//    }
//
//    public CentralizedQtableObject(CentralizedState state, double[] actions) {
//        this.state = state;
//        this.actions = actions;
//    }
//
//    public CentralizedQtableObject(CentralizedQtableObject obj) {
//        this.state = obj.state;
//        this.actions = obj.actions;
//    }
//
//    public CentralizedQtableObject(CentralizedQtableObject obj, boolean empty) {
//        this.state = obj.state;
//        this.actions = new double[(obj.state.button_states.size() + 1) * (obj.state.button_states.size() + 1)];
//    }
//
//    public void printQtableObject() {
//        System.out.println("+++ State_Actions +++");
//        state.print_currentState();
//        for (int i = 0; i < actions.length; i++)
//            System.out.println("Action " + i + " = " + actions[i]);
//    }
//
//    @Override
//    public String toString() {
////        return "QtableObject {State=" + state.toString() + ", Actions=" + actions.toString() + "}";
//        return "QtableObject {State=(" + state.toString() + ") , Actions(" + Arrays.toString(actions) + ")}";
//    }
//}
//
//class CentralizedTransitionObject {
//
//    CentralizedState currentState;
//    ArrayList<CentralizedQtableObject> transitions = new ArrayList<CentralizedQtableObject>();
//
//    public CentralizedTransitionObject(CentralizedState currentState) {
//        this.currentState = currentState;
//    }
//
//    public void udpateTransitionListAction(CentralizedQtableObject nextState, int action) {
//        boolean exists = false;
//        for (CentralizedQtableObject state : transitions) {
//            if (state.state.checkAllEquals(nextState.state)) {
//                state.actions[action] = 1;
//                exists = true;
//                break;
//            }
//        }
//        if (!exists) {
//            transitions.add(new CentralizedQtableObject(nextState, true));
//            transitions.get(transitions.size() - 1).actions[action] = 1;
//        }
//    }
//
//    public void udpateTransitionListReward(CentralizedQtableObject nextState, int action, double reward) {
//        boolean exists = false;
//        for (CentralizedQtableObject state : transitions) {
//            if (state.state.checkAllEquals(nextState.state)) {
//                state.actions[action] = reward;
//                exists = true;
//                break;
//            }
//        }
//        if (!exists) {
//            transitions.add(new CentralizedQtableObject(nextState, true));
//            transitions.get(transitions.size() - 1).actions[action] = reward;
//        }
//    }
//
//    @Override
//    public String toString() {
//        return "TransitionTable {State=(" + currentState.toString() + ") , Transitions(" + transitions.toString() + ")}";
//    }
//
//}