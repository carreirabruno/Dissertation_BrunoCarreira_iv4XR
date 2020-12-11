package agents;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import java.io.*;
import java.util.*;

import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;

public class Bruno_2agents_AnalyseUsers {

    String[] actions;
    String scenario;

    ArrayList<String[]> centralizedActions;
    String[] targetButtons;
    int[] doorsState;
    int[] buttonsState;
    ArrayList<String[]> initialMapMatrix;
    ArrayList<String[]> mapMatrix;
    ArrayList<String[]> connectionButtonsDoors;

    LinkedHashMap<Integer, DoorCentralizedQTableObj> QTable;

    LinkedHashMap<Integer, DoorCentralizedTransitionObj> TransitionTable;

    ArrayList<String> targetButtonsAlreadyClicked;

    @BeforeAll
    static void start() {}

    @AfterAll
    static void close(){}

    @Test
    public void run() {
        this.actions = new String[]{"Nothing", "Up", "Down", "Left", "Right", "Press"};

        readEmails();
    }

    public void readEmails() {
        String username = "bruno.dissertation@gmail.com";
        String password = "bruno.dissertation123";

        try {
            Properties properties = new Properties();
            properties.setProperty("mail.store.protocol", "imaps");
            Session emailSession = Session.getDefaultInstance(properties);

            Store emailStore = emailSession.getStore("imaps");
            emailStore.connect("imap.gmail.com", username, password);

            Folder emailFolder = emailStore.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            Message[] messages = emailFolder.getMessages();
            for (int i = messages.length-9; i < messages.length; i++) {
                System.out.println("Subject " + messages[i].getSubject());

                String subject =  messages[i].getSubject();
                subject = subject.replace("Dissertation Playtesting Results  +  ", "");

                String[] scenario = subject.split("_");
                this.scenario = scenario[0];

                setContentToList((MimeMultipart) messages[i].getContent());
                System.out.println();
            }

            emailFolder.close(false);
            emailStore.close();

        } catch (MessagingException | IOException e){
            e.printStackTrace();
        }
    }

    void setContentToList(MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        ArrayList<CompareObject> behaviouralTraces = new ArrayList<CompareObject>();
        String[] stringsContent = mimeMultipart.getBodyPart(0).getContent().toString().split("\n");
        for (String state : stringsContent){
            if (state.startsWith("<")) {
                CompareObject obj = treatStringState(state);
                if (behaviouralTraces.size() == 0 || !behaviouralTraces.get(behaviouralTraces.size()-1).equalTo(obj)) {
//                    if (behaviouralTraces.size() == 5) {
//                        System.out.println(Arrays.toString(behaviouralTraces.toArray()));
//                        for(CompareObject a: behaviouralTraces)
//                            System.out.println(a.toString());
//                        System.exit(5);
//                    }
//                    System.out.println(obj);
                    behaviouralTraces.add(obj);
                }
                else if(behaviouralTraces.get(behaviouralTraces.size()-1).equalTo(obj)){
                    behaviouralTraces.remove(behaviouralTraces.size()-1);
                    behaviouralTraces.add(obj);
                }
            }
        }

//        System.exit(123);

        comparePolicies(arrangeListWithActionCount(behaviouralTraces));
    }

