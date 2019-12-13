package my.demo.service.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.config.annotation.Service;

import my.demo.domain.Item;
import my.demo.service.ItemService;
import my.demo.service.ServiceResult;
import my.demo.utils.Tracer;
 
@Service(cluster="failfast", retries=0, loadbalance="roundrobin", timeout=2000)
public class ItemServiceImpl implements ItemService {
	private static Map<Integer, Item> items = new HashMap<>();
	
	static {
		Item item = new Item();
		item.setId(10201);
		item.setTitle("小米电视4X/4S 65英寸PRO 4K超高清 HDR 蓝牙语音人工智能网络液晶平板电视");
		item.setPrice(2599);
		items.put(item.getId(), item);
		
		item = new Item();
		item.setId(20912);
		item.setTitle("华为(HUAWEI)MateBook 13 锐龙版 第三方Linux版 全面屏轻薄性能笔记本电脑 (AMD R5 8+512GB 2K 集显)银");
		item.setPrice(4099);
		items.put(item.getId(), item);
		
		item = new Item();
		item.setId(98143);
		item.setTitle("ECCO爱步2019秋冬新款休闲女鞋 运动户外女士鞋拼色低帮鞋平底 适动836283 辛芷蕾明星同款 褐色/森绿色/裸粉色/棕色");
		item.setPrice(1999);
		items.put(item.getId(), item);
		
		item = new Item();
		item.setId(65982);
		item.setTitle("五粮液股份公司出品 五粮精酿 礼鉴珍品 52度 浓香型白酒 500ml");
		item.setPrice(599);
		items.put(item.getId(), item);
		
		item = new Item();
		item.setId(30975);
		item.setTitle("西铁城(CITIZEN)手表 蓝天使男士手表石英光动能多局电波表运动腕表AT8020-54L");
		item.setPrice(3920);
		items.put(item.getId(), item);
	}

	@Override
	public ServiceResult<List<Item>> findItem() {
		return new ServiceResult<>(new ArrayList<>(items.values()));
	}

	@Override
	public ServiceResult<Item> getItem(int itemId) {
		Tracer.trace("itemId", itemId);
		Item item = items.get(itemId);
		if(item==null) {
			return new ServiceResult<Item>().fail("Item " + itemId + " not found");
		} else {
			return new ServiceResult<>(item);
		}
	}

}