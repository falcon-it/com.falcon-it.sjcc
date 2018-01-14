package packet;

/**
 * базовый класс исключений для пакетов
 * @author falcon-it
 */
@SuppressWarnings("serial")
public class PacketException extends Exception {
	public PacketException() { super(); }
	public PacketException(String message) 
	{ super(message); }
	public PacketException(Throwable cause) 
	{ super(cause); }
	public PacketException(
			String message, 
			Throwable cause) { 
		super(message, cause);
	}
	public PacketException(
			String message, 
			Throwable cause, 
			boolean enableSuppression, 
			boolean writableStackTrace) {
		super(message, 
				cause, 
				enableSuppression, 
				writableStackTrace);
	}
}
