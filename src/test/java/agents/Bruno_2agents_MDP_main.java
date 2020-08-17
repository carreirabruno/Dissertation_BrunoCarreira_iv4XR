package agents;

import helperclasses.datastructures.Tuple;
import helperclasses.datastructures.Vec3;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;

public class Bruno_2agents_MDP_main {

    ArrayList<String[]> actions = new ArrayList<String[]>();
    ArrayList<String> singular_actions = new ArrayList<String>();
    ArrayList<String> existing_buttons = new ArrayList<String>();

    @BeforeAll
    static void start() {
        // Uncomment this to make the game's graphic visible:
//        TestSettings.USE_GRAPHICS = true;
//        String labRecruitsExeRootDir = System.getProperty("user.dir");
//        labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitsExeRootDir);
    }

    @AfterAll
    static void close() {
    }

    @Test
    public void run() throws Exception {

        // Set the possible buttons to act
        this.actions.add(new String[]{"null", "null"});
        this.actions.add(new String[]{"null", "button1"});
        this.actions.add(new String[]{"null", "button2"});
        this.actions.add(new String[]{"null", "button3"});
        this.actions.add(new String[]{"null", "button4"});
        this.actions.add(new String[]{"null", "button5"});
//        this.actions.add(new String[]{"null", "button6"});
        this.actions.add(new String[]{"button1", "null"});
        this.actions.add(new String[]{"button1", "button1"});
        this.actions.add(new String[]{"button1", "button2"});
        this.actions.add(new String[]{"button1", "button3"});
        this.actions.add(new String[]{"button1", "button4"});
        this.actions.add(new String[]{"button1", "button5"});
//        this.actions.add(new String[]{"button1", "button6"});
        this.actions.add(new String[]{"button2", "null"});
        this.actions.add(new String[]{"button2", "button1"});
        this.actions.add(new String[]{"button2", "button2"});
        this.actions.add(new String[]{"button2", "button3"});
        this.actions.add(new String[]{"button2", "button4"});
        this.actions.add(new String[]{"button2", "button5"});
//        this.actions.add(new String[]{"button2", "button6"});
        this.actions.add(new String[]{"button3", "null"});
        this.actions.add(new String[]{"button3", "button1"});
        this.actions.add(new String[]{"button3", "button2"});
        this.actions.add(new String[]{"button3", "button3"});
        this.actions.add(new String[]{"button3", "button4"});
        this.actions.add(new String[]{"button3", "button5"});
//        this.actions.add(new String[]{"button3", "button6"});
        this.actions.add(new String[]{"button4", "null"});
        this.actions.add(new String[]{"button4", "button1"});
        this.actions.add(new String[]{"button4", "button2"});
        this.actions.add(new String[]{"button4", "button3"});
        this.actions.add(new String[]{"button4", "button4"});
        this.actions.add(new String[]{"button4", "button5"});
//        this.actions.add(new String[]{"button4", "button6"});
        this.actions.add(new String[]{"button5", "null"});
        this.actions.add(new String[]{"button5", "button1"});
        this.actions.add(new String[]{"button5", "button2"});
        this.actions.add(new String[]{"button5", "button3"});
        this.actions.add(new String[]{"button5", "button4"});
        this.actions.add(new String[]{"button5", "button5"});
//        this.actions.add(new String[]{"button5", "button6"});
//        this.actions.add(new String[]{"button6", "null"});
//        this.actions.add(new String[]{"button6", "button1"});
//        this.actions.add(new String[]{"button6", "button2"});
//        this.actions.add(new String[]{"button6", "button3"});
//        this.actions.add(new String[]{"button6", "button4"});
//        this.actions.add(new String[]{"button6", "button5"});
//        this.actions.add(new String[]{"button6", "button6"});

        this.singular_actions.add("null");
        this.singular_actions.add("button1");
        this.singular_actions.add("button2");
        this.singular_actions.add("button3");
        this.singular_actions.add("button4");
        this.singular_actions.add("button5");
//        this.singular_actions.add("button6");

        this.existing_buttons.add("button1");
        this.existing_buttons.add("button2");
        this.existing_buttons.add("button3");
        this.existing_buttons.add("button4");
        this.existing_buttons.add("button5");
//        this.existing_buttons.add("button6");

        Tuple<ArrayList<Vec3>, ArrayList<Vec3>> positions = null;

//        String scenario = "scenario13";
//        String target1 = "button2";
//        String target2 = "button3";
//        String scenario = "scenario14";
//        String target1 = "button1";
//        String target2 = "button3";

//        String scenario = "scenario1";
//        String target1 = "button3";
//        String target2 = "button3";
//        String target3 = "button3";
//        String scenario = "scenario2";

//        String scenario = "scenario3";
//        String target1 = "button5";
//        String target2 = "button6";

//        String scenario = "scenario4";
//        String target1 = "button5";
//        String target2 = "button5";

        String scenario = "scenario5";
        String target1 = "button5";
        String target2 = "button5";


        centralizedTraining(scenario, target1, target2, this.actions, this.singular_actions, this.existing_buttons);
//        positions = centralizedTesting(scenario, target1, target2, this.actions, this.existing_buttons);

//        individualTraining(scenario, target1, target2, this.singular_actions, this.existing_buttons);
//        positions = individualTesting(scenario, target1, target2, this.singular_actions, this.existing_buttons);

//        saveToTXT("C:/Users/bruno/Desktop/Ambiente de Trabalho/" + scenario + "_individual_agent0.txt", positions.object1);
//        saveToTXT("C:/Users/bruno/Desktop/Ambiente de Trabalho/" + scenario + "_individual_agent1.txt", positions.object2);

    }

