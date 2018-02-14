package packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import packet.Registry.DynamicIDTypeArrayException;
import packet.Registry.IsMultiLevelArrayException;
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
	
	public static final class Field {
		public final Object Value;
		public final int TypeID;
		
		public <T> Field(T value, int tid) {
			Value = value;
			TypeID = tid;
		}
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
	private final LinkedList<Pair<Integer, Object>> m_ItemList = new LinkedList<>();
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
	 * добавить массив именованных элементов
	 * @param newArrItems массив элементов
	 * @throws DuplicateKeyException
	 * @throws DynamicIDTypeArrayException 
	 * @throws IsMultiLevelArrayException 
	 * @throws NullPointerException 
	 */
	public <T> Packet(Pair<String, T>[] newArrItems) throws DuplicateKeyException, NullPointerException, IsMultiLevelArrayException, DynamicIDTypeArrayException { 
		add(newArrItems);
	}
	
	/**
	 * добавить новый элемент<br />
	 * имя будет соответствовать индексу, добавляемого элемента
	 * @param newItem элемент
	 * @throws DuplicateKeyException
	 * @throws DynamicIDTypeArrayException 
	 * @throws IsMultiLevelArrayException 
	 * @throws NullPointerException 
	 */
	public final <T> void add(T newItem) throws DuplicateKeyException, NullPointerException, IsMultiLevelArrayException, DynamicIDTypeArrayException {
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
	 * @throws DynamicIDTypeArrayException 
	 * @throws IsMultiLevelArrayException 
	 * @throws NullPointerException 
	 */
	public final <T> void addAll(T[] newArrItems) throws DuplicateKeyException, NullPointerException, IsMultiLevelArrayException, DynamicIDTypeArrayException {
		for(T item : newArrItems) { add(item); }
	}
	
	/**
	 * добавить новый именованных элемент
	 * @param key имя (ключ)
	 * @param item элемент
	 * @throws DuplicateKeyException
	 * @throws DynamicIDTypeArrayException 
	 * @throws IsMultiLevelArrayException 
	 */
	public final <T> void add(String key, T item) throws DuplicateKeyException, NullPointerException, IsMultiLevelArrayException, DynamicIDTypeArrayException {
		m_wLock.lock();
		
		try {
			Pair<Integer, Object> newItem = null;
			if(item == null) { throw new NullPointerException(); }
			if(m_IndexMap.containsKey(key)) { throw new DuplicateKeyException(); }
			
			if(item instanceof Field) {
				Field f = (Field)item;
				newItem = new Pair<>(f.TypeID, f.Value);
			}
			else { 
				newItem = new Pair<>(
						Registry.calculateInstanceID(item), 
						item); 
			}
			
			m_ItemList.add(newItem);
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
	 * @throws DynamicIDTypeArrayException 
	 * @throws IsMultiLevelArrayException 
	 * @throws NullPointerException 
	 */
	public final <T> void addAll(Pair<String, T>[] newArrItems) throws DuplicateKeyException, NullPointerException, IsMultiLevelArrayException, DynamicIDTypeArrayException {
		for(Pair<String, T> item : newArrItems) { add(item.getFirst(), item.getSecond()); }
	}
	
	/**
	 * вставить новый элемент на указанную позицию
	 * @param newItem новый элемент
	 * @param index позиция для вставки
	 * @throws DuplicateKeyException
	 * @throws IndexOutOfBoundsException
	 * @throws DynamicIDTypeArrayException 
	 * @throws IsMultiLevelArrayException 
	 */
	public final <T> void insert(T newItem, int index) throws DuplicateKeyException, IndexOutOfBoundsException, IsMultiLevelArrayException, DynamicIDTypeArrayException {
		insert(Integer.toString(index), newItem, index);
	}
	
	/**
	 * вставить новый именованный элемент на указанную позицию
	 * @param key имя (ключ)
	 * @param newItem вставляемый элемент
	 * @param index позиция для вставки
	 * @throws DuplicateKeyException
	 * @throws IndexOutOfBoundsException
	 * @throws DynamicIDTypeArrayException 
	 * @throws IsMultiLevelArrayException 
	 */
	public final <T> void insert(String key, T newItem, int index) throws DuplicateKeyException, IndexOutOfBoundsException, IsMultiLevelArrayException, DynamicIDTypeArrayException {
		m_wLock.lock();
		
		try {
			Pair<Integer, Object> new_item = null;
			if(newItem == null) { throw new NullPointerException(); }
			if(m_IndexMap.containsKey(key)) { throw new DuplicateKeyException(); }
			if(newItem instanceof Field) {
				Field f = (Field)newItem;
				new_item = new Pair<>(f.TypeID, f.Value);
			}
			else { 
				new_item = new Pair<>(
						Registry.calculateInstanceID(newItem), 
						newItem); 
			}
			m_ItemList.add(index, new_item);
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
	 * установить значение по индексу
	 * @param index индекс элемента
	 * @param v новое значение
	 * @throws IndexOutOfBoundsException
	 */
	public final <T> void put(int index, T v) throws IndexOutOfBoundsException {
		m_rLock.lock();
		
		try {
			m_ItemList.get(index).putSecond(v);
		}
		finally {
			m_rLock.unlock();
		}
	}
	
	/**
	 * установить значение по ключу
	 * @param key ключ
	 * @param v новое значение
	 * @throws KeyNotFoundException
	 */
	public final <T> void put(String key, T v) throws KeyNotFoundException {
		m_rLock.lock();
		
		try {
			if(!m_IndexMap.containsKey(key)) { throw new KeyNotFoundException(); }
			m_ItemList.get(m_IndexMap.get(key)).putSecond(v);
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
			String[] keys = getKeysSortedByIndex();
			for(String key : keys) {
				try {
					Pair<Integer, Object> _item = m_ItemList.get(m_IndexMap.get(key));
					_copy.add(key, new Field(_item.getSecond(), _item.getFirst()));
				}
				catch (Exception e) { 
					//отловим все исключения, так как внутренний список должен быть согласован
				}
			}
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
		String[] sortedKeys = new String[keys.size()];
		for(String key : keys) { arr.add(new Pair<>(key, m_IndexMap.get(key))); }
		Collections.sort(arr, 
				(Pair<String, Integer> o1, Pair<String, Integer> o2) -> {
						return o1.getSecond() < o2.getSecond() ? -1 : 1;
					});
		for(int i = 0; i < arr.size(); ++i) { sortedKeys[i] = arr.get(i).getFirst(); }
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
				_sb.append("#");
				_sb.append(
						Integer.toString(
								m_ItemList.get(
										m_IndexMap.get(key)
										).getFirst()));
			}
		}
		finally {
			m_rLock.unlock();
		}
		
		return _sb.toString().hashCode();
	}
}
