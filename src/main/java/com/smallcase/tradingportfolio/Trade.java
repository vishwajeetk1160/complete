package com.smallcase.tradingportfolio;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name="trade")
public class Trade {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    @Column(name = "Id")
    private int id;

    private String tickerSymbol;

    private String tradeType;

    private double price;

    private int numberOfShares;

    public Trade(String tickerSymbol, String tradeType, double price, int numberOfShares) {
        this.tickerSymbol = tickerSymbol;
        this.tradeType = tradeType;
        this.price = price;
        this.numberOfShares = numberOfShares;
    }
}
