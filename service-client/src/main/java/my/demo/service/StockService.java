package my.demo.service;

import my.demo.domain.Stock;

public interface StockService {
	ServiceResult<Stock> getStock(int itemId);
	ServiceResult<Boolean> lock(int itemId, int lockQty);
}