package com.my.copybot;

import java.util.Scanner;

public class InputString implements Runnable {

    @Override
    public void run() {

        Scanner inputString = null;
        try {
            inputString = new Scanner(System.in);
            while (true) {
                String newString = inputString.nextLine().toUpperCase();
                if (newString == null) {
                    System.out.println(" Re-Enter bitter ");
                } else if (newString.length() == 0) {
                    System.out.println(" Re-Enter bitter ");
                } else if (newString.charAt(0) == '#') {
                    System.out.println("Вы ввели = " + newString);
                    CopyBot.codeInput(newString.substring(1));
                } else if (newString.charAt(0) == '?') {
                    System.out.println("\u001B[33m Add functions for console input: ");
                    System.out.println(" #AS?????USDT - add short position, for example: #ASBTCUSDT - add short for pair BTCUSDT");
                    System.out.println(" #AL?????USDT - add short position, for example: #ALBTCUSDT - add long for pair BTCUSDT");
                    System.out.println(" #D?????USDT  - close  position, for example: #DBTCUSDT - close position for pair BTCUSDT");
                    System.out.println(" #STOP        - close all positions and open new position");
                    System.out.println(" #RL          - reload settings file ");
                    System.out.println(" #END         - close all position and wait \u001B[0m");
                }
            }

        } catch (Exception e) {
            System.out.println(" Re-Enter bitter ");

        }
    }
}
