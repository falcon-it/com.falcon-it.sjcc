package packet;

/**
 * исключение при возникновении ошибок ввода/вывода
 * в cause реальное исключение
 * @author Ilya Sokolov
 */
@SuppressWarnings("serial")
public final class PacketIOException extends PacketException {
	public PacketIOException(Throwable cause) {
		super(cause);
	}
}
