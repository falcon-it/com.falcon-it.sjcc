package protocol.stack;

public class Main {
	public interface ReadEvent {
		<T> void read(T rd);
	}
	public void addReadListener(ReadEvent listener) {
		
	}
	
	public <T> void write(T wr) {
		
	}
}
