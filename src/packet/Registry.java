package packet;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	private final HashMap<Integer, TypeSerializer> m_TypeMap = new HashMap<>();
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
	
	public static int calculateClassID(Class<?> c) {
		return c.getName().hashCode();
	}
	
	public Registry() {
		
	}
	
	/**
	 * добавить новый тип в реестр
	 * @param c класс типа
	 * @param s сериалайзер типа
	 * @throws DuplicateTypeIDException
	 */
	public final void addType(Class<?> c, TypeSerializer s) throws DuplicateTypeIDException {
		m_wLock.lock();
		try {
			int tid = Registry.calculateClassID(c);
			if(m_TypeMap.containsKey(tid)) { throw new DuplicateTypeIDException(); }
			m_TypeMap.put(tid, s);
		}
		finally {
			m_rLock.unlock();
		}
	}
}
