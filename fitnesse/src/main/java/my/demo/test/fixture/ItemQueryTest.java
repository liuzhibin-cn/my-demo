package my.demo.test.fixture;

import java.util.List;

import fit.RowFixture;
import my.demo.entity.Item;
import my.demo.service.ItemService;
import my.demo.service.ServiceResult;
import my.demo.test.Manager;

public class ItemQueryTest extends RowFixture {
	@Override
	public Object[] query() throws Exception {
		ItemService service = Manager.getItemService();
		ServiceResult<List<Item>> serviceResult = service.findItem();
		if(!serviceResult.isSuccess()) return new Object[] {};
		Object[] result = new Object[serviceResult.getResult().size()];
		for(int i=0; i<serviceResult.getResult().size(); i++) {
			result[i] = serviceResult.getResult().get(i);
		}
		return result;
	}
	@Override
	public Class<?> getTargetClass() {
		return Item.class;
	}
}