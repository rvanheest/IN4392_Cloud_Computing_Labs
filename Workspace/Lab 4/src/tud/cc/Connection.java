package tud.cc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

class Connection
	implements AutoCloseable
{
	public final Socket socket;
	public final ObjectOutputStream out;
	public final ObjectInputStream in;
	
	public Connection(Socket socket) throws IOException
	{
		this.socket = socket;
		this.in = new ObjectInputStream(socket.getInputStream());
		this.out = new ObjectOutputStream(socket.getOutputStream());
	}

	@Override
	public void close() throws Exception 
	{
		this.in.close();
		this.out.close();
		this.socket.close();
	}
	
	public void send(Object o) throws IOException
	{
		synchronized (out)
		{
			out.writeObject(o);
		}
	}
	
	public <T> T receive() throws ClassNotFoundException, IOException
	{
		synchronized (in)
		{
			T readObject = (T) this.in.readObject();
			return readObject;
		}
	}
	
	@Override
	public int hashCode() 
	{
		return Objects.hash(socket, out, in);
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof Connection)
		{
			Connection other = (Connection) obj;
			return this.socket.equals(other.socket);
		}
		return false;
	}
}