    CompareObject treatStringState(String stringState){
        String temp = new String(stringState);

        temp = temp.replace(", ,",",");
        temp = temp.replace("<","");
        temp = temp.replace(">","");

        String[] varsPart1 = temp.split("\\),");

        String stringAgent0Pos = varsPart1[0].replace("(","");
        stringAgent0Pos = stringAgent0Pos.replace(", 0,","");
        String[] arrayAgent0Pos = stringAgent0Pos.split(" ");

        int[] agent0Pos = new int[]{Integer.parseInt(arrayAgent0Pos[1]), Integer.parseInt(arrayAgent0Pos[0])};



        String stringAgent1Pos = varsPart1[1].replace(" (","");
        stringAgent1Pos = stringAgent1Pos.replace(", 0,","");
        String[] arrayAgent1Pos = stringAgent1Pos.split(" ");
        int[] agent1Pos = new int[]{Integer.parseInt(arrayAgent1Pos[1]), Integer.parseInt(arrayAgent1Pos[0])};

        String[] varsPart2 = varsPart1[2].split("], ");

        String stringDoors = varsPart2[0].replace(" [","");
        String[] arrayDoors = stringDoors.split(", ");
        int[] doorState = new int[]{Integer.parseInt(arrayDoors[0]), Integer.parseInt(arrayDoors[1])};

        DoorCentralizedState state = new DoorCentralizedState(agent0Pos, agent1Pos, doorState.length);
        for (int i = 0; i < doorState.length; i++)
            if (doorState[i] == 1)
                state.changeDoorState(i+1);

        String stringActions = varsPart2[1].replace("(", "");
        stringActions = stringActions.replace(")", "");
        String[] actions = stringActions.split(", ");
        actions[1] = actions[1].substring(0, actions[1].length()-1);

        return new CompareObject(state, actions);
    }

    ArrayList<CompareObject> arrangeListWithActionCount(ArrayList<CompareObject> behaviouralTraces){

        ArrayList<CompareObject> finalList = new ArrayList<CompareObject>();
        ArrayList<Integer> hashList = new ArrayList<Integer>();


//        List<String> actionAgent0 = new ArrayList<String>();
//        List<String> actionAgent1 = new ArrayList<String>();

        String actionAgent0 = "";
        String actionAgent1 = "";

        String stringHash = "";
        int hash = 0;

        String tempStringHash = "";
        int tempHash = 0;

        for (CompareObject obj : behaviouralTraces){
            stringHash = "" + obj.state.agent0Pos[0] + obj.state.agent0Pos[1] + obj.state.agent1Pos[0] + obj.state.agent1Pos[1];
            for (int d : obj.state.doorsState)
                stringHash += "" + d;
            hash = Objects.hash(stringHash);

            for (CompareObject temp : behaviouralTraces){
                tempStringHash = "" + temp.state.agent0Pos[0] + temp.state.agent0Pos[1] + temp.state.agent1Pos[0] + temp.state.agent1Pos[1];
                for (int d : temp.state.doorsState)
                    tempStringHash += "" + d;
                tempHash = Objects.hash(tempStringHash);

                if (hash==tempHash){
                    actionAgent0 = temp.actions[0];
                    actionAgent1 = temp.actions[1];
//                    actionAgent0.add(temp.actions[0]);
//                    actionAgent1.add(temp.actions[1]);
                }
            }

//            String[] finalActions = new String[]{getActionBiggestCount(actionAgent0), getActionBiggestCount(actionAgent1)} ;
//
//            actionAgent0.clear();
//            actionAgent1.clear();

            CompareObject newObj = new CompareObject(obj.state, new String[]{actionAgent0, actionAgent1});
            if (hashList.size() == 0 || !hashList.contains(hash)) {
                finalList.add(newObj);
                hashList.add(hash);
            }
        }

//        runVisualize(finalList);
//        System.exit(4);

        return finalList;
    }

    String getActionBiggestCount(List<String> list){
        int index = -1;
        int[] actionsCount = new int[this.actions.length];

        for(int i = 0; i < list.size(); i++){

            for(int j = 0; j < this.actions.length; j++) {
                if (list.get(i).equals(this.actions[j]))
                    actionsCount[j] += 1;
            }
        }

        int maxValue = -1;
        for(int i = 0; i < actionsCount.length; i++)
            if (actionsCount[i] >= maxValue){
                maxValue= actionsCount[i];
                index = i;
            }

        return this.actions[index];
    }

