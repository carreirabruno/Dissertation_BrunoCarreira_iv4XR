package agents;

import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import world.BeliefState;

import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static agents.TestSettings.USE_SERVER_FOR_TEST;

public class Bruno_2agents_ComparePolicies {


    @BeforeAll
    static void start() {}

    @AfterAll
    static void close(){}


    @Test
    public void run(ArrayList<String[]> pressedButtons_centralized, ArrayList<String[]> pressedButtons_individual) {

        ArrayList<String[]> _longestList = null;
        ArrayList<String[]> _shortestList = null;
        float _similarityValue = 0;

        if(pressedButtons_centralized.size() >= pressedButtons_individual.size()){
            _longestList = pressedButtons_centralized;
            _shortestList = pressedButtons_individual;
        }else{
            _longestList = pressedButtons_individual;
            _shortestList = pressedButtons_centralized;
        }

        for(int i = 0; i < _shortestList.size(); i++) {
            if (_shortestList.get(i)[0].equals(_longestList.get(i)[0]) && _shortestList.get(i)[1].equals(_longestList.get(i)[1]))
                _similarityValue++;
        }

        _similarityValue = _similarityValue/_longestList.size();

        System.out.println("SIMILARITY VALUE = " + _similarityValue);
    }
}

