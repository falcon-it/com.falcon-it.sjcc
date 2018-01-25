package packet;

import java.util.HashMap;

/**
 * реестр типов
 * @author Ilya Sokolov
 */
public final class Registry {
	public interface TypeSerializer {
		<T, ReaderObjectType> T read(ReaderObjectType in, Reader<?> reader);
		<T, WriteObjectType> void write(WriteObjectType out, T v, Writer<?> writer);
	}
	
	private final HashMap<Integer, TypeSerializer> m_TypeMap = new HashMap<>();
}
