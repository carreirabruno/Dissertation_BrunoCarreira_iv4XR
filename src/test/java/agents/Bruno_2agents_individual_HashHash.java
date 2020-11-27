package agents;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;


public class Bruno_2agents_individual_HashHash {

    String[] actions;
    String[] targetButtons;
    int[] doorsState;
    int[] buttonsState;
    ArrayList<String[]> initialMapMatrix;
    ArrayList<String[]> mapMatrix;
    ArrayList<String[]> connectionButtonsDoors;

    LinkedHashMap<Integer, DoorIndividualQTableObj> QTableAgent0;
    LinkedHashMap<Integer, DoorIndividualQTableObj> QTableAgent1;

    LinkedHashMap<Integer, DoorIndividualTransitionObj> TransitionTable;

    ArrayList<String> targetButtonsAlreadyClicked;

    float learning_rate = 0.1f;
    float gamma = 0.95f;

    int early_stop_counter_reset = 5;
    int early_stop_counter = early_stop_counter_reset;


    float epsilon = 1.0f;
    float epsilonRate = 0.0001f;

    boolean validationEpisode = false;

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

        if (train) {
            this.QTableAgent0 = new LinkedHashMap<Integer, DoorIndividualQTableObj>();
            this.QTableAgent1 = new LinkedHashMap<Integer, DoorIndividualQTableObj>();
            this.TransitionTable = new LinkedHashMap<Integer, DoorIndividualTransitionObj>();

            runTraining();

//            saveTransitionTableToFile("individualHashHashTransitionTable_" + scenario_filename);
            savePolicyToFile("individualHashHash_" + scenario_filename + "_agent0", this.QTableAgent0);
            savePolicyToFile("individualHashHash_" + scenario_filename + "_agent1", this.QTableAgent1);

        } else {
            this.epsilon = 0;

            this.QTableAgent0 = getPolicyFromFile("individualHashHash_" + scenario_filename + "_agent0");
            this.QTableAgent1 = getPolicyFromFile("individualHashHash_" + scenario_filename + "_agent1");

            runVisualize();
        }

//        printQTable(this.QTableAgent0);
//        System.out.println("--------");
//        printQTable(this.QTableAgent1);


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

            DoorIndividualState currentStateAgent0 = new DoorIndividualState(findTruePosInCurrentMapMatrix("agent0"), countApperancesOfWordOnInitialMap("door"));
            DoorIndividualState currentStateAgent1 = new DoorIndividualState(findTruePosInCurrentMapMatrix("agent1"), countApperancesOfWordOnInitialMap("door"));

            DoorIndividualState nextStateAgent0;
            DoorIndividualState nextStateAgent1;

            int actionAgent0;
            int actionAgent1;
            int rewardAgent0;
            int rewardAgent1;
            DoorRewardRewardStateStateObject doorRewardRewardStateStateObject;

            boolean reachedEnd = false;
            int step;
            for (step = 0; step < max_steps; step++) {

                //Prints para parceber o que acontece
                if (early_stop_counter == 1 && this.validationEpisode) {
                System.out.println(currentStateAgent0.toString());
                System.out.println(currentStateAgent1.toString());
                printInvertedMapMatrix();
                }

                //Check if the target buttons have been clicked
                if (checkIfEndend()) {
                    reachedEnd = true;
                    break;
                }

                //Get action Agent0
                actionAgent0 = chooseAction(currentStateAgent0, this.QTableAgent0);

                //Get action Agent1
                actionAgent1 = chooseAction(currentStateAgent1, this.QTableAgent1);


                //Act on map, get rewards and nextState
                doorRewardRewardStateStateObject = new DoorRewardRewardStateStateObject(actOnMap(currentStateAgent0, actionAgent0, currentStateAgent1, actionAgent1));
                rewardAgent0 = doorRewardRewardStateStateObject.rewardAgent0;
                rewardAgent1 = doorRewardRewardStateStateObject.rewardAgent1;
                nextStateAgent0 = doorRewardRewardStateStateObject.stateAgent0;
                nextStateAgent1 = doorRewardRewardStateStateObject.stateAgent1;


                //Update Q Table
                updateQTable(currentStateAgent0, actionAgent0, rewardAgent0, nextStateAgent0, this.QTableAgent0);
                updateQTable(currentStateAgent1, actionAgent1, rewardAgent1, nextStateAgent1, this.QTableAgent1);

                //Update Transition table
                updateTransitionTable(currentStateAgent0, actionAgent0, rewardAgent0, nextStateAgent0);
                updateTransitionTable(currentStateAgent1, actionAgent1, rewardAgent1, nextStateAgent1);

                //Set new currentState
                currentStateAgent0 = new DoorIndividualState(nextStateAgent0);
                currentStateAgent1 = new DoorIndividualState(nextStateAgent1);
            }

