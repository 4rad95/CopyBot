
package com.my.copybot.model;

public class ExecutedOrder {
	
	private Long creationTime;
	private String symbol;
	private Double price;
	private String quantity;
	private Double quantityDouble;
	private Long closeTime;
	private Double closePrice;
	private Double initialStopLoss;
	private Double currentStopLoss;
	private String orderId;
	private Double priceAvg;
	private String type; // short or long

    public String getOrderId() {
        return orderId;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getQuantityDouble() {
        return quantityDouble;
    }

    public Double getPriceAvg() {
        return priceAvg;
    }

    public void setPriceAvg(Double priceAvg) {
        this.priceAvg = priceAvg;
    }

    public void setQuantityDouble(Double quantityDouble) {
        this.quantityDouble = quantityDouble;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
	public Long getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public String getQuantity() {
		return quantity;
	}
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	public Long getCloseTime() {
		return closeTime;
	}
	public void setCloseTime(Long closeTime) {
		this.closeTime = closeTime;
	}
	public Double getClosePrice() {
		return closePrice;
	}
	public void setClosePrice(Double closePrice) {
		this.closePrice = closePrice;
	}
	public Double getCurrentStopLoss() {
		return currentStopLoss;
	}
	public void setCurrentStopLoss(Double currentStopLoss) {
		this.currentStopLoss = currentStopLoss;
	}
	public Double getProfit() {
		if (type.equals("LONG")) {
			return Double.valueOf(quantity) * (closePrice - price);
		} else {
			return Double.valueOf(quantity) * (price - closePrice);
		}
	}
	public Double getInitialStopLoss() {
		return initialStopLoss;
	}
	public void setInitialStopLoss(Double initialStopLoss) {
		this.initialStopLoss = initialStopLoss;
	}
	public Boolean trailingStopShouldCloseOrder(Double currentPrice) {
        return currentPrice < currentStopLoss;
    }
	public String getCurrentProfit(Double currentPrice) {

		if (type.equals("LONG")) {
			Double profitPercentage = ((currentPrice - price) / price) * 100 * 20;
			return String.format("%.2f", profitPercentage);
		} else {
			Double profitPercentage = ((price - currentPrice) / price) * 100 * 20;
			return String.format("%.2f", profitPercentage);
		}

	}
	
	@Override
	public String toString() {
		return "ExecutedOrder [creationTime=" + creationTime + ", symbol="
				+ symbol + ", price=" + price + ", quantity=" + quantity
				+ ", closeTime=" + closeTime + ", closePrice=" + closePrice
				+ ", initialStopLoss=" + initialStopLoss + ", currentStopLoss="
				+ currentStopLoss + "]";
	}
        

}
