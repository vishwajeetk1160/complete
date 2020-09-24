package com.smallcase.tradingportfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path="/portfolio")
public class TradeController {
	
	@Autowired TradeRepository tradeRepository;
	@Autowired PortfolioRepository portfolioRepository;

	@PostMapping(path="/add")
	public @ResponseBody ResponseEntity<String> add (@RequestParam String ticker
			, @RequestParam String tradeType
			, @RequestParam double price
			, @RequestParam int quantity) {
		try {
			String resp;
			validateTrade(tradeType, price, quantity);
			resp = executeTrade(ticker, quantity, price, tradeType);
			Trade trade = tradeRepository.save(new Trade(ticker, tradeType, price, quantity));
			return new ResponseEntity(resp + ":" + trade.getId(), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path="/update")
	public @ResponseBody ResponseEntity<String> update(@RequestParam int tradeId
			, @RequestParam String ticker
			, @RequestParam String tradeType
			, @RequestParam double price
			, @RequestParam int quantity) {
		try {
			validateTrade(tradeType, price, quantity);
			Trade trade = tradeRepository.findById(tradeId).orElse(null);
			if(trade==null) {
				throw new Exception("Invalid trade Id");
			}
			//reverse the original trade by reversing the original quantity
			executeTrade(trade.getTickerSymbol(), -trade.getNumberOfShares(), trade.getPrice(), trade.getTradeType());

			//try to do the new trade. If the trade fails undo the previous change
			try {
				executeTrade(ticker, quantity, price, tradeType);
			} catch (Exception e) {
				executeTrade(trade.getTickerSymbol(), trade.getNumberOfShares(), trade.getPrice(), trade.getTradeType());
				throw e;
			}
			trade.setTickerSymbol(ticker);
			trade.setTradeType(tradeType);
			trade.setPrice(price);
			trade.setNumberOfShares(quantity);
			tradeRepository.save(trade);
			return new ResponseEntity("SUCCESS:" + trade.getId(), HttpStatus.OK );
		} catch (Exception e) {
			return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/delete")
	public @ResponseBody ResponseEntity<String> delete(@RequestParam int tradeId) {
		try {
			Trade trade = tradeRepository.findById(tradeId).orElse(null);
			if(trade==null) {
				throw new Exception("Invalid trade Id");
			} else {
				executeTrade(trade.getTickerSymbol(), -trade.getNumberOfShares(), trade.getPrice(), trade.getTradeType());
				tradeRepository.delete(trade);
				return new ResponseEntity("SUCCESS", HttpStatus.OK);
			}
		} catch (Exception e) {
			return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/fetch")
	public @ResponseBody ResponseEntity<Map<String, PortfolioResp>> fetch() {
		Map<String, PortfolioResp> resp = fetchPortfolio(true);
		return new ResponseEntity(resp, HttpStatus.OK);
	}

	@GetMapping("/fetch/aggregate")
	public @ResponseBody ResponseEntity<Map<String, PortfolioResp>> fetchAggregate() {
		Map<String, PortfolioResp> resp = fetchPortfolio(false);
		return new ResponseEntity(resp, HttpStatus.OK);
	}

	@GetMapping("/fetch/returns")
	public @ResponseBody ResponseEntity<Long> fetchReturns() {
		double cashValue = 0;
		List<Portfolio> portfolios = (List) portfolioRepository.findAll();
		for (Portfolio portfolio : portfolios) {
			cashValue += (100*portfolio.getNumberOfShares() + portfolio.getCashAccStatus());
		}
		return new ResponseEntity(cashValue, HttpStatus.OK);
	}

	public String executeTrade(String ticker, int quantity, double price, String tradeType) throws Exception {
		if ("S".equals(tradeType)) {
			quantity *= -1; // To decrease it in the portfolio
		}
		Portfolio portfolio = portfolioRepository.findById(ticker).orElse(new Portfolio(ticker,0, 0));
		if (portfolio.getNumberOfShares()+quantity<0) {
			throw new Exception("Invalid trade");
		} else {
			if ("B".equals(tradeType) && portfolio.getNumberOfShares()+quantity!=0) {
				portfolio.setAveragePrice(((portfolio.getAveragePrice() * portfolio.getNumberOfShares()) + (price * quantity)) / (portfolio.getNumberOfShares() + quantity));
			}
			portfolio.setNumberOfShares(portfolio.getNumberOfShares() + quantity);
			portfolio.setCashAccStatus(portfolio.getCashAccStatus() - quantity*price);
			portfolioRepository.save(portfolio);
		}
		return "SUCCESS";
	}

	public Map<String, PortfolioResp> fetchPortfolio(boolean fetchDetailed) {
		Map<String, PortfolioResp> resp = new HashMap<>();
		List<Portfolio> portfolios = (List) portfolioRepository.findAll();
		for (Portfolio portfolio : portfolios) {
			if (portfolio.getNumberOfShares()==0) {
				continue;
			}
			List<Trade> trades = tradeRepository.findAllByTickerSymbol(portfolio.getTickerSymbol());

			PortfolioResp portfolioResp = new PortfolioResp();
			portfolioResp.setTicker(portfolio.getTickerSymbol());
			portfolioResp.setQuantity(portfolio.getNumberOfShares());
			portfolioResp.setAverageBuyPrice(portfolio.getAveragePrice());
			portfolioResp.setNetReturns(portfolio.getCashAccStatus() + portfolio.getNumberOfShares()*100);
			if(fetchDetailed)
				portfolioResp.setTrades(trades);

			resp.put(portfolio.getTickerSymbol(), portfolioResp);
		}
		return resp;
	}

	public void validateTrade(String tradeType, double price, int quantity) throws Exception {
		if(price <= 0 || quantity <= 0) {
			throw new Exception("Price and Quantity cannot be negative. Please ensure positive values in both.");
		}
		if(!"B".equals(tradeType) && !"S".equals(tradeType))
			throw new Exception("Invalid trade type. Trade Type should be 'B' or 'S'");
	}
}
