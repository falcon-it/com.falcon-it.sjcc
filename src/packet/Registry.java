package packet;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import packet.serialize.ArraySerialize;
import packet.serialize.BooleanSerialize;
import packet.serialize.ByteSerialize;
import packet.serialize.CharSerialize;
import packet.serialize.DoubleSerialize;
import packet.serialize.FloatSerialize;
import packet.serialize.IntegerSerialize;
import packet.serialize.LongSerialize;
import packet.serialize.ObjectSerialize;
import packet.serialize.ShortSerialize;
import packet.serialize.StringSerialize;

/**
 * реестр типов
 * @author Ilya Sokolov
 */
public final class Registry {
	/**
	 * возникает при попытке добавать в реест тип с таким же id
	 */
	@SuppressWarnings("serial")
	public static final class DuplicateTypeIDException extends PacketException {
		public DuplicateTypeIDException() { super(); }
	}
	/**
	 * возникает если тип не найден в реестре
	 */
	@SuppressWarnings("serial")
	public static final class NotFoundTypeException extends PacketException {
		public NotFoundTypeException() { super(); }
	}
	/**
	 * возникает если экземпляр не реализует интефейс Clone 
	 */
	@SuppressWarnings("serial")
	public static final class NotImplementsCloneException extends PacketException {
		public NotImplementsCloneException() { super(); }
	}
	/**
	 * возникает если экземпляр не реализует интефейс Serialize 
	 */
	@SuppressWarnings("serial")
	public static final class NotImplementsSerializeException extends PacketException {
		public NotImplementsSerializeException() { super(); }
	}
	/**
	 * тип не найден в реестре 
	 */
	@SuppressWarnings("serial")
	public static final class NotTypeIDException extends PacketException {
		public NotTypeIDException() { super(); }
	}
	/**
	 * переданный класс или экзмепляр данных является массивом
	 */
	@SuppressWarnings("serial")
	public static final class TypeIsArrayException extends PacketException {
		public TypeIsArrayException() { super(); }
	}
	
	/**
	 * мап с типами
	 */
	private final HashMap<Integer, Serialize> m_TypeMap = new HashMap<>();
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
	 * рассчитать id типа по классу
	 * @param p класс типа
	 * @return id типа
	 * @throws TypeIsArrayException 
	 */
	public static <T> int calculateClassID(Class<T> p) {
		return p.getName().hashCode();
	}
	/**
	 * рассчитать id типа/экземпляра
	 * @param p экзепляр класса типа или экземпляр типа
	 * @return id типа
	 * @throws TypeIsArrayException 
	 */
	public static <T> int calculateInstanceID(T p) {
		if(p instanceof DynamicID) { return ((DynamicID)p).calculateDynamicID(); }
		return Registry.calculateClassID(p.getClass());
	}
	
	/**
	 * конструктор, добавляет базовые типы
	 */
	public Registry() {
		try {
			//boolean
			addType(new BooleanSerialize());
			//byte
			addType(new ByteSerialize());
			//char
			addType(new CharSerialize());
			//double
			addType(new DoubleSerialize());
			//float
			addType(new FloatSerialize());
			//int
			addType(new IntegerSerialize());
			//long
			addType(new LongSerialize());
			//short
			addType(new ShortSerialize());
			//string
			addType(new StringSerialize());
			//object
			addType(new ObjectSerialize());
			//array
			addType(new ArraySerialize());
		}
		catch (DuplicateTypeIDException|CloneNotSupportedException e) { 
			/*тут всё нормально, поэтому не выпустим исключение*/
		}
	}
	
	/**
	 * добавить новый тип в реестр, вычислив id по экземпляру типа
	 * @param s сериалайзер типа
	 * @throws DuplicateTypeIDException
	 * @throws CloneNotSupportedException 
	 * @throws TypeIsArrayException 
	 */
	public final <T> void addType(Serialize s) throws DuplicateTypeIDException, CloneNotSupportedException {
		m_wLock.lock();
		
		try {
			for(int tid : s.ids()) {
				if(m_TypeMap.containsKey(tid)) { throw new DuplicateTypeIDException(); }
				m_TypeMap.put(tid, s);
			}
		}
		finally {
			m_wLock.unlock();
		}
	}
	
	/**
	 * получить сериалайзер по id типа
	 * @param tid id типа
	 * @return cериалайзер
	 * @throws NotTypeIDException
	 * @throws CloneNotSupportedException
	 */
	public final Serialize getSerializer(int tid) throws NotTypeIDException {
		Serialize s = null;
		
		m_rLock.lock();
		
		try {
			if(!m_TypeMap.containsKey(tid)) { throw new NotTypeIDException(); }
			s = m_TypeMap.get(tid);
		}
		finally {
			m_rLock.unlock();
		}
		
		return (s instanceof Clone) ? ((Clone)s).clone() : s;
	}
	
	/**
	 * получить сериалайзер по экземпляру типа
	 * @param instance экземпляр типа
	 * @return сериалайзер
	 * @throws NotTypeIDException
	 * @throws CloneNotSupportedException
	 */
	public final <T> Serialize getSerializerByInstance(T instance) throws NotTypeIDException, CloneNotSupportedException {
		if(instance.getClass().isArray()) { 
			return getSerializer(calculateClassID(ArraySerialize.class));
		}
		
		return getSerializer(Registry.calculateInstanceID(instance));
	}
	
	/**
	 * получить сериалайзер по классу типа
	 * @param c класс типа
	 * @return сериалайзер
	 * @throws NotTypeIDException
	 * @throws CloneNotSupportedException
	 */
	public final Serialize getSerializerByClass(Class<?> c) throws NotTypeIDException, CloneNotSupportedException {
		if(c.isArray()) { 
			return getSerializer(calculateClassID(ArraySerialize.class));
		}
		
		return getSerializer(Registry.calculateClassID(c));
	}
	
	/**
	 * @param tid id типа
	 * @return true - тип есть в реестре
	 */
	public final boolean containsTypeID(int tid) {
		m_rLock.lock();
		
		try {
			return m_TypeMap.containsKey(tid);
		}
		finally {
			m_rLock.unlock();
		}
	}
	
	/**
	 * @param c класс типа
	 * @return true - тип есть в реестре
	 */
	public final boolean containsTypeIDByClass(Class<?> c) {
		return containsTypeID(calculateClassID(c));
	}
	
	/**
	 * @param instance экземпляр
	 * @return true - тип есть в реестре
	 */
	public final <T> boolean containsTypeIDByInstance(T instance) {
		return containsTypeID(calculateInstanceID(instance));
	}
	
	/**
	 * удалить тип из реестра по id
	 * @param tid id типа
	 */
	public final void removeType(int tid) {
		m_rLock.lock();
		
		try {
			m_TypeMap.remove(tid);
		}
		finally {
			m_rLock.unlock();
		}
	}
	
	/**
	 * удалить тип из реестра по экземпляру типа
	 * @param instance экземпляр типа
	 * @throws TypeIsArrayException 
	 */
	public final <T> void removeTypeByInstance(T instance) {
		removeType(Registry.calculateInstanceID(instance));
	}
	
	/**
	 * удалить тип по его классу
	 * @param c класс типа
	 * @throws TypeIsArrayException 
	 */
	public final void removeTypeByClass(Class<?> c) {
		removeType(Registry.calculateClassID(c));
	}
}
