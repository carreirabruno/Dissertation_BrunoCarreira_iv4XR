package agents;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;


public class Bruno_2agents_individual_Hash_train {

    String[] actions;
    String[] targetButtons;
    int[] doorsState;
    ArrayList<String[]> initialMapMatrix;
    ArrayList<String[]> mapMatrix;
    ArrayList<String[]> connectionButtonsDoors;

    ArrayList<IndividualQTableObj> QTableAgent0;
    ArrayList<IndividualQTableObj> QTableAgent1;

    LinkedHashMap<Integer, IndividualTransitionObj> TransitionTable;

    ArrayList<String> targetButtonsAlreadyClicked;

    double epsilon = 1;

    int epsilonRate = 1000; //Must be multiple of 10

    float learning_rate = 0.2f;
    float gamma = 0.65f;

    int early_stop_counter_reset = 3;
    int early_stop_counter = early_stop_counter_reset;

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
    public void create_policy_train(String scenario_filename, String[] targetButtons) throws InterruptedException, IOException {

        this.initialMapMatrix = new ArrayList<String[]>();
        this.connectionButtonsDoors = new ArrayList<String[]>();
        this.QTableAgent0 = new ArrayList<IndividualQTableObj>();
        this.QTableAgent1 = new ArrayList<IndividualQTableObj>();

        setUpScenarioMatrix(scenario_filename);

        this.targetButtons = targetButtons;
        this.targetButtonsAlreadyClicked = new ArrayList<String>();
        this.actions = new String[]{"Nothing", "Up", "Down", "Left", "Right", "Press"};

        this.TransitionTable = new LinkedHashMap<Integer, IndividualTransitionObj>();

        runTraining(5_000);

        saveTransitionTableToFile("individualHashTransitionTable_" + scenario_filename);
        savePolicyToFile("individualHash_" + scenario_filename + "_agent0", this.QTableAgent0);
        savePolicyToFile("individualHash_" + scenario_filename + "_agent1", this.QTableAgent1);

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

    int chooseAction(IndividualState state, int agent, ArrayList<IndividualQTableObj> table) {
        Random r = new Random();
        if (r.nextDouble() > this.epsilon || this.validationEpisode) {
            for (IndividualQTableObj obj : table)
                if (obj.state.equalsTo(state))
                    return obj.maxAction();
            return 0;
        }
        return r.nextInt(this.actions.length);

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

    boolean checkIfEndend(IndividualState stateAgent0, IndividualState stateAgent1) {
        int count = targetButtons.length;
//        System.out.println("count = " + count);
        for (String button : targetButtons) {
            if ((stateAgent0.buttonsState[Integer.parseInt(new String(button.substring(button.length() - 1))) - 1]) == 1 && (stateAgent1.buttonsState[Integer.parseInt(new String(button.substring(button.length() - 1))) - 1]) == 1)
                count--;
        }
        return count == 0;
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

    boolean objExistsInQTable(ArrayList<IndividualQTableObj> table, IndividualQTableObj obj) {
        for (IndividualQTableObj temp : table)
            if (temp.equalsTo(obj))
                return true;
        return false;
    }

    void updateQTable(IndividualState currentState, int action, int reward, IndividualState nextState, ArrayList<IndividualQTableObj> table) {

        IndividualQTableObj obj = new IndividualQTableObj(currentState);

        if (table.size() == 0 || !objExistsInQTable(table,obj))
            table.add(obj);

        /*
        //FORMULA 1 = qtable[state][action] = (1 - learning_rate) * qtable[state, action] + learning_rate * (reward + gamma * Nd4j.max(qtable[next_state,:]));

        //Part1 = (1 - learning_rate) * qtable[state, action]
        float part1 = (float) ((1 - this.learning_rate) * getQValueQTable(currentState, action));

        //Part2 = learning_rate * (reward + gamma * Nd4j.max(qtable[next_state,:]));
        float part2 = (float) (this.learning_rate * (reward + this.gamma * getQValueQTable(nextState, -1)));

        float value = part1 + part2;
        */

        //FORMULA 2 =
        float value = this.learning_rate * ((float) reward + (this.gamma * getQValueQTable(nextState, -1, table)) - getQValueQTable(currentState, action, table));

        for (IndividualQTableObj temp : table)
            if (temp.equalsTo(obj)) {
                if (Float.toString(temp.actionsQValues[action] + value).equals("Infinity")) {
                    System.out.println("AQUIIIIIIIIII 22222   " + temp.actionsQValues[action] + "  " + value);
                    System.exit(112);
                }
                temp.changeActionQValue(action, value);
            }

    }

    float getQValueQTable(IndividualState state, int action, ArrayList<IndividualQTableObj> table ) {
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

    void savePolicyToFile(String filename, ArrayList<IndividualQTableObj> list) {
        // save the object to file
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);

            for (IndividualQTableObj obj : list)
                out.writeObject(obj);

            out.close();
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

    void updateTransitionTable(IndividualState currentState, int action, int reward, IndividualState nextState) {
        String currentStateHash = "" + currentState.agentPos[0] + currentState.agentPos[1];
        for (int btn: currentState.buttonsState)
            currentStateHash += "" + btn;
        currentStateHash += "" + action;
        int hash = Objects.hash(currentStateHash);

        if (this.TransitionTable.isEmpty() || !this.TransitionTable.containsKey(hash)) {
            IndividualTransitionObj temp = new IndividualTransitionObj(currentState, action, reward, nextState);
            this.TransitionTable.put(hash, temp);
        }
    }

    void runDynaQ(int DynaQSteps) {
//        System.out.println("RUN DYNA");

        List<Integer> keysAsArray = new ArrayList<Integer>(this.TransitionTable.keySet());
        Random r = new Random();

        IndividualTransitionObj transitionObj;

        for (float dynaStep = 0; dynaStep < DynaQSteps; dynaStep++) {
            transitionObj = this.TransitionTable.get(keysAsArray.get(r.nextInt(keysAsArray.size())));
            updateQTable(transitionObj.currentState, transitionObj.action, transitionObj.reward, transitionObj.nextState, this.QTableAgent0);
            
            transitionObj = this.TransitionTable.get(keysAsArray.get(r.nextInt(keysAsArray.size())));
            updateQTable(transitionObj.currentState, transitionObj.action, transitionObj.reward, transitionObj.nextState, this.QTableAgent1);
        }
    }

    void runTraining(long maxEpisodes){
        int w = countApperancesOfWordOnInitialMap("w");

        int max_steps = ((this.initialMapMatrix.size() * this.initialMapMatrix.get(0).length) - w) * this.actions.length;
        int minimumValidationSteps = max_steps;

        for(int _episode = 0; _episode < maxEpisodes; _episode++) {
            if ((_episode + 1) % 10 == 0 && _episode != 0)
                this.validationEpisode = true;
            else
                this.validationEpisode = false;

//            createCurrentMapMatrix();
            resetMapMatrix();

            this.doorsState = new int[countApperancesOfWordOnInitialMap("door")];

            IndividualState currentStateAgent0 = new IndividualState(findTruePosInCurrentMapMatrix("agent0"), countApperancesOfWordOnInitialMap("button"));
            IndividualState currentStateAgent1 = new IndividualState(findTruePosInCurrentMapMatrix("agent1"), countApperancesOfWordOnInitialMap("button"));

            IndividualState nextStateAgent0;
            IndividualState nextStateAgent1;

            int actionAgent0;
            int actionAgent1;
            int rewardAgent0;
            int rewardAgent1;
            RewardRewardStateStateObject rewardRewardStateStateObject;

            boolean reachedEnd = false;

            int step;
            for (step = 0; step < max_steps; step++) {

                //action Agent0
                actionAgent0 = chooseAction(currentStateAgent0, 0, this.QTableAgent0);

                //action Agent1
                actionAgent1 = chooseAction(currentStateAgent1, 1, this.QTableAgent1);

                //Act on map, get rewards and nextState
                rewardRewardStateStateObject = new RewardRewardStateStateObject(actOnMap(currentStateAgent0, actionAgent0, currentStateAgent1, actionAgent1));
                rewardAgent0 = rewardRewardStateStateObject.rewardAgent0;
                rewardAgent1 = rewardRewardStateStateObject.rewardAgent1;
                nextStateAgent0 = rewardRewardStateStateObject.stateAgent0;
                nextStateAgent1 = rewardRewardStateStateObject.stateAgent1;

//                //Update Q Table
                if (!this.validationEpisode) {
                    updateQTable(currentStateAgent0, actionAgent0, rewardAgent0, nextStateAgent0, this.QTableAgent0);
                    updateQTable(currentStateAgent1, actionAgent1, rewardAgent1, nextStateAgent1, this.QTableAgent1);

                    //Update Transition table
                    updateTransitionTable(currentStateAgent0, actionAgent0, rewardAgent0, nextStateAgent0);
                    updateTransitionTable(currentStateAgent1, actionAgent1, rewardAgent1, nextStateAgent1);
                }

//                System.out.println(currentStateAgent0.toString() + " " + this.actions[actionAgent0] + " " + rewardAgent0 + " " + nextStateAgent0.toString());
//                System.out.println(currentStateAgent1.toString() + " " + this.actions[actionAgent1] + " " +
//                rewardAgent1 + " " + nextStateAgent1.toString());
////                printInvertedMapMatrix();
//                System.out.println("----------------------------");


                //Check if the target buttons have been clicked
                if (checkIfEndend(nextStateAgent0, nextStateAgent1)) {
//                    System.out.println(nextStateAgent0.toString() + " " + this.actions[actionAgent0]);
//                    System.out.println(nextStateAgent1.toString() + " " + this.actions[actionAgent1] );
                    reachedEnd = true;
                    break;
                }

                //Set current State
                currentStateAgent0 = new IndividualState(nextStateAgent0);
                currentStateAgent1 = new IndividualState(nextStateAgent1);
            }

            //Dyna-Q
            if (!this.validationEpisode)
                runDynaQ(1000);

            if (_episode % 1000 == 0 && _episode != 0) {
                printQTable(this.QTableAgent0);
                System.out.println("-------------");
                printQTable(this.QTableAgent1);
            }

            //Early Stop
            if (this.validationEpisode) {

                if (step < max_steps && step == minimumValidationSteps)
                    early_stop_counter--;
                else if (step < minimumValidationSteps)
                    minimumValidationSteps = step;
                else {
                    early_stop_counter = early_stop_counter_reset;
                    minimumValidationSteps = max_steps;
                }

                System.out.println("Validation Episode " + _episode + "/" + (maxEpisodes-1) + " done | Reached end = " + reachedEnd + " | #Steps = " + step + " | Best validations steps = " + minimumValidationSteps + " | Early Stop Counter = " + early_stop_counter);

                if (early_stop_counter == 0)
                    break;
            }
//            else
//                System.out.println("Training Episode " + _episode + " done | Reached end = " + reachedEnd + " | #Steps = " + step);
        }
    }

    void saveTransitionTableToFile(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos);

//            for (Map.Entry<Integer, CentralizedTransitionObj> obj : this.TransitionTable.entrySet())
//                out.writeObject(obj);
            out.writeObject(this.TransitionTable);

            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    LinkedHashMap<Integer, CentralizedTransitionObj> getTransitionTable(String filename) throws IOException {
        LinkedHashMap<Integer, CentralizedTransitionObj> table = new LinkedHashMap<Integer, CentralizedTransitionObj>();
        try {
//            Object obj;
            Map.Entry<Integer, CentralizedTransitionObj> obj;
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis);
            table = (LinkedHashMap<Integer, CentralizedTransitionObj>) in.readObject();
//            for (; ; ) {
//                obj = (Map.Entry<Integer, CentralizedTransitionObj>) in.readObject();
////                obj = in.readObject();
////                Map.Entry<Integer, CentralizedTransitionObj> temp = (Map.Entry<Integer, CentralizedTransitionObj>) obj;
//                table.put(obj.getKey(), obj.getValue());
//            }

            in.close();
        } catch (Exception ignored) {
        }

        return table;
    }

}


class IndividualTransitionObj implements Serializable {
    IndividualState currentState;
    int action;
    int reward;
    IndividualState nextState;

    public IndividualTransitionObj(IndividualState currentState, int action, int reward, IndividualState nextState) {
        this.currentState = new IndividualState(currentState);
        this.action = action;
        this.reward = reward;
        this.nextState = new IndividualState(nextState);
    }

    public IndividualTransitionObj(IndividualTransitionObj transition) {
        this.currentState = new IndividualState(transition.currentState);
        this.action = transition.action;
        this.reward = transition.reward;
        this.nextState = new IndividualState(transition.nextState);
    }

    @Override
    public String toString() {
        return this.currentState.toString() + " | " + action + " _ " + reward + " | " + nextState.toString();
    }
}

class IndividualState implements Serializable {

    int[] agentPos;
    int[] buttonsState;

    public IndividualState (int[] agent_pos, int numberExistingButtons) {
        this.agentPos = new int[]{agent_pos[0], agent_pos[1]};
        this.buttonsState = new int[numberExistingButtons];
    }

    public IndividualState (IndividualState  state) {
        this.agentPos = new int[]{state.agentPos[0], state.agentPos[1]};
        this.buttonsState = new int[state.buttonsState.length];
        System.arraycopy(state.buttonsState, 0, this.buttonsState, 0, this.buttonsState.length);
    }

    public IndividualState (int[] agent_pos, int[] _buttonsState) {
        this.agentPos = new int[]{agent_pos[0], agent_pos[1]};
        this.buttonsState = new int[_buttonsState.length];
        System.arraycopy(_buttonsState, 0, this.buttonsState, 0, this.buttonsState.length);
    }
    
    public void changeAgentPos(int z, int x) {
        this.agentPos[0] = z;
        this.agentPos[1] = x;
    }
    
    public void changeButtonState(int buttonIndex) {
        this.buttonsState[buttonIndex - 1] = this.buttonsState[buttonIndex - 1] ^= 1;
    }

    public void copyAllButtonsState(int[] newButtonsState) {
        for(int i = 0; i < this.buttonsState.length; i++)
            this.buttonsState[i] = newButtonsState[i];
    }

    public boolean equalsTo(IndividualState state){
        return (Arrays.equals(this.agentPos, state.agentPos) && Arrays.equals(this.buttonsState, state.buttonsState));
    }

    @Override
    public String toString() {
        return "<(" + this.agentPos[1] + ", 0, " + this.agentPos[0] + "), " + Arrays.toString(this.buttonsState) + ">";
    }

}

class RewardRewardStateStateObject {

    int rewardAgent0;
    int rewardAgent1;
    IndividualState stateAgent0;
    IndividualState stateAgent1;

    public RewardRewardStateStateObject(int rewardAgent0, int rewardAgent1, IndividualState stateAgent0, IndividualState stateAgent1) {
        this.rewardAgent0 = rewardAgent0;
        this.rewardAgent1 = rewardAgent1;
        this.stateAgent0 = new IndividualState(stateAgent0);
        this.stateAgent1 = new IndividualState(stateAgent1);
    }

    public RewardRewardStateStateObject(RewardRewardStateStateObject obj) {
        this.rewardAgent0 = obj.rewardAgent0;
        this.rewardAgent1 = obj.rewardAgent1;
        this.stateAgent0 = new IndividualState(obj.stateAgent0);
        this.stateAgent1 = new IndividualState(obj.stateAgent1);
    }


}

class IndividualQTableObj implements Serializable {

    IndividualState state;
    float[] actionsQValues = new float[6];  //number of centralized actions

    public IndividualQTableObj(IndividualState state){
        this.state = new IndividualState(state);
    }

    public IndividualQTableObj(IndividualQTableObj obj){
        this.state = new IndividualState(obj.state);
        actionsQValues = (float[])obj.actionsQValues.clone();
    }

    public void changeActionQValue(int actionIndex, float value){
        if(Float.toString(this.actionsQValues[actionIndex] + value).equals("Infinity")) {
            System.out.println("AQUIIIIIIIIII  " + this.actionsQValues[actionIndex] + "  " + value);
            System.exit(112);
        }
        this.actionsQValues[actionIndex] += value;
    }

    public boolean equalsTo(IndividualQTableObj obj){
        return this.state.equalsTo(obj.state);
    }

    public float maxActionQValue(){
        float maxValue = Float.NEGATIVE_INFINITY;
        for(float v: this.actionsQValues)
            if(v > maxValue)
                maxValue = v;

        return maxValue;
    }

    public int maxAction(){
        int index = -1;
        double maxValue = Double.NEGATIVE_INFINITY;
        for(int i = 0; i< this.actionsQValues.length; i++)
            if(this.actionsQValues[i] > maxValue) {
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