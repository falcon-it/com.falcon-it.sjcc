package packet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import packet.IOMethodInfo.MethodType;
import utils.Pair;
import utils.Triple;

/**
 * реестр делегатов для ввод/вывода
 * @author Ilya Sokolov
 */
public final class Registry {
	/**
	 * id дефолтного типа - Object - к которому можно привести любой объект
	 */
	@SuppressWarnings("unused")
	private static final int DEFAULT_TYPE_ID = Serializer.calculateTypeIDByClass(Object.class);
	/**
	 * id интерфейса уже есть
	 */
	@SuppressWarnings("serial")
	public static final class DelegateDuplicateException extends PacketException {
		public DelegateDuplicateException() { super(); }
	}
	/**
	 * некорректная сигнатура метода
	 */
	@SuppressWarnings("serial")
	public static final class FailSignatureException extends PacketException {
		public FailSignatureException() { super(); }
	}
	/**
	 * объект реализует своё чтение/зипись, но не реализует Cloneable
	 */
	@SuppressWarnings("serial")
	public static final class CloneableNotImplementedException extends PacketException {
		public CloneableNotImplementedException() { super(); }
	}
	/**
	 * не найден класс объекта ввода/вывода при создании accessor'ра
	 */
	@SuppressWarnings("serial")
	public static final class NotFountIOObjectClassException extends PacketException {
		public NotFountIOObjectClassException() { super(); }
	}
	/**
	 * не найден тип по id
	 */
	@SuppressWarnings("serial")
	public static final class NotFoundTypeIDException extends PacketException {
		public NotFoundTypeIDException() { super(); }
	}
	/**
	 * при исполнии делегата было брошено исключение
	 * получить брошенное исключение можно через getCause​() 
	 */
	@SuppressWarnings("serial")
	public static final class ExecuteDelegateException extends PacketException {
		public ExecuteDelegateException(Exception e) { super(e); }
	}
	
	/**
	 * доступ делегатам при сериализации
	 */
	public abstract class Accessor {
		/**
		 * мап с делегатами для типа
		 */
		protected final HashMap<
						Integer, //id типа
						Pair<
							Method, //метод типа
							Object //экземпляр диспетчера, содержащего делегат
							>
						> m_Delegates;
		/**
		 * объект ввода/вывода
		 */
		protected final Object m_IOObject;
		/**
		 * @param del мап с делегатами
		 * @param ioo объект ввода/вывода
		 */
		public Accessor(HashMap<Integer, Pair<Method, Object>> del, Object ioo) {
			m_Delegates = del;
			m_IOObject = ioo;
		}
	}
	
	/**
	 * accessor для чтения из объекта ввода/вывода
	 */
	public final class ReadAccessor extends Accessor {
		/**
		 * @param del мап с делегатами
		 * @param ioo объект ввода/вывода
		 */
		public ReadAccessor(HashMap<Integer, Pair<Method, Object>> del, Object ioo) {
			super(del, ioo);
		}
		
		/**
		 * прочитать экземпляр типа из объекта ввода/вывода
		 * @param tid id типа
		 * @param ioo объект ввода/вывода
		 * @return прочитанный экземпляр объекта
		 * @throws NotFountTypeIDException
		 * @throws ExecuteDelegateException
		 */
		@SuppressWarnings("unchecked")
		public final <ReadingType, IOObjectType> ReadingType read(int tid, IOObjectType ioo) throws NotFoundTypeIDException, ExecuteDelegateException {
			Pair<Method, Object> delegate = null;
			
			//ищем делегат
			m_rLock.lock();
			try {
				delegate = m_Delegates.get(tid); //сначала в списке с конкретным объектом ввода/вывода
				if(delegate == null) { delegate = m_VoidReadDelegates.get(tid); } //потом в списке с универсальными типами
			}
			finally {
				m_rLock.unlock();
			}
			
			//не удалось найти тип
			if(delegate == null) { throw new NotFoundTypeIDException(); }
			
			try {
				//вызываем делегат
				return (ReadingType)delegate.getFirst().invoke(delegate.getSecond(), ioo, this);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ExecuteDelegateException(e);
			}
		}
	}
	
