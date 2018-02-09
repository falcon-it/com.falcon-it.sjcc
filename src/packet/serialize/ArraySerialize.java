package packet.serialize;

import java.lang.reflect.Array;
import java.util.LinkedList;

import packet.DynamicID;
import packet.PacketException;
import packet.PacketIOException;
import packet.Reader;
import packet.Registry;
import packet.Registry.NotTypeIDException;
import packet.Registry.TypeIsArrayException;
import packet.Serialize;
import packet.Writer;
import utils.Pair;

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
public final class ArraySerialize implements Serialize {
	/**
	 * переданный класс или экзмепляр данных является массивом
	 */
	@SuppressWarnings("serial")
	public static final class TypeIsNotArrayException extends PacketException {
		public TypeIsNotArrayException() { super(); }
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
	private static final byte IS_SUB_ARRAY = 8;
	/**
	 * начальный индекс массива, указывающий на то что перебор массива не производился
	 */
	private static final int NOT_INIT_INDEX = -1;
	
	private final <ReadObjectType> boolean readImpl(
			ReadObjectType in, 
			Registry reg, 
			Reader<ReadObjectType> reader, 
			LinkedList<Pair<Object, Integer>> stack) throws PacketIOException {
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader)
			throws PacketIOException {
		LinkedList<Pair<Object, Integer>> stack = new LinkedList<>();
		while(true) {
			if(!readImpl(in, reg, reader, stack)) { break; }
		}
		return (T)stack.getFirst().getFirst();
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
						writer.writeString(out, o.getClass().getName());
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
		LinkedList<Pair<Object, Integer>> stack = new LinkedList<>();
		stack.add(new Pair<>(v, NOT_INIT_INDEX));
		do {
			writeImpl(out, reg, writer, stack);
		}
		while(stack.size() > 0);
	}

}
