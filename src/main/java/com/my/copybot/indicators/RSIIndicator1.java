package com.my.copybot.indicators;

import org.ta4j.core.Decimal;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;



public class RSIIndicator1 {

    private final ClosePriceIndicator closePrice;
    private final int barCount;

    public RSIIndicator1(ClosePriceIndicator closePrice, int barCount) {
        this.closePrice = closePrice;
        this.barCount = barCount;
    }

    public Decimal getValue(int index) {
        if (index < barCount) {
            return Decimal.ZERO;
        }

        Decimal sumGain = Decimal.ZERO;
        Decimal sumLoss = Decimal.ZERO;
        for (int i = index - barCount + 1; i <= index; i++) {
            Decimal priceDiff = closePrice.getValue(i).minus(closePrice.getValue(i - 1));
            if (priceDiff.isPositive()) {
                sumGain = sumGain.plus(priceDiff);
            } else {
                sumLoss = sumLoss.plus(priceDiff.abs());
            }
        }

        Decimal avgGain = sumGain.dividedBy(Decimal.valueOf(barCount));
        Decimal avgLoss = sumLoss.dividedBy(Decimal.valueOf(barCount));

        Decimal relativeStrength = avgGain.dividedBy(avgLoss);
        Decimal rsIndex = Decimal.ONE.plus(relativeStrength).dividedBy(Decimal.ONE.plus(relativeStrength)).multipliedBy(Decimal.valueOf(-1)).plus(Decimal.ONE);
        Decimal rsi = Decimal.HUNDRED.minus(rsIndex.multipliedBy(Decimal.HUNDRED));

        return rsi;
    }
}