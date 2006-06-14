package sej;

import java.io.IOException;
import java.io.OutputStream;

public interface SaveableEngine extends Engine
{

	public void saveTo( OutputStream _stream ) throws IOException;

}
