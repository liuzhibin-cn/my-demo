package my.demo.service.stock;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.config.annotation.Service;

import my.demo.domain.Stock;
import my.demo.service.ServiceResult;
import my.demo.service.StockService;
import my.demo.utils.Tracer;

@Service(cluster="failfast", retries=0, loadbalance="roundrobin", timeout=2000)
public class StockServiceImpl implements StockService {
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	private Map<Integer, Stock> stocks = new HashMap<>();

	@Override
	public ServiceResult<Stock> getStock(int itemId) {
		Tracer.trace("itemId", itemId);
		if(!stocks.containsKey(itemId)) {
			synchronized (stocks) {
				if(!stocks.containsKey(itemId)) {
					Stock stock = new Stock();
					stock.setItemId(itemId);
					stock.setTotalQty(500);
					stocks.put(itemId, stock);
				}
			}
		}
		Stock stock = stocks.get(itemId);
		if(stock==null) {
			log.info("[get] Stock not found, item-id: " + itemId);
			return new ServiceResult<>(null);
		}
		if(log.isDebugEnabled()) {
			log.debug("[get] item-id: " + itemId + ", available: " + stock.getAvailableQty() + ", lock: " + stock.getLockQty());
		}
		return new ServiceResult<>(stock);
	}

	@Override
	public ServiceResult<Boolean> lock(int itemId, int lockQty) {
		Tracer.trace("itemId", itemId);
		Tracer.trace("lockQty", lockQty);
		if(!stocks.containsKey(itemId)) {
			log.info("[lock] Stock not found, item-id: " + itemId);
			return new ServiceResult<Boolean>().fail("Item " + itemId + " not found"); 
		}
		if(lockQty<=0) {
			return new ServiceResult<Boolean>().fail("Illegal quantity " + lockQty + " to lock");
		}
		
		//随机休眠一段时间模拟服务耗时
		try {
			long time = new Random(System.currentTimeMillis()).nextInt(200);
			Thread.sleep(time);
		} catch (InterruptedException e) {}
		
		//锁定库存
		Stock stock = stocks.get(itemId);
		synchronized (stock) {
			if(stock.getAvailableQty() < lockQty) {
				log.info("[lock] Failed, item-id: " + itemId + ", avalaible: " + stock.getAvailableQty() + ", request: " + lockQty);
				return new ServiceResult<>(false);
			}
			log.info("[lock] Success, item-id: " + itemId + ", avalaible: " + stock.getAvailableQty() + ", request: " + lockQty);
			stock.setLockQty(stock.getLockQty() + lockQty);
			return new ServiceResult<>(true);
		}
	}

} 