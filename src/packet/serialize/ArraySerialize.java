package packet.serialize;

import java.lang.reflect.Array;
import java.util.LinkedList;

import packet.DynamicID;
import packet.PacketException;
import packet.PacketIOException;
import packet.Reader;
import packet.Registry;
import packet.Registry.NotTypeIDException;
import packet.Serialize;
import packet.Writer;
import utils.Pair;
import utils.Triple;

/**
 * array
 * формат:
 * byte: битовая маска из констант
 * [
 * int: id типов элементов массива
 *	|
 * String: имя типа
 * ]
 * [int]: количество элементов массива
 * @author Ilya Sokolov
 */
public final class ArraySerialize extends BaseSerialize {
	public static final Class<?>[] classes = new Class<?>[] { ArraySerialize.class };
	public static final int[] classesID = new int[] { Registry.calculateClassID(classes[0]) };
	/* (non-Javadoc)
	 * @see packet.Serialize#classes()
	 */
	@Override
	public Class<?>[] classes() { return classes; }
	/* (non-Javadoc)
	 * @see packet.Serialize#ids(int)
	 */
	@Override
	public int[] ids() { return classesID; }
	/**
	 * переданный класс или экзмепляр данных является массивом
	 */
	@SuppressWarnings("serial")
	public static final class TypeIsNotArrayException extends PacketException {
		public TypeIsNotArrayException() { super(); }
	}
	/**
	 * это многомерный массив
	 */
	@SuppressWarnings("serial")
	public static final class IsMultiDimArrayException extends PacketException {
		public IsMultiDimArrayException() { super(); }
	}
	/**
	 * следующим идёт поле int с id типа
	 * флаг исключает флаги USE_CLASS_NAME & USE_DYNAMIC_ID_TYPES & IS_ARRAY
	 * подзаголовок элемента отсутствует
	 */
	private static final byte USE_TYPE_ID = 1;
	/**
	 * следующим идёт поле String с именем типа
	 * флаг исключает флаги USE_TYPE_ID & USE_DYNAMIC_ID_TYPES & IS_ARRAY
	 * подзаголовок элемента отсутствует
	 */
	private static final byte USE_CLASS_NAME = 2;
	/**
	 * важен для элементов массива
	 * если флаг установлен, то элементы массива тоже являются массивами
	 * подзаголовок элемента обязателен
	 * тип берётся из подзаголовка элемента
	 */
	private static final byte IS_SUB_ARRAY = 4;
	/**
	 * начальный индекс массива, указывающий на то что перебор массива не производился
	 */
	private static final int NOT_INIT_INDEX = -1;
	
