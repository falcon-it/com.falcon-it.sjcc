package packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import utils.Pair;

/**
 * последовательный пакет разнотипных данных
 * доступ возможен по имени поля и по индексу
 * @author Ilya Sokolov
 */
public final class Packet implements Clone, DynamicID {
	/**
	 * возникает при попытке добавать в реест тип с таким же id
	 */
	@SuppressWarnings("serial")
	public static final class DuplicateKeyException extends PacketException {
		public DuplicateKeyException() { super(); }
	}
	/**
	 * возникает при попытке добавать в реест тип с таким же id
	 */
	@SuppressWarnings("serial")
	public static final class KeyNotFoundException extends PacketException {
		public KeyNotFoundException() { super(); }
	}
	/**
	 * мап, увязывает индексы списка с именами<br />
	 * если при добавлении не указано имя, то вместо имени указать строковое значение индекса
	 */
	private final HashMap<String, Integer> m_IndexMap = new HashMap<>();
	/**
	 * список элементов данных<br />
	 * может содержать значение нуль, если объект является шаблоном реального пакета
	 */
	private final LinkedList<Object> m_ItemList = new LinkedList<>();
	/**
	 * read/write блокировка
	 */
	private final ReadWriteLock m_rwLock = new ReentrantReadWriteLock();
	/**
	 * блокировка для читателей
	 */
	private final Lock m_rLock = m_rwLock.readLock();
	/**
	 * блокировка для писателей
	 */
	private final Lock m_wLock = m_rwLock.writeLock();
	
	/**
	 * список классов
	 */
	public static final Class<?>[] classes = new Class<?>[] { Packet.class };
	
	/**
	 * конструктор<br />
	 * создаёт пустой объект
	 */
	public Packet() { }
	/**
	 * создаёт набор пустых именованных элементов
	 * @param keyArr массив имён элементов
	 * @throws DuplicateKeyException
	 */
	public Packet(String[] keyArr) throws DuplicateKeyException {
		addEmptyNamedItemAll(keyArr);
	}
	/**
	 * создаёт набор пустых элементов <br />
	 * проименованных индексами
	 * @param capacity количество пустых элементов
	 * @throws IllegalArgumentException
	 * @throws DuplicateKeyException
	 */
	public Packet(int capacity) throws IllegalArgumentException, DuplicateKeyException {
		addEmptyItems(capacity);
	}
	/**
	 * добавить массив именованных элементов
	 * @param newArrItems массив элементов
	 * @throws DuplicateKeyException
	 */
	public <T> Packet(Pair<String, T>[] newArrItems) throws DuplicateKeyException { 
		add(newArrItems);
	}
	
	/**
	 * добавить новый элемент<br />
	 * имя будет соответствовать индексу, добавляемого элемента
	 * @param newItem элемент
	 * @throws DuplicateKeyException
	 */
	public final <T> void add(T newItem) throws DuplicateKeyException {
		m_wLock.lock();
		
		try {
			int newIndex = m_ItemList.size();
			if(newIndex > 0) { --newIndex; }
			add(Integer.toString(newIndex), newItem);
		}
		finally {
			m_wLock.unlock();
		}
	}
	
	/**
	 * добавить массив новых элементов<br />
	 * имя будет соответствовать индексу добавляемого элемента
	 * @param newArrItems массив элемнтов
	 * @throws DuplicateKeyException
	 */
	public final <T> void addAll(T[] newArrItems) throws DuplicateKeyException {
		for(T item : newArrItems) { add(item); }
	}
	
	/**
	 * добавить новый именованных элемент
	 * @param key имя (ключ)
	 * @param item элемент
	 * @throws DuplicateKeyException
	 */
	public final <T> void add(String key, T item) throws DuplicateKeyException {
		m_wLock.lock();
		
		try {
			if(m_IndexMap.containsKey(key)) { throw new DuplicateKeyException(); }
			m_ItemList.add(item);
			m_IndexMap.put(key, m_ItemList.size() - 1);
		}
		finally {
			m_wLock.unlock();
		}
	}
	
