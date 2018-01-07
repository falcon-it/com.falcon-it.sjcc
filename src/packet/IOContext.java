package packet;

/**
 * интефейс контекста ввода/вывода данных
 * @author Ilya Sokolov
 */
public interface IOContext {
	boolean isUseName();
	boolean isUseMetaData();
	<IOObjectType> IOObjectType getCurrentIOObject();
}