    public void centralizedTraining(String scenario, String target1, String target2, ArrayList<String[]> actions, ArrayList<String> singular_actions, ArrayList<String> existing_buttons) throws Exception {
        Bruno_2agents_centralized_MDP_train.start();
        Bruno_2agents_centralized_MDP_train centralized_train_new = new Bruno_2agents_centralized_MDP_train();
        centralized_train_new.create_policy_train(scenario, target1, target2, actions, singular_actions, existing_buttons);
        Bruno_2agents_centralized_MDP_train.close();

    }

    public Tuple<ArrayList<Vec3>, ArrayList<Vec3>> centralizedTesting(String scenario, String target1, String target2, ArrayList<String[]> actions, ArrayList<String> existing_buttons) throws Exception {
        Tuple<ArrayList<Vec3>, ArrayList<Vec3>> Positions = null;
        Bruno_2agents_centralized_MDP_test.start();
        Bruno_2agents_centralized_MDP_test centralized_test = new Bruno_2agents_centralized_MDP_test();
        Positions = centralized_test.run(scenario, target1, target2, actions, existing_buttons);
        Bruno_2agents_centralized_MDP_test.close();

        return Positions;
    }

    public void individualTraining(String scenario, String target1, String target2, ArrayList<String> singular_actions, ArrayList<String> existing_buttons) throws Exception {
        Bruno_2agents_individual_MDP_train.start();
        Bruno_2agents_individual_MDP_train individual_train_new = new Bruno_2agents_individual_MDP_train();
        individual_train_new.create_policy_train(scenario, target1, target2, singular_actions, existing_buttons);
        Bruno_2agents_individual_MDP_train.close();
    }

    public Tuple<ArrayList<Vec3>, ArrayList<Vec3>> individualTesting(String scenario, String target1, String target2, ArrayList<String> singular_actions, ArrayList<String> existing_buttons) throws Exception {
        Tuple<ArrayList<Vec3>, ArrayList<Vec3>> Positions = null;

        Bruno_2agents_individual_MDP_test.start();
        Bruno_2agents_individual_MDP_test individual_test = new Bruno_2agents_individual_MDP_test();
        Positions = individual_test.run(scenario, target1, target2, singular_actions, existing_buttons);
        Bruno_2agents_individual_MDP_test.close();


        return Positions;
    }

    public void saveToTXT(String filename, ArrayList<Vec3> list) throws IOException {
        FileWriter write = new FileWriter(filename, false);
        PrintWriter print_line = new PrintWriter(write);

        for (Vec3 pos : list)
            print_line.println(pos.toString());

        print_line.close();

    }

}