	private final <ReadObjectType> boolean readImpl(
			ReadObjectType in, 
			Registry reg, 
			Reader<ReadObjectType> reader, 
			LinkedList<Triple<Object, Integer, Integer>> stack) throws PacketIOException {
		byte tl = reader.readByte(in);
		
		if((stack.size() > 0)) {
			//проверяем состояние индекса массива
			Triple<Object, Integer, Integer> last = stack.getLast();
			if((last.getMiddle() < 0) || 
					(last.getMiddle() >= last.getRight())) {
				throw new PacketIOException(new ArrayIndexOutOfBoundsException());
			}
		}
		
		try {
			switch(tl) {
				case USE_TYPE_ID:
					//простой тип - не массив - есть в реестре
					int tid = reader.readInt(in);
					int l1 = reader.readInt(in);
					Serialize s1 = reg.getSerializer(tid);
					Object arr1 = Array.newInstance(s1.classByID(tid), l1);
					for(int i = 0; i < l1; ++i) {
						Array.set(arr1, i, s1.read(in, reg, reader));
					}
					if(stack.size() == 0) {//если стек пустой, то добавим его - это результат
						stack.add(new Triple<>(arr1, NOT_INIT_INDEX, NOT_INIT_INDEX));
					}
					else {//в стеке есть массив массивов - добавим этом массив туда
						Triple<Object, Integer, Integer> stackLast = stack.getLast();
						Array.set(stackLast.getLeft(), stackLast.getMiddle(), arr1);
						stackLast.putMiddle(stackLast.getMiddle());
					}
					break;
				case USE_CLASS_NAME:
					//простой тип - не массив - нет в реестре
					Class<?> c2 = Class.forName(reader.readString(in));
					int l2 = reader.readInt(in);
					Object arr2 = Array.newInstance(c2, l2);
					Serialize so2 = reg.getSerializerByClass(Object.class);
					for(int i = 0; i < l2; ++i) {
						switch(reader.readByte(in)) {
							case USE_TYPE_ID://тип есть в реестре
								Serialize sti = reg.getSerializer(reader.readInt(in));
								Array.set(arr2, i, sti.read(in, reg, reader));
								break;
							case USE_CLASS_NAME://тип записан как объект
								Array.set(arr2, i, so2.read(in, reg, reader));
								break;
						}
					}
					if(stack.size() == 0) {//если стек пустой, то добавим его - это результат
						stack.add(new Triple<>(arr2, NOT_INIT_INDEX, NOT_INIT_INDEX));
					}
					else {//в стеке есть массив массивов - добавим этом массив туда
						Triple<Object, Integer, Integer> stackLast = stack.getLast();
						Array.set(stackLast.getLeft(), stackLast.getMiddle(), arr2);
						stackLast.putMiddle(stackLast.getMiddle());
					}
					break;
				case IS_SUB_ARRAY:
					//массив массивов
					Class<?> c3 = Class.forName(reader.readString(in));
					int l3 = reader.readInt(in);
					Object arr3 = Array.newInstance(Object.class, l3);
					if(stack.size() > 0) {//добавим его как элемент вышестоящего массива
						Triple<Object, Integer, Integer> stackLast = stack.getLast();
						Array.set(stackLast.getLeft(), stackLast.getMiddle(), arr3);
						stackLast.putMiddle(stackLast.getMiddle());
					}
					if(l3 > 0) {//надо прочитать подэлементы
						stack.add(new Triple<>(arr3, 0, l3));
					}
					break;
				default://прочитан неправильный аргумент
					throw new PacketIOException(new IllegalArgumentException());
			}
			Triple<Object, Integer, Integer> stackLast = stack.getLast();
			if(stackLast.getRight() != NOT_INIT_INDEX) {//если нет вложенных элементов
				if(stackLast.getRight() == (stackLast.getMiddle() + 1)) {//заполнен последний элемент массива
					if(stack.size() > 1) {
						//элемент не последний - удалим из стека
						stack.removeLast();
						return true;
					}
					else { return false; }//заполнен последний элемент верхнего массива массивов
				}
				else { return true; }
			}
			return false;
		}
		catch(NotTypeIDException|NotFoundTypeIDException|NegativeArraySizeException | ClassNotFoundException | CloneNotSupportedException e) {
			throw new PacketIOException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader)
			throws PacketIOException {
		LinkedList<Triple<Object, Integer, Integer>> stack = new LinkedList<>();
		while(true) {
			if(!readImpl(in, reg, reader, stack)) { break; }
		}
		return (T)stack.getFirst().getLeft();
	}
	
	private final <WriteObjectType> void writeImpl(
			WriteObjectType out, 
			Registry reg, 
			Writer<WriteObjectType> writer, 
			LinkedList<Pair<Object, Integer>> stack) 
			throws PacketIOException {
		Pair<Object, Integer> item = stack.getLast();
		Object v = item.getFirst();
		Class<?> vc = v.getClass();
		if(!vc.isArray()) { throw new PacketIOException(new TypeIsNotArrayException()); }
		
		Class<?> vcc = vc.getComponentType();
		if(vcc.isArray()) {
			int len = Array.getLength(v);
			if(item.getSecond() == NOT_INIT_INDEX) {
				//если массив состоит из массивов
				//записываем заголовок и начинаем перебирать элемненты
				writer.writeByte(out, IS_SUB_ARRAY);
				writer.writeString(out, v.getClass().getName());
				writer.writeInt(out, len);
				if(len > 0) {
					item.putSecond(0);
					stack.add(new Pair<>(Array.get(v, 0), NOT_INIT_INDEX));
				}
				return;
			}
			else {
				//записали какой-то элемент
				//если есть ещё элементы передвинем на следующий
				//или вообще завершим
				int currIndex = item.getSecond();
				++currIndex;
				if(currIndex < len) { 
					item.putSecond(currIndex);
					stack.addLast(new Pair<>(Array.get(v, 0), NOT_INIT_INDEX));
				}
				else { stack.removeLast(); }
				return;
			}
		}
		else {
			int len = Array.getLength(v);
			int tid = Registry.calculateClassID(vcc);
			try {
				if(reg.containsTypeID(tid) && 
						!vcc.isAssignableFrom(DynamicID.class)) {
					Serialize s = reg.getSerializer(tid);
					writer.writeByte(out, USE_TYPE_ID);
					writer.writeInt(out, tid);
					writer.writeInt(out, len);
					for(int i = 0; i < len; ++i) {
						s.write(out, Array.get(v, i), reg, writer);
					}
				}
				else {
					Serialize s = reg.getSerializerByClass(Object.class);
					writer.writeByte(out, USE_CLASS_NAME);
					writer.writeString(out, v.getClass().getName());
					writer.writeInt(out, len);
					for(int i = 0; i < len; ++i) {
						Object o = Array.get(v, i);
						tid = Registry.calculateInstanceID(o);
						if(reg.containsTypeID(tid)) {
							writer.writeByte(out, USE_TYPE_ID);
							writer.writeInt(out, tid);
							Serialize so = reg.getSerializer(tid);
							so.write(out, o, reg, writer);
						}
						else {
							writer.writeByte(out, USE_CLASS_NAME);
							//writer.writeString(out, o.getClass().getName());
							s.write(out, o, reg, writer);
						}
					}
				}
			}
			catch(CloneNotSupportedException|NotTypeIDException e) {
				throw new PacketIOException(e);
			}
		}
		
		stack.removeLast();
	}

	@Override
	public <T, WriteObjectType> void write(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer)
			throws PacketIOException {
		Class<?> vc = v.getClass();
		if(!vc.isArray()) { throw new PacketIOException(new TypeIsNotArrayException()); }
		if(vc.getComponentType().isArray()) { throw new PacketIOException(new IsMultiDimArrayException()); }
		
		int len = Array.getLength(v);
	}

}
