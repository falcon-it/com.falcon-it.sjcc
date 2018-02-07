package packet.serialize;

import java.lang.reflect.Array;

import packet.DynamicID;
import packet.PacketException;
import packet.PacketIOException;
import packet.Reader;
import packet.Registry;
import packet.Registry.NotTypeIDException;
import packet.Serialize;
import packet.Writer;

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
	/**
	 * для основного заголовка всегда установлен
	 * важен для подзаголовков элементов массива
	 * если не установлен, то элемент не является массивом и длина не устанавливается (1 элемент)
	 */
	public static final byte IS_ARRAY = 16;
	
	@Override
	public <T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader)
			throws PacketIOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private final <T, WriteObjectType> void writeSubArrays(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer) {
		
	}

	@Override
	public <T, WriteObjectType> void write(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer)
			throws PacketIOException {
		Class<?> vc = v.getClass();
		if(!vc.isArray()) { throw new PacketIOException(new TypeIsNotArrayException()); }
		
		Class<?> vcc = vc.getComponentType();
		if(vcc.isArray()) {
			writeSubArrays(out, v, reg, writer);
			return;//??
		}
		else if(vcc.isAssignableFrom(DynamicID.class)) {
			try {
				Serialize s = reg.getSerializerByClass(vcc);
				writer.writeByte(out, (byte) (IS_ARRAY | USE_DYNAMIC_ID_TYPES));
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
				Serialize s = reg.getSerializerByClass(vcc);
			}
			catch(NotTypeIDException e) {
				
			} catch (CloneNotSupportedException e) {
				throw new PacketIOException(e);
			}
		}
	}

}
