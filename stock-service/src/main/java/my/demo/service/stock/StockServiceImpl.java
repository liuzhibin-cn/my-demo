package my.demo.service.stock;

import java.util.HashMap;
import java.util.Map;

import org.apache.dubbo.config.annotation.Service;

import my.demo.domain.Stock;
import my.demo.service.ServiceResult;
import my.demo.service.StockService;

@Service
public class StockServiceImpl implements StockService {
	private Map<Integer, Stock> stocks = new HashMap<>();

	@Override
	public ServiceResult<Stock> getStock(int itemId) {
		if(!stocks.containsKey(itemId)) {
			synchronized (stocks) {
				if(!stocks.containsKey(itemId)) {
					Stock stock = new Stock();
					stock.setItemId(itemId);
					stock.setTotalQty(1000000);
					stocks.put(itemId, stock);
				}
			}
		}
		return new ServiceResult<>(stocks.get(itemId));
	}

	@Override
	public ServiceResult<Boolean> lock(int itemId, int lockQty) {
		if(!stocks.containsKey(itemId)) {
			return new ServiceResult<Boolean>().fail("Item " + itemId + " not found"); 
		}
		if(lockQty<=0) {
			return new ServiceResult<Boolean>().fail("Illegal quantity " + lockQty + " to lock");
		}
		Stock stock = stocks.get(itemId);
		synchronized (stock) {
			if(stock.getAvailableQty() < lockQty) return new ServiceResult<>(false);
			stock.setLockQty(stock.getLockQty() + lockQty);
			return new ServiceResult<>(true);
		}
	}

}