	/**
	 * accessor для чтения из объекта ввода/вывода
	 */
	public final class WriteAccessor extends Accessor {
		/**
		 * @param del мап с делегатами
		 * @param ioo объект ввода/вывода
		 */
		public WriteAccessor(HashMap<Integer, Pair<Method, Object>> del, Object ioo) {
			super(del, ioo);
		}
		
		public final <RecordableType, IOObjectType> void write(IOObjectType ioo, RecordableType instance) throws NotFoundTypeIDException, ExecuteDelegateException {
			Pair<Method, Object> delegate = null;
			int _tid = Serializer.calculateTypeIDByInstance(instance);
			
			//ищем делегат
			m_rLock.lock();
			try {
				delegate = m_Delegates.get(_tid); //сначала в списке с конкретным объектом ввода/вывода
				if(delegate == null) { delegate = m_VoidWriteDelegates.get(_tid); } //потом в списке с универсальными типами
			}
			finally {
				m_rLock.unlock();
			}
			
			//не удалось найти тип
			if(delegate == null) { throw new NotFoundTypeIDException(); }
			
			try {
				//вызываем делегат
				delegate.getFirst().invoke(delegate.getSecond(), ioo, this, instance);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new ExecuteDelegateException(e);
			}
		}
	}
	
	/*
	 * делегаты для чтения/записи объекта типа T
	 * нестатические методы классов:
	 * для чтения T readXXX(IOObject ioo, Registry.ReadAccessor racc) 
	 * для записи void writeXXX(IOObject ioo, Registry.WriteAccessor wacc, T instance)
	 * также могут содержать реализацию интерфейса StructureSerialize и/или DynamicStructureSerialize
	 * для этих методов установлена аннотация IOMethodType
	 * при чтении клонируем объект реализующий StructureSerialize (instanceof Cloneable) и выполняем чтение методом нового объекта
	 */
	/**
	 * список делегав ввода/вывода
	 */
	private final LinkedList<
					Triple<
						Class<?>, //класс объекта для чтениея/записи
						IOMethodInfo.MethodType, //тип делегата - чтение/запись
						HashMap<
							Integer, //id типа
							Pair<
								Method, //метод типа
								Object //экземпляр диспетчера, содержащего делегат
								>
							>
						>
					> m_TypeDelegates = new LinkedList<>();
	private final HashMap<
					Integer, //id типа
					Pair<
						Method, //метод типа
						Object //экземпляр диспетчера, содержащего делегат
						>
					> m_VoidReadDelegates = new HashMap<>(), //общие делегаты для чтения
					m_VoidWriteDelegates = new HashMap<>(); //общие делегаты для записи
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
	 * регистрация сериалайзера
	 * для регистрации статических методов надо передать класс сериалайзера
	 * @param serializerInst экземпляр сериалайзера
	 * @throws DelegateDuplicateException
	 * @throws CloneableNotImplementedException
	 * @throws FailSignatureException 
	 */
	public final <SerializerType> void registrationSerializer(SerializerType serializerInst) 
			throws DelegateDuplicateException, CloneableNotImplementedException, FailSignatureException {
		Class<?> serClass = serializerInst.getClass();
		//если передан класс будем собирать только статические методы
		boolean pIsClass = Class.class.isAssignableFrom(serClass); //переданный параметер представляет класс объекта, а не сам объект
		if(pIsClass) { serClass = (Class<?>)serializerInst; }
		Method[] mets = serClass.getMethods();
		Class<IOMethodInfo> annMeth = IOMethodInfo.class;
		Class<?> voidClass = void.class;
		Class<?> voidLangClass = Void.class;
		Class<?> registryReadAccessorClass = Registry.ReadAccessor.class;
		Class<?> registryWriteAccessorClass = Registry.WriteAccessor.class;
		boolean isSS = (serializerInst instanceof StructureSerialize); //реализует интерфейс чтения/записи
		
		for(Method mi : mets) {
			if(mi.isAnnotationPresent(annMeth)) {
				IOMethodInfo mAnn = mi.getAnnotation(annMeth);
				boolean univ = mAnn.universal();
				IOMethodInfo.MethodType mt = mAnn.type();
				Class<?>[] prms = mi.getParameterTypes();
				Class<?> retT = mi.getReturnType();
				
				if( //проверяем сигнатуру метода
					(((mt == MethodType.read) && //метод для чтения
							(prms.length == 2) && //у него 2 параметр
							(!prms[0].isPrimitive()) && //первый параметер не примитив
							(prms[1].isAssignableFrom(registryReadAccessorClass)) && //второй параметер Registry.Accessor
							(isSS ? 
									((retT == voidClass) || (retT == voidLangClass)) : //не возвращает значение
									((retT != voidClass) || (retT != voidLangClass))) //возвращает 
							) || 
					((mt == MethodType.write) && //метод для записи
							(prms.length == (isSS ? 2 : 3)) &&  //если реализует StructureSerialize, то 2 параметер, иначе 3
							(!prms[0].isPrimitive()) && //первый параметер не примитив
							(prms[1].isAssignableFrom(registryWriteAccessorClass)) && //второй параметер Registry.Accessor
							((retT == voidClass) || (retT == voidLangClass))) //не возвращает значение
					) && 
					Modifier.isPublic(mi.getModifiers()) && 
					(pIsClass ? Modifier.isStatic(mi.getModifiers()) : true)
						) {
					//если метод реализует StructureSerialize проверяем реализацию Cloneable
					if(isSS && !(serializerInst instanceof Cloneable)) { throw new CloneableNotImplementedException(); }
					//вычисли id типа
					int _tid = (
							isSS ? //реализует StructureSerialize
								Serializer.calculateTypeIDByInstance(serializerInst) : //вычисляем id 
								((mAnn.type() == MethodType.read) ? 
									Serializer.calculateTypeIDByClass(retT) : //метод для чтения - вычисляем id возвращаемого значения
									Serializer.calculateTypeIDByClass(prms[1])) //метод для записи - вычисляем id второго параметра
							);
					m_wLock.lock();
					try {
						if(univ) {//общий тип - для ввода/вывода не требуется конкретный объект
							switch(mt) {
								case read:
									if(m_VoidReadDelegates.containsKey(_tid)) { throw new DelegateDuplicateException(); } //тип уже есть
									m_VoidReadDelegates.put(_tid, new Pair<>(mi, pIsClass ? null : serializerInst)); //добавим
									break;
								case write:
									if(m_VoidWriteDelegates.containsKey(_tid)) { throw new DelegateDuplicateException(); } //тип уже есть
									m_VoidWriteDelegates.put(_tid, new Pair<>(mi, pIsClass ? null : serializerInst)); //добавим
									break;
							}
						}
						else {
							boolean _exist_type = false;
							//переберём типы
							for(Triple<Class<?>, IOMethodInfo.MethodType, HashMap<Integer, Pair<Method, Object>>> delOL : m_TypeDelegates)  {
								if(delOL.getLeft().isAssignableFrom(prms[0]) && //сравниваем класс объекта ввода/вывода
										(delOL.getMiddle() == mt)) { //совпадает тип делегатов
									if(delOL.getRight().containsKey(_tid)) { throw new DelegateDuplicateException(); } //тип уже есть
									delOL.getRight().put(_tid, new Pair<>(mi, pIsClass ? null : serializerInst)); //добавим
									_exist_type = true;
									break; //и прервём цикл
								}
							}
							
							if(!_exist_type) {
								//не нашли тип создадим
								Triple<Class<?>, IOMethodInfo.MethodType, HashMap<Integer, Pair<Method, Object>>> newDelOL = new Triple<>();
								newDelOL.putLeft(prms[0]);
								newDelOL.putMiddle(mAnn.type());
								newDelOL.getRight().put(_tid, new Pair<>(mi, pIsClass ? null : serializerInst));
								m_TypeDelegates.add(newDelOL);
							}
						}
					}
					finally {
						m_wLock.unlock();
					}
				} else {
					throw new FailSignatureException();
				}
			}
		}
	}
	
