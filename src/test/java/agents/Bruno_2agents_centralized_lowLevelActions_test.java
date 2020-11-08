package agents;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class Bruno_2agents_centralized_lowLevelActions_test {

    String[] actions;
    ArrayList<String[]> centralizedActions;
    String[] targetButtons;
    int[] doorsState;
    ArrayList<String[]> initialMapMatrix;
    ArrayList<String[]> mapMatrix;
    ArrayList<String[]> connectionButtonsDoors;

    ArrayList<CentralizedQTableObj> QTable;

    ArrayList<String> targetButtonsAlreadyClicked;


    @BeforeAll
    static void start() {
    }

    @AfterAll
    static void close() {
    }

    /**
     * Test that the agent can train in this scenario
     */
    @Test
    public void test(String scenario_filename, String[] targetButtons) throws InterruptedException, IOException {

        this.initialMapMatrix = new ArrayList<String[]>();
        this.connectionButtonsDoors = new ArrayList<String[]>();
//        this.QTable = getPolicy("centralizedLowLevelActions_" + scenario_filename + "bestQyet");
        this.QTable = getPolicy("centralizedLowLevelActions_" + scenario_filename);

//        printQTable();
//        System.exit(113);

        setUpScenarioMatrix(scenario_filename);

        this.targetButtons = targetButtons;
        this.targetButtonsAlreadyClicked = new ArrayList<String>();
        this.actions = new String[]{"Nothing", "Up", "Down", "Left", "Right", "Press"};
        setupCentralizedActions();

//        for(int i = 0; i <this.centralizedActions.size(); i++)
//            System.out.println(i + "  " + Arrays.toString(this.centralizedActions.get(i)));

        resetMapMatrix();
        this.doorsState = new int[countApperancesOfWordOnInitialMap("door")];

        CentralizedState currentState = new CentralizedState(findTruePosInInitialMapMatrix("agent0"), findTruePosInInitialMapMatrix("agent1"), countApperancesOfWordOnInitialMap("button"));


        int actionAgent0;
        int actionAgent1;
        RewardRewardStateObject rewardRewardStateObject;

        printInvertedMapMatrix();

        boolean ended = false;

        int step = 0;
//        while (!ended) {
        while (step < 10) {
            step++;
            //action Agent0
            actionAgent0 = chooseAction(currentState, 0);

            //action Agent1
            actionAgent1 = chooseAction(currentState, 1);

            printInvertedMapMatrix();
            System.out.println(currentState.toString());
            System.out.println("Agent0, " + this.actions[actionAgent0]);
            System.out.println("Agent1, " + this.actions[actionAgent1]);
            System.out.println(Arrays.toString(this.targetButtonsAlreadyClicked.toArray()));


            //Act on map, get rewards and nextState
            currentState= new CentralizedState(actOnMap(currentState, actionAgent0, actionAgent1));

            //Prints to understand whats is happening
            System.out.println(currentState.toString());
//            System.out.println(Arrays.toString(this.doorsState));
//            System.out.println(nextState.toString());
//            System.out.println();
            printInvertedMapMatrix();
            System.out.println("-------------------------------");

            //Check if the target buttons have been clicked
            if (checkIfEndend(currentState)) {
                ended = true;
            }
        }
        System.out.println("Steps = " + step);

        System.out.println(this.QTable.get(0).toString());
    }

    void setUpScenarioMatrix(String scenario_filename) {
        String csvFile = "D:/GitHub/Tese_iv4XR_Pessoal/src/test/resources/levels/bruno_" + scenario_filename + ".csv";
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
            for (int x = this.mapMatrix.get(z).length - 1; x >= 0; x--)
                System.out.print(" " + this.mapMatrix.get(z)[x] + " ");
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

    void printQTable() {
        for (CentralizedQTableObj obj : this.QTable)
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

    int chooseAction(CentralizedState state, int agent) {
        for (CentralizedQTableObj obj : this.QTable) {
            if (obj.state.equalsTo(state)) {
                for (int i = 0; i < this.actions.length; i++)
                    if (this.actions[i].equals(this.centralizedActions.get(obj.maxAction())[agent]))
                        return i;
            }
        }
        return -1;
    }

    CentralizedState actOnMap(CentralizedState currentState, int actionAgent0, int actionAgent1) {
        CentralizedState nextState = new CentralizedState(currentState);
        int rewardAgent0 = 0;
        int rewardAgent1 = 0;
        int[] newPos;

        //Agent0
        if (this.actions[actionAgent0].equals("Press")) {
            if (this.mapMatrix.get(nextState.agent0Pos[0])[nextState.agent0Pos[1]].contains("button")) {
                String buttonToClick = new String(this.mapMatrix.get(nextState.agent0Pos[0])[nextState.agent0Pos[1]].substring(0, 7));
                nextState.changeButtonState(Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)));
                rewardAgent0 = getRewardFromPressingButton(nextState,buttonToClick);
                openCloseDoor(buttonToClick);

//                printInvertedMapMatrix();
//                System.out.println(nextState.toString());
//                System.out.println("Agent0 buttonPressed " + buttonToClick + " | Reward " + rewardAgent0);
//                System.out.println(this.targetButtonsAlreadyClicked.size() + " | " + Arrays.toString(this.targetButtonsAlreadyClicked.toArray()));
//                System.out.println();
            }
        }
        else {
            newPos = new int[]{nextState.agent0Pos[0], nextState.agent0Pos[1]};
            if (this.actions[actionAgent0].equals("Nothing")) {
                //Do Nothing
            } else {
                if (this.actions[actionAgent0].equals("Up"))
                    newPos = new int[]{nextState.agent0Pos[0] + 1, nextState.agent0Pos[1]};
                else if (this.actions[actionAgent0].equals("Down"))
                    newPos = new int[]{nextState.agent0Pos[0] - 1, nextState.agent0Pos[1]};
                else if (this.actions[actionAgent0].equals("Left"))
                    newPos = new int[]{nextState.agent0Pos[0], nextState.agent0Pos[1] + 1};
                else if (this.actions[actionAgent0].equals("Right"))
                    newPos = new int[]{nextState.agent0Pos[0], nextState.agent0Pos[1] - 1};
                if (checkCanMove(newPos[0], newPos[1])) {
                    nextState.changeAgent0Pos(newPos[0], newPos[1]);
                    rewardAgent0--;
                    changeMapMatrixAgentPositions("agent0", currentState.agent0Pos[0], currentState.agent0Pos[1], newPos[0], newPos[1]);
                }

            }


        }

        //Agent1
        if (this.actions[actionAgent1].equals("Press")) {
            if (mapMatrix.get(nextState.agent1Pos[0])[nextState.agent1Pos[1]].contains("button")) {
                String buttonToClick = new String(this.mapMatrix.get(nextState.agent1Pos[0])[nextState.agent1Pos[1]].substring(0, 7));
                nextState.changeButtonState(Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)));
                rewardAgent1 = getRewardFromPressingButton(nextState, buttonToClick);
                openCloseDoor(buttonToClick);

//                printInvertedMapMatrix();
//                System.out.println(nextState.toString());
//                System.out.println("Agent1 buttonPressed " + buttonToClick + " | Reward " + rewardAgent1);
//                System.out.println(this.targetButtonsAlreadyClicked.size() + " | " + Arrays.toString(this.targetButtonsAlreadyClicked.toArray()));
//                System.out.println();
            }
        }
        else {
            newPos = new int[]{nextState.agent1Pos[0], nextState.agent1Pos[1]};
            if (this.actions[actionAgent1].equals("Nothing")) {
                //Do Nothing
            } else {
                if (this.actions[actionAgent1].equals("Up"))
                    newPos = new int[]{nextState.agent1Pos[0] + 1, nextState.agent1Pos[1]};
                else if (this.actions[actionAgent1].equals("Down"))
                    newPos = new int[]{nextState.agent1Pos[0] - 1, nextState.agent1Pos[1]};
                else if (this.actions[actionAgent1].equals("Left"))
                    newPos = new int[]{nextState.agent1Pos[0], nextState.agent1Pos[1] + 1};
                else if (this.actions[actionAgent1].equals("Right"))
                    newPos = new int[]{nextState.agent1Pos[0], nextState.agent1Pos[1] - 1};
                if (checkCanMove(newPos[0], newPos[1])) {
                    nextState.changeAgent1Pos(newPos[0], newPos[1]);
                    rewardAgent1--;
                    changeMapMatrixAgentPositions("agent1", currentState.agent1Pos[0], currentState.agent1Pos[1], newPos[0], newPos[1]);
                }

            }
        }

        System.out.println("Reward0 = " + rewardAgent0);
        System.out.println("Reward1 = " + rewardAgent1);

        return nextState;
    }

    boolean checkCanMove(int z, int x) {
        return !mapMatrix.get(z)[x].equals("w") && !mapMatrix.get(z)[x].contains("door") && !mapMatrix.get(z)[x].contains("agent");
    }

    boolean checkIfEndend(CentralizedState state) {
        int count = targetButtons.length;
        for (String button : targetButtons) {
            if ((state.buttonsState[Integer.parseInt(new String(button.substring(button.length() - 1))) - 1]) == 1)
                count--;
        }
        return count == 0;
    }

    int getRewardFromPressingButton(CentralizedState state, String buttonPressed) {
        if(state.buttonsState[Integer.parseInt(buttonPressed.substring(buttonPressed.length() - 1)) - 1] == 1) {  // Se o agent ligou
            for (String targetBtn : targetButtons) {
                if (targetBtn.equals(buttonPressed)) {
                    if (!AlreadyClicked(buttonPressed))
                        return 100;
                    else
                        return -1;
                }
            }
        }
        return 1;
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

    void resetMapMatrix(){
        this.mapMatrix = new ArrayList<String[]>();
        for (String[] a : this.initialMapMatrix)
            this.mapMatrix.add((String[])a.clone());
    }

    void setupCentralizedActions(){
        this.centralizedActions = new ArrayList<String[]>();
        for(String action_agent0 : this.actions)
            for (String action_agent1: this.actions)
                this.centralizedActions.add(new String[]{action_agent0, action_agent1});
    }

    int getCentralizedAction(int actionAgent0, int actionAgent1){
        for(int i = 0; i < this.centralizedActions.size(); i++)
            if (Arrays.equals(this.centralizedActions.get(i), new String[]{this.actions[actionAgent0], this.actions[actionAgent1]}))
                return i;
        return -1;
    }

    boolean objExistsInQTable(CentralizedQTableObj obj){
        for(CentralizedQTableObj temp: this.QTable)
            if(temp.equalsTo(obj))
                return true;
        return false;
    }

    float getQValueQTable(CentralizedState state, int action){
        for (CentralizedQTableObj temp : this.QTable) {
            if (temp.state.equalsTo(state)) {
                if (action != -1)
                    return temp.actionsQValues[action];
                else
                    return temp.maxActionQValue();
            }
        }
        return -1;
    }

    ArrayList<CentralizedQTableObj> getPolicy(String filename) throws IOException {
        ArrayList<CentralizedQTableObj> policy = new ArrayList<CentralizedQTableObj>();
        // read the object from file
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            Object obj;
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            for (; ; ) {
                obj = in.readObject();
                policy.add((CentralizedQTableObj) obj);
//                System.out.println(obj.toString());
            }

        } catch (Exception ignored) {
        }

//        System.out.println();
        assert in != null;
        in.close();

        return policy;
    }

    boolean AlreadyClicked(String buttonPressed){
       if (this.targetButtonsAlreadyClicked.size() == 0) {
        }
        else{
            for (String btn : this.targetButtonsAlreadyClicked)
                if (btn.equals(buttonPressed))
                    return true;
        }
        this.targetButtonsAlreadyClicked.add(buttonPressed);
        return false;
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