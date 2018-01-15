package packet;

import java.lang.reflect.Method;
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
	private static final int DEFAULT_TYPE_ID = Serializer.calculateTypeIDByClass(Object.class);
	/**
	 * исключение, если id интерфейса уже есть
	 */
	@SuppressWarnings("serial")
	public static final class DelegateDuplicateException extends PacketException {
		public DelegateDuplicateException() { super(); }
	}
	/**
	 * исключение, если объект реализует своё чтение/зипись, но не реализует Cloneable
	 */
	@SuppressWarnings("serial")
	public static final class CloneableNotImplementedException extends PacketException {
		public CloneableNotImplementedException() { super(); }
	}
	
	/**
	 * доступ делегатам при сериализации
	 */
	public final class Accessor implements Cloneable {
		/**
		 * 
		 */
		private final HashMap<
						Integer, //id типа
						Pair<
							Method, //метод типа
							Object //экземпляр диспетчера, содержащего делегат
							>
						> m_TypeDelegatesItem;
		/**
		 * @param delItem
		 */
		public Accessor(HashMap<Integer, Pair<Method, Object>> delItem) {
			m_rLock.lock();
			m_TypeDelegatesItem = delItem;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#clone()
		 */
		@Override
		protected Object clone() throws CloneNotSupportedException {
			m_rLock.unlock();
			return super.clone();
		}
	}
	
	/*
	 * делегаты для чтения/записи объекта типа T
	 * нестатические методы классов:
	 * для чтения T readXXX(IOContext ioCTX, Registry.Accessor acc) 
	 * для записи void writeXXX(IOContext ioCTX, Registry.Accessor acc, T instance)
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
	 * @param serializerInst экземпляр сериалайзера
	 * @throws DelegateDuplicateException
	 * @throws CloneableNotImplementedException
	 */
	public final <SerializerType> void registrationSerializer(SerializerType serializerInst) 
			throws DelegateDuplicateException, CloneableNotImplementedException {
		Class<?> serClass = serializerInst.getClass();
		Method[] mets = serClass.getMethods();
		Class<IOMethodInfo> annMeth = IOMethodInfo.class;
		Class<?> voidClass = void.class;
		Class<?> voidLangClass = Void.class;
		Class<?> registryAccessorClass = Registry.Accessor.class;
		boolean isSS = (serializerInst instanceof StructureSerialize); //реализует интерфейс чтения/записи
		
		for(Method mi : mets) {
			if(mi.isAnnotationPresent(annMeth)) {
				IOMethodInfo mAnn = mi.getAnnotation(annMeth);
				boolean univ = mAnn.universal();
				IOMethodInfo.MethodType mt = mAnn.type();
				Class<?>[] prms = mi.getParameterTypes();
				Class<?> retT = mi.getReturnType();
				
				if( //проверяем сигнатуру метода
					!((mt == MethodType.read) && //метод для чтения
							(prms.length == 2) && //у него 2 параметр
							(!prms[0].isPrimitive()) && //первый параметер реализует IOContext
							(prms[1].isAssignableFrom(registryAccessorClass)) && //второй параметер Registry.Accessor
							(isSS ? 
									((retT == voidClass) || (retT == voidLangClass)) : //не возвращает значение
									((retT != voidClass) || (retT != voidLangClass))) //возвращает 
							) || 
					!((mt == MethodType.write) && //метод для записи
							(prms.length == (isSS ? 2 : 3)) &&  //если реализует StructureSerialize, то 2 параметер, иначе 3
							(!prms[0].isPrimitive()) && //первый параметер реализует IOContext
							(prms[1].isAssignableFrom(registryAccessorClass)) && //второй параметер Registry.Accessor
							((retT == voidClass) || (retT == voidLangClass))) //не возвращает значение
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
									m_VoidReadDelegates.put(_tid, new Pair<>(mi, serializerInst)); //добавим
									break;
								case write:
									if(m_VoidWriteDelegates.containsKey(_tid)) { throw new DelegateDuplicateException(); } //тип уже есть
									m_VoidWriteDelegates.put(_tid, new Pair<>(mi, serializerInst)); //добавим
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
									delOL.getRight().put(_tid, new Pair<>(mi, serializerInst)); //добавим
									_exist_type = true;
									break; //и прервём цикл
								}
							}
							
							if(!_exist_type) {
								//не нашли тип создадим
								Triple<Class<?>, IOMethodInfo.MethodType, HashMap<Integer, Pair<Method, Object>>> newDelOL = new Triple<>();
								newDelOL.putLeft(prms[0]);
								newDelOL.putMiddle(mAnn.type());
								newDelOL.getRight().put(_tid, new Pair<>(mi, serializerInst));
								m_TypeDelegates.add(newDelOL);
							}
						}
					}
					finally {
						m_wLock.unlock();
					}
				}
			}
		}
	}
	
	/**
	 * регистрация массива сериалайзеров
	 * @param serializerArrInst массив сериалайзеров
	 * @throws DelegateDuplicateException
	 * @throws CloneableNotImplementedException
	 */
	public final <SerializerType> void registrationSerializer(Iterable<SerializerType> serializerArrInst) 
			throws DelegateDuplicateException, CloneableNotImplementedException {
		for(SerializerType serializerInst : serializerArrInst) {
			registrationSerializer(serializerInst);
		}
	}
	
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
					}
				}
				finally {
					m_wLock.unlock();
				}
			}
		}
	}
	
	public final <SerializerType> void unregistrationSerializer(Iterable<SerializerType> serializerArrInst) {
		for(SerializerType serializerInst : serializerArrInst) {
			unregistrationSerializer(serializerArrInst);
		}
	}
	
	public final void unregistrationOIObjectDelegates(Class<?> ioClass) {
		m_wLock.lock();
		try {
			//переберём типы
			for(Triple<Class<?>, IOMethodInfo.MethodType, HashMap<Integer, Pair<Method, Object>>> delOL : m_TypeDelegates)  {
				if(delOL.getLeft().isAssignableFrom(ioClass)) {}
			}
		}
		finally {
			m_wLock.unlock();
		}
	}
}
