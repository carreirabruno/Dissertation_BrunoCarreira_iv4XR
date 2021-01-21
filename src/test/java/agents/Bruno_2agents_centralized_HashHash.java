package agents;

import helperclasses.datastructures.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;


public class Bruno_2agents_centralized_HashHash {

    String[] actions;
    ArrayList<String[]> centralizedActions;
    String[] targetButtons;
    int[] doorsState;
    int[] buttonsState;
    ArrayList<String[]> initialMapMatrix;
    ArrayList<String[]> mapMatrix;
    ArrayList<String[]> connectionButtonsDoors;

    LinkedHashMap<Integer, DoorCentralizedQTableObj> QTable;

    LinkedHashMap<Integer, DoorCentralizedTransitionObj> TransitionTable;

    ArrayList<String> targetButtonsAlreadyClicked;

    float learning_rate = 0.1f;
    float gamma = 0.95f;

    int early_stop_counter_reset = 5;
    int early_stop_counter = early_stop_counter_reset;


    float epsilon = 1.0f;
    float epsilonRate = 0.0001f;

    boolean validationEpisode = false;

    ArrayList<CompareObject> behaviouralTraces;

    String scenario;


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
    public void run(boolean train, String scenario_filename, String[] targetButtons) throws InterruptedException, IOException {
        this.targetButtons = targetButtons;

        setUpScenarioMatrix(scenario_filename);
        this.scenario = scenario_filename;

        if (train) {
            this.QTable = new LinkedHashMap<Integer, DoorCentralizedQTableObj>();
            this.TransitionTable = new LinkedHashMap<Integer, DoorCentralizedTransitionObj>();

            runTraining();

            savePolicyToFile("centralizedHashHash_" + this.scenario);
        } else {
            this.epsilon = 0;
            getPolicyFromFile("centralizedHashHash_" + this.scenario);
            runVisualize();
        }


    }

    void runTraining() {
        int max_steps = ((this.initialMapMatrix.size() * this.initialMapMatrix.get(0).length) - countApperancesOfWordOnInitialMap("w")) * this.actions.length;
        int minimumValidationSteps = max_steps;
        int temp = max_steps;

        int _episode = 0;
        while (early_stop_counter >= 0) {
            _episode++;

            resetMapMatrix();

            this.doorsState = new int[countApperancesOfWordOnInitialMap("door")];
            this.buttonsState = new int[countApperancesOfWordOnInitialMap("button")];
            this.targetButtonsAlreadyClicked = new ArrayList<String>();

            DoorCentralizedState currentState = new DoorCentralizedState(findTruePosInCurrentMapMatrix("agent0"), findTruePosInCurrentMapMatrix("agent1"), countApperancesOfWordOnInitialMap("door"));
            DoorCentralizedState nextState = null;

            int actionAgent0;
            int actionAgent1;
            int rewardAgent0;
            int rewardAgent1;
            int reward;
            int action;
            DoorRewardRewardStateObject doorRewardRewardStateObject;

            boolean reachedEnd = false;

            int step;
            for (step = 0; step < max_steps; step++) {

                //Prints para parceber o que acontece
                if (early_stop_counter == 1)
//                    printMapMatrix();
                    printInvertedMapMatrix();

                //Check if the target buttons have been clicked
                if (checkIfEndend()) {
                    reachedEnd = true;
                    break;
                }

                //Get action Agent0
                actionAgent0 = chooseAction(currentState, 0);

                //Get action Agent1
                actionAgent1 = chooseAction(currentState, 1);
//                actionAgent1 = 3;

                action = getCentralizedAction(actionAgent0, actionAgent1);


                //Act on map, get rewards and nextState
                doorRewardRewardStateObject = new DoorRewardRewardStateObject(actOnMap(currentState, actionAgent0, actionAgent1));
                rewardAgent0 = doorRewardRewardStateObject.rewardAgent0;
                rewardAgent1 = doorRewardRewardStateObject.rewardAgent1;
                nextState = doorRewardRewardStateObject.state;
                reward = rewardAgent0 + rewardAgent1;

                if (early_stop_counter == 1){
                    System.out.println(currentState.toString() + " " + Arrays.toString(this.centralizedActions.get(action)) + " " + nextState.toString());
                    System.out.println();
                }

                //Update Q Table
                updateQTable(currentState, action, reward, nextState);

                //Update Transition table
                updateTransitionTable(currentState, action, reward, nextState);

                //Set new currentState
                currentState = new DoorCentralizedState(nextState);
            }



            //Dyna-Q
            runDynaQ(max_steps);

            if (step < temp)
                temp = step;

            //Early Stop

            if (this.validationEpisode) {

                if (step < minimumValidationSteps)
                    minimumValidationSteps = step;
                else if (step < max_steps && step == minimumValidationSteps)
                    early_stop_counter--;
                else {
                    early_stop_counter = early_stop_counter_reset;
                    minimumValidationSteps = max_steps;
                }
                System.out.println("Validation Episode " + _episode + " done | Reached end = " + reachedEnd + " | #Steps = " + step + " | Best validations steps = " + minimumValidationSteps + " | TempSteps = " + temp + " | Early Stop Counter = " + early_stop_counter + " | Epsilon = " + this.epsilon);

                if (early_stop_counter == 0)
                    break;
            }

            if (temp < max_steps) {
                this.epsilon -= this.epsilonRate;
                if (this.epsilon > 0.1f)
                    System.out.println(this.epsilon);
            }
//            if (this.epsilon < -2*this.epsilonRate)
//                this.epsilon = 1.0f;
//
//            this.validationEpisode = this.epsilon < 0;

            if (this.epsilon <= 0)
                this.epsilon = 0.1f;

            this.validationEpisode = this.epsilon < 0.1f;

        }

    }

