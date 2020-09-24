package com.smallcase.tradingportfolio;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PortfolioRepository extends CrudRepository<Portfolio, String> {
}
