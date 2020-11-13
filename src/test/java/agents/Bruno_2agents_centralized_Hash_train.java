package agents;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;


public class Bruno_2agents_centralized_Hash_train {

    String[] actions;
    ArrayList<String[]> centralizedActions;
    String[] targetButtons;
    int[] doorsState;
    ArrayList<String[]> initialMapMatrix;
    ArrayList<String[]> mapMatrix;
    ArrayList<String[]> connectionButtonsDoors;

    ArrayList<CentralizedQTableObj> QTable;

    //    ArrayList<CentralizedTransitionObj> TransitionTable;
    LinkedHashMap<Integer, CentralizedTransitionObj> TransitionTable;

//    ArrayList<CentralizedQTableObj> bestQTableYet;

    ArrayList<String> targetButtonsAlreadyClicked;

    double epsilon = 1;

    int epsilonRate = 1000; //Must be multiple of 10

    float learning_rate = 0.2f;
    float gamma = 0.65f;

    int max_steps;

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
        this.QTable = new ArrayList<CentralizedQTableObj>();

        setUpScenarioMatrix(scenario_filename);

        this.targetButtons = targetButtons;
        this.targetButtonsAlreadyClicked = new ArrayList<String>();
        this.actions = new String[]{"Nothing", "Up", "Down", "Left", "Right", "Press"};
        setupCentralizedActions();

        this.TransitionTable = new LinkedHashMap<Integer, CentralizedTransitionObj>();
//        this.TransitionTable = getTransitionTable("centralizedHashTransitionTable_" + scenario_filename);


//        for (int i = 0; i< this.centralizedActions.size(); i++)
//            System.out.println(i + " " + Arrays.toString(this.centralizedActions.get(i)));

        runTraining(1_000_001);

