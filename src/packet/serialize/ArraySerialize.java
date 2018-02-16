package packet.serialize;

import java.lang.reflect.Array;

import packet.DynamicID;
import packet.PacketException;
import packet.PacketIOException;
import packet.Reader;
import packet.Registry;
import packet.Registry.DynamicIDTypeArrayException;
import packet.Registry.IsMultiLevelArrayException;
import packet.Registry.NotTypeIDException;
import packet.Serialize;
import packet.Writer;

/**
 * array
 * @author Ilya Sokolov
 */
public final class ArraySerialize extends BaseSerialize {
	public static final Class<?>[] classes = new Class<?>[] { ArraySerialize.class };
	public static final int[] classesIDs = new int[] { Registry.calculateThisClassID(classes[0]) };
	/* (non-Javadoc)
	 * @see packet.Serialize#supportedClasses()
	 */
	@Override
	public Class<?>[] supportedClasses() { return classes; }
	/* (non-Javadoc)
	 * @see packet.Serialize#supportedClassesIDs()
	 */
	@Override
	public int[] supportedClassesIDs() { return classesIDs; }
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
	 * по id типа (тип есть в реестре)
	 * int id типа
	 * int длина массива
	 */
	private static final byte USE_TYPE_ID = 1;
	/**
	 * по имени типа (типа нет в реестре)
	 * String тип элементов массива
	 * int длина массива
	 */
	private static final byte USE_CLASS_NAME = 2;
	/**
	 * динамческие структуры данных
	 * String тип элементов массива
	 * int длина массива
	 * перед каждым элементов указано int - id типа
	 */
	private static final byte USE_DYNAMIC = 4;
	
	/* (non-Javadoc)
	 * @see packet.Serialize#read(java.lang.Object, packet.Registry, packet.Reader)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader)
			throws PacketIOException {
		Serialize s = null;
		Class<?> c = null;
		int len = 0;
		byte tflag = reader.readByte(in);
		
		try {
			switch(tflag) {
				case USE_TYPE_ID:
					int tid = reader.readInt(in);
					s = reg.getSerializer(tid);
					c = s.classByID(tid);
					break;
				case USE_CLASS_NAME:
				case USE_DYNAMIC:
					s = reg.getSerializerByClass(Object.class);
					c = Class.forName(reader.readString(in));
					break;
			}
			
			len = reader.readInt(in);
			Object readArr = Array.newInstance(c, len);
			for(int i = 0; i < len; ++i) {
				if(tflag == USE_DYNAMIC) { s = reg.getSerializer(reader.readInt(in)); }
				Array.set(readArr, i, s.read(in, reg, reader));
			}
			
			return (T)readArr;
		} catch (NotTypeIDException | NotFoundTypeIDException | 
				IsMultiLevelArrayException | DynamicIDTypeArrayException | 
				 ClassNotFoundException e) {
			throw new PacketIOException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see packet.Serialize#write(java.lang.Object, java.lang.Object, packet.Registry, packet.Writer)
	 */
	@Override
	public <T, WriteObjectType> void write(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer)
			throws PacketIOException {
		Class<?> vc = v.getClass();
		if(!vc.isArray()) { throw new PacketIOException(new TypeIsNotArrayException()); }
		Class<?> cvc = vc.getComponentType();
		if(cvc.isArray()) { throw new PacketIOException(new IsMultiDimArrayException()); }
		
		int len = Array.getLength(v);
		if(DynamicID.class.isAssignableFrom(cvc)) {
			writer.writeByte(out, USE_DYNAMIC);
			writer.writeString(out, cvc.getName());
			writer.writeInt(out, len);
			for(int i = 0; i < len; ++i) {
				Object val = Array.get(v, i);
				int tid = ((DynamicID)val).calculateDynamicID();
				writer.writeInt(out, tid);
				try {
					reg.getSerializer(tid).write(out, val, reg, writer);
				} catch (NotTypeIDException e) {
					throw new PacketIOException(e);
				}
			}
		}
		else {
			try {
				int tid = Registry.calculateClassID(cvc);
				Serialize s = null; 
				
				try {
					s = reg.getSerializer(tid);
					writer.writeByte(out, USE_TYPE_ID);
					writer.writeInt(out, tid);
				} catch (NotTypeIDException e) {
					s = reg.getSerializerByClass(Object.class);
					writer.writeByte(out, USE_CLASS_NAME);
					writer.writeString(out, cvc.getName());
				}
				
				writer.writeInt(out, len);
				for(int i = 0; i < len; ++i) {
					s.write(out, Array.get(v, i), reg, writer);
				}
			} catch (IsMultiLevelArrayException | DynamicIDTypeArrayException|NotTypeIDException e1) {
				throw new PacketIOException(e1);
			}
		}
	}

}
