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
import utils.DeepCopy.Clone;

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
	 * тип не найден в реестре 
	 */
	@SuppressWarnings("serial")
	public static final class NotTypeIDException extends PacketException {
		public NotTypeIDException() { super(); }
	}
	/**
	 * многомерные массивы не поддерживаются
	 */
	@SuppressWarnings("serial")
	public static final class IsMultiLevelArrayException extends PacketException {
		public IsMultiLevelArrayException() { super(); }
	}
	/**
	 * массив из динамических типов - расчитать id невозможно
	 */
	@SuppressWarnings("serial")
	public static final class DynamicIDTypeArrayException extends PacketException {
		public DynamicIDTypeArrayException() { super(); }
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
	 * @param c класс элемента
	 * @return id элемента
	 */
	public static <T> int calculateThisClassID(Class<?> c) {
		return c.getName().hashCode();
	}
	
	/**
	 * @param c класс элемента
	 * @return сериалайзера для массивов
	 * @throws IsMultiLevelArrayException
	 * @throws DynamicIDTypeArrayException
	 */
	private static <T> int calculateArrayComponentID(Class<?> c) throws IsMultiLevelArrayException {
		if(c.getComponentType().isArray()) { throw new IsMultiLevelArrayException(); }
		return calculateThisClassID(ArraySerialize.class);
	}
	/**
	 * рассчитать id типа по классу
	 * @param p класс типа
	 * @return id типа
	 * @throws IsMultiLevelArrayException 
	 * @throws DynamicIDTypeArrayException 
	 */
	public static <T> int calculateClassID(Class<T> c) throws IsMultiLevelArrayException, DynamicIDTypeArrayException {
		if(DynamicID.class.isAssignableFrom(c)) { throw new DynamicIDTypeArrayException(); }
		if(c.isArray()) { return calculateArrayComponentID(c); }
		return calculateThisClassID(c);
	}
	/**
	 * рассчитать id типа/экземпляра
	 * @param p экзепляр класса типа или экземпляр типа
	 * @return id типа
	 * @throws IsMultiLevelArrayException 
	 * @throws DynamicIDTypeArrayException 
	 */
	public static <T> int calculateInstanceID(T p) throws IsMultiLevelArrayException, DynamicIDTypeArrayException {
		if(p instanceof DynamicID) { return ((DynamicID)p).calculateDynamicID(); }
		return calculateClassID(p.getClass());
	}
	
	/**
	 * конструктор, добавляет базовые типы
	 */
	public Registry() {
		try {
			addType(new BooleanSerialize());//boolean
			addType(new ByteSerialize());//byte
			addType(new CharSerialize());//char
			addType(new DoubleSerialize());//double
			addType(new FloatSerialize());//float
			addType(new IntegerSerialize());//int
			addType(new LongSerialize());//long
			addType(new ShortSerialize());//short
			addType(new StringSerialize());//string
			addType(new ObjectSerialize());//object
			addType(new ArraySerialize());//array
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
			for(int tid : s.supportedClassesIDs()) {
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
	public final Serialize getSerializer(int tid) throws NotTypeIDException, CloneNotSupportedException {
		Serialize s = null;
		
		m_rLock.lock();
		
		try {
			if(!m_TypeMap.containsKey(tid)) { throw new NotTypeIDException(); }
			s = m_TypeMap.get(tid);
		}
		finally {
			m_rLock.unlock();
		}
		
		return (s instanceof Clone) ? (Serialize)((Clone)s).clone() : s;
	}
	
	/**
	 * получить сериалайзер по экземпляру типа
	 * @param instance экземпляр типа
	 * @return сериалайзер
	 * @throws NotTypeIDException
	 * @throws CloneNotSupportedException
	 * @throws DynamicIDTypeArrayException 
	 * @throws IsMultiLevelArrayException 
	 */
	public final <T> Serialize getSerializerByInstance(T instance) 
			throws NotTypeIDException, CloneNotSupportedException, IsMultiLevelArrayException, DynamicIDTypeArrayException {
		return getSerializer(calculateInstanceID(instance));
	}
	
	/**
	 * получить сериалайзер по классу типа
	 * @param c класс типа
	 * @return сериалайзер
	 * @throws NotTypeIDException
	 * @throws CloneNotSupportedException
	 * @throws DynamicIDTypeArrayException 
	 * @throws IsMultiLevelArrayException 
	 */
	public final Serialize getSerializerByClass(Class<?> c) 
			throws NotTypeIDException, CloneNotSupportedException, IsMultiLevelArrayException, DynamicIDTypeArrayException {
		if(c.isArray()) { 
			return getSerializer(calculateClassID(ArraySerialize.class));
		}
		
		return getSerializer(calculateClassID(c));
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
	public final boolean containsTypeIDByClass(Class<?> c)  {
		try {
			return containsTypeID(calculateClassID(c));
		} catch (IsMultiLevelArrayException | DynamicIDTypeArrayException e) {
			return false;
		}
	}
	
	/**
	 * @param instance экземпляр
	 * @return true - тип есть в реестре
	 */
	public final <T> boolean containsTypeIDByInstance(T instance) {
		try {
			return containsTypeID(calculateInstanceID(instance));
		} catch (IsMultiLevelArrayException | DynamicIDTypeArrayException e) { 
			return false;
		}
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
	 * @throws DynamicIDTypeArrayException 
	 * @throws IsMultiLevelArrayException 
	 * @throws TypeIsArrayException 
	 */
	public final <T> void removeTypeByInstance(T instance) 
			throws IsMultiLevelArrayException, DynamicIDTypeArrayException {
		removeType(calculateInstanceID(instance));
	}
	
	/**
	 * удалить тип по его классу
	 * @param c класс типа
	 * @throws DynamicIDTypeArrayException 
	 * @throws IsMultiLevelArrayException 
	 * @throws TypeIsArrayException 
	 */
	public final void removeTypeByClass(Class<?> c) 
			throws IsMultiLevelArrayException, DynamicIDTypeArrayException {
		removeType(calculateClassID(c));
	}
}