	/**
	 * добавить массив именованных элементов
	 * @param newArrItems массив элементов
	 * @throws DuplicateKeyException
	 */
	public final <T> void addAll(Pair<String, T>[] newArrItems) throws DuplicateKeyException {
		for(Pair<String, T> item : newArrItems) { add(item.getFirst(), item.getSecond()); }
	}
	
	/**
	 * добавить новые пустые элементы
	 * @param capacity количество добавляемых элементов
	 * @throws DuplicateKeyException
	 * @throws IllegalArgumentException
	 */
	public final void addEmptyItems(int capacity) throws DuplicateKeyException, IllegalArgumentException {
		if(capacity <= 0) { throw new IllegalArgumentException(); }
		for(int i = 0; i < capacity; ++i) { add(null); }
	}
	
	/**
	 * добавить пустой именовынный элемент
	 * @param key имя (ключ)
	 * @throws DuplicateKeyException
	 */
	public final void addEmptyNamedItem(String key) throws DuplicateKeyException {
		add(key, null);
	}

	/**
	 * добавить массив пустых именованных элементов
	 * @param keyArr массив имён (ключей)
	 * @throws DuplicateKeyException
	 */
	public final void addEmptyNamedItemAll(String[] keyArr) throws DuplicateKeyException {
		for(String key : keyArr) { addEmptyNamedItem(key); }
	}
	
	/**
	 * вставить новый элемент на указанную позицию
	 * @param newItem новый элемент
	 * @param index позиция для вставки
	 * @throws DuplicateKeyException
	 * @throws IndexOutOfBoundsException
	 */
	public final <T> void insert(T newItem, int index) throws DuplicateKeyException, IndexOutOfBoundsException {
		insert(Integer.toString(index), newItem, index);
	}
	
	/**
	 * вставить новый именованный элемент на указанную позицию
	 * @param key имя (ключ)
	 * @param newItem вставляемый элемент
	 * @param index позиция для вставки
	 * @throws DuplicateKeyException
	 * @throws IndexOutOfBoundsException
	 */
	public final <T> void insert(String key, T newItem, int index) throws DuplicateKeyException, IndexOutOfBoundsException {
		m_wLock.lock();
		
		try {
			if(m_IndexMap.containsKey(key)) { throw new DuplicateKeyException(); }
			m_ItemList.add(index, newItem);
			Set<String> keys = m_IndexMap.keySet();
			for(String iKey : keys) {
				int currIndex = m_IndexMap.get(iKey);
				if(currIndex >= index) { m_IndexMap.put(iKey, currIndex + 1); }
			}
			m_IndexMap.put(key, index);
		}
		finally {
			m_wLock.unlock();
		}
	}
	
	/**
	 * получить элемнт по индексу
	 * @param index индекс элемнта
	 * @return требуемый элемент
	 * @throws IndexOutOfBoundsException
	 */
	@SuppressWarnings("unchecked")
	public final <T> T get(int index) throws IndexOutOfBoundsException {
		m_rLock.lock();
		
		try {
			return (T) m_ItemList.get(index);
		}
		finally {
			m_rLock.unlock();
		}
	}
	
	/**
	 * получить элемент по имени
	 * @param key имя (ключ) элемента
	 * @return требуемый элемент
	 * @throws KeyNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public final <T> T get(String key) throws KeyNotFoundException {
		m_rLock.lock();
		
		try {
			if(!m_IndexMap.containsKey(key)) { throw new KeyNotFoundException(); }
			return (T) m_ItemList.get(m_IndexMap.get(key));
		}
		finally {
			m_rLock.unlock();
		}
	}
	
	/**
	 * @return количество элементов в пакете
	 */
	public final int size() {
		m_rLock.lock();
		
		try {
			return m_ItemList.size();
		}
		finally {
			m_rLock.unlock();
		}
	}
	