    void runVisualize(){
        resetMapMatrix();

        this.doorsState = new int[countApperancesOfWordOnInitialMap("door")];
        this.buttonsState = new int[countApperancesOfWordOnInitialMap("button")];
        this.targetButtonsAlreadyClicked = new ArrayList<String>();

        DoorCentralizedState currentState = new DoorCentralizedState(findTruePosInCurrentMapMatrix("agent0"), findTruePosInCurrentMapMatrix("agent1"), countApperancesOfWordOnInitialMap("door"));
        DoorCentralizedState nextState = null;

        int actionAgent0;
        int actionAgent1;
        int reward0;
        int reward1;
        int action;
        DoorRewardRewardStateObject doorRewardRewardStateObject;

        behaviouralTraces = new ArrayList<CompareObject>();

        int step = 0;
        while (!checkIfEndend()) {

            //Get action Agent0
            actionAgent0 = chooseAction(currentState, 0);

            //Get action Agent1
            actionAgent1 = chooseAction(currentState, 1);

            //Get centralized action
            action = getCentralizedAction(actionAgent0, actionAgent1);

            behaviouralTraces.add(new CompareObject(currentState, this.centralizedActions.get(action)));

            //Prints to see
//            printInvertedMapMatrix();
//            System.out.println(currentState.toString() + " " + Arrays.toString(this.centralizedActions.get(action)));
//            System.out.println();


            //Act on map, get rewards and nextState
            doorRewardRewardStateObject = new DoorRewardRewardStateObject(actOnMap(currentState, actionAgent0, actionAgent1));
            nextState = doorRewardRewardStateObject.state;
            reward0 = doorRewardRewardStateObject.rewardAgent0;
            reward1 = doorRewardRewardStateObject.rewardAgent1;

            //Set new currentState
            currentState = new DoorCentralizedState(nextState);

            step++;
        }

//        System.out.println("####Steps = " + step);

        comparePolicies();
    }