    void comparePolicies(ArrayList<CompareObject> behaviouralTraces){
        Bruno_2agents_ComparePolicies.start();
        Bruno_2agents_ComparePolicies comparePolicies = new Bruno_2agents_ComparePolicies();
        comparePolicies.run(behaviouralTraces, "real" + this.scenario);
        Bruno_2agents_ComparePolicies.close();
    }

    void runVisualize(ArrayList<CompareObject> traces){
        setUpScenarioMatrix();
        resetMapMatrix();

        this.doorsState = new int[countApperancesOfWordOnInitialMap("door")];
        this.buttonsState = new int[countApperancesOfWordOnInitialMap("button")];
        this.targetButtonsAlreadyClicked = new ArrayList<String>();

        DoorCentralizedState currentState = new DoorCentralizedState(traces.get(0).state.agent0Pos, traces.get(0).state.agent1Pos, traces.get(0).state.doorsState.length);
        DoorCentralizedState nextState = null;

        int actionAgent0;
        int actionAgent1;
        DoorRewardRewardStateObject doorRewardRewardStateObject;


        int step = 0;
        for(CompareObject obj: traces){

            //Get action Agent0
            actionAgent0 = actionToIndex(obj.actions[0]);

            //Get action Agent1
            actionAgent1 = actionToIndex(obj.actions[1]);

            //Prints to see
            printInvertedMapMatrix();
            System.out.println(currentState.toString() + " " + Arrays.toString(obj.actions));
            System.out.println();


            //Act on map, get rewards and nextState
            doorRewardRewardStateObject = new DoorRewardRewardStateObject(actOnMap(currentState, actionAgent0, actionAgent1));
            nextState = doorRewardRewardStateObject.state;

            //Set new currentState
            currentState = new DoorCentralizedState(nextState);

            step++;
        }

        System.out.println("####Steps = " + step);

    }

    void resetMapMatrix() {
        this.mapMatrix = new ArrayList<String[]>();
        for (String[] a : this.initialMapMatrix)
            this.mapMatrix.add((String[]) a.clone());
    }

    void setUpScenarioMatrix() {

        this.initialMapMatrix = new ArrayList<String[]>();
        this.connectionButtonsDoors = new ArrayList<String[]>();

        String csvFile = "D:/GitHub/Tese_iv4XR_Pessoal/src/test/resources/levels/bruno_real" + this.scenario + ".csv";

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

        this.actions = new String[]{"Nothing", "Up", "Down", "Left", "Right", "Press"};
    }

    int countApperancesOfWordOnInitialMap(String word) {
        int count = 0;
        for (String[] a : this.initialMapMatrix)
            for (String b : a)
                if (b.contains(word))
                    count++;

        return count;
    }

    int actionToIndex(String action){
        for(int i = 0; i < this.actions.length; i++)
            if( this.actions[i].equals(action))
                return i;

        return -1;
    }

    void printInvertedMapMatrix() {
        for (int z = this.mapMatrix.size() - 1; z >= 0; z--) {
            for (int x = 0; x < this.mapMatrix.get(z).length; x++) {
                String temp = new String(this.mapMatrix.get(z)[x]);
                temp = temp.replace("gent", "");
                temp = temp.replace("utton", "");
                temp = temp.replace("oor", "");
                System.out.print(" " + temp + " ");
            }
            System.out.println();
        }
    }

