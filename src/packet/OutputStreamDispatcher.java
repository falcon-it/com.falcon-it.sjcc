package packet;

import java.io.IOException;
import java.io.OutputStream;

/**
 * диспетчер, перенаправляющий данные реальному потоку
 * @author Ilya Sokolov
 */
public final class OutputStreamDispatcher extends OutputStream {
	/**
	 * поток для записи
	 */
	public OutputStream m_RealStream = null;
	
	/**
	 * получить поток для записи
	 * @return поток для записи
	 */
	public OutputStream getStream() { return m_RealStream; }
	/**
	 * установить поток для записи
	 * @param out поток для записи
	 */
	public void putStream(OutputStream out) {
		m_RealStream = out;
	}
	
	public OutputStreamDispatcher() { }
	public OutputStreamDispatcher(OutputStream out) { m_RealStream = out; }
	
	@Override
	public void close() throws IOException {
		m_RealStream.close();
	}
	
	@Override
	public void flush() throws IOException {
		m_RealStream.flush();
	}

	@Override
	public void write(int b) throws IOException {
		m_RealStream.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		m_RealStream.write(b, off, len);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		m_RealStream.write(b);
	}
}
