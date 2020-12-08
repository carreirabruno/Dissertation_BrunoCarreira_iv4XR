package agents;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class Bruno_2agents_MDP_main {

//    ArrayList<String[]> actions = new ArrayList<String[]>();
//    ArrayList<String> singular_actions = new ArrayList<String>();
//    ArrayList<String> existing_buttons = new ArrayList<String>();
//
//    ArrayList<String[]> pressedButtons_centralized = new ArrayList<String[]>();
//    ArrayList<String[]> pressedButtons_individual = new ArrayList<String[]>();

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

        /*
        // Set the possible buttons to act
//        this.actions.add(new String[]{"null", "null"});
//        this.actions.add(new String[]{"null", "button1"});
//        this.actions.add(new String[]{"null", "button2"});
//        this.actions.add(new String[]{"null", "button3"});
//        this.actions.add(new String[]{"null", "button4"});
//        this.actions.add(new String[]{"null", "button5"});
//        this.actions.add(new String[]{"null", "button6"});
//        this.actions.add(new String[]{"button1", "null"});
//        this.actions.add(new String[]{"button1", "button1"});
//        this.actions.add(new String[]{"button1", "button2"});
//        this.actions.add(new String[]{"button1", "button3"});
//        this.actions.add(new String[]{"button1", "button4"});
//        this.actions.add(new String[]{"button1", "button5"});
//        this.actions.add(new String[]{"button1", "button6"});
//        this.actions.add(new String[]{"button2", "null"});
//        this.actions.add(new String[]{"button2", "button1"});
//        this.actions.add(new String[]{"button2", "button2"});
//        this.actions.add(new String[]{"button2", "button3"});
//        this.actions.add(new String[]{"button2", "button4"});
//        this.actions.add(new String[]{"button2", "button5"});
//        this.actions.add(new String[]{"button2", "button6"});
//        this.actions.add(new String[]{"button3", "null"});
//        this.actions.add(new String[]{"button3", "button1"});
//        this.actions.add(new String[]{"button3", "button2"});
//        this.actions.add(new String[]{"button3", "button3"});
//        this.actions.add(new String[]{"button3", "button4"});
//        this.actions.add(new String[]{"button3", "button5"});
//        this.actions.add(new String[]{"button3", "button6"});
//        this.actions.add(new String[]{"button4", "null"});
//        this.actions.add(new String[]{"button4", "button1"});
//        this.actions.add(new String[]{"button4", "button2"});
//        this.actions.add(new String[]{"button4", "button3"});
//        this.actions.add(new String[]{"button4", "button4"});
//        this.actions.add(new String[]{"button4", "button5"});
//        this.actions.add(new String[]{"button4", "button6"});
//        this.actions.add(new String[]{"button5", "null"});
//        this.actions.add(new String[]{"button5", "button1"});
//        this.actions.add(new String[]{"button5", "button2"});
//        this.actions.add(new String[]{"button5", "button3"});
//        this.actions.add(new String[]{"button5", "button4"});
//        this.actions.add(new String[]{"button5", "button5"});
//        this.actions.add(new String[]{"button5", "button6"});
//        this.actions.add(new String[]{"button6", "null"});
//        this.actions.add(new String[]{"button6", "button1"});
//        this.actions.add(new String[]{"button6", "button2"});
//        this.actions.add(new String[]{"button6", "button3"});
//        this.actions.add(new String[]{"button6", "button4"});
//        this.actions.add(new String[]{"button6", "button5"});
//        this.actions.add(new String[]{"button6", "button6"});

//        this.singular_actions.add("null");
//        this.singular_actions.add("button1");
//        this.singular_actions.add("button2");
//        this.singular_actions.add("button3");
//        this.singular_actions.add("button4");
//        this.singular_actions.add("button5");
//        this.singular_actions.add("button6");

//        this.existing_buttons.add("button1");
//        this.existing_buttons.add("button2");
//        this.existing_buttons.add("button3");
//        this.existing_buttons.add("button4");
//        this.existing_buttons.add("button5");
//        this.existing_buttons.add("button6");

         */


        String scenario = "realScenario2";
        String[] targetButtons = new String[]{"button3", "button4"};

        hashHash(false, false, scenario, targetButtons);

//        manualControl(scenario);
    }

    public void centralizedTraining(String scenario, String target1, String target2, ArrayList<String[]> actions, ArrayList<String> singular_actions, ArrayList<String> existing_buttons) throws Exception {
        Bruno_2agents_centralized_MDP_train.start();
        Bruno_2agents_centralized_MDP_train centralized_train_new = new Bruno_2agents_centralized_MDP_train();
        centralized_train_new.create_policy_train(scenario, target1, target2, actions, singular_actions, existing_buttons);
        Bruno_2agents_centralized_MDP_train.close();

    }

    public void centralizedTesting_pressedButtons(String scenario, String target1, String target2, ArrayList<String[]> actions, ArrayList<String> existing_buttons) throws Exception {
//        Bruno_2agents_centralized_MDP_test.start();
//        Bruno_2agents_centralized_MDP_test centralized_test = new Bruno_2agents_centralized_MDP_test();
//        centralized_test.run(scenario, target1, target2, actions, existing_buttons);
//        Bruno_2agents_centralized_MDP_test.close();

        Bruno_2agents_singular_centralized_MDP_test.start();
        Bruno_2agents_singular_centralized_MDP_test centralized_test = new Bruno_2agents_singular_centralized_MDP_test();
        centralized_test.run(scenario, target1, target2, actions, existing_buttons);
        Bruno_2agents_singular_centralized_MDP_test.close();

    }

    public void individualTraining(String scenario, String target1, String target2, ArrayList<String> singular_actions, ArrayList<String> existing_buttons) throws Exception {
        Bruno_2agents_individual_MDP_train.start();
        Bruno_2agents_individual_MDP_train individual_train_new = new Bruno_2agents_individual_MDP_train();
        individual_train_new.create_policy_train(scenario, target1, target2, singular_actions, existing_buttons);
        Bruno_2agents_individual_MDP_train.close();
    }

    public void individualTesting_pressedButtons(String scenario, String target1, String target2, ArrayList<String> singular_actions, ArrayList<String> existing_buttons) throws Exception {
//        Bruno_2agents_individual_MDP_test.start();
//        Bruno_2agents_individual_MDP_test individual_test = new Bruno_2agents_individual_MDP_test();
//        pressedButtons_individual = individual_test.run(scenario, target1, target2, singular_actions, existing_buttons);
//        Bruno_2agents_individual_MDP_test.close();

        Bruno_2agents_singular_individual_MDP_test.start();
        Bruno_2agents_singular_individual_MDP_test individual_test = new Bruno_2agents_singular_individual_MDP_test();
        individual_test.run(scenario, target1, target2, singular_actions, existing_buttons);
        Bruno_2agents_singular_individual_MDP_test.close();
    }

    public void manualControl(String scenario) throws Exception {
        Bruno_2agents_manualControl.start();
        Bruno_2agents_manualControl individual_train_new = new Bruno_2agents_manualControl();
        individual_train_new.create_policy_manually(scenario);
        Bruno_2agents_manualControl.close();
    }

    public void lowLevelCentralizedTraining(String scenario, String[] targetButtons) throws Exception {
        Bruno_2agents_centralized_lowLevelActions_train.start();
        Bruno_2agents_centralized_lowLevelActions_train lowLevelActionsCentralized_train = new Bruno_2agents_centralized_lowLevelActions_train();
        lowLevelActionsCentralized_train.create_policy_train(scenario, targetButtons);
        Bruno_2agents_centralized_lowLevelActions_train.close();
    }

    public void hashCentralizedTraining(String scenario, String[] targetButtons) throws Exception {
        Bruno_2agents_centralized_Hash_train.start();
        Bruno_2agents_centralized_Hash_train HashCentralized_train = new Bruno_2agents_centralized_Hash_train();
        HashCentralized_train.create_policy_train(scenario, targetButtons);
        Bruno_2agents_centralized_Hash_train.close();
    }

    public void hashHash(boolean centralized, boolean train, String scenario, String[] targetButtons) throws Exception {
        if (centralized) {
            Bruno_2agents_centralized_HashHash.start();
            Bruno_2agents_centralized_HashHash hashHashCentralized = new Bruno_2agents_centralized_HashHash();
            hashHashCentralized.run(train, scenario, targetButtons);
            Bruno_2agents_centralized_HashHash.close();
        }else{
            Bruno_2agents_individual_HashHash.start();
            Bruno_2agents_individual_HashHash hashHashindividual = new Bruno_2agents_individual_HashHash();
            hashHashindividual.run(train, scenario, targetButtons);
            Bruno_2agents_individual_HashHash.close();
        }
    }

    public void lowLevelIndividualTraining(String scenario, String[] targetButtons) throws Exception {
        Bruno_2agents_individual_lowLevelActions_train.start();
        Bruno_2agents_individual_lowLevelActions_train lowLevelActionsIndividual_train = new Bruno_2agents_individual_lowLevelActions_train();
        lowLevelActionsIndividual_train.create_policy_train(scenario, targetButtons);
        Bruno_2agents_individual_lowLevelActions_train.close();
    }

    public void lowLevelCentralizedTesting(String scenario, String[] targetButtons, boolean useLabrecruits) throws Exception {
        Bruno_2agents_centralized_lowLevelActions_test.start(useLabrecruits);
        Bruno_2agents_centralized_lowLevelActions_test lowLevelActionsCentralized_test = new Bruno_2agents_centralized_lowLevelActions_test();
        lowLevelActionsCentralized_test.test(scenario, targetButtons, useLabrecruits);
        Bruno_2agents_centralized_lowLevelActions_test.close(useLabrecruits);
    }

    public void hashIndividualTraining(String scenario, String[] targetButtons) throws Exception {
        Bruno_2agents_individual_Hash_train.start();
        Bruno_2agents_individual_Hash_train HashIndividual_train = new Bruno_2agents_individual_Hash_train();
        HashIndividual_train.create_policy_train(scenario, targetButtons);
        Bruno_2agents_individual_Hash_train.close();
    }

    public void lowLevelIndividualTesting(String scenario, String[] targetButtons, boolean useLabrecruits) throws Exception {
        Bruno_2agents_individual_lowLevelActions_test.start(useLabrecruits);
        Bruno_2agents_individual_lowLevelActions_test lowLevelActionsIndividual_test = new Bruno_2agents_individual_lowLevelActions_test();
        lowLevelActionsIndividual_test.test(scenario, targetButtons, useLabrecruits);
        Bruno_2agents_individual_lowLevelActions_test.close(useLabrecruits);
    }

}
