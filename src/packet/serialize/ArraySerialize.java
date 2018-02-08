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
	public static final byte USE_TYPE_ID = 1;
	/**
	 * следующим идёт поле String с именем типа
	 * флаг исключает флаги USE_TYPE_ID & USE_DYNAMIC_ID_TYPES & IS_ARRAY
	 * подзаголовок элемента отсутствует
	 */
	public static final byte USE_CLASS_NAME = 2;
	/**
	 * следующее поле с типом элементов отсутствует
	 * флаг исключает флаги USE_TYPE_ID & USE_CLASS_NAME
	 * исключается тип элемента
	 * подзаголовок элемента обязателен
	 */
	public static final byte USE_DYNAMIC_ID_TYPES = 4;
	/**
	 * важен для элементов массива
	 * если флаг установлен, то элементы массива тоже являются массивами
	 * подзаголовок элемента обязателен
	 * тип берётся из подзаголовка элемента
	 */
	public static final byte IS_SUB_ARRAY = 8;
	private static final int NOT_INIT_INDEX = -1;
	/**
	 * для основного заголовка всегда установлен
	 * важен для подзаголовков элементов массива
	 * если не установлен, то элемент не является массивом и длина не устанавливается (1 элемент)
	 */
	//public static final byte IS_ARRAY = 16;
	
	@Override
	public <T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader)
			throws PacketIOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private final <WriteObjectType> void writeImpl(
			WriteObjectType out, 
			Registry reg, 
			Writer<WriteObjectType> writer, 
			LinkedList<Pair<Object, Integer>> state) 
			throws PacketIOException {
		Pair<Object, Integer> currItem = state.getLast();
		Object v = currItem.getFirst();
		
		Class<?> vc = v.getClass();
		if(!vc.isArray()) { throw new PacketIOException(new TypeIsNotArrayException()); }
		
		Class<?> vcc = vc.getComponentType();
		if(vcc.isArray()) {
			//записываем заголовок и начинаем перебирать элемнеты
			int len = Array.getLength(v);
			if(currItem.getSecond() == NOT_INIT_INDEX) {
				writer.writeByte(out, IS_SUB_ARRAY);
				writer.writeInt(out, len);
				state.add(new Pair<>());
			}
			return;
		}
		else if(vcc.isAssignableFrom(DynamicID.class)) {
			try {
				Serialize s = reg.getSerializerByClass(vcc);
				writer.writeByte(out, USE_DYNAMIC_ID_TYPES);
				int len = Array.getLength(v);
				writer.writeInt(out, len);
				for(int i = 0; i < len; ++i) {
					s.write(out, Array.get(v, i), reg, writer);
				}
			}
			catch(NotTypeIDException|CloneNotSupportedException e) {
				throw new PacketIOException(e);
			}
		}
		else {
			try {
				int tid = Registry.calculateClassID(vcc);
				Serialize s = reg.getSerializer(tid);
				writer.writeByte(out, USE_TYPE_ID);
				writer.writeInt(out, tid);
				int len = Array.getLength(v);
				writer.writeInt(out, len);
				for(int i = 0; i < len; ++i) {
					s.write(out, Array.get(v, i), reg, writer);
				}
			}
			catch(NotTypeIDException e) {
				try {
					Serialize s = reg.getSerializerByClass(Object.class);
					writer.writeByte(out, USE_CLASS_NAME);
					writer.writeString(out, vcc.getName());
					int len = Array.getLength(v);
					writer.writeInt(out, len);
					for(int i = 0; i < len; ++i) {
						s.write(out, Array.get(v, i), reg, writer);
					}
				} catch (NotTypeIDException | CloneNotSupportedException e1) {
					throw new PacketIOException(e1);
				}
				
			} catch (CloneNotSupportedException|TypeIsArrayException e) {
				throw new PacketIOException(e);
			}
		}
	}

	@Override
	public <T, WriteObjectType> void write(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer)
			throws PacketIOException {
		LinkedList<Pair<Object, Integer>> state = new LinkedList<>();
		state.add(new Pair<>(v, NOT_INIT_INDEX));
		do {
			writeImpl(out, reg, writer, state);
		}
		while(state.size() == 0);
	}

}
