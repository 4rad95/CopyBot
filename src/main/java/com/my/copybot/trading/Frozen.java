package com.my.copybot.trading;

import com.my.copybot.CopyBot;

import static java.lang.Thread.sleep;

public class Frozen implements Runnable {
    public Thread thisThread;
    int period;

    public Frozen(int period) {
        this.period = period;

    }

    @Override
    public void run() {
        while (true) {
            try {
                sleep(60000);
                System.out.println("Check frozzen position -  complete");
                CopyBot.modifyFrozenList();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