	/**
	 * удалить элемент по индексу
	 * @param index индекс удаляемого элемента
	 * @throws IndexOutOfBoundsException
	 */
	public final void remove(int index) throws IndexOutOfBoundsException {
		m_wLock.lock();
		
		try {
			m_ItemList.remove(index);
			Set<String> keys = m_IndexMap.keySet();
			String remKey = null;
			for(String iKey : keys) {
				int currIndex = m_IndexMap.get(iKey);
				if(currIndex == index) { remKey = iKey; }
				if(currIndex > index) { m_IndexMap.put(iKey, currIndex - 1); }
			}
			m_IndexMap.remove(remKey);
		}
		finally {
			m_wLock.unlock();
		}
	}
	
	/**
	 * удалить элемент по ключу
	 * @param key имя (ключ) удаляемого элемента
	 * @throws KeyNotFoundException
	 */
	public final void remove(String key) throws KeyNotFoundException {
		m_wLock.lock();
		
		try {
			if(!m_IndexMap.containsKey(key)) { throw new KeyNotFoundException(); }
			remove(m_IndexMap.get(key));
		}
		finally {
			m_rLock.unlock();
		}
	}
	
	/**
	 * удалить все элементы
	 */
	public final void removeAll() {
		m_wLock.lock();
		
		try {
			m_IndexMap.clear();
			m_ItemList.clear();
		}
		finally {
			m_rLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see packet.Serialize#supportedClasses()
	 */
	@Override
	public Class<?>[] supportedClasses() {
		return classes;
	}
	/* (non-Javadoc)
	 * @see packet.Serialize#supportedClassesIDs()
	 */
	@Override
	public int[] supportedClassesIDs() {
		return new int[] { calculateDynamicID() };
	}
	/* (non-Javadoc)
	 * @see packet.Serialize#classByID(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> Class<T> classByID(int tid) throws NotFoundTypeIDException {
		return (Class<T>) classes[0];
	}
	/* (non-Javadoc)
	 * @see packet.Serialize#newInstance(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(int tid) throws NotFoundTypeIDException, InstantiationException, IllegalAccessException {
		return (T) clone();
	}
	@Override
	public <T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader)
			throws PacketIOException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public <T, WriteObjectType> void write(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer)
			throws PacketIOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Clone clone() {
		Packet _copy = new Packet();
		
		m_rLock.lock();
		
		try {
			
		}
		finally {
			m_rLock.unlock();
		}
		
		return _copy;
	}
	
	/**
	 * получим список ключей, отсотированный по индексу в списке
	 * @return массив ключей
	 */
	private final String[] getKeysSortedByIndex() {
		Set<String> keys = m_IndexMap.keySet();
		ArrayList<Pair<String, Integer>> arr = new ArrayList<>(keys.size());
		for(String key : keys) {
			arr.add(new Pair<>(key, m_IndexMap.get(key)));
		}
		Collections.sort(arr, 
				(Pair<String, Integer> o1, Pair<String, Integer> o2) -> {
						return o1.getSecond() < o2.getSecond() ? -1 : 1;
					});
		String[] sortedKeys = new String[keys.size()];
		for(int i = 0; i < arr.size(); ++i) {
			sortedKeys[i] = arr.get(i).getFirst();
		}
		return sortedKeys;
	}
	
	/* (non-Javadoc)
	 * @see packet.DynamicID#calculateDynamicID()
	 */
	@Override
	public int calculateDynamicID() {
		StringBuilder _sb = new StringBuilder();
		_sb.append(getClass().getName());
		
		m_rLock.lock();
		
		try {
			String[] keys = getKeysSortedByIndex();
			for(String key : keys) {
				_sb.append("$");
				_sb.append(key);
				Object o = m_ItemList.get(m_IndexMap.get(key));
				if(o != null) {
					_sb.append("#");
					_sb.append(o.getClass().getName());
				}
				else {
					_sb.append("#null");
				}
			}
		}
		finally {
			m_rLock.unlock();
		}
		
		return _sb.toString().hashCode();
	}
}
