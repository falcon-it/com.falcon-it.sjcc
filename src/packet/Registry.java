package packet;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	
	public Registry() {
		try {
			//boolean
			BooleanSerialize s1 = new BooleanSerialize();
			addTypeByClass(boolean.class, s1);
			addTypeByClass(Boolean.class, s1);
			//byte
			ByteSerialize s2 = new ByteSerialize(); 
			addTypeByClass(byte.class, s2);
			addTypeByClass(Byte.class, s2);
			//char
			CharSerialize s3 = new CharSerialize();
			addTypeByClass(char.class, s3);
			addTypeByClass(Character.class, s3);
			//double
			DoubleSerialize s4 = new DoubleSerialize();
			addTypeByClass(double.class, s4);
			addTypeByClass(Double.class, s4);
			//float
			FloatSerialize s5 = new FloatSerialize();
			addTypeByClass(float.class, s5);
			addTypeByClass(Float.class, s5);
			//int
			IntegerSerialize s6 = new IntegerSerialize();
			addTypeByClass(int.class, s6);
			addTypeByClass(Integer.class, s6);
			//long
			LongSerialize s7 = new LongSerialize();
			addTypeByClass(long.class, s7);
			addTypeByClass(Long.class, s7);
			//short
			ShortSerialize s8 = new ShortSerialize();
			addTypeByClass(short.class, s8);
			addTypeByClass(Short.class, s8);
			//string
			addTypeByClass(String.class, new StringSerialize());
			//object
			addTypeByClass(Object.class, new ObjectSerialize());
		}
		catch (DuplicateTypeIDException e) { 
			/*тут всё нормально, поэтому не выпустим исключение*/
		}
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
	 * добавить новый тип в реестр, вычислив id по экземпляру типа
	 * и создаётся кория объекта
	 * @param instance экземпляр типа
	 * @throws DuplicateTypeIDException
	 * @throws NotImplementsCloneException
	 * @throws NotImplementsSerializeException 
	 */
	public final <T> void addType(T instance) throws DuplicateTypeIDException, NotImplementsCloneException, NotImplementsSerializeException {
		if(!(instance instanceof Clone)) { throw new NotImplementsCloneException(); }
		if(!(instance instanceof Serialize)) { throw new NotImplementsSerializeException(); }
		addType(Registry.calculateInstabnceID(instance), ((Clone)instance).copy());
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
