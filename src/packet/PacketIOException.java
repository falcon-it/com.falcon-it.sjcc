package packet;

@SuppressWarnings("serial")
public final class PacketIOException extends PacketException {
	public PacketIOException(Throwable cause) {
		super(cause);
	}
	public PacketIOException(String message) {
		super(message);
	}
}