    void setUpScenarioMatrix(String scenario_filename) {

        this.initialMapMatrix = new ArrayList<String[]>();
        this.connectionButtonsDoors = new ArrayList<String[]>();

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

        this.actions = new String[]{"Nothing", "Up", "Down", "Left", "Right", "Press"};
        setupCentralizedActions();

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
            for (int x = 0; x < this.mapMatrix.get(z).length; x++) {
                String temp = new String(this.mapMatrix.get(z)[x]);
                temp = temp.replace("gent", "");
                temp = temp.replace("utton", "");
                temp = temp.replace("oor", "");
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

    void printQTable() {
        for (DoorCentralizedQTableObj obj : this.QTable.values())
            System.out.println(obj.toString());
        System.out.println();
    }

    void printTransitionTable() {
        for (DoorCentralizedTransitionObj a : this.TransitionTable.values())
            System.out.println(a.toString());
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

    int chooseAction(DoorCentralizedState state, int agent) {
        Random r = new Random();
        if (r.nextDouble() > this.epsilon) {
            for (DoorCentralizedQTableObj obj : this.QTable.values())
                if (obj.state.equalsTo(state)) {
                    for (int i = 0; i < this.actions.length; i++)
                        if (this.actions[i].equals(this.centralizedActions.get(obj.maxAction())[agent])) {
                            return i;
                        }
                }
            return 0;
        }
        return r.nextInt(this.actions.length);

    }

    DoorRewardRewardStateObject actOnMap(DoorCentralizedState currentState, int actionAgent0, int actionAgent1) {
        DoorCentralizedState nextState = new DoorCentralizedState(currentState);
        int rewardAgent0 = 0;
        int rewardAgent1 = 0;
        int[] newPos;

        //Agent0
        if (this.actions[actionAgent0].equals("Press")) {
            if (this.mapMatrix.get(nextState.agent0Pos[0])[nextState.agent0Pos[1]].contains("button")) {
                String buttonToClick = new String(this.mapMatrix.get(nextState.agent0Pos[0])[nextState.agent0Pos[1]].substring(0, 7));
                this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1] = 1 ^ this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1];

                ArrayList<Integer> doorsToChange = getDoorsFromConnections(buttonToClick);
                for (Integer _door : doorsToChange)
                    nextState.changeDoorState(_door);

                rewardAgent0 = getRewardFromPressingButton(buttonToClick);
                openCloseDoor(buttonToClick);
            }
            else
                rewardAgent0--;
        } else if (!this.actions[actionAgent0].equals("Nothing")) {
            newPos = new int[]{nextState.agent0Pos[0], nextState.agent0Pos[1]};

            if (this.actions[actionAgent0].equals("Up"))
                newPos = new int[]{nextState.agent0Pos[0] + 1, nextState.agent0Pos[1]};
            else if (this.actions[actionAgent0].equals("Down"))
                newPos = new int[]{nextState.agent0Pos[0] - 1, nextState.agent0Pos[1]};
            else if (this.actions[actionAgent0].equals("Left"))
                newPos = new int[]{nextState.agent0Pos[0], nextState.agent0Pos[1] - 1};
            else if (this.actions[actionAgent0].equals("Right"))
                newPos = new int[]{nextState.agent0Pos[0], nextState.agent0Pos[1] + 1};
            if (checkCanMove(newPos[0], newPos[1])) {
                nextState.changeAgent0Pos(newPos[0], newPos[1]);

                changeMapMatrixAgentPositions("agent0", currentState.agent0Pos[0], currentState.agent0Pos[1], newPos[0], newPos[1]);
            }
            rewardAgent0--;
        }

        //Agent1
        if (this.actions[actionAgent1].equals("Press")) {
            if (mapMatrix.get(nextState.agent1Pos[0])[nextState.agent1Pos[1]].contains("button")) {
                String buttonToClick = new String(this.mapMatrix.get(nextState.agent1Pos[0])[nextState.agent1Pos[1]].substring(0, 7));
//                nextState.changeDoorState(Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)));

//                this.buttonsState.set(Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)), 1^this.buttonsState.get(Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1))));
                this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1] = 1 ^ this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1];


                ArrayList<Integer> doorsToChange = getDoorsFromConnections(buttonToClick);
                for (Integer _door : doorsToChange)
                    nextState.changeDoorState(_door);

                rewardAgent1 = getRewardFromPressingButton(buttonToClick);
                openCloseDoor(buttonToClick);

//                printInvertedMapMatrix();
//                System.out.println(nextState.toString());
//                System.out.println("Agent1 buttonPressed " + buttonToClick + " | Reward " + rewardAgent1);
//                System.out.println(this.targetButtonsAlreadyClicked.size() + " | " + Arrays.toString(this.targetButtonsAlreadyClicked.toArray()));
//                System.out.println();
            }
            else
                rewardAgent1--;
        } else if (!this.actions[actionAgent1].equals("Nothing")) {
            newPos = new int[]{nextState.agent1Pos[0], nextState.agent1Pos[1]};
            if (this.actions[actionAgent1].equals("Up"))
                newPos = new int[]{nextState.agent1Pos[0] + 1, nextState.agent1Pos[1]};
            else if (this.actions[actionAgent1].equals("Down"))
                newPos = new int[]{nextState.agent1Pos[0] - 1, nextState.agent1Pos[1]};
            else if (this.actions[actionAgent1].equals("Left"))
                newPos = new int[]{nextState.agent1Pos[0], nextState.agent1Pos[1] - 1};
            else if (this.actions[actionAgent1].equals("Right"))
                newPos = new int[]{nextState.agent1Pos[0], nextState.agent1Pos[1] + 1};
            if (checkCanMove(newPos[0], newPos[1])) {
                nextState.changeAgent1Pos(newPos[0], newPos[1]);

                changeMapMatrixAgentPositions("agent1", currentState.agent1Pos[0], currentState.agent1Pos[1], newPos[0], newPos[1]);
            }
            rewardAgent1--;
        }

