package com.my.copybot;

import java.util.Scanner;

public class InputString implements Runnable {

    public static void printHelp() {

        System.out.println("\u001B[33m Add functions for console input: ");
        System.out.println(" #AS?????     - add short position, for example: #ASBTC or #asBtc  - add short for pair BTCUSDT");
        System.out.println(" #AL?????     - add short position, for example: #ALBTC or #aLbTc - add long for pair BTCUSDT");
        System.out.println(" #D?????      - close  position, for example: #DBTCUSDT or #dbTc- close position for pair BTCUSDT");
        System.out.println(" #STOP        - not open new positions and wait old position, after TC(1-...) continue working.");
        System.out.println(" #TC          - set max position , for example #TC5 set 5 open position maximum");
        System.out.println(" #OR          - set size open position, for example #OR30 -set position size equal 30 USDT");
        System.out.println(" #SL          - set percent STOP_LOSS, for example #SL2 - (2*20 Cross-level) - 40% exit price");
        System.out.println(" #SN          - set percent STOP_NO_LOSS, for example #SN22 - stop loss order change +22% proffit");
        System.out.println(" #END         - close all position and wait, after TC(1-...) continue working. ");
        System.out.println(" #CHK         - Output basic traid parametrs \u001B[0m");

    }

    @Override
    public void run() {
        while (true) {
        Scanner inputString = null;
        try {
            inputString = new Scanner(System.in);
            while (true) {
                String newString = inputString.nextLine().toUpperCase();
                if ((newString == null) || (newString.length() == 0) || (newString.charAt(0) == '?')) {
                    System.out.println("(? or (Enter) - for output help)");
                    printHelp();
                } else if (newString.charAt(0) == '#') {
                    System.out.println("Вы ввели = " + newString);
                    CopyBot.codeInput(newString.substring(1));
                } else {
                    System.out.println("(? or (Enter) - for output help)");
                }
            }
        } catch (Exception e) {
            System.out.println(" Re-Enter bitter ");
        }
        }
    }
}
