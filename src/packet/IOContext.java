package packet;

/**
 * интефейс контекста ввода/вывода данных
 * @author Ilya Sokolov
 */
public interface IOContext {
	/**
	 * @return true ввод/вывод имён
	 */
	boolean isUseName();
	/**
	 * @return true ввод/вывод метаданных
	 */
	boolean isUseMetaData();
	/**
	 * @return объект ввода/вывода
	 */
	<IOObjectType> IOObjectType getCurrentIOObject();
}
