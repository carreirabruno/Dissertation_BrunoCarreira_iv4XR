package agents;

import agents.tactics.GoalLib;
import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import helperclasses.datastructures.Vec3;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import world.BeliefState;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static agents.TestSettings.USE_SERVER_FOR_TEST;

public class Bruno_Manuel_1agent_MDP {

    private static LabRecruitsTestServer labRecruitsTestServer;

    ArrayList<String> actions = new ArrayList<String>();
    ArrayList<String> existing_buttons = new ArrayList<String>();

    ArrayList<QtableObject> Qtable = new ArrayList<QtableObject>();

    ArrayList<TransitionObject> TransitionTable = new ArrayList<TransitionObject>();

    ArrayList<QtableObject> RewardTable = new ArrayList<QtableObject>();

    ArrayList<Number> TimePerEpisode = new ArrayList<Number>();

    int episodes = 1001;

    double epsilon = 1;

    double learning_rate = 0.1;
    double gamma = 0.65;

    int max_time = 30;
    long best_time = max_time;

    int early_stop_counter_reset = 3;
    int early_stop_counter = early_stop_counter_reset;


    @BeforeAll
    static void start() throws Exception {
        // Uncomment this to make the game's graphic visible:
//        TestSettings.USE_GRAPHICS = true;
        String labRecruitsExeRootDir = System.getProperty("user.dir");
        labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitsExeRootDir);
    }

    @AfterAll
    static void close() {
        if (USE_SERVER_FOR_TEST) labRecruitsTestServer.close();
    }

    /**
     * Test that the agent can train in this scenario
     */
    @Test
    public void run() throws InterruptedException, IOException {

        System.out.println(LocalDateTime.now());

        // Set the possible actions
//        this.actions.add("null");
        this.actions.add("forward");
        this.actions.add("backward");
        this.actions.add("left");
        this.actions.add("right");

        this.existing_buttons.add("button0");
        this.existing_buttons.add("button1");


        String scenario_filename = "bruno_manuel_scenario1";
        boolean buttons = false;


        // Train
        for (int i = 0; i < episodes; i++) {
            System.out.println("Episode " + i + " of " + (episodes - 1) + " epsilon " + epsilon);

            var environment = new LabRecruitsEnvironment(new EnvironmentConfig(scenario_filename));

            // Create the agents
            var agent0 = new LabRecruitsTestAgent("agent0")
                    .attachState(new BeliefState())
                    .attachEnvironment(environment);
            agent0.setSamplingInterval(0);


            // press play in Unity
            if (!environment.startSimulation())
                throw new InterruptedException("Unity refuses to start the Simulation!");


            // set up the initial states
            agent0.update();
            QtableObject currentState_qtableObj = new QtableObject(new State(agent0, this.existing_buttons, buttons), this.actions);
            Qtable_add(this.Qtable, currentState_qtableObj);



            double reward = 0;
            int action = getNextActionIndex(this.Qtable, currentState_qtableObj.state);

            // Set initial goals to agents
            var g0 = doNextAction(agent0, action);
            agent0.setGoal(g0);

            QtableObject nextState_qtableObj = new QtableObject(new State(agent0, this.existing_buttons, buttons), this.actions);

            int stuckTicks = 0;

            long start = System.nanoTime();
            long lasTime = System.nanoTime();
            final double amountOfTicks = 5.0;  //update 5x per second
            double ns = 1000000000 / amountOfTicks;
            double delta = 0;

//            while(true){
            while (((System.nanoTime() - start) / 1_000_000_000) < max_time) {
                long now = System.nanoTime();
                delta += (now - lasTime) / ns;
                lasTime = now;

                if (delta >= 1) {
                    var button0 = agent0.getState().worldmodel.getElement("button0");
                    var goalButton = agent0.getState().worldmodel.getElement("button1");

                    if (g0.getStatus().inProgress()) {
                        if (action == 0)
                            reward -= 0;
                        else
                            reward -= 1;
                    }

                    // Agents
                    if (!g0.getStatus().inProgress() || stuckTicks >= (3 * amountOfTicks)) {
//                        System.out.println("action " + action);
//                        System.out.println(currentState_qtableObj);
//                        System.exit(56);

                        if (stuckTicks >= (3 * amountOfTicks))
                            System.out.println("agent0 stuck");
                        //Reward
                        if (goalButton != null && goalButton.getBooleanProperty("isOn"))
                            reward += 1000;

                        //Next state
                        nextState_qtableObj = new QtableObject(new State(agent0, this.existing_buttons, buttons), this.actions);
                        Qtable_add(this.Qtable, nextState_qtableObj);

                        //Q-learning
                        updateQtable(this.Qtable, currentState_qtableObj, nextState_qtableObj, action, reward);


//                        //Dyna-Q
//                        QtableObject _currentState_qtableObj = new QtableObject(currentState_qtableObj);
//                        QtableObject _nextState_qtableObj = new QtableObject(nextState_qtableObj);
//                        int _action = action;
//                        double _reward = reward;
//
//                        //          Update model
//                        //update T'[s,a,s']
//                        TransitionTable_update(this.TransitionTable, _currentState_qtableObj.state, _nextState_qtableObj.state, _action);
//
//                        //udpate R'[s,a]
//                        RewardTable_update(this.RewardTable, _currentState_qtableObj.state, _action, _reward);
//
//                        for (int j = 0; j < 50; j++) {
//
//                            //          Hallucinate
//                            //s = random
//                            _currentState_qtableObj = new QtableObject(this.TransitionTable.get(new Random().nextInt(this.TransitionTable.size())).currentState, this.actions);
//                            //a = random
//                            _action = getPossibleAction_TransitiontTable(this.TransitionTable, _currentState_qtableObj);
//                            //s' = infer from T[]
//                            _nextState_qtableObj = new QtableObject(getNextState_TransitionTable(this.TransitionTable, _currentState_qtableObj), this.actions);
//                            //r = infer from R[s',a]
//                            _reward = getReward_RewardTable(this.RewardTable, _currentState_qtableObj, _action);
//
//                            try {
//                                //          Q update
//                                updateQtable(this.Qtable, _currentState_qtableObj, _nextState_qtableObj, _action, _reward);
//                            } catch (Exception o) {
//                            }
//
//                        }


                        currentState_qtableObj = new QtableObject(nextState_qtableObj);

                        //Action
                        action = getNextActionIndex(this.Qtable, currentState_qtableObj.state);
                        g0 = doNextAction(agent0, action);
                        agent0.setGoal(g0);
                        g0.getStatus().resetToInProgress();
                        stuckTicks = 0;
//                        System.out.println(action);

                        reward = 0;
                    }
//                    if (button0 != null) {
//                        System.out.println("as " + button0.toString());
//                        System.out.println(button0.getBooleanProperty("isOn"));
//                    }
//
//                    System.out.println("lalalalalaalal");
                    // Check if the agents got stuck for too long
                    stuckTicks++;

                    // Check if the target button isOn to end the game
                    if (goalButton != null && goalButton.getBooleanProperty("isOn")) {
                        System.out.println("Objetive completed");
                        break;
                    }

                    try {
                        agent0.update();
                    } catch (Exception ignored) {
                    }

                    delta--;
                }
            }




            long episode_time = System.nanoTime() - start;
            System.out.println("Total time " + episode_time / 1_000_000_000);

            if ((i + 1) % 10 == 0)
                epsilon = 0;
            else
                epsilon = 1;

            if (i % 10 == 0 && i > 0) {
                long episodes_time_in_seconds = episode_time / 1_000_000_000;
                this.TimePerEpisode.add(episodes_time_in_seconds);
                System.out.println("added time");
                printQtable();
//                printTransitiontable();



                //Early stop
                if (episodes_time_in_seconds < best_time) {
                    best_time = episodes_time_in_seconds;
                    System.out.println("best time = " + best_time);
                    early_stop_counter = early_stop_counter_reset;
                }

                if (episodes_time_in_seconds <= best_time + 1 && best_time != max_time && best_time + 1 != max_time) {
                    early_stop_counter--;
                    System.out.println("Early stop counter = " + early_stop_counter);
                } else
                    early_stop_counter = early_stop_counter_reset;

                if (early_stop_counter == 0)
                    break;

            }

            if (!environment.close())
                throw new InterruptedException("Unity refuses to close the Simulation!");
        }

        if(buttons)
            savePoliciesToFile(this.Qtable, "Manuel_1agent_" + scenario_filename);
        else
            savePoliciesToFile(this.Qtable, "Manuel_1agent_" + scenario_filename + "noButtons");

    }

    public int getNextActionIndex(ArrayList<QtableObject> Qtable, State state) {
        int action_object_index = -1;
        if (new Random().nextDouble() > epsilon) {
            for (QtableObject qtableObject : Qtable)
                if (qtableObject.state.checkAllEquals(state))
                    action_object_index = getArgMax_double(qtableObject.actions);

        } else
            action_object_index = ThreadLocalRandom.current().nextInt(0, this.actions.size());

        return action_object_index;
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

    public void Qtable_add(ArrayList<QtableObject> Qtable, QtableObject qtableObject) {
        if (Qtable.size() == 0) {
            Qtable.add(new QtableObject(qtableObject));
        } else {
            boolean exists = false;
            for (QtableObject tempObject : Qtable) {
                if (tempObject.state.checkAllEquals(qtableObject.state)) {
                    exists = true;
                    break;
                }
            }
            if (!exists)
                Qtable.add(new QtableObject(qtableObject));
        }
    }

    public void printQtable() {
        for (QtableObject qtableObject : this.Qtable) {
            System.out.println(qtableObject.toString());
        }
    }

    public void printTransitiontable() {
        for (TransitionObject obj : this.TransitionTable) {
            System.out.println(obj.toString());
        }
    }

    public int getArgMax_double(double[] array) {
        double max = array[0];
        int maxIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    public double getMaxActionValue(ArrayList<QtableObject> Qtable, QtableObject qtableObject) {
        double maxValue = qtableObject.actions[0];
        for (QtableObject obj : Qtable) {
            if (obj.state.checkAllEquals(qtableObject.state)) {
                for (int i = 0; i < obj.actions.length; i++)
                    if (obj.actions[i] > maxValue)
                        maxValue = obj.actions[i];
                break;
            }
        }

        return maxValue;
    }

    public double getActionValue(ArrayList<QtableObject> Qtable, QtableObject qtableObject, int action) {
        for (QtableObject tempObject : Qtable) {
            if (qtableObject.state.checkAllEquals(tempObject.state))
                return tempObject.actions[action];
        }
        return 0;
    }

    public void updateQtableActionValue(ArrayList<QtableObject> Qtable, QtableObject qtableObject, int action, double value) {
        for (QtableObject tempObject : Qtable) {
            if (qtableObject.state.checkAllEquals(tempObject.state)) {
                tempObject.actions[action] = value;
                break;
            }
        }
    }

    public void savePoliciesToFile(ArrayList<QtableObject> Qtable, String filename) {
        // save the object to file
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);

            for (QtableObject obj : Qtable)
                out.writeObject(obj);

            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void updateQtable(ArrayList<QtableObject> Qtable, QtableObject currentState, QtableObject nextState, int action, double reward) {
//        qtable[state][action] = (1 - learning_rate) * qtable[state, action] + learning_rate * (reward + gamma * Nd4j.max(qtable[next_state,:]));

        double nextStateMaxActionValue = getMaxActionValue(Qtable, nextState);
        double part2 = learning_rate * (reward + gamma * nextStateMaxActionValue);
        double currentStateActionValue = getActionValue(Qtable, currentState, action);
        double part1 = (1 - learning_rate) * currentStateActionValue;
//        System.out.println("action " + action + "  nextStateAction Value " + nextStateMaxActionValue + "  currentStateACtionValue " + currentStateActionValue);
        updateQtableActionValue(Qtable, currentState, action, part1 + part2);
    }

    public void RewardTable_update(ArrayList<QtableObject> RewardTable, State currentState, int action, double reward) {
        if (RewardTable.size() == 0) {
            RewardTable.add(new QtableObject(currentState, this.actions));
            RewardTable.get(0).actions[action] = reward;
        } else {
            boolean exists = false;
            for (QtableObject tempObject : RewardTable) {
                if (tempObject.state.checkAllEquals(currentState)) {
                    tempObject.actions[action] = reward;
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                RewardTable.add(new QtableObject(currentState, this.actions));
                RewardTable.get(RewardTable.size() - 1).actions[action] = reward;
            }
        }
    }

    public double getReward_RewardTable(ArrayList<QtableObject> RewardTable, QtableObject currentState, int action) {
        for (QtableObject tempObj : RewardTable) {
            if (tempObj.state.checkAllEquals(currentState.state)) {
                return tempObj.actions[action];
            }
        }
        return -1;
    }

    public void TransitionTable_update(ArrayList<TransitionObject> TransitionTable, State currentState, State nextState, int action) {
        if (TransitionTable.size() == 0) {
            TransitionTable.add(new TransitionObject(currentState));
            TransitionTable.get(0).udpateTransitionList(new QtableObject(nextState, this.actions), action);
        } else {
            boolean exists = false;
            for (TransitionObject tempObject : TransitionTable) {
                if (tempObject.currentState.checkAllEquals(currentState)) {
                    tempObject.udpateTransitionList(new QtableObject(nextState, this.actions), action);
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                TransitionTable.add(new TransitionObject(currentState));
                TransitionTable.get(TransitionTable.size() - 1).udpateTransitionList(new QtableObject(nextState, this.actions), action);
            }
        }
    }

    public int getPossibleAction_TransitiontTable(ArrayList<TransitionObject> TransitionTable, QtableObject state) {
        ArrayList<Integer> experiencedActions = new ArrayList<Integer>();

        for (TransitionObject tempObj : TransitionTable) {
            if (tempObj.currentState.checkAllEquals(state.state)) {
                for (QtableObject nextState : tempObj.transitions) {
                    for (int i = 0; i < nextState.actions.length; i++) {
                        if (nextState.actions[i] != 0)
                            experiencedActions.add(i);
                    }
                }
            }
        }

        int randomIndex = new Random().nextInt(experiencedActions.size());
        return experiencedActions.get(randomIndex);

    }

    public State getNextState_TransitionTable(ArrayList<TransitionObject> TransitionTable, QtableObject currentState) {
        for (TransitionObject tempObj : TransitionTable) {
            if (tempObj.currentState.checkAllEquals(currentState.state)) {
                for (QtableObject nextState : tempObj.transitions) {
                    for (int i = 0; i < nextState.actions.length; i++) {
                        if (nextState.actions[i] != 0)
                            return nextState.state;
                    }
                }
            }
        }
        return null;
    }

}

class State implements Serializable {

    Vec3 agent_pos;
    ArrayList<Integer> button_states = new ArrayList<Integer>();

    public State(LabRecruitsTestAgent agent, ArrayList<String> buttons_list, boolean buttons) {
        //Agent pos
        if (agent.getState().worldmodel.position != null)
            this.agent_pos = new Vec3((int) agent.getState().worldmodel.position.x, (int) agent.getState().worldmodel.position.y, (int) agent.getState().worldmodel.position.z);
        else
            this.agent_pos = null;

        if (buttons) {
            //Set up the buttons state
            for (String button : buttons_list) {
                int button_state = -1;
                var e = agent.getState().worldmodel.getElement(button);

                if (e != null && e.getBooleanProperty("isOn"))
                    button_state = 1;
                else if (e != null && e.getBooleanProperty("isOff"))
                    button_state = 0;

                button_states.add(button_state);
            }
        }
    }

    public State(Vec3 agent_pos, ArrayList<Integer> button_states, boolean buttons) {
        this.agent_pos = agent_pos;
        if (buttons)
            this.button_states = button_states;
    }

    public State(State state) {
        this.agent_pos = state.agent_pos;
        this.button_states = state.button_states;
    }

    public void print_currentState() {
        System.out.println("Agent_pos: " + agent_pos);
        for (int i = 0; i < button_states.size(); i++)
            System.out.println("Button" + i + " state: " + button_states.get(i));
    }

    @Override
    public String toString() {
        if (this.agent_pos == null)
            return "null, " + button_states.toString();
        else
            return this.agent_pos.toString() + ", " + button_states.toString();
    }

    public boolean checkAllEquals(State anotherState) {
        return ((this.agent_pos == null && anotherState.agent_pos == null ||
                (this.agent_pos != null && anotherState.agent_pos != null &&
                        this.agent_pos.equals(anotherState.agent_pos))) &&
                this.button_states.equals(anotherState.button_states));
    }

}

class QtableObject implements Serializable {

    State state;
    double[] actions;

    public QtableObject(State state, ArrayList<String> actions) {
        this.state = state;
        this.actions = new double[actions.size()];
    }

    public QtableObject(QtableObject obj) {
        this.state = obj.state;
        this.actions = obj.actions;
    }

    public QtableObject(QtableObject obj, boolean empty) {
        this.state = obj.state;
        this.actions = new double[obj.actions.length];
    }

    public void printQtableObject() {
        System.out.println("+++ State_Actions +++");
        state.print_currentState();
        for (int i = 0; i < actions.length; i++)
            System.out.println("Action " + i + " = " + actions[i]);
    }

    @Override
    public String toString() {
//        return "QtableObject {State=" + state.toString() + ", Actions=" + actions.toString() + "}";
        return "QtableObject {State=(" + state.toString() + ") , Actions(" + Arrays.toString(actions) + ")}";
    }

}

class TransitionObject {

    State currentState;
    ArrayList<QtableObject> transitions = new ArrayList<QtableObject>();

    public TransitionObject(State currentState) {
        this.currentState = currentState;
    }

    public void udpateTransitionList(QtableObject nextState, int action) {
//        System.out.println(nextState + "  aa  " + action);
        boolean exists = false;
        for (QtableObject state : transitions) {
            if (state.state.checkAllEquals(nextState.state)) {
                state.actions[action] = 1;
                exists = true;
                break;
            }
        }
        if (!exists) {
            transitions.add(new QtableObject(nextState, true));
            transitions.get(transitions.size() - 1).actions[action] = 1;
        }
    }

    @Override
    public String toString() {
//        return "QtableObject {State=" + state.toString() + ", Actions=" + actions.toString() + "}";
        return "TransitionTable {State=(" + currentState.toString() + ") , Transitions(" + transitions.toString() + ")}";
    }

}
