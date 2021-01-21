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

    ArrayList<ScenarioObject> scenarios = new ArrayList<ScenarioObject>();

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


        /**
        Set "0" to test all Scenarios. Any other index means only that scenario. Example 2 -> Scenario2
         */
        setupScenarioList(3);
        for (ScenarioObject obj : this.scenarios) {
            System.out.println(obj.scenario);
//            System.out.println("--Centralized agents--");
//            hashHash(true, false, obj.scenario, obj.targetButtons);
            System.out.println("--Individual agents--");
            hashHash(false, false, obj.scenario, obj.targetButtons);
            System.out.println();
        }

//        analyseUsers();

//        manualControl(scenario);
    }


    void setupScenarioList(int onlyOne){

        this.scenarios.add(new ScenarioObject("realScenario1", new String[]{"button4"}));
        this.scenarios.add(new ScenarioObject("realScenario2", new String[]{"button3", "button4"}));
        this.scenarios.add(new ScenarioObject("realScenario3", new String[]{"button3"}));

        if (onlyOne != 0) {
            ScenarioObject temp = new ScenarioObject(this.scenarios.get(onlyOne-1).scenario, this.scenarios.get(onlyOne-1).targetButtons);
            this.scenarios.clear();
            this.scenarios.add(temp);
        }
    }

    public void manualControl(String scenario) throws Exception {
        Bruno_2agents_manualControl.start();
        Bruno_2agents_manualControl individual_train_new = new Bruno_2agents_manualControl();
        individual_train_new.create_policy_manually(scenario);
        Bruno_2agents_manualControl.close();
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

    public void analyseUsers() throws Exception {
        Bruno_2agents_AnalyseUsers.start();
        Bruno_2agents_AnalyseUsers analyseUsers = new Bruno_2agents_AnalyseUsers();
        analyseUsers.run();
        Bruno_2agents_AnalyseUsers.close();
    }



}

class ScenarioObject{
    String scenario;
    String[] targetButtons;

    public ScenarioObject(String scenario, String[] targetButtons){
        this.scenario = scenario;
        this.targetButtons = targetButtons;
    }
}