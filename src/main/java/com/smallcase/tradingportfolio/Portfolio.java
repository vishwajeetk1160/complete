package com.smallcase.tradingportfolio;


import lombok.*;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name="portfolio")
public class Portfolio {
    @Id
    @Getter
    @Setter
    private String tickerSymbol;

    private double averagePrice;

    private int numberOfShares;

    private double cashAccStatus;

    public Portfolio(String tickerSymbol,double averagePrice, int numberOfShares) {
        this.tickerSymbol = tickerSymbol;
        this.averagePrice = averagePrice;
        this.numberOfShares = numberOfShares;
        this.cashAccStatus = -averagePrice*numberOfShares;
    }
}
