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
import java.util.Arrays;
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
//            centCount += getVoteCentralized(obj)/behaviouralTraces.size();
//            indCount += getVoteIndividual(obj)/behaviouralTraces.size();
            centCount += getVoteCentralizedEPOW(obj);
            indCount += getVoteIndividualEPOW(obj);
        }

        //TODO
        //Make normalized Sum
//        float total = centCount + indCount;
//        centCount = centCount/total;
//        indCount = indCount/total;

        centCount = (float) (centCount/(behaviouralTraces.size() * Math.exp(-1f/this.centralizedActions.size())));
        indCount = (float) (indCount/(behaviouralTraces.size() * Math.exp(- (1f/this.actions.length) * (1f/this.actions.length))));


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

    float getVoteCentralizedEPOW(CompareObject obj){
//        float vote = 0;
        for (DoorCentralizedQTableObj temp : this.CentralizedPolicy.values()) {
            if (temp.state.equalsTo(obj.state)) {
//                System.out.println(obj.state.toString() + "-" + Arrays.toString(obj.actions));
//                System.out.println(temp.state.toString() + "---" + Arrays.toString(this.centralizedActions.get(temp.maxAction())));
//                System.exit(999);
//                int[] values = getCentralizedActionIndexOrdered(temp.actionsQValues, obj.actions);
//                vote += Math.exp(-values[0]) + Math.exp(-values[1]);
                return (float) Math.exp(-getCentralizedActionIndexOrdered(temp.actionsQValues, obj.actions) / this.centralizedActions.size());
            }
        }

//        float denominator = 0;
//        for (int i = 1; i < this.actions.length+1; i++)
//            denominator += Math.exp(-i);

//        return vote/(denominator*2);

        return 0;
    }

    float getCentralizedActionIndexOrdered(float[] actionsQValues, String[] stringActions){

//        //Organize individual arrays of actions values from the centralized actions values
//        float[] actionsAgent0 = new float[this.actions.length];
//        float[] actionsAgent1 = new float[this.actions.length];
//
//        for (int i = 0; i < actionValues.length; i++){
//            for( int j = 0; j < this.actions.length; j++){
//                if (this.centralizedActions.get(i)[0].equals(this.actions[j]))
//                    actionsAgent0[j] += actionValues[i];
//                if (this.centralizedActions.get(i)[1].equals(this.actions[j]))
//                    actionsAgent1[j] += actionValues[i];
//            }
//        }
//
//        for(int v = 0; v < actionsAgent0.length; v++){
//            actionsAgent0[v] = actionsAgent0[v]/actionsAgent0.length;
//            actionsAgent1[v] = actionsAgent1[v]/actionsAgent1.length;
//        }
//
//        return new int[]{getIndividualActionIndexOrdered(actionsAgent0, actions[0]), getIndividualActionIndexOrdered(actionsAgent1, actions[1])};

        float[] tempActionValues = Arrays.copyOf(actionsQValues, actionsQValues.length);

        Arrays.sort(tempActionValues);

        float actionV = -1;

        for (int i = 0; i < actionsQValues.length; i++)
            if (Arrays.equals(stringActions, this.centralizedActions.get(i)))
                actionV = actionsQValues[i];

        for (int i = tempActionValues.length - 1; i >= 0; i--)
            if (actionV == tempActionValues[i])
                return tempActionValues.length - i;

        return -1;

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

    float getVoteIndividualEPOW(CompareObject obj){
        
//        float vote = 0;
//        for (DoorIndividualQTableObj temp : this.IndividualPolicyAgent0.values())
//            if (temp.state.equalsTo(obj.getIndividualStates()[0]))
//                vote += Math.exp(-getIndividualActionIndexOrdered(temp.actionsQValues, obj.actions[0]));
//
//        for (DoorIndividualQTableObj temp : this.IndividualPolicyAgent1.values())
//            if (temp.state.equalsTo(obj.getIndividualStates()[1]))
//                vote += Math.exp(-getIndividualActionIndexOrdered(temp.actionsQValues, obj.actions[1]));
//
//        float denominator = 0;
//        for (int i = 1; i < this.actions.length+1; i++)
//            denominator += Math.exp(-i);
//
//
//        return vote/(denominator*2);

        boolean returnVote = false;
        float vote = 1;
        for (DoorIndividualQTableObj temp : this.IndividualPolicyAgent0.values())
            if (temp.state.equalsTo(obj.getIndividualStates()[0])) {
                returnVote = true;
                vote *= getIndividualActionIndex(temp.actionsQValues, obj.actions[0]) / this.actions.length;
            }

        for (DoorIndividualQTableObj temp : this.IndividualPolicyAgent1.values())
            if (temp.state.equalsTo(obj.getIndividualStates()[1]))
                vote *= getIndividualActionIndex(temp.actionsQValues, obj.actions[1])/this.actions.length;

        if (returnVote)
            return (float) Math.exp(-vote);
        return 0;

    }

    float getIndividualActionIndex(float[] actionValues, String action){
        float[] temp = Arrays.copyOf(actionValues, actionValues.length);
        Arrays.sort(temp);

        float actionV = -1;

        for (int i = 0; i< actionValues.length; i++)
            if( this.actions[i].equals(action))
                actionV = actionValues[i];

        for(int i = temp.length - 1; i >= 0; i--)
            if (actionV == temp[i])
                return temp.length - i;

        return -1;
        
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

