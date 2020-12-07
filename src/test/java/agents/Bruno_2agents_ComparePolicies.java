package agents;

import com.google.gson.internal.$Gson$Preconditions;
import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import world.BeliefState;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static agents.TestSettings.USE_SERVER_FOR_TEST;

public class Bruno_2agents_ComparePolicies {

    LinkedHashMap<Integer, DoorCentralizedQTableObj> CentralizedPolicy;
    LinkedHashMap<Integer, DoorIndividualQTableObj> IndividualPolicyAgent0;
    LinkedHashMap<Integer, DoorIndividualQTableObj> IndividualPolicyAgent1;

    ArrayList<String[]> centralizedActions;
    String[] actions;

    @BeforeAll
    static void start() {}

    @AfterAll
    static void close(){}

    @Test
    public void run(ArrayList<CompareObject> behaviouralTraces, String scenario) {

        getCentralizedPolicyFromFile("centralizedHashHash_" + scenario);
        setupCentralizedActions();

        this.IndividualPolicyAgent0 = getIndividualPolicyFromFile("individualHashHash_" + scenario + "_agent0");
        this.IndividualPolicyAgent1 = getIndividualPolicyFromFile("individualHashHash_" + scenario + "_agent1");

        float centCount = 0 ;
        float indCount = 0 ;

        //Analyse Behavioural Trace
        for (CompareObject obj : behaviouralTraces) {
            centCount += getVoteCentralized(obj)/behaviouralTraces.size();
            indCount += getVoteIndividual(obj)/behaviouralTraces.size();
        }

        System.out.println("Centralized " + String.format("%.2f", centCount) + " " + behaviouralTraces.size());
        System.out.println("Individual " + String.format("%.2f", indCount) + " " + behaviouralTraces.size());
    }

    float getVoteCentralized(CompareObject obj){
        float vote = 0;
        for (DoorCentralizedQTableObj temp : this.CentralizedPolicy.values()) {
            if (temp.state.equalsTo(obj.state)) {
                if (this.centralizedActions.get(temp.maxAction())[0].equals(obj.actions[0]))
                    vote += 0.5f;
                if (this.centralizedActions.get(temp.maxAction())[1].equals(obj.actions[1]))
                    vote += 0.5f;
            }
        }
        return vote;
    }

    float getVoteIndividual(CompareObject obj){
        float vote = 0;
        for (DoorIndividualQTableObj temp : this.IndividualPolicyAgent0.values()) {
            if (temp.state.equalsTo(obj.getIndividualStates()[0])) {
                if (this.actions[temp.maxAction()].equals(obj.actions[0]))
                    vote += 0.5f;
            }
        }
        for (DoorIndividualQTableObj temp : this.IndividualPolicyAgent1.values()) {
            if (temp.state.equalsTo(obj.getIndividualStates()[1])) {
                if (this.actions[temp.maxAction()].equals(obj.actions[1]))
                    vote += 0.5f;
            }
        }
        return vote;
    }

    void setupCentralizedActions() {
        this.actions = new String[]{"Nothing", "Up", "Down", "Left", "Right", "Press"};

        this.centralizedActions = new ArrayList<String[]>();
        for (String action_agent0 : this.actions)
            for (String action_agent1 : this.actions)
                this.centralizedActions.add(new String[]{action_agent0, action_agent1});
    }

    LinkedHashMap<Integer, DoorIndividualQTableObj> getIndividualPolicyFromFile(String filename) {
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

    void getCentralizedPolicyFromFile(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis);

            this.CentralizedPolicy = (LinkedHashMap<Integer, DoorCentralizedQTableObj>) in.readObject();

            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

