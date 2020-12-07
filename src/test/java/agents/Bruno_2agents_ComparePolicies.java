package agents;

import com.google.gson.internal.$Gson$Preconditions;
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
    public void run(ArrayList<CompareObject> behaviouralTraces) {
        for (CompareObject a : behaviouralTraces)
            System.out.println(a.toString());
    }
}