            //Dyna-Q
            runDynaQ(max_steps);

            if (step < temp)
                temp = step;

            //Early Stop
            if(this.validationEpisode) {

                if (step < minimumValidationSteps) {
                    minimumValidationSteps = step;
                }else if (step < max_steps && step == minimumValidationSteps)
                    early_stop_counter--;
                else {
                    early_stop_counter = early_stop_counter_reset;
                    minimumValidationSteps = max_steps;
                }

                System.out.println("Validation Episode " + _episode + " done | Reached end = " + reachedEnd + " | #Steps = " + step + " | Best validations steps = " + minimumValidationSteps + " | TempSteps = " + temp + " | Early Stop Counter = " + early_stop_counter + " | Epsilon = " + this.epsilon);

                if (early_stop_counter == 0)
                    break;
            }


//            System.out.println("Training Episode " + _episode + " done | Reached end = " + reachedEnd + " | #Steps = " + step + " | Epsilon = " + this.epsilon);

            if (temp < max_steps)
                this.epsilon -= this.epsilonRate;
//            if (this.epsilon < -2*this.epsilonRate)
//                this.epsilon = 1.0f;
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

        DoorIndividualState currentStateAgent0 = new DoorIndividualState(findTruePosInCurrentMapMatrix("agent0"), countApperancesOfWordOnInitialMap("door"));
        DoorIndividualState currentStateAgent1 = new DoorIndividualState(findTruePosInCurrentMapMatrix("agent1"), countApperancesOfWordOnInitialMap("door"));

        DoorIndividualState nextStateAgent0;
        DoorIndividualState nextStateAgent1;

        int actionAgent0;
        int actionAgent1;

        DoorRewardRewardStateStateObject doorRewardRewardStateStateObject;

