package com.my.copybot.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Position {
    private final Long creationTime;
    private final Long closeTime;
    private final String type; // short or long
    private final String symbol;
    private final Double openPrice;
    private final Double closePrice;
    private final String quantity;   // ?
    private final Double proffit;
    private final String status;

    public Position(Long creationTime, Long closeTime, String type, String symbol, Double openPrice, Double closePrice, String quantity, Double proffit, String status) {
        this.creationTime = creationTime;
        this.closeTime = closeTime;
        this.type = type;
        this.symbol = symbol;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.quantity = quantity;
        this.proffit = proffit;
        this.status = status;
    }

    public void printPosition() {


        String item = "|";
        Instant instant = Instant.ofEpochMilli(creationTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        ZoneId zoneId = ZoneId.systemDefault();
        String formattedDate = formatter.withZone(zoneId).format(instant);
        item = item + formattedDate + " | ";
        int seconds = (int) ((closeTime - creationTime) / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;
        minutes = minutes - hours * 60;
        seconds = seconds - minutes * 60 - hours * 3600;
        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        item = item + formattedTime + "  | " +
                formatStr(type, 5) + " | " +
                formatStr(symbol, 10) + "    | " +
                formatStr(openPrice.toString(), 10) + "     | " +
                formatStr(closePrice.toString(), 10) + "     | ";
        String proffitStr = proffit.toString();
        if (proffit > 0) {
            proffitStr = " " + proffitStr;
        }
        System.out.println(item + proffitStr);


    }

    private String formatStr(String str, int length) {
        while (str.length() < length) {
            str += " ";
        }
        return str;
    }

    @Override
    public String toString() {
        return
                " | " + new Date(creationTime) +
                " |  " + new Date(closeTime) +
                " | " + type +
                " | " + symbol +
                " | " + openPrice +
                " | " + closePrice +
                " | " + proffit +
                "  | ";
    }
}
