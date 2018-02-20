package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public final class NamedList<V> implements Iterable<V> {
	/**
	 * возникает при попытке добавать уже сущеутвующий ключ
	 */
	@SuppressWarnings("serial")
	public static final class DuplicateKeyException extends Exception {
		public DuplicateKeyException() { super(); }
	}
	/**
	 * ключ не найден
	 */
	@SuppressWarnings("serial")
	public static final class KeyNotFoundException extends Exception {
		public KeyNotFoundException() { super(); }
	}
	
	private LinkedList<V> m_List = new LinkedList<>();
	private HashMap<String, Integer> m_Map = new HashMap<>();
	
	public final void add(V newValue) {
		int newIndex = m_List.size();
		while(m_Map.containsKey(Integer.toString(newIndex))) {
			++newIndex;
		}
		
		try {
			add(Integer.toString(newIndex), newValue);
		} catch (DuplicateKeyException e) {
			//не долно быть исключений
		}
	}
	
	public final void add(V[] newArrValue) {
		for(V newValue : newArrValue) { add(newValue); }
	}
	
	public final void add(String key, V newValue) throws DuplicateKeyException {
		if(m_Map.containsKey(key)) { throw new DuplicateKeyException(); }
		
		m_List.add(newValue);
		m_Map.put(key, m_List.size() - 1);
	}
	
	public final void add(Pair<String, V>[] newArrValue) throws DuplicateKeyException {
		for(Pair<String, V> newValue : newArrValue) { add(newValue.m_First, newValue.getSecond()); }
	}
	
	public final void insert(V newValue, int index) {
		int newIndex = m_List.size();
		while(m_Map.containsKey(Integer.toString(newIndex))) {
			++newIndex;
		}
		
		try {
			insert(Integer.toString(newIndex), newValue, newIndex);
		} catch (DuplicateKeyException e) {
			//не долно быть исключений
		}
	}
	
	public final void insert(String key, V newValue, int index) throws DuplicateKeyException {
		if(m_Map.containsKey(key)) { throw new DuplicateKeyException(); }
		
		m_List.add(index, newValue);
		
		Set<String> keys = m_Map.keySet();
		for(String itKey : keys) {
			int itIndex = m_Map.get(itKey);
			if(itIndex >= index) { m_Map.put(itKey, itIndex + 1); }
		}
		
		m_Map.put(key, index);
	}
	
	public final boolean containsKey(String key) {
		return m_Map.containsKey(key);
	}
	
	public final boolean containsValue(V value) {
		return m_List.contains(value);
	}
	
	public final V get(int index) {
		return m_List.get(index);
	}
	
	public final V get(String key) throws KeyNotFoundException {
		if(!m_Map.containsKey(key)) { throw new KeyNotFoundException(); }
		return m_List.get(m_Map.get(key));
	}
	
	public final void put(V newValue, int index) {
		m_List.set(index, newValue);
	}
	
	public final void put(V newValue, String key) throws KeyNotFoundException {
		if(!m_Map.containsKey(key)) { throw new KeyNotFoundException(); }
		m_List.set(m_Map.get(key), newValue);
	}
	
	public final int size() {
		return m_List.size();
	}
	
	public final void remove(int index) {
		m_List.remove(index);
		
		Set<String> keys = m_Map.keySet();
		for(String itKey : keys) {
			int itIndex = m_Map.get(itKey);
			if(index < itIndex) { m_Map.put(itKey, itIndex - 1); }
			if(itIndex == index) { m_Map.remove(itKey); }
		}
	}
	
	public final void remove(String key) throws KeyNotFoundException {
		if(!m_Map.containsKey(key)) { throw new KeyNotFoundException(); }
		
		int remIndex = m_Map.get(key);
		
		m_List.remove(remIndex);
		m_Map.remove(key);
		
		Set<String> keys = m_Map.keySet();
		for(String itKey : keys) {
			int index = m_Map.get(itKey);
			if(index > remIndex) { m_Map.put(itKey, index - 1); }
		}
	}
	
	public final void clear() {
		m_List.clear();
		m_Map.clear();
	}
	
	public final String[] getKeysSortedByIndex() {
		Set<String> keys = m_Map.keySet();
		ArrayList<Pair<String, Integer>> arr = new ArrayList<>(keys.size());
		String[] sortedKeys = new String[keys.size()];
		for(String key : keys) { arr.add(new Pair<>(key, m_Map.get(key))); }
		Collections.sort(arr, 
				(Pair<String, Integer> o1, Pair<String, Integer> o2) -> {
						return o1.getSecond() < o2.getSecond() ? -1 : 1;
					});
		for(int i = 0; i < arr.size(); ++i) { sortedKeys[i] = arr.get(i).getFirst(); }
		return sortedKeys;
	}

	@Override
	public Iterator<V> iterator() {
		return m_List.iterator();
	}
}
