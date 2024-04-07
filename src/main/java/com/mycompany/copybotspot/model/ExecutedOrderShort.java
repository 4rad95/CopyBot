
package com.mycompany.copybotspot.model;

public class ExecutedOrderShort {
	
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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Double getQuantityDouble() {
        return quantityDouble;
    }

    public void setQuantityDouble(Double quantityDouble) {
        this.quantityDouble = quantityDouble;
    }

    public Double getPriceAvg() {
        return priceAvg;
    }

    public void setPriceAvg(Double priceAvg) {
        this.priceAvg = priceAvg;
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
		return Double.valueOf(quantity) * (price - closePrice);
	}
	public Double getInitialStopLoss() {
		return initialStopLoss;
	}
	public void setInitialStopLoss(Double initialStopLoss) {
		this.initialStopLoss = initialStopLoss;
	}
	public Boolean trailingStopShouldCloseOrder(Double currentPrice) {
		if(currentPrice < currentStopLoss) {
			return true;
		}
		return false;
	}
	public String getCurrentProfit(Double currentPrice) {
		Double profitPercentage = ((price -currentPrice )/price)*100*20;
		return String.format("%.2f", profitPercentage);
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
