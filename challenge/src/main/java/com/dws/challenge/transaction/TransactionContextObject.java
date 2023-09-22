package com.dws.challenge.transaction;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class TransactionContextObject<K, V> {
	@Getter
	private Map<K, V> savePoints = new HashMap<>();

	public Map<K, V> getSavePoints() {
		return savePoints;
	}

	public void setSavePoints(Map<K, V> savePoints) {
		this.savePoints = savePoints;
	}
}