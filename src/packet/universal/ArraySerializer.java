package packet.universal;

import packet.PacketException;

/**
 * сериализация массива
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
	 * список простых типов типов
	 * @return массив классов типов
	 */
	public static Class<?>[] getArrayDefaultClasses() {
		return new Class<?>[] { 
			boolean[].class, Boolean[].class,
			byte[].class, Byte[].class,
			char[].class, Character[].class,
			short[].class, Short[].class,
			int[].class, Integer[].class,
			long[].class, Long[].class,
			float[].class, Float[].class,
			double[].class, Double[].class,
			String[].class, Object[].class };
	}
	
	/**
	 * @param add массив элементов, которые надо добавить к массиву простых элементов
	 * @return результирующий массив
	 * @throws NotArrayClassException 
	 */
	public static Class<?>[] getArrayClasses(Class<?>[] add) throws NotArrayClassException {
		for(Class<?> ci : add) { if(!ci.isArray()) { throw new NotArrayClassException(); } }
		Class<?>[] _def = ArraySerializer.getArrayDefaultClasses();
		Class<?>[] _new = new Class<?>[_def.length + add.length];
		System.arraycopy(_def, 0, _new, 0, _def.length);
		System.arraycopy(add, 0, _new, _def.length, add.length);
		return _new;
	}
}
