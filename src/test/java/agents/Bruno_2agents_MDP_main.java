package agents;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class Bruno_2agents_MDP_main {

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

        /**
        Set "0" to test all Scenarios. Any other index means only that scenario. Example "2" -> Scenario2
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