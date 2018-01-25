package packet;

import java.util.HashMap;

/**
 * реестр типов
 * @author Ilya Sokolov
 */
public final class Registry {
	private final HashMap<Integer, TypeSerializer> m_TypeMap = new HashMap<>();
	
	public static int calculateClassID(Class<?> c) {
		return c.getName().hashCode();
	}
	
	/**
	 * реализация интерфейса для boolean
	 */
	private final class BooleanTypeSerializer implements TypeSerializer {
		/* (non-Javadoc)
		 * @see packet.TypeSerializer#read(java.lang.Object, packet.Registry, packet.Reader)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T, ReadObjectType> T read(ReadObjectType in, Registry reg, Reader<ReadObjectType> reader) throws PacketIOException {
			return (T)(Boolean)reader.readBoolean(in);
		}
		
		/* (non-Javadoc)
		 * @see packet.TypeSerializer#write(java.lang.Object, java.lang.Object, packet.Registry, packet.Writer)
		 */
		@Override
		public <T, WriteObjectType> void write(WriteObjectType out, T v, Registry reg, Writer<WriteObjectType> writer) throws PacketIOException {
			writer.writeBoolean(out, (Boolean)v);
		}
	}
	
	public Registry() {
		
	}
}
