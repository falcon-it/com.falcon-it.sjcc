package packet;

import java.lang.reflect.Array;

import packet.IOMethodInformation;
import packet.Registry;
import packet.Registry.ExecuteDelegateException;
import packet.Registry.NotFoundTypeIDException;
import packet.IOMethodInformation.MethodType;

/**
 * сериализация массива<br />
 * для сериализации массива любых объектов неужно передать массив Object[]<br />
 * можно зарегистрировать свой тип для элементов массива<br />
 * главное что бы сам тип был зарегистрирован в реестре<br /><br />
 * формат хранения данных следующий<br />
 * String - имя класса элементов массива<br />
 * int - количество элементов массива <br />
 * [] - массив элементов - размер в зависимости от типа
 * @author Ilya Sokolov
 */
public final class ArraySerializer {
	/**
	 * элемент не является массивом
	 */
	@SuppressWarnings("serial")
	public static final class NotArrayClassException extends PacketException {
		public NotArrayClassException() { super(); }
	}
	
	/**
	 * дефолтный массив классов сериализуемых массивов
	 */
	public static final Class<?>[] DEFAULT_ARRAY_CLASSES = 
			new Class<?>[] { 
				boolean[].class, Boolean[].class,
				byte[].class, Byte[].class,
				char[].class, Character[].class,
				short[].class, Short[].class,
				int[].class, Integer[].class,
				long[].class, Long[].class,
				float[].class, Float[].class,
				double[].class, Double[].class,
				String[].class, Object[].class };
	
	/**
	 * массив любого типа преобразовать в Object[]
	 * @param arrInstance массив любого типа
	 * @return массив Object[]
	 * @throws NotArrayClassException
	 */
	public static Object[] copyArrayToObjectArray(Object arrInstance) throws NotArrayClassException {
		Class<?> clazz = arrInstance.getClass();
		if(!clazz.isArray()) { throw new NotArrayClassException(); }
		Object[] oArr = new Object[Array.getLength(arrInstance)];
		System.arraycopy(arrInstance, 0, oArr, 0, oArr.length);
		return oArr;
	}
	
	/**
	 * возможно оргинизовать ввод/вывод любых типо, если они зарегистрированы в реестре
	 * @param add массив элементов, которые надо добавить к массиву простых элементов
	 * @return результирующий массив
	 * @throws NotArrayClassException 
	 */
	public static Class<?>[] getArrayClasses(Class<?>[] add) throws NotArrayClassException {
		for(Class<?> ci : add) { if(!ci.isArray()) { throw new NotArrayClassException(); } }
		Class<?>[] _new = new Class<?>[ArraySerializer.DEFAULT_ARRAY_CLASSES.length + add.length];
		System.arraycopy(ArraySerializer.DEFAULT_ARRAY_CLASSES, 0, _new, 0, ArraySerializer.DEFAULT_ARRAY_CLASSES.length);
		System.arraycopy(add, 0, _new, ArraySerializer.DEFAULT_ARRAY_CLASSES.length, add.length);
		return _new;
	}
	
	/*
	 * формат записи:
	 * String - имя класса элементов 
	 * int - длина массива
	 * [] - массив элементов
	 */
	
	@IOMethodInformation(type=MethodType.read, universal=true, classes={boolean[].class, Boolean[].class})
	public static Object read(Object read, Registry.ReadAccessor racc) 
			throws PacketIOException, NotFoundTypeIDException, ExecuteDelegateException, ClassNotFoundException {
		String className = racc.read(String.class, read);
		Class<?> clazz = Class.forName(className);
		int len = racc.read(int.class, read);
		Object readArray = Array.newInstance(clazz, len);
		for(int i = 0; i < len; ++i) {
			Array.set(readArray, i, racc.read(clazz, racc));
		}
		return readArray;
	}

	@IOMethodInformation(type=MethodType.write, universal=true)
	public final void writeByteArray(Object write, Registry.WriteAccessor wacc, Object arrInst) 
			throws PacketIOException, NotFoundTypeIDException, ExecuteDelegateException {
		Class<?> clazz = arrInst.getClass();
		Class<?> itemClass = clazz.getComponentType();
		wacc.write(write, itemClass.getName());
		wacc.write(write, Array.getLength(arrInst));
		for(int i = 0; i < Array.getLength(arrInst); ++i) {
			wacc.write(write, Array.get(arrInst, i), Registry.DEFAULT_TYPE_ID);
		}
	}
}