        return new DoorRewardRewardStateObject(rewardAgent0, rewardAgent1, nextState);
    }

    boolean checkCanMove(int z, int x) {
        return !mapMatrix.get(z)[x].equals("w") && !mapMatrix.get(z)[x].contains("door") && !mapMatrix.get(z)[x].contains("agent");
    }

    boolean checkIfEndend() {
//        int count = targetButtons.length;
//        for (String button : targetButtons) {
//            if (this.buttonsState[(Integer.parseInt(new String(button.substring(button.length() - 1))) - 1)] == 1)
//                count--;
//        }
//        return count == 0;

        return  this.targetButtonsAlreadyClicked.size() == this.targetButtons.length;
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
                    return 100;
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

    /**
     * Creates a map with different initial positions for the agents every time
     */
    void createCurrentMapMatrix() {
        resetMapMatrix();

        Random r = new Random();
        int[] newAgent0Pos = findTruePosInInitialMapMatrix("agent0");
        int[] newAgent1Pos = findTruePosInInitialMapMatrix("agent1");
        this.mapMatrix.get(newAgent0Pos[0])[newAgent0Pos[1]] = "f";
        this.mapMatrix.get(newAgent1Pos[0])[newAgent1Pos[1]] = "f";
        newAgent0Pos = new int[]{r.nextInt(this.mapMatrix.size()), r.nextInt(this.mapMatrix.get(0).length)};
        newAgent1Pos = new int[]{r.nextInt(this.mapMatrix.size()), r.nextInt(this.mapMatrix.get(0).length)};

        while (!this.mapMatrix.get(newAgent0Pos[0])[newAgent0Pos[1]].equals("f") || !this.mapMatrix.get(newAgent1Pos[0])[newAgent1Pos[1]].equals("f") || Arrays.equals(newAgent0Pos, newAgent1Pos)) {
            newAgent0Pos = new int[]{r.nextInt(this.mapMatrix.size()), r.nextInt(this.mapMatrix.get(0).length)};
            newAgent1Pos = new int[]{r.nextInt(this.mapMatrix.size()), r.nextInt(this.mapMatrix.get(0).length)};
        }
        this.mapMatrix.get(newAgent0Pos[0])[newAgent0Pos[1]] = "agent0";
        this.mapMatrix.get(newAgent1Pos[0])[newAgent1Pos[1]] = "agent1";

    }

    void setupCentralizedActions() {
        this.centralizedActions = new ArrayList<String[]>();
        for (String action_agent0 : this.actions)
            for (String action_agent1 : this.actions)
                this.centralizedActions.add(new String[]{action_agent0, action_agent1});
    }

    int getCentralizedAction(int actionAgent0, int actionAgent1) {
        for (int i = 0; i < this.centralizedActions.size(); i++)
            if (Arrays.equals(this.centralizedActions.get(i), new String[]{this.actions[actionAgent0], this.actions[actionAgent1]}))
                return i;
        return -1;
    }

    void updateQTable(DoorCentralizedState currentState, int action, int reward, DoorCentralizedState nextState) {
        String currentStateHash = "" + currentState.agent0Pos[0] + currentState.agent0Pos[1] + currentState.agent1Pos[0] + currentState.agent1Pos[1];

        for (int door : currentState.doorsState)
            currentStateHash += "" + door;

        int hashQTable = Objects.hash(currentStateHash);

        if (this.QTable.isEmpty() || !this.QTable.containsKey(hashQTable))
            this.QTable.put(hashQTable, new DoorCentralizedQTableObj(currentState));

        //FORMULA Q Learning
        float value = this.learning_rate * ((float) reward + (this.gamma * getQValueQTable(nextState, -1)) - getQValueQTable(currentState, action));

        this.QTable.get(hashQTable).changeActionQValue(action, value);

    }

    float getQValueQTable(DoorCentralizedState state, int action) {
        for (DoorCentralizedQTableObj temp : this.QTable.values()) {
            if (temp.state.equalsTo(state)) {
                if (action != -1)
                    return temp.actionsQValues[action];
                else
                    return temp.maxActionQValue();
            }
        }
        return 0;
    }

    void savePolicyToFile(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos);

            out.writeObject(this.QTable);

            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void getPolicyFromFile(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis);

            this.QTable = (LinkedHashMap<Integer, DoorCentralizedQTableObj>) in.readObject();

            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    void printBestActionCurrentState(DoorCentralizedState state) {
        for (DoorCentralizedQTableObj obj : this.QTable.values())
            if (obj.state.equalsTo(state))
                System.out.println("Best Action = " + Arrays.toString(this.centralizedActions.get(obj.maxAction())));
    }

    void updateTransitionTable(DoorCentralizedState currentState, int action, int reward, DoorCentralizedState nextState) {
        String currentStateHash = "" + currentState.agent0Pos[0] + currentState.agent0Pos[1] + currentState.agent1Pos[0] + currentState.agent1Pos[1];

        for (int d : currentState.doorsState)
            currentStateHash += "" + d;

        currentStateHash += "" + action;
        int hash = Objects.hash(currentStateHash);

        if (this.TransitionTable.isEmpty() || !this.TransitionTable.containsKey(hash)) {
            this.TransitionTable.put(hash, new DoorCentralizedTransitionObj(currentState, action, reward, nextState));
        }
    }

    void runDynaQ(int DynaQSteps) {
//        System.out.println("RUN DYNA");

        List<Integer> keysAsArray = new ArrayList<Integer>(this.TransitionTable.keySet());
        Random r = new Random();

        DoorCentralizedTransitionObj transitionObj;

        for (float dynaStep = 0; dynaStep < DynaQSteps; dynaStep++) {
            transitionObj = this.TransitionTable.get(keysAsArray.get(r.nextInt(keysAsArray.size())));
            updateQTable(transitionObj.currentState, transitionObj.action, transitionObj.reward, transitionObj.nextState);
        }
    }


    ArrayList<Integer> getDoorsFromConnections(String realButton) {
        ArrayList<Integer> doors = new ArrayList<Integer>();
        for (String[] conn : this.connectionButtonsDoors) {
            if (conn[0].equals(realButton) && conn.length > 1)
                doors.add(Integer.parseInt(conn[1].substring(conn[1].length() - 1)));
        }

        return doors;
    }

    void comparePolicies(){
        Bruno_2agents_ComparePolicies.start();
        Bruno_2agents_ComparePolicies comparePolicies = new Bruno_2agents_ComparePolicies();
        comparePolicies.run(this.behaviouralTraces, this.scenario);
        Bruno_2agents_ComparePolicies.close();
    }

}

