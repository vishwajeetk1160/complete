package com.smallcase.tradingportfolio;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TradeRepository extends CrudRepository<Trade, Integer> {//crudrepo has predefined methods and functions for querying DB
    List<Trade> findAllByTickerSymbol(String ticker);
}
