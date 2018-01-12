package packet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import packet.IOMethodType.MethodType;
import utils.Pair;
import utils.Triple;

/**
 * реестр делегатов для ввод/вывода
 * @author Ilya Sokolov
 */
public final class Registry {
	public final class Accessor {
		
	}
	
	/**
	 * список делегав ввода/вывода
	 */
	private final LinkedList<
					Triple<
						Class<?>, //класс объекта для чтениея/записи
						IOMethodType.MethodType, //тип делегата - чтение/запись
						HashMap<
							Integer, //id типа
							Pair<
								Method, //метод типа
								Object //экземплар диспетчера, содержащего делегат
								>
							>
						>
					> m_delegates = new LinkedList<>();
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
	
	public final <SerializerType> void registrationSerializer(SerializerType serializerInst) {
		Class<?> serClass = serializerInst.getClass();
		Method[] mets = serClass.getMethods();
		Class<IOMethodType> annMeth = IOMethodType.class;
		Class<?> voidClass = void.class;
		Class<?> voidLangClass = Void.class;
		boolean isSS = (serializerInst instanceof StructureSerialize);
		
		for(Method mi : mets) {
			if(mi.isAnnotationPresent(annMeth)) {
				IOMethodType mAnn = mi.getAnnotation(annMeth);
				Class<?>[] prms = mi.getParameterTypes();
				Class<?> retT = mi.getReturnType();
				//IOMethodType.MethodType type = mAnn.type();
				//boolean general = mAnn.general();
				
				if(
					((mAnn.type() == MethodType.read) && (prms.length == 1) && !prms[0].isPrimitive() && (isSS ? ((retT == voidClass) ||  (retT == voidLangClass)) : ((retT != voidClass) ||  (retT != voidLangClass)))) ||
					((mAnn.type() == MethodType.read) && (prms.length == 1) && !prms[0].isPrimitive() && (isSS ? ((retT == voidClass) ||  (retT == voidLangClass)) : ((retT != voidClass) ||  (retT != voidLangClass)))) 
						) {
					
				}
			}
		}
	}
}