        int step = 0;
        while(!checkIfEndend()){

            //Prints para parceber o que acontece
            if (early_stop_counter == 1 && this.validationEpisode) {
                System.out.println(currentStateAgent0.toString());
                System.out.println(currentStateAgent1.toString());
                printInvertedMapMatrix();
            }

            //Get action Agent0
            actionAgent0 = chooseAction(currentStateAgent0, this.QTableAgent0);

            //Get action Agent1
            actionAgent1 = chooseAction(currentStateAgent1, this.QTableAgent1);

            //Prints to see
            printInvertedMapMatrix();
            System.out.println(currentStateAgent0.toString() + " " + this.actions[actionAgent0]);
            System.out.println(currentStateAgent1.toString() + " " + this.actions[actionAgent1]);
            System.out.println();

            //Act on map, get rewards and nextState
            doorRewardRewardStateStateObject = new DoorRewardRewardStateStateObject(actOnMap(currentStateAgent0, actionAgent0, currentStateAgent1, actionAgent1));
            nextStateAgent0 = doorRewardRewardStateStateObject.stateAgent0;
            nextStateAgent1 = doorRewardRewardStateStateObject.stateAgent1;

            //Set new currentState
            currentStateAgent0 = new DoorIndividualState(nextStateAgent0);
            currentStateAgent1 = new DoorIndividualState(nextStateAgent1);
        }

    }

    void setUpScenarioMatrix(String scenario_filename) {

        this.initialMapMatrix = new ArrayList<String[]>();
        this.connectionButtonsDoors = new ArrayList<String[]>();

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

        this.actions = new String[]{"Nothing", "Up", "Down", "Left", "Right", "Press"};

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

    void printTransitionTable(LinkedHashMap<Integer, IndividualTransitionObj> table) {
        for (IndividualTransitionObj a : table.values())
            System.out.println(a.toString());
        System.out.println();
    }

    void printQTable(LinkedHashMap<Integer, DoorIndividualQTableObj> table) {
        for (DoorIndividualQTableObj obj : table.values())
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

    int chooseAction(DoorIndividualState state, LinkedHashMap<Integer, DoorIndividualQTableObj> table) {
        Random r = new Random();
        if (r.nextFloat() > this.epsilon) {
            for (DoorIndividualQTableObj obj : table.values())
                if (obj.state.equalsTo(state))
                    return obj.maxAction();
            return 0;
        }
        return r.nextInt(this.actions.length);

    }

    DoorRewardRewardStateStateObject actOnMap(DoorIndividualState currentStateAgent0, int actionAgent0, DoorIndividualState currentStateAgent1, int actionAgent1) {
        int[] newPos;
        //Agent0
        DoorIndividualState nextStateAgent0 = new DoorIndividualState(currentStateAgent0);
        int rewardAgent0 = 0;

        boolean clicked = false;

        if (this.actions[actionAgent0].equals("Press")) {
            if (this.mapMatrix.get(nextStateAgent0.agentPos[0])[nextStateAgent0.agentPos[1]].contains("button")) {
                String buttonToClick = new String(this.mapMatrix.get(nextStateAgent0.agentPos[0])[nextStateAgent0.agentPos[1]].substring(0, 7));
                this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1] = 1 ^ this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1];

//                System.out.println(buttonToClick + " a0");

//                ArrayList<Integer> doorsToChange = getDoorsFromConnections(buttonToClick);
//                for (Integer _door : doorsToChange)
//                    nextStateAgent0.changeDoorState(_door);

                rewardAgent0 = getRewardFromPressingButton(buttonToClick);
                openCloseDoor(buttonToClick);
//                nextStateAgent0.copyAllDoorsState(this.buttonsState);

                clicked = true;

            }
            else
                rewardAgent0--;
        } else if(!this.actions[actionAgent0].equals("Nothing")) {
            newPos = new int[]{nextStateAgent0.agentPos[0], nextStateAgent0.agentPos[1]};

            if (this.actions[actionAgent0].equals("Up"))
                newPos = new int[]{nextStateAgent0.agentPos[0] + 1, nextStateAgent0.agentPos[1]};
            else if (this.actions[actionAgent0].equals("Down"))
                newPos = new int[]{nextStateAgent0.agentPos[0] - 1, nextStateAgent0.agentPos[1]};
            else if (this.actions[actionAgent0].equals("Left"))
                newPos = new int[]{nextStateAgent0.agentPos[0], nextStateAgent0.agentPos[1] - 1};
            else if (this.actions[actionAgent0].equals("Right"))
                newPos = new int[]{nextStateAgent0.agentPos[0], nextStateAgent0.agentPos[1] + 1};
            if (checkCanMove(newPos[0], newPos[1])) {
                nextStateAgent0.changeAgentPos(newPos[0], newPos[1]);
                changeMapMatrixAgentPositions("agent0", currentStateAgent0.agentPos[0], currentStateAgent0.agentPos[1], newPos[0], newPos[1]);
            }
            rewardAgent0--;
        }

        //Agent1
        DoorIndividualState nextStateAgent1 = new DoorIndividualState(currentStateAgent1);
//        nextStateAgent1.copyAllDoorsState(nextStateAgent0.doorsState);
        int rewardAgent1 = 0;

        if (this.actions[actionAgent1].equals("Press")) {
            if (mapMatrix.get(nextStateAgent1.agentPos[0])[nextStateAgent1.agentPos[1]].contains("button")) {
                String buttonToClick = new String(this.mapMatrix.get(nextStateAgent1.agentPos[0])[nextStateAgent1.agentPos[1]].substring(0, 7));
                this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1] = 1 ^ this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1];

//                System.out.println(buttonToClick + " a1");

//                ArrayList<Integer> doorsToChange = getDoorsFromConnections(buttonToClick);
//                for (Integer _door : doorsToChange)
//                    nextStateAgent1.changeDoorState(_door);



                rewardAgent1 = getRewardFromPressingButton(buttonToClick);
                openCloseDoor(buttonToClick);


                clicked = true;

            }
            else
                rewardAgent1--;
        }
        else if(!this.actions[actionAgent1].equals("Nothing")) {
            newPos = new int[]{nextStateAgent1.agentPos[0], nextStateAgent1.agentPos[1]};
            if (this.actions[actionAgent1].equals("Up"))
                newPos = new int[]{nextStateAgent1.agentPos[0] + 1, nextStateAgent1.agentPos[1]};
            else if (this.actions[actionAgent1].equals("Down"))
                newPos = new int[]{nextStateAgent1.agentPos[0] - 1, nextStateAgent1.agentPos[1]};
            else if (this.actions[actionAgent1].equals("Left"))
                newPos = new int[]{nextStateAgent1.agentPos[0], nextStateAgent1.agentPos[1] - 1};
            else if (this.actions[actionAgent1].equals("Right"))
                newPos = new int[]{nextStateAgent1.agentPos[0], nextStateAgent1.agentPos[1] + 1};
            if (checkCanMove(newPos[0], newPos[1])) {
                nextStateAgent1.changeAgentPos(newPos[0], newPos[1]);

                changeMapMatrixAgentPositions("agent1", currentStateAgent1.agentPos[0], currentStateAgent1.agentPos[1], newPos[0], newPos[1]);
            }
            rewardAgent1--;
        }
        nextStateAgent1.copyAllDoorsState(this.doorsState);
        nextStateAgent0.copyAllDoorsState(this.doorsState);

        if (clicked) {
//            System.out.println(currentStateAgent0.toString() + " " + actionAgent0 + " " + rewardAgent0 + " " + nextStateAgent0.toString());
//            System.out.println(currentStateAgent1.toString() + " " + actionAgent1 + " " + rewardAgent1 + " " + nextStateAgent1.toString());
//            System.out.println();
        }
        return new DoorRewardRewardStateStateObject(rewardAgent0, rewardAgent1, nextStateAgent0, nextStateAgent1);
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

        return this.targetButtonsAlreadyClicked.size()==this.targetButtons.length;
    }

    int getRewardFromPressingButton(String buttonPressed) {
        for (String targetBtn : targetButtons) {
            if (targetBtn.equals(buttonPressed)) {
                if (!AlreadyClicked(buttonPressed))
                    return 100;
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

    void updateQTable(DoorIndividualState currentState, int action, int reward, DoorIndividualState nextState, LinkedHashMap<Integer, DoorIndividualQTableObj> table) {
        String currentStateHash = "" + currentState.agentPos[0] + currentState.agentPos[1];

        for (int door : currentState.doorsState)
            currentStateHash += "" + door;

        int hashQTable = Objects.hash(currentStateHash);

        if (table.isEmpty() || !table.containsKey(hashQTable))
            table.put(hashQTable, new DoorIndividualQTableObj(currentState));

        //FORMULA Q Learning
        float value = this.learning_rate * ((float) reward + (this.gamma * getQValueQTable(nextState, -1, table)) - getQValueQTable(currentState, action, table));

        table.get(hashQTable).changeActionQValue(action, value);

    }

    float getQValueQTable(DoorIndividualState state, int action, LinkedHashMap<Integer, DoorIndividualQTableObj> table) {
        for (DoorIndividualQTableObj temp : table.values()) {
            if (temp.state.equalsTo(state)) {
                if (action != -1)
                    return temp.actionsQValues[action];
                else
                    return temp.maxActionQValue();
            }
        }
        return 0;
    }

    void savePolicyToFile(String filename, LinkedHashMap<Integer, DoorIndividualQTableObj> table) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos);

            out.writeObject(table);

            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    LinkedHashMap<Integer, DoorIndividualQTableObj> getPolicyFromFile(String filename) {
        LinkedHashMap<Integer, DoorIndividualQTableObj> table = null;
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis);

            table = (LinkedHashMap<Integer, DoorIndividualQTableObj>) in.readObject();

            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return table;
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

    void runDynaQ(int DynaQSteps) {
//        System.out.println("RUN DYNA");

        List<Integer> keysAsArray = new ArrayList<Integer>(this.TransitionTable.keySet());
        Random r = new Random();

        DoorIndividualTransitionObj transitionObj;

        for (float dynaStep = 0; dynaStep < DynaQSteps; dynaStep++) {
            transitionObj = this.TransitionTable.get(keysAsArray.get(r.nextInt(keysAsArray.size())));
            updateQTable(transitionObj.currentState, transitionObj.action, transitionObj.reward, transitionObj.nextState, this.QTableAgent0);

            transitionObj = this.TransitionTable.get(keysAsArray.get(r.nextInt(keysAsArray.size())));
            updateQTable(transitionObj.currentState, transitionObj.action, transitionObj.reward, transitionObj.nextState, this.QTableAgent1);
        }
    }

    void updateTransitionTable(DoorIndividualState currentState, int action, int reward, DoorIndividualState nextState) {
        String currentStateHash = "" + currentState.agentPos[0] + currentState.agentPos[1];

        for (int door : currentState.doorsState)
            currentStateHash += "" + door;

        currentStateHash += "" + action;
        int hash = Objects.hash(currentStateHash);

        if (this.TransitionTable.isEmpty() || !this.TransitionTable.containsKey(hash)) {
            this.TransitionTable.put(hash, new DoorIndividualTransitionObj(currentState, action, reward, nextState));
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

}


class DoorIndividualTransitionObj implements Serializable {
    DoorIndividualState currentState;
    int action;
    int reward;
    DoorIndividualState nextState;

    public DoorIndividualTransitionObj(DoorIndividualState currentState, int action, int reward, DoorIndividualState nextState) {
        this.currentState = new DoorIndividualState(currentState);
        this.action = action;
        this.reward = reward;
        this.nextState = new DoorIndividualState(nextState);
    }

    public DoorIndividualTransitionObj(DoorIndividualTransitionObj transition) {
        this.currentState = new DoorIndividualState(transition.currentState);
        this.action = transition.action;
        this.reward = transition.reward;
        this.nextState = new DoorIndividualState(transition.nextState);
    }

    @Override
    public String toString() {
        return this.currentState.toString() + " | " + action + " _ " + reward + " | " + nextState.toString();
    }
}

class DoorIndividualState implements Serializable {

    int[] agentPos;
    int[] doorsState;

    public DoorIndividualState(int[] agent_pos, int numberExistingDoors) {
        this.agentPos = new int[]{agent_pos[0], agent_pos[1]};
        this.doorsState = new int[numberExistingDoors];
    }

    public DoorIndividualState(DoorIndividualState state) {
        this.agentPos = new int[]{state.agentPos[0], state.agentPos[1]};
        this.doorsState = new int[state.doorsState.length];
        System.arraycopy(state.doorsState, 0, this.doorsState, 0, this.doorsState.length);
    }

    public DoorIndividualState(int[] agent_pos, int[] _buttonsState) {
        this.agentPos = new int[]{agent_pos[0], agent_pos[1]};
        this.doorsState = new int[_buttonsState.length];
        System.arraycopy(_buttonsState, 0, this.doorsState, 0, this.doorsState.length);
    }

    public void changeAgentPos(int z, int x) {
        this.agentPos[0] = z;
        this.agentPos[1] = x;
    }

    public void changeDoorState(int doorIndex) {
        this.doorsState[doorIndex - 1] = this.doorsState[doorIndex - 1] ^= 1;
    }

    public void copyAllDoorsState(int[] newButtonsState) {
        System.arraycopy(newButtonsState, 0, this.doorsState, 0, this.doorsState.length);
    }

    public boolean equalsTo(DoorIndividualState state) {
        return (Arrays.equals(this.agentPos, state.agentPos) && Arrays.equals(this.doorsState, state.doorsState));
    }

    @Override
    public String toString() {
        return "<(" + this.agentPos[1] + ", 0, " + this.agentPos[0] + "), " + Arrays.toString(this.doorsState) + ">";
    }

}

class DoorRewardRewardStateStateObject {

    int rewardAgent0;
    int rewardAgent1;
    DoorIndividualState stateAgent0;
    DoorIndividualState stateAgent1;

    public DoorRewardRewardStateStateObject(int rewardAgent0, int rewardAgent1, DoorIndividualState stateAgent0, DoorIndividualState stateAgent1) {
        this.rewardAgent0 = rewardAgent0;
        this.rewardAgent1 = rewardAgent1;
        this.stateAgent0 = new DoorIndividualState(stateAgent0);
        this.stateAgent1 = new DoorIndividualState(stateAgent1);
    }

    public DoorRewardRewardStateStateObject(DoorRewardRewardStateStateObject obj) {
        this.rewardAgent0 = obj.rewardAgent0;
        this.rewardAgent1 = obj.rewardAgent1;
        this.stateAgent0 = new DoorIndividualState(obj.stateAgent0);
        this.stateAgent1 = new DoorIndividualState(obj.stateAgent1);
    }


}

class DoorIndividualQTableObj implements Serializable {

    DoorIndividualState state;
    float[] actionsQValues = new float[6];  //number of individual actions

    public DoorIndividualQTableObj(DoorIndividualState state) {
        this.state = new DoorIndividualState(state);
    }

    public DoorIndividualQTableObj(DoorIndividualQTableObj obj) {
        this.state = new DoorIndividualState(obj.state);
        actionsQValues = (float[]) obj.actionsQValues.clone();
    }

    public void changeActionQValue(int actionIndex, float value) {
        if (Float.toString(this.actionsQValues[actionIndex] + value).equals("Infinity")) {
            System.out.println("AQUIIIIIIIIII  " + this.actionsQValues[actionIndex] + "  " + value);
            System.exit(112);
        }
        this.actionsQValues[actionIndex] += value;
    }

    public boolean equalsTo(DoorIndividualQTableObj obj) {
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