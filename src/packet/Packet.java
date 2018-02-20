package packet;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import packet.Registry.DynamicIDTypeArrayException;
import packet.Registry.IsMultiLevelArrayException;
import packet.Registry.NotTypeIDException;
import utils.DeepCopy;
import utils.DeepCopy.Clone;
import utils.NamedList;
import utils.NamedList.DuplicateKeyException;
import utils.NamedList.KeyNotFoundException;
import utils.Pair;

/**
 * последовательный пакет разнотипных данных
 * доступ возможен по имени поля и по индексу
 * @author Ilya Sokolov
 */
public final class Packet implements Clone, DynamicID, Serialize {
	/**
	 * флаг перед полем объект<br />
	 * не примитивный объект<br />
	 * не записан == null
	 */
	private static final byte IS_NULL_VALUE = 1;
	/**
	 * флаг перед полем объект<br />
	 * не примитивный объект<br />
	 * записан != null
	 */
	private static final byte IS_NOT_NULL_VALUE = 2;
	
	/**
	 * хранение элементов в списке
	 */
	public static final class Field {
		public final Object Value;
		public final Class<?> TypeClass;
		
		public <T> Field(T value, Class<?> clazz) {
			Value = value;
			TypeClass = clazz;
		}
	}
	/**
	 * список элементов данных<br />
	 * может содержать значение нуль, если объект является шаблоном реального пакета
	 */
	private final NamedList<Pair<Class<?>, Object>> m_NamedList = new NamedList<>();
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
	 * копирующий конструктор
	 * @param p объект копирования
	 * @throws DeepCopyNotSupportException 
	 */
	public Packet(Packet p) throws CloneNotSupportedException {
		String[] keys = p.getKeysSortedByIndex();
		for(String key : keys) {
			try {
				add(key, DeepCopy.copy(p.get(key)));
			} catch (DuplicateKeyException | NullPointerException | IsMultiLevelArrayException
					| DynamicIDTypeArrayException | KeyNotFoundException e) {
				//будем считать что объект внутренне согласован
			}
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
	public <T> Packet(Pair<String, T>[] newArrItems) throws DuplicateKeyException, NullPointerException, IsMultiLevelArrayException, DynamicIDTypeArrayException { 
		add(newArrItems);
	}
	
	private final <T> Pair<Class<?>, Object> field2pair(T value) {
		if(value instanceof Field) {
			Field f = (Field)value;
			return new Pair<>(f.TypeClass, f.Value);
		}
		return new Pair<>(value.getClass(), value); 
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
			m_NamedList.add(field2pair(newItem));
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
			if(item == null) { throw new NullPointerException(); }
			if(m_NamedList.containsKey(key)) { throw new DuplicateKeyException(); }
			Pair<Class<?>, Object> newItem = field2pair(item);
			m_NamedList.add(key, newItem);
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
			if(newItem == null) { throw new NullPointerException(); }
			if(m_NamedList.containsKey(key)) { throw new DuplicateKeyException(); }
			Pair<Class<?>, Object> new_item = field2pair(newItem);
			m_NamedList.insert(key, new_item, index);
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
			return (T)m_NamedList.get(index).getSecond();
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
			return (T) m_NamedList.get(key).getSecond();
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
			m_NamedList.put(field2pair(v), index);
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
			m_NamedList.put(field2pair(v), key);
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
			return m_NamedList.size();
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
			m_NamedList.remove(index);
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
			m_NamedList.remove(key);
		}
		finally {
			m_wLock.unlock();
		}
	}
	
	/**
	 * удалить все элементы
	 */
	public final void removeAll() {
		m_wLock.lock();
		
		try {
			m_NamedList.clear();
		}
		finally {
			m_wLock.unlock();
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
	public <T> T newInstance(int tid) throws NotFoundTypeIDException, InstantiationException {
		try {
			return (T) clone();
		} catch (CloneNotSupportedException e) {
			throw new InstantiationException(e.toString());
		}
	}
	/* (non-Javadoc)
	 * @see packet.Serialize#read(java.lang.Object, packet.Registry, packet.Reader)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader)
			throws PacketIOException {
		try {
			Packet new_p = (Packet)this.clone();
			for(int i = 0; i < new_p.size(); ++i) {
				switch(reader.readByte(in)) {
					case IS_NOT_NULL_VALUE:
					try {
						Serialize s = reg.getSerializerByInstance(new_p.get(i));
						new_p.put(i, s.read(in, reg, reader));
					} catch (NotTypeIDException | IsMultiLevelArrayException | DynamicIDTypeArrayException e) {
						throw new PacketIOException(e);
					}
						break;
					case IS_NULL_VALUE:
						//ничего не делаем - ничего не записано
						break;
					default:
						throw new PacketIOException(new IllegalArgumentException());
				}
			}
			return (T) new_p;
		} catch (CloneNotSupportedException e) {
			throw new PacketIOException(e);
		}
	}
	/* (non-Javadoc)
	 * @see packet.Serialize#write(java.lang.Object, java.lang.Object, packet.Registry, packet.Writer)
	 */
	@Override
	public <T, WriteObjectType> void write(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer)
			throws PacketIOException {
		for(Pair<Class<?>, Object> item : m_NamedList) {
			try {
				if(item.getSecond() != null) {
					Serialize s = reg.getSerializerByInstance(item.getSecond());
					writer.writeByte(out, IS_NOT_NULL_VALUE);
					s.write(out, item.getSecond(), reg, writer);
				}
				else {
					writer.writeByte(out, IS_NULL_VALUE);
				}
			} catch (NotTypeIDException | IsMultiLevelArrayException | DynamicIDTypeArrayException e) {
				throw new PacketIOException(e);
			}
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Clone clone() throws CloneNotSupportedException {
		m_rLock.lock();
		
		try {
			return (Clone)new Packet(this);
		}
		finally {
			m_rLock.unlock();
		}
	}
	
	/**
	 * получим список ключей, отсотированный по индексу в списке
	 * @return массив ключей
	 */
	public final String[] getKeysSortedByIndex() {
		return m_NamedList.getKeysSortedByIndex();
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
			String[] keys = m_NamedList.getKeysSortedByIndex();
			for(String key : keys) {
				Pair<Class<?>, Object> o = m_NamedList.get(key);
				if(o.getSecond() == null) {
					_sb.append("$");
					_sb.append(o.getFirst().getName());
				}
				else {
					_sb.append("$");
					_sb.append(o.getFirst().getName());
					_sb.append("#");
					try {
						_sb.append(Integer.toString(Registry.calculateInstanceID(o.getSecond())));
					} catch (IsMultiLevelArrayException|DynamicIDTypeArrayException e) {
						throw new RuntimeException(e);
					}
				}
			}
		} catch (KeyNotFoundException e1) {
			//ничего не делаем - всё должно быть согласованно
		}
		finally {
			m_rLock.unlock();
		}
		
		return _sb.toString().hashCode();
	}
}