	/**
	 * регистрация массива сериалайзеров
	 * @param serializerArrInst массив сериалайзеров
	 * @throws DelegateDuplicateException
	 * @throws CloneableNotImplementedException
	 * @throws FailSignatureException 
	 */
	public final <SerializerType> void registrationSerializer(Iterable<SerializerType> serializerArrInst) 
			throws DelegateDuplicateException, CloneableNotImplementedException, FailSignatureException {
		for(SerializerType serializerInst : serializerArrInst) {
			registrationSerializer(serializerInst);
		}
	}
	
	/**
	 * удалить делегаты типа из реестра
	 * @param serializerInst сериалайзер
	 */
	public final <SerializerType> void unregistrationSerializer(SerializerType serializerInst) {
		Class<?> serClass = serializerInst.getClass();
		Method[] mets = serClass.getMethods();
		Class<IOMethodInfo> annMeth = IOMethodInfo.class;
		
		for(Method mi : mets) {
			if(mi.isAnnotationPresent(annMeth)) {
				IOMethodInfo mAnn = mi.getAnnotation(annMeth);
				boolean univ = mAnn.universal();
				IOMethodInfo.MethodType mt = mAnn.type();
				Class<?>[] prms = mi.getParameterTypes();
				Class<?> retT = mi.getReturnType();
				
				//вычисли id типа
				int _tid = (
						(serializerInst instanceof StructureSerialize) ? //реализует StructureSerialize
							Serializer.calculateTypeIDByInstance(serializerInst) : //вычисляем id 
							((mAnn.type() == MethodType.read) ? 
								Serializer.calculateTypeIDByClass(retT) : //метод для чтения - вычисляем id возвращаемого значения
								Serializer.calculateTypeIDByClass(prms[1])) //метод для записи - вычисляем id второго параметра
						);
				m_wLock.lock();
				try {
					if(univ) {//общий тип - для ввода/вывода не требуется конкретный объект
						switch(mt) {//удалить соответствующий делегат
							case read:
								if(m_VoidReadDelegates.containsKey(_tid)) { m_VoidReadDelegates.remove(_tid); }
								break;
							case write:
								if(m_VoidWriteDelegates.containsKey(_tid)) { m_VoidWriteDelegates.remove(_tid); }
								break;
						}
					}
					else {
						//переберём типы
						for(Triple<Class<?>, IOMethodInfo.MethodType, HashMap<Integer, Pair<Method, Object>>> delOL : m_TypeDelegates)  {
							if(delOL.getLeft().isAssignableFrom(prms[0]) && //сравниваем класс объекта ввода/вывода
									(delOL.getMiddle() == mt)) { //совпадает тип делегатов
								if(delOL.getRight().containsKey(_tid)) { delOL.getRight().remove(_tid); }//удаляем если нашли
							}
						}
						
						m_TypeDelegates.removeIf(x -> x.getRight().size() == 0);
					}
				}
				finally {
					m_wLock.unlock();
				}
			}
		}
	}
	
