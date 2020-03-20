package my.demo.service;

import java.util.ArrayList;

import my.demo.entity.Item;

public interface ItemService {
	ServiceResult<Item> getItem(int itemId);
	ServiceResult<ArrayList<Item>> findItem();
}