        saveTransitionTableToFile("centralizedHashTransitionTable_" + scenario_filename);
        savePolicyToFile("centralizedHash_" + scenario_filename, this.QTable);

    }

    void setUpScenarioMatrix(String scenario_filename) {
//        String csvFile = "D:/GitHub/Tese_iv4XR_Pessoal/src/test/resources/levels/bruno_" + scenario_filename + ".csv";
        String csvFile = "C:\\Users\\Beatriz Carreirer\\Documents\\Bruno_Programas\\GitHub\\Tese_iv4XR_Pessoal\\src\\test\\resources\\levels\\bruno_" + scenario_filename + ".csv";

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

    void printQTable() {
        for (CentralizedQTableObj obj : this.QTable)
            System.out.println(obj.toString());
        System.out.println();
    }

    void printTransitionTable() {
        for (CentralizedTransitionObj a : this.TransitionTable.values())
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

    int chooseAction(CentralizedState state, int agent) {
        Random r = new Random();
        if (r.nextDouble() > this.epsilon || this.validationEpisode) {
            for (CentralizedQTableObj obj : this.QTable)
                if (obj.state.equalsTo(state)) {
                    for (int i = 0; i < this.actions.length; i++)
                        if (this.actions[i].equals(this.centralizedActions.get(obj.maxAction())[agent]))
                            return i;
                }
            return 0;
        }
        return r.nextInt(this.actions.length);

    }

    RewardRewardStateObject actOnMap(CentralizedState currentState, int actionAgent0, int actionAgent1) {
        CentralizedState nextState = new CentralizedState(currentState);
        int rewardAgent0 = 0;
        int rewardAgent1 = 0;
        int[] newPos;

        //Agent0
        if (this.actions[actionAgent0].equals("Press")) {
            if (this.mapMatrix.get(nextState.agent0Pos[0])[nextState.agent0Pos[1]].contains("button")) {
                String buttonToClick = new String(this.mapMatrix.get(nextState.agent0Pos[0])[nextState.agent0Pos[1]].substring(0, 7));
                nextState.changeButtonState(Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)));
                rewardAgent0 = getRewardFromPressingButton(nextState, buttonToClick);
                openCloseDoor(buttonToClick);

//                printInvertedMapMatrix();
//                System.out.println(nextState.toString());
//                System.out.println("Agent0 buttonPressed " + buttonToClick + " | Reward " + rewardAgent0);
//                System.out.println(this.targetButtonsAlreadyClicked.size() + " | " + Arrays.toString(this.targetButtonsAlreadyClicked.toArray()));
//                System.out.println();
            } else
                rewardAgent0--;
        } else {
            newPos = new int[]{nextState.agent0Pos[0], nextState.agent0Pos[1]};

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

                changeMapMatrixAgentPositions("agent0", currentState.agent0Pos[0], currentState.agent0Pos[1], newPos[0], newPos[1]);
            }
            rewardAgent0--;
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
            } else
                rewardAgent1--;
        } else {
            newPos = new int[]{nextState.agent1Pos[0], nextState.agent1Pos[1]};
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

                changeMapMatrixAgentPositions("agent1", currentState.agent1Pos[0], currentState.agent1Pos[1], newPos[0], newPos[1]);
            }
            rewardAgent1--;
        }

        return new RewardRewardStateObject(rewardAgent0, rewardAgent1, nextState);
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

    void updateQTable(CentralizedState currentState, int action, int reward, CentralizedState nextState) {

        CentralizedQTableObj obj = new CentralizedQTableObj(currentState);

        if (this.QTable.size() == 0 || !objExistsInQTable(obj))
            this.QTable.add(obj);

        /*
        //FORMULA 1 = qtable[state][action] = (1 - learning_rate) * qtable[state, action] + learning_rate * (reward + gamma * Nd4j.max(qtable[next_state,:]));

        //Part1 = (1 - learning_rate) * qtable[state, action]
        float part1 = (float) ((1 - this.learning_rate) * getQValueQTable(currentState, action));

        //Part2 = learning_rate * (reward + gamma * Nd4j.max(qtable[next_state,:]));
        float part2 = (float) (this.learning_rate * (reward + this.gamma * getQValueQTable(nextState, -1)));

        float value = part1 + part2;
        */

        //FORMULA 2 =
        float value = this.learning_rate * ((float) reward + (this.gamma * getQValueQTable(nextState, -1)) - getQValueQTable(currentState, action));

        for (CentralizedQTableObj temp : this.QTable)
            if (temp.equalsTo(obj)) {
                if (Float.toString(temp.actionsQValues[action] + value).equals("Infinity")) {
                    System.out.println("AQUIIIIIIIIII 22222   " + temp.actionsQValues[action] + "  " + value);
                    System.exit(112);
                }
                temp.changeActionQValue(action, value);
            }

    }

    boolean objExistsInQTable(CentralizedQTableObj obj) {
        for (CentralizedQTableObj temp : this.QTable)
            if (temp.equalsTo(obj))
                return true;
        return false;
    }

    float getQValueQTable(CentralizedState state, int action) {
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

    void savePolicyToFile(String filename, ArrayList<CentralizedQTableObj> list) {
        // save the object to file
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);

            for (CentralizedQTableObj obj : list)
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

    void printBestActionCurrentState(CentralizedState state) {
        for (CentralizedQTableObj obj : this.QTable)
            if (obj.state.equalsTo(state))
                System.out.println("Best Action = " + Arrays.toString(this.centralizedActions.get(obj.maxAction())));
    }

    void updateTransitionTable(CentralizedState currentState, int action, int reward, CentralizedState nextState) {
        String currentStateHash = "" + currentState.agent0Pos[0] + currentState.agent0Pos[1] + currentState.agent1Pos[0] + currentState.agent1Pos[1];
        for (int btn: currentState.buttonsState)
            currentStateHash += "" + btn;
        currentStateHash += "" + action;
        int hash = Objects.hash(currentStateHash);

        if (this.TransitionTable.isEmpty() || !this.TransitionTable.containsKey(hash)) {
            CentralizedTransitionObj temp = new CentralizedTransitionObj(currentState, action, reward, nextState);
            this.TransitionTable.put(hash, temp);
        }
    }

    void runDynaQ(int DynaQSteps) {
//        System.out.println("RUN DYNA");

        List<Integer> keysAsArray = new ArrayList<Integer>(this.TransitionTable.keySet());
        Random r = new Random();

        CentralizedTransitionObj transitionObj;

        for (float dynaStep = 0; dynaStep < DynaQSteps; dynaStep++) {
            transitionObj = this.TransitionTable.get(keysAsArray.get(r.nextInt(keysAsArray.size())));
            updateQTable(transitionObj.currentState, transitionObj.action, transitionObj.reward, transitionObj.nextState);
        }
    }

    void runTraining(long maxEpisodes){
        int w = countApperancesOfWordOnInitialMap("w");

        int max_steps = ((this.initialMapMatrix.size() * this.initialMapMatrix.get(0).length) - w) * this.actions.length;
        int minimumValidationSteps = max_steps;  //Menos 1 porque os agentes tem que conseguir resolver com menos ações dos que as totais possiveis

        for(int _episode = 0; _episode < maxEpisodes; _episode++){
            if ((_episode + 1) % 10 == 0 && _episode!=0) {
                this.validationEpisode = true;
            }
            else {
                this.validationEpisode = false;
            }

//            createCurrentMapMatrix();
            resetMapMatrix();

            this.doorsState = new int[countApperancesOfWordOnInitialMap("door")];

            CentralizedState nextState = new CentralizedState(findTruePosInCurrentMapMatrix("agent0"), findTruePosInCurrentMapMatrix("agent1"), countApperancesOfWordOnInitialMap("button"));
            CentralizedState currentState = new CentralizedState(nextState);

//            if (this.validationEpisode)
//                System.out.println(this.QTable.get(0).toString());

            int actionAgent0;
            int actionAgent1;
            int rewardAgent0;
            int rewardAgent1;
            int reward;
            int action;
            RewardRewardStateObject rewardRewardStateObject;

            boolean reachedEnd = false;

            int step;
            for (step = 0; step < max_steps + 1; step++) {

                //action Agent0
                actionAgent0 = chooseAction(currentState, 0);

                //action Agent1
                actionAgent1 = chooseAction(currentState, 1);

                //Act on map, get rewards and nextState
                rewardRewardStateObject = new RewardRewardStateObject(actOnMap(currentState, actionAgent0, actionAgent1));
                rewardAgent0 = rewardRewardStateObject.rewardAgent0;
                rewardAgent1 = rewardRewardStateObject.rewardAgent1;
                nextState = rewardRewardStateObject.state;
                reward = rewardAgent0 + rewardAgent1;

                //Update Q Table
                if (!this.validationEpisode) {
                    action = getCentralizedAction(actionAgent0, actionAgent1);

                    updateQTable(currentState, action, reward, nextState);

                    //Update Transition table
                    updateTransitionTable(currentState, action, reward, nextState);
                }

                //Check if the target buttons have been clicked
                if (checkIfEndend(nextState)) {
                    reachedEnd = true;
                    break;
                }

                //Set current State
                currentState = new CentralizedState(nextState);
            }

            //Dyna-Q
            if(!this.validationEpisode)
                runDynaQ(1_000);

            //Early Stop
            if (this.validationEpisode) {

                if (step == minimumValidationSteps)
                    early_stop_counter--;
                else if (step < minimumValidationSteps)
                    minimumValidationSteps = step;
                else
                    early_stop_counter = early_stop_counter_reset;

                System.out.println("Validation Episode " + _episode + "/" + maxEpisodes + " done | Reached end = " + reachedEnd + " | #Steps = " + step + " | Best validations steps = " + minimumValidationSteps + " | Early Stop Counter = " + early_stop_counter);

                if (early_stop_counter == 0)
                    break;
            }
//            else
//                System.out.println("Training Episode " + _episode + " done | Reached end = " + reachedEnd + " | #Steps = " + step + " | Transition Size = " + this.TransitionTable.size());
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


class CentralizedTransitionObj implements Serializable {
    CentralizedState currentState;
    int action;
    int reward;
    CentralizedState nextState;

    public CentralizedTransitionObj(CentralizedState currentState, int action, int reward, CentralizedState nextState) {
        this.currentState = new CentralizedState(currentState);
        this.action = action;
        this.reward = reward;
        this.nextState = new CentralizedState(nextState);
    }

    public CentralizedTransitionObj(CentralizedTransitionObj transition) {
        this.currentState = new CentralizedState(transition.currentState);
        this.action = transition.action;
        this.reward = transition.reward;
        this.nextState = new CentralizedState(transition.nextState);
    }

    @Override
    public String toString() {
        return this.currentState.toString() + " | " + action + " | " + reward + " | " + nextState.toString();
    }
}

