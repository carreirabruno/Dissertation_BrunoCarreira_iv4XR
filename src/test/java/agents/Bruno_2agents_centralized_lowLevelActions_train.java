package agents;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class Bruno_2agents_centralized_lowLevelActions_train {

    String[] actions;
    ArrayList<String[]> centralizedActions;
    String[] targetButtons;
    int[] doorsState;
    ArrayList<String[]> initialMapMatrix;
    ArrayList<String[]> mapMatrix;
    ArrayList<String[]> connectionButtonsDoors;

    ArrayList<CentralizedQTableObj> QTable;

    ArrayList<CentralizedQTableObj> bestQTableYet;

    ArrayList<String> targetButtonsAlreadyClicked;

    long episodes = 100_000_001;
//    long episodes = 1;

    double epsilon = 1;

    int epsilonRate = 1000; //Must be multiple of 10

    float learning_rate = 0.2f;
    float gamma = 0.65f;

    int max_steps;

    int early_stop_counter_reset = 3;
    int early_stop_counter = early_stop_counter_reset;

    int minimumValidationSteps;

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

//        printInitialMapMatrix();

        this.targetButtons = targetButtons;
        this.targetButtonsAlreadyClicked = new ArrayList<String>();
        this.actions = new String[]{"Nothing", "Up", "Down", "Left", "Right", "Press"};
        setupCentralizedActions();

//        for(int i = 0; i <this.centralizedActions.size(); i++)
//            System.out.println(i + "  " + Arrays.toString(this.centralizedActions.get(i)));

        int w = countApperancesOfWordOnInitialMap("w");

        max_steps = ((this.initialMapMatrix.size() * this.initialMapMatrix.get(0).length)-w) * this.actions.length;
//        max_steps = 10;
        minimumValidationSteps = max_steps;  //Menos porque os agentes tem que conseguir resolver com menos ações dos que as totais possiveis

        for (int _episode = 0; _episode < this.episodes; _episode++) {

            resetMapMatrix();
//            printInvertedMapMatrix();
//            System.exit(112);
            this.doorsState = new int[countApperancesOfWordOnInitialMap("door")];

            CentralizedState nextState = new CentralizedState(findTruePosInInitialMapMatrix("agent0"), findTruePosInInitialMapMatrix("agent1"), countApperancesOfWordOnInitialMap("button"));
            CentralizedState currentState = new CentralizedState(nextState);
            int actionAgent0;
            int actionAgent1;
            int rewardAgent0;
            int rewardAgent1;
            int reward;
            int action;
            RewardRewardStateObject rewardRewardStateObject;

            boolean reachedEnd = false;

           int step;
//            while(!reachedEnd){
            for (step = 1; step < max_steps + 1; step++) {

                if(this.validationEpisode && this.early_stop_counter == 1) {
                    System.out.println("Episode = " + _episode);
                    printInvertedMapMatrix();
                    System.out.println(currentState.toString());
                    printBestActionCurrentState(currentState);
                    System.out.println("------------------------------");
                }

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

                //Prints to understand whats is happening

//                printInvertedMapMatrix();
//                System.out.println(currentState.toString());
//                System.out.println("Agent0, " + this.actions[actionAgent0] + ", " + rewardAgent0);
//                System.out.println("Agent1, " + this.actions[actionAgent1] + ", " + rewardAgent1);
//                System.out.println("----------------------------------");
//                System.out.println(Arrays.toString(this.targetButtonsAlreadyClicked.toArray()));
//                System.out.println(nextState.toString());
//                System.out.println();

                //Update Q Table
                if (!this.validationEpisode || this.early_stop_counter != 1) {
                    action = getCentralizedAction(actionAgent0, actionAgent1);
                    updateQTable(currentState, action, reward, nextState);
                }

                //Check if the target buttons have been clicked
                if (checkIfEndend(nextState)) {
                    reachedEnd = true;
                    break;
                }

                //Set current State
                currentState = new CentralizedState(nextState);
            }

//            System.out.println("Episode " + _episode + "/" + this.episodes + " done | Reached end = " + reachedEnd);


//            //Reset Epsilon
//            if((_episode + 1) % this.epsilonRate == 0){
//                this.epsilon = 1;
//            }



            //Early Stop
            if (this.validationEpisode) {

                if (step == this.minimumValidationSteps)
                    early_stop_counter--;
                else if (step < this.minimumValidationSteps) {
                    this.minimumValidationSteps = step;
                    this.bestQTableYet = new ArrayList<CentralizedQTableObj>();
                    for (CentralizedQTableObj a : this.QTable)
                        this.bestQTableYet.add(new CentralizedQTableObj(a));
                }
                else
                    early_stop_counter = early_stop_counter_reset;

                System.out.println("Validation Episode " + _episode + "/" + this.episodes + " done | Reached end = " + reachedEnd + " | #Steps = " + step + " | Best validations steps = " + this.minimumValidationSteps + " | Early Stop Counter = " + early_stop_counter);
//             System.out.println("Best initial action " + this.QTable.get(0).toString());

                if (early_stop_counter == 0)
                    break;
            }
            else
                System.out.println("Training Episode " + _episode + "/" + this.episodes + " done | Reached end = " + reachedEnd + " | #Steps = " + step + " | Epsilon = " + this.epsilon);


            if((_episode + 1) % this.epsilonRate == 0)
                this.epsilon = 1;

            if ((_episode + 1) % 5 == 0){
                this.validationEpisode = true;
            }
            else {
                this.epsilon -= 1.0/this.epsilonRate;
                this.validationEpisode = false;
            }

        }


//        printQTable();

        savePolicyToFile("centralizedLowLevelActions_" + scenario_filename, this.QTable);
//        savePolicyToFile("centralizedLowLevelActions_" + scenario_filename + "_bestQyet", this.bestQTableYet);

        //UNITY
        /*
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

        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

         Train
        for (int i = 0; i < episodes; i++) {
            System.out.println("Episode " + i + " of " + (episodes - 1) + " epsilon " + epsilon);

            CentralizedQtableObject currentState_qtableObj = new CentralizedQtableObject(new CentralizedState(agent0, agent1, this.existing_buttons));

            double reward = 0;
//            int action = getNextActionIndex(currentState_qtableObj.state, agent0, agent1);
            int action = 0;

            // Set initial goals to agents
            var g0 = doNextAction(action, 0, agent0);
            agent0.setGoal(g0);
            var g1 = doNextAction(action, 1, agent1);
            agent1.setGoal(g1);

            // set up the initial state
            while (agent0.getState().worldmodel.position == null && agent1.getState().worldmodel.position == null) {
                agent0.update();
                agent1.update();
            }
            currentState_qtableObj = new CentralizedQtableObject(new CentralizedState(agent0, agent1, this.existing_buttons));
            Qtable_add(currentState_qtableObj);

            CentralizedQtableObject nextState_qtableObj = new CentralizedQtableObject(new CentralizedState(agent0, agent1, this.existing_buttons));

            int stuckTicks = 0;

            long start = System.nanoTime();
            long lasTime = System.nanoTime();
            final double amountOfTicks = 5.0;  // update 5x per second
            double ns = 1000000000 / amountOfTicks;
            double delta = 0;

            boolean target1_clicked = false;
            boolean target2_clicked = false;

            while (((System.nanoTime() - start) / 1_000_000_000) < max_time) {
                long now = System.nanoTime();
                delta += (now - lasTime) / ns;
                lasTime = now;
                if (delta >= 1) {

                    if (this.actions.get(action)[0].equals("null"))
                        reward -= 0;
                    else
                        reward -= 1;

                    if (this.actions.get(action)[1].equals("null"))
                        reward -= 0;
                    else
                        reward -= 1;


                    //Update agents
//                    if (!g0.getStatus().inProgress() && !g1.getStatus().inProgress()) {
                    if ((!g0.getStatus().inProgress() && !g1.getStatus().inProgress()) || stuckTicks >= stuck_counter * amountOfTicks) {

                        if (epsilon == 0 && g0.getStatus().success() && g1.getStatus().success())
                            System.out.println(this.actions.get(action)[0] + "   " + this.actions.get(action)[1]);

                        if (stuckTicks >= stuck_counter * amountOfTicks) {
                            System.out.println("Stuck");
                            System.out.println(this.actions.get(action)[0] + "   " + this.actions.get(action)[1]);
                        }


                        if (e1 != null && e1.getBooleanProperty("isOn") && this.actions.get(action)[0].equals(e1.id) && !target1_clicked) {
                            reward += 100;
                            target1_clicked = true;
                        } else if (f1 != null && f1.getBooleanProperty("isOn") && this.actions.get(action)[1].equals(f1.id) && !target1_clicked) {
                            reward += 100;
                            target1_clicked = true;
                        }
                        if (e2 != null && e2.getBooleanProperty("isOn") && this.actions.get(action)[0].equals(e2.id) && !target2_clicked) {
                            reward += 100;
                            target2_clicked = true;
                        } else if (f2 != null && f2.getBooleanProperty("isOn") && this.actions.get(action)[1].equals(f2.id) && !target2_clicked) {
                            reward += 100;
                            target2_clicked = true;
                        }


                        //Next state
                        nextState_qtableObj = new CentralizedQtableObject(new CentralizedState(agent0, agent1, this.existing_buttons));
                        Qtable_add(nextState_qtableObj);

                        //Q-learning
                        updateQtable(currentState_qtableObj, nextState_qtableObj, action, reward);


                        //Dyna-Q
                        CentralizedQtableObject _currentState_qtableObj = new CentralizedQtableObject(currentState_qtableObj);
                        CentralizedQtableObject _nextState_qtableObj = new CentralizedQtableObject(nextState_qtableObj);
                        int _action = action;
                        double _reward = reward;

                        for (int j = 0; j < 10; j++) {
                            //          Update model
                            //update T'[s,a,s']
                            TransitionTable_update(_currentState_qtableObj.state, _nextState_qtableObj.state, _action);

                            //udpate R'[s,a]
                            RewardTable_update(_currentState_qtableObj.state, _nextState_qtableObj.state, _action, _reward);

                            //          Hallucinate
                            //s = random
                            _currentState_qtableObj = new CentralizedQtableObject(this.TransitionTable.get(new Random().nextInt(this.TransitionTable.size())).currentState);
                            //a = random
                            _action = getPossibleAction_TransitiontTable(_currentState_qtableObj);
                            //s' = infer from T[]
                            _nextState_qtableObj = new CentralizedQtableObject(getNextState_TransitionTable(_currentState_qtableObj));
                            //r = infer from R[s',a]
                            _reward = getReward_RewardTable(_currentState_qtableObj, _nextState_qtableObj, _action);
                            try {
                                //          Q update
                                updateQtable(_currentState_qtableObj, _nextState_qtableObj, _action, _reward);
                            } catch (Exception o) {
                            }

                        }

                        currentState_qtableObj = new CentralizedQtableObject(nextState_qtableObj);

                        //Action
                        action = getNextActionIndex(currentState_qtableObj.state, agent0, agent1);
                        g0 = doNextAction(action, 0, agent0);
                        agent0.setGoal(g0);
                        g0.getStatus().resetToInProgress();
                        g1 = doNextAction(action, 1, agent1);
                        agent1.setGoal(g1);
                        g1.getStatus().resetToInProgress();

                        stuckTicks = 0;

                        reward = 0;
                    }


                    // Check if the agents got stuck for too long
                    stuckTicks++;

                    // Check if the target button isOn to end the game - Tem que estar aqui para o reward ser válido
//                    if ((e1 != null && e1.getBooleanProperty("isOn") || f1 != null && f1.getBooleanProperty("isOn")) &&
//                            (e2 != null && e2.getBooleanProperty("isOn") || f2 != null && f2.getBooleanProperty("isOn"))) {
                    if (target1_clicked && target2_clicked) {
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

            long episode_time = System.nanoTime() - start;
            System.out.println(episode_time / 1_000_000_000);

            if ((i + 1) % 10 == 0)
                epsilon = 0;
            else
                epsilon = 1;

            if (i % 10 == 0 && i > 0) {
                long episodes_time_in_seconds = episode_time / 1_000_000_000;
                this.TimePerEpisode.add(episodes_time_in_seconds);
                System.out.println("added time");

                //Early stop
                if (episodes_time_in_seconds < best_time) {
                    best_time = episodes_time_in_seconds;
                    System.out.println("best time = " + best_time);
                    early_stop_counter = early_stop_counter_reset;
                }

                if (episodes_time_in_seconds <= best_time + 1 && best_time != max_time) {
                    early_stop_counter--;
                    System.out.println("Early stop counter = " + early_stop_counter);
                } else {
                    early_stop_counter = early_stop_counter_reset;
                    best_time = max_time;
                }

                if (early_stop_counter == 0)
                    break;
            }


        }

        printQtable();

        savePolicyToFile("2agents_" + scenario_filename + "_centralized_agents");

         */


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
                rewardAgent0 = getRewardFromPressingButton(nextState,buttonToClick);
                openCloseDoor(buttonToClick);

//                printInvertedMapMatrix();
//                System.out.println(nextState.toString());
//                System.out.println("Agent0 buttonPressed " + buttonToClick + " | Reward " + rewardAgent0);
//                System.out.println(this.targetButtonsAlreadyClicked.size() + " | " + Arrays.toString(this.targetButtonsAlreadyClicked.toArray()));
//                System.out.println();
            }
            else
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
            }
            else
                rewardAgent1--;
        }
        else {
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

    void updateQTable(CentralizedState currentState, int action, int reward, CentralizedState nextState) {

        CentralizedQTableObj obj = new CentralizedQTableObj(currentState);

        if(this.QTable.size() == 0 || !objExistsInQTable(obj))
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
        float value = this.learning_rate * ((float)reward + (this.gamma * getQValueQTable(nextState, -1)) - getQValueQTable(currentState, action));

        for(CentralizedQTableObj temp: this.QTable)
            if(temp.equalsTo(obj)) {
                if(Float.toString(temp.actionsQValues[action] + value).equals("Infinity")) {
                    System.out.println("AQUIIIIIIIIII 22222   " + temp.actionsQValues[action] + "  " + value);
                    System.exit(112);
                }
                temp.changeActionQValue(action, value);
            }

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

    void printBestActionCurrentState(CentralizedState state){
        for(CentralizedQTableObj obj: this.QTable)
            if (obj.state.equalsTo(state))
                System.out.println("Best Action = " + Arrays.toString(this.centralizedActions.get(obj.maxAction())));
    }

}

class CentralizedState implements Serializable {

    int[] agent0Pos;
    int[] agent1Pos;
    int[] buttonsState;

    public CentralizedState(int[] agent0_pos, int[] agent1_pos, int numberExistingButtons) {
        this.agent0Pos = new int[]{agent0_pos[0], agent0_pos[1]};
        this.agent1Pos = new int[]{agent1_pos[0], agent1_pos[1]};
        this.buttonsState = new int[numberExistingButtons];
    }

    public CentralizedState(CentralizedState state) {
        this.agent0Pos = new int[]{state.agent0Pos[0], state.agent0Pos[1]};
        this.agent1Pos = new int[]{state.agent1Pos[0], state.agent1Pos[1]};
        this.buttonsState = new int[state.buttonsState.length];
        System.arraycopy(state.buttonsState, 0, this.buttonsState, 0, this.buttonsState.length);
    }


    public void changeAgent0Pos(int z, int x) {
        this.agent0Pos[0] = z;
        this.agent0Pos[1] = x;
    }

    public void changeAgent1Pos(int z, int x) {
        this.agent1Pos[0] = z;
        this.agent1Pos[1] = x;
    }

    public void changeButtonState(int buttonIndex) {
        this.buttonsState[buttonIndex - 1] = this.buttonsState[buttonIndex - 1] ^= 1;
    }

    public boolean equalsTo(CentralizedState state){
        return (Arrays.equals(this.agent0Pos, state.agent0Pos) && Arrays.equals(this.agent1Pos, state.agent1Pos) && Arrays.equals(this.buttonsState, state.buttonsState));
    }

    @Override
    public String toString() {
        return "<(" + this.agent0Pos[1] + ", 0, " + this.agent0Pos[0] + "), (" + this.agent1Pos[1] + ", 0, " + this.agent1Pos[0] + "), " + Arrays.toString(this.buttonsState) + ">";
    }

}

class RewardRewardStateObject {

    int rewardAgent0;
    int rewardAgent1;
    CentralizedState state;

    public RewardRewardStateObject(int rewardAgent0, int rewardAgent1, CentralizedState state) {
        this.rewardAgent0 = rewardAgent0;
        this.rewardAgent1 = rewardAgent1;
        this.state = new CentralizedState(state);
    }

    public RewardRewardStateObject(RewardRewardStateObject obj) {
        this.rewardAgent0 = obj.rewardAgent0;
        this.rewardAgent1 = obj.rewardAgent1;
        this.state = new CentralizedState(obj.state);
    }


}

class CentralizedQTableObj implements Serializable {

    CentralizedState state;
    float[] actionsQValues = new float[36];  //number of centralized actions

    public CentralizedQTableObj(CentralizedState state){
        this.state = new CentralizedState(state);
    }

    public CentralizedQTableObj(CentralizedQTableObj obj){
        this.state = new CentralizedState(obj.state);
        actionsQValues = (float[])obj.actionsQValues.clone();
    }

    public void changeActionQValue(int actionIndex, float value){
        if(Float.toString(this.actionsQValues[actionIndex] + value).equals("Infinity")) {
            System.out.println("AQUIIIIIIIIII  " + this.actionsQValues[actionIndex] + "  " + value);
            System.exit(112);
        }
        this.actionsQValues[actionIndex] += value;
    }

    public boolean equalsTo(CentralizedQTableObj obj){
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