    DoorRewardRewardStateObject actOnMap(DoorCentralizedState currentState, int actionAgent0, int actionAgent1) {
        DoorCentralizedState nextState = new DoorCentralizedState(currentState);
        int rewardAgent0 = 0;
        int rewardAgent1 = 0;
        int[] newPos;

        //Agent0
        if (this.actions[actionAgent0].equals("Press")) {
            if (this.mapMatrix.get(nextState.agent0Pos[0])[nextState.agent0Pos[1]].contains("button")) {
                String buttonToClick = new String(this.mapMatrix.get(nextState.agent0Pos[0])[nextState.agent0Pos[1]].substring(0, 7));
                this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1] = 1 ^ this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1];

                ArrayList<Integer> doorsToChange = getDoorsFromConnections(buttonToClick);
                for (Integer _door : doorsToChange)
                    nextState.changeDoorState(_door);

                openCloseDoor(buttonToClick);
            }
        } else if (!this.actions[actionAgent0].equals("Nothing")) {
            newPos = new int[]{nextState.agent0Pos[0], nextState.agent0Pos[1]};

            if (this.actions[actionAgent0].equals("Up"))
                newPos = new int[]{nextState.agent0Pos[0] + 1, nextState.agent0Pos[1]};
            else if (this.actions[actionAgent0].equals("Down"))
                newPos = new int[]{nextState.agent0Pos[0] - 1, nextState.agent0Pos[1]};
            else if (this.actions[actionAgent0].equals("Left"))
                newPos = new int[]{nextState.agent0Pos[0], nextState.agent0Pos[1] - 1};
            else if (this.actions[actionAgent0].equals("Right"))
                newPos = new int[]{nextState.agent0Pos[0], nextState.agent0Pos[1] + 1};
            if (checkCanMove(newPos[0], newPos[1])) {
                nextState.changeAgent0Pos(newPos[0], newPos[1]);
                changeMapMatrixAgentPositions("agent0", currentState.agent0Pos[0], currentState.agent0Pos[1], newPos[0], newPos[1]);
            }
        }

        //Agent1
        if (this.actions[actionAgent1].equals("Press")) {
            if (mapMatrix.get(nextState.agent1Pos[0])[nextState.agent1Pos[1]].contains("button")) {
                String buttonToClick = new String(this.mapMatrix.get(nextState.agent1Pos[0])[nextState.agent1Pos[1]].substring(0, 7));
                this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1] = 1 ^ this.buttonsState[Integer.parseInt(buttonToClick.substring(buttonToClick.length() - 1)) - 1];

                ArrayList<Integer> doorsToChange = getDoorsFromConnections(buttonToClick);
                for (Integer _door : doorsToChange)
                    nextState.changeDoorState(_door);

                openCloseDoor(buttonToClick);

            }
            else
                rewardAgent1--;
        } else if (!this.actions[actionAgent1].equals("Nothing")) {
            newPos = new int[]{nextState.agent1Pos[0], nextState.agent1Pos[1]};
            if (this.actions[actionAgent1].equals("Up"))
                newPos = new int[]{nextState.agent1Pos[0] + 1, nextState.agent1Pos[1]};
            else if (this.actions[actionAgent1].equals("Down"))
                newPos = new int[]{nextState.agent1Pos[0] - 1, nextState.agent1Pos[1]};
            else if (this.actions[actionAgent1].equals("Left"))
                newPos = new int[]{nextState.agent1Pos[0], nextState.agent1Pos[1] - 1};
            else if (this.actions[actionAgent1].equals("Right"))
                newPos = new int[]{nextState.agent1Pos[0], nextState.agent1Pos[1] + 1};
            if (checkCanMove(newPos[0], newPos[1])) {
                nextState.changeAgent1Pos(newPos[0], newPos[1]);

                changeMapMatrixAgentPositions("agent1", currentState.agent1Pos[0], currentState.agent1Pos[1], newPos[0], newPos[1]);
            }
            rewardAgent1--;
        }

        return new DoorRewardRewardStateObject(rewardAgent0, rewardAgent1, nextState);
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

    boolean checkCanMove(int z, int x) {
        return !mapMatrix.get(z)[x].equals("w") && !mapMatrix.get(z)[x].contains("door") && !mapMatrix.get(z)[x].contains("agent");
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

    ArrayList<Integer> getDoorsFromConnections(String realButton) {
        ArrayList<Integer> doors = new ArrayList<Integer>();
        for (String[] conn : this.connectionButtonsDoors) {
            if (conn[0].equals(realButton) && conn.length > 1)
                doors.add(Integer.parseInt(conn[1].substring(conn[1].length() - 1)));
        }

        return doors;
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

}

