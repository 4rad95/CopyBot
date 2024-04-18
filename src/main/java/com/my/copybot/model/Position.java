package com.my.copybot.model;

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

    @Override
    public String toString() {
        return "| " +
                " | " + new Date(creationTime) +
                " |  " + new Date(closeTime) +
                " | " + type +
                " | " + symbol +
                " | " + openPrice +
                " | " + closePrice +
                //       " | QTY " + quantity + '\'' +
                " | " + proffit +
                "  | ";
    }
}
