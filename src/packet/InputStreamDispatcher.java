package packet;

import java.io.IOException;
import java.io.InputStream;

/**
 * диспетчер, перенаправляющий данные реальному потоку
 * @author Ilya Sokolov
 */
public final class InputStreamDispatcher extends InputStream {
	/**
	 * поток для чтения
	 */
	public InputStream m_RealStream = null;
	
	/**
	 * получить поток для чтения
	 * @return поток для чтения
	 */
	public InputStream getStream() { return m_RealStream; }
	/**
	 * установить поток для чтения
	 * @param in устанавливаемый поток
	 */
	public void putStream(InputStream in) {
		m_RealStream = in;
	}
	
	public InputStreamDispatcher() { }
	public InputStreamDispatcher(InputStream in) { m_RealStream = in; }
	
	@Override
	public int available() throws IOException {
		return m_RealStream.available();
	}
	
	@Override
	public void close() throws IOException {
		m_RealStream.close();
	}
	
	@Override
	public synchronized void mark(int readlimit) {
		m_RealStream.mark(readlimit);
	}
	
	@Override
	public boolean markSupported() {
		return m_RealStream.markSupported();
	}

	@Override
	public int read() throws IOException {
		return m_RealStream.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return m_RealStream.read(b);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return m_RealStream.read(b, off, len);
	}
	
	@Override
	public synchronized void reset() throws IOException {
		m_RealStream.reset();
	}
	
	@Override
	public long skip(long n) throws IOException {
		return m_RealStream.skip(n);
	}
}