	/**
	 * удалить делегаты типа из реестра
	 * @param serializerArrInst массив сериалайзеров
	 */
	public final <SerializerType> void unregistrationSerializer(Iterable<SerializerType> serializerArrInst) {
		for(SerializerType serializerInst : serializerArrInst) {
			unregistrationSerializer(serializerInst);
		}
	}
	
	/**
	 * удалить из реестра делегаты для определённого класса объекта ввода/вывода
	 * @param ioClass класс объекта ввода/вывода
	 */
	public final void unregistrationOIObjectDelegates(Class<?> ioClass) {
		m_wLock.lock();
		try {
			m_TypeDelegates.removeIf(x -> x.getLeft().isAssignableFrom(ioClass));
		}
		finally {
			m_wLock.unlock();
		}
	}
	
	/**
	 * удалить из реестра делегаты для определённых классов объекта ввода/вывода
	 * @param ioArrClass массив классов объектов ввода/вывода
	 */
	public final void unregistrationOIObjectDelegates(Iterable<Class<?>> ioArrClass) {
		for(Class<?> ioClass : ioArrClass) { unregistrationOIObjectDelegates(ioClass); }
	}
	
	/**
	 * удалить делегаты по id сериализуемого типа
	 * @param typeID id сериализуемого типа
	 */
	public final void unregistrationTypeID(int typeID) {
		m_wLock.lock();
		try {
			if(m_VoidReadDelegates.containsKey(typeID)) { m_VoidReadDelegates.remove(typeID); }
			if(m_VoidWriteDelegates.containsKey(typeID)) { m_VoidWriteDelegates.remove(typeID); }
			for(Triple<Class<?>, IOMethodInfo.MethodType, HashMap<Integer, Pair<Method, Object>>> delOL : m_TypeDelegates)  {
				if(delOL.getRight().containsKey(typeID)) { delOL.getRight().remove(typeID); }
			}
			m_TypeDelegates.removeIf(x -> x.getRight().size() == 0);
		}
		finally {
			m_wLock.unlock();
		}
	}
	
