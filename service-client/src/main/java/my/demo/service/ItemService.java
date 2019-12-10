package my.demo.service;

import java.util.List;

import my.demo.domain.Item;

public interface ItemService {
	ServiceResult<Item> getItem(int itemId);
	ServiceResult<List<Item>> findItem();
}