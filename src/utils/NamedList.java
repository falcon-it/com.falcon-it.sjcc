package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * список<br />
 * доступ к параметрам по имени и по индексу<br />
 * элементы проиндексированы по порядку их добавления<br />
 * при сериализации/десиализации индексация не меняется
 * 
 * @author Ilya Sokolov
 *
 * @param <V>
 */
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
	
	/**
	 * список с данными
	 */
	private LinkedList<V> m_List = new LinkedList<>();
	/**
	 * мап: ключ - имя элемента; значение - индекс m_List
	 */
	private HashMap<String, Integer> m_Map = new HashMap<>();
	
	/**
	 * добавить новый элемент<br />
	 * ключ сгенерировать по индексу
	 * @param newValue экземпляр данных
	 */
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
	
	/**
	 * добавить массив элементов
	 * @param newArrValue добавляемый массив элементов
	 */
	public final void add(V[] newArrValue) {
		for(V newValue : newArrValue) { add(newValue); }
	}
	
	/**
	 * добавить новый элемент
	 * @param key ключ
	 * @param newValue новой элемент
	 * @throws DuplicateKeyException
	 */
	public final void add(String key, V newValue) throws DuplicateKeyException {
		if(m_Map.containsKey(key)) { throw new DuplicateKeyException(); }
		
		m_List.add(newValue);
		m_Map.put(key, m_List.size() - 1);
	}
	
	/**
	 * добавить массив элементов
	 * @param newArrValue массив пар ключ-значение
	 * @throws DuplicateKeyException
	 */
	public final void add(Pair<String, V>[] newArrValue) throws DuplicateKeyException {
		for(Pair<String, V> newValue : newArrValue) { add(newValue.m_First, newValue.getSecond()); }
	}
	
	/**
	 * вставить новый элемент на указанную позицию
	 * @param newValue новый элемент
	 * @param index индекс в m_List
	 */
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
	
	/**
	 * вставить новый элемент на указанную позицию
	 * @param key ключ
	 * @param newValue новый элемент
	 * @param index индекс в m_List
	 * @throws DuplicateKeyException
	 */
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
	
	/**
	 * проверить существование элемента по ключу
	 * @param key ключ
	 * @return true если ключ существует
	 */
	public final boolean containsKey(String key) {
		return m_Map.containsKey(key);
	}
	
	/**
	 * проверить существование элемента
	 * @param value искомое значение
	 * @return true если элемент содержиться в m_list
	 */
	public final boolean containsValue(V value) {
		return m_List.contains(value);
	}
	
	/**
	 * получить элемент по индексу
	 * @param index индекс элемента
	 * @return значение
	 */
	public final V get(int index) {
		return m_List.get(index);
	}
	
	/**
	 * найти элемент по ключу
	 * @param key ключ
	 * @return значение
	 * @throws KeyNotFoundException
	 */
	public final V get(String key) throws KeyNotFoundException {
		if(!m_Map.containsKey(key)) { throw new KeyNotFoundException(); }
		return m_List.get(m_Map.get(key));
	}
	
	/**
	 * заменить значение элемента по индексу
	 * @param newValue новое значение
	 * @param index индекс
	 */
	public final void put(V newValue, int index) {
		m_List.set(index, newValue);
	}
	
	/**
	 * заменить значение по ключу
	 * @param newValue новое значение
	 * @param key ключ
	 * @throws KeyNotFoundException
	 */
	public final void put(V newValue, String key) throws KeyNotFoundException {
		if(!m_Map.containsKey(key)) { throw new KeyNotFoundException(); }
		m_List.set(m_Map.get(key), newValue);
	}
	
	/**
	 * получить количество элементов в списке
	 * @return
	 */
	public final int size() {
		return m_List.size();
	}
	
	/**
	 * удалить элемент по индексу
	 * @param index индекс
	 */
	public final void remove(int index) {
		m_List.remove(index);
		
		Set<String> keys = m_Map.keySet();
		for(String itKey : keys) {
			int itIndex = m_Map.get(itKey);
			if(index < itIndex) { m_Map.put(itKey, itIndex - 1); }
			if(itIndex == index) { m_Map.remove(itKey); }
		}
	}
	
	/**
	 * удалить элемент по ключу
	 * @param key ключ
	 * @throws KeyNotFoundException
	 */
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
	
	/**
	 * удалить все значениея и ключи
	 */
	public final void clear() {
		m_List.clear();
		m_Map.clear();
	}
	
	/**
	 * получить список ключей, отсортированных по порядку расположения элементов в m_List
	 * @return массив ключей
	 */
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

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<V> iterator() {
		return m_List.iterator();
	}
}