class DoorCentralizedState implements Serializable {
    int[] agent0Pos;
    int[] agent1Pos;
    int[] doorsState;

    public DoorCentralizedState(int[] agent0_pos, int[] agent1_pos, int numberExistingDoors) {
        this.agent0Pos = new int[]{agent0_pos[0], agent0_pos[1]};
        this.agent1Pos = new int[]{agent1_pos[0], agent1_pos[1]};
        this.doorsState = new int[numberExistingDoors];
    }

    public DoorCentralizedState(DoorCentralizedState state) {
        this.agent0Pos = new int[]{state.agent0Pos[0], state.agent0Pos[1]};
        this.agent1Pos = new int[]{state.agent1Pos[0], state.agent1Pos[1]};
        this.doorsState = new int[state.doorsState.length];
        System.arraycopy(state.doorsState, 0, this.doorsState, 0, this.doorsState.length);
    }

    public void changeAgent0Pos(int z, int x) {
        this.agent0Pos[0] = z;
        this.agent0Pos[1] = x;
    }

    public void changeAgent1Pos(int z, int x) {
        this.agent1Pos[0] = z;
        this.agent1Pos[1] = x;
    }

    public void changeDoorState(int doorIndex) {
        this.doorsState[doorIndex - 1] = this.doorsState[doorIndex - 1] ^= 1;
    }

    public boolean equalsTo(DoorCentralizedState state) {
        return (Arrays.equals(this.agent0Pos, state.agent0Pos) && Arrays.equals(this.agent1Pos, state.agent1Pos) && Arrays.equals(this.doorsState, state.doorsState));
    }

    @Override
    public String toString() {
        return "<(" + this.agent0Pos[1] + ", 0, " + this.agent0Pos[0] + "), (" + this.agent1Pos[1] + ", 0, " + this.agent1Pos[0] + "), " + Arrays.toString(this.doorsState) + ">";
    }
}

class DoorCentralizedQTableObj implements Serializable {

    DoorCentralizedState state;
    float[] actionsQValues = new float[36];  //number of centralized actions

