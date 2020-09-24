package com.smallcase.tradingportfolio;

import lombok.Data;

import java.util.List;

@Data
public class PortfolioResp {

    private String ticker;
    private int quantity;
    private double averageBuyPrice;
    private double netReturns;
    private List<Trade> trades;
}