	/**
	 * удалить делегаты по id сериализуемого типа
	 * @param typeArrID массив id сериализуемого типа
	 */
	public final void unregistrationTypeID(Iterable<Integer> typeArrID) {
		for(int typeID : typeArrID) { unregistrationTypeID(typeID); }
	}
	
	/**
	 * поиск мапа с делегатами по классу объекта ввода/вывода и по типу метода
	 * @param ioo объект ввода/вывода
	 * @param mt тип метода
	 * @return мап с делегатами
	 * @throws NotFountIOObjectClassException
	 */
	private final <IOObjectType> HashMap<Integer, Pair<Method, Object>> getAccessor(IOObjectType ioo, IOMethodInfo.MethodType mt) throws NotFountIOObjectClassException {
		m_rLock.lock();
		try {
			Class<?> iooClass = ioo.getClass();
			for(Triple<Class<?>, IOMethodInfo.MethodType, HashMap<Integer, Pair<Method, Object>>> delOL : m_TypeDelegates)  {
				if(delOL.getLeft().isAssignableFrom(iooClass) && (delOL.getMiddle() == mt)) {
					return delOL.getRight();
				}
			}
		}
		finally {
			m_rLock.unlock();
		}
		
		throw new NotFountIOObjectClassException();
	}
	
	/**
	 * создание объекта accessor'а для чтения по классу объекта ввода/вывода
	 * @param ioo объект ввода/вывода
	 * @return мап с делегатами
	 * @throws NotFountIOObjectClassException
	 */
	public final <IOObjectType> ReadAccessor getReadAccessor(IOObjectType ioo) throws NotFountIOObjectClassException {
		return new ReadAccessor(getAccessor(ioo, MethodType.read), ioo);
	}
	
	/**
	 * создание объекта accessor'а для записи по классу объекта ввода/вывода
	 * @param ioo объект ввода/вывода
	 * @return мап с делегатами
	 * @throws NotFountIOObjectClassException
	 */
	public final <IOObjectType> ReadAccessor getWriteAccessor(IOObjectType ioo) throws NotFountIOObjectClassException {
		return new ReadAccessor(getAccessor(ioo, MethodType.write), ioo);
	}
}