    public DoorCentralizedQTableObj(DoorCentralizedState state) {
        this.state = new DoorCentralizedState(state);
    }

    public DoorCentralizedQTableObj(DoorCentralizedQTableObj obj) {
        this.state = new DoorCentralizedState(obj.state);
        actionsQValues = (float[]) obj.actionsQValues.clone();
    }

    public void changeActionQValue(int actionIndex, float value) {
        if (Float.toString(this.actionsQValues[actionIndex] + value).equals("Infinity")) {
            System.out.println("Value too high  " + this.actionsQValues[actionIndex] + "  " + value);
            System.exit(112);
        }
        this.actionsQValues[actionIndex] += value;
    }

    public boolean equalsTo(DoorCentralizedQTableObj obj) {
        return this.state.equalsTo(obj.state);
    }

    public float maxActionQValue() {
        float maxValue = Float.NEGATIVE_INFINITY;
        for (float v : this.actionsQValues)
            if (v > maxValue)
                maxValue = v;
        return maxValue;
    }

    public int maxAction() {
        int index = -1;
        double maxValue = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < this.actionsQValues.length; i++)
            if (this.actionsQValues[i] > maxValue) {
                maxValue = this.actionsQValues[i];
                index = i;
            }
        return index;
    }

    @Override
    public String toString() {
        return (state.toString() + Arrays.toString(actionsQValues));
    }

}

class DoorCentralizedTransitionObj implements Serializable {
    DoorCentralizedState currentState;
    int action;
    int reward;
    DoorCentralizedState nextState;

    public DoorCentralizedTransitionObj(DoorCentralizedState currentState, int action, int reward, DoorCentralizedState nextState) {
        this.currentState = new DoorCentralizedState(currentState);
        this.action = action;
        this.reward = reward;
        this.nextState = new DoorCentralizedState(nextState);
    }

    public DoorCentralizedTransitionObj(DoorCentralizedTransitionObj transition) {
        this.currentState = new DoorCentralizedState(transition.currentState);
        this.action = transition.action;
        this.reward = transition.reward;
        this.nextState = new DoorCentralizedState(transition.nextState);
    }

    @Override
    public String toString() {
        return this.currentState.toString() + " | " + action + " | " + reward + " | " + nextState.toString();
    }
}

class DoorRewardRewardStateObject {

    int rewardAgent0;
    int rewardAgent1;
    DoorCentralizedState state;

    public DoorRewardRewardStateObject(int rewardAgent0, int rewardAgent1, DoorCentralizedState state) {
        this.rewardAgent0 = rewardAgent0;
        this.rewardAgent1 = rewardAgent1;
        this.state = new DoorCentralizedState(state);
    }

    public DoorRewardRewardStateObject(DoorRewardRewardStateObject obj) {
        this.rewardAgent0 = obj.rewardAgent0;
        this.rewardAgent1 = obj.rewardAgent1;
        this.state = new DoorCentralizedState(obj.state);
    }


}

class CompareObject{
    DoorCentralizedState state;
    String[] actions;

    public CompareObject(DoorCentralizedState state, String[] actions){
        this.state = state;
        this.actions = actions;
    }

    public boolean equalIndividualStates(DoorIndividualState agent0State, DoorIndividualState agent1State){
        return Arrays.equals(state.agent0Pos, agent0State.agentPos) && Arrays.equals(state.agent1Pos, agent1State.agentPos) && Arrays.equals(state.doorsState, agent0State.doorsState) && Arrays.equals(state.doorsState, agent1State.doorsState);
    }

    public boolean equalTo(CompareObject obj){
//        System.out.println(this.toString());
//        System.out.println(obj.toString());
//        System.out.println("----");
//        return Arrays.equals(this.state.agent0Pos, obj.state.agent0Pos) && Arrays.equals(this.state.agent1Pos, obj.state.agent1Pos) && Arrays.equals(this.state.doorsState, obj.state.doorsState) && Arrays.equals(this.actions, obj.actions);
        return Arrays.equals(this.state.agent0Pos, obj.state.agent0Pos) && Arrays.equals(this.state.agent1Pos, obj.state.agent1Pos) && Arrays.equals(this.state.doorsState, obj.state.doorsState);
    }

    public DoorIndividualState[] getIndividualStates(){
       return new DoorIndividualState[]{new DoorIndividualState(state.agent0Pos, state.doorsState), new DoorIndividualState(state.agent1Pos, state.doorsState),};
    }

    @Override
    public String toString() {
        return this.state.toString() + " | " + Arrays.toString(this.actions);
    }

}