package sej;

import java.io.IOException;
import java.io.OutputStream;

import sej.runtime.Engine;

public interface SaveableEngine extends Engine
{

	public void saveTo( OutputStream _stream ) throws IOException;

}
