package com.maximus.dm.decentralizedmessenger;

import android.util.Log;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void networking() throws Exception {
        // Networking cannot exist without MainActivity
        MainActivity.Networking networking = new MainActivity().new Networking();
        assertEquals(4, 2 + 2);
    }
}