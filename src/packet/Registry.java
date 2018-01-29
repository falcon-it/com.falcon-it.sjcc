package packet;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import packet.serialize.BooleanSerialize;
import packet.serialize.ByteSerialize;

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
	 */
	public static <T> int calculateClassID(Class<T> p) {
		return p.getName().hashCode();
	}
	/**
	 * рассчитать id типа/экземпляра
	 * @param p экзепляр класса типа или экземпляр типа
	 * @return id типа
	 */
	public static <T> int calculateInstabnceID(T p) {
		if(p instanceof DynamicID) { return ((DynamicID)p).calculateDynamicID(); }
		return Registry.calculateInstabnceID(p.getClass());
	}
	
	public Registry() throws DuplicateTypeIDException {
		BooleanSerialize s1 = new BooleanSerialize();
		addTypeByClass(boolean.class, s1);
		addTypeByClass(Boolean.class, s1);
		
		ByteSerialize s2 = new ByteSerialize(); 
		addTypeByClass(byte.class, s2);
		addTypeByClass(Byte.class, s2);
	}
	
	/**
	 * добавить новый тип в реестр
	 * @param tid id типа
	 * @param s сериалайзер типа
	 * @throws DuplicateTypeIDException
	 */
	public final void addType(int tid, Serialize s) throws DuplicateTypeIDException {
		m_wLock.lock();
		try {
			if(m_TypeMap.containsKey(tid)) { throw new DuplicateTypeIDException(); }
			m_TypeMap.put(tid, s);
		}
		finally {
			m_wLock.unlock();
		}
	}
	
	/**
	 * добавить новый тип в реестр, вычислив id по экземпляру типа
	 * @param instance экземпляр типа
	 * @param s сериалайзер типа
	 * @throws DuplicateTypeIDException
	 */
	public final <T> void addType(T instance, Serialize s) throws DuplicateTypeIDException {
		addType(Registry.calculateInstabnceID(instance), s);
	}
	
	/**
	 * добавить новый тип в реестр, вычислив id по классу типа
	 * @param c класс экземпляра данных
	 * @param s сериалайзер типа
	 * @throws DuplicateTypeIDException
	 */
	public final <T> void addTypeByClass(Class<?> c, Serialize s) throws DuplicateTypeIDException {
		addType(Registry.calculateClassID(c), s);
	}
}
