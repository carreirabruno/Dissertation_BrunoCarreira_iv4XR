package agents;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import java.io.*;
import java.util.*;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

public class Bruno_2agents_AnalyseUsers {

    @BeforeAll
    static void start() {}

    @AfterAll
    static void close(){}

    @Test
    public void run() {

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
            for (int i = messages.length - 7; i < messages.length-6; i++) {
                System.out.println("Subject " + messages[i].getSubject());
                System.out.println("Date " + messages[i].getSentDate());
                setContentToList((MimeMultipart) messages[i].getContent());
            }

            emailFolder.close(false);
            emailStore.close();

        } catch (MessagingException | IOException e){
            e.printStackTrace();
        }
    }

    void setContentToList(MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        ArrayList<CompareObject> runStates = new ArrayList<CompareObject>();
        String[] stringsContent = mimeMultipart.getBodyPart(0).getContent().toString().split("\n");
        for (String state : stringsContent){
            if (state.startsWith("<")) {
                CompareObject obj = treatStringState(state);
//                System.out.println(obj.toString());
//                System.exit(123);

                if (runStates.size() == 0 || !runStates.get(runStates.size()-1).equal(obj)) {
                    runStates.add(obj);
                    System.out.println(state);
                }
            }
        }

        comparePolicies(runStates, "realScenario1");
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
        int[] agent0Pos = new int[]{Integer.parseInt(arrayAgent0Pos[0]), Integer.parseInt(arrayAgent0Pos[1])};

        String stringAgent1Pos = varsPart1[1].replace(" (","");
        stringAgent1Pos = stringAgent1Pos.replace(", 0,","");
        String[] arrayAgent1Pos = stringAgent1Pos.split(" ");
        int[] agent1Pos = new int[]{Integer.parseInt(arrayAgent1Pos[0]), Integer.parseInt(arrayAgent1Pos[1])};

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

        return new CompareObject(state, actions);
    }

    void comparePolicies(ArrayList<CompareObject> behaviouralTraces, String scenario){
        Bruno_2agents_ComparePolicies.start();
        Bruno_2agents_ComparePolicies comparePolicies = new Bruno_2agents_ComparePolicies();
        comparePolicies.run(behaviouralTraces, scenario);
        Bruno_2agents_ComparePolicies.close();
    }
}

