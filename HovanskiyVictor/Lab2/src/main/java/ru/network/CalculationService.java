package ru.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author victor
 */
public class CalculationService {
    private static CalculationService instance;
    private String data;

    public CalculationService() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/pi.dat")))) {
            this.data = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CalculationService instance() {
        if (instance == null) {
            instance = new CalculationService();
        }
        return instance;
    }

    public String nextNumbers(int i, int count) {
        assert i >= 0;
        assert count > 0;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return data.substring(i, i + count);
    }
}
