package server;

import java.net.InetAddress;

public class Client {
	
	private InetAddress address;
	private int port;
	
	public Client(InetAddress address, int port)
	{
		this.address = address;
		this.port = port;
	}
	
	public InetAddress getAddress()
	{
		return this.address;
	}
	public int getPort()
	{
		return this.port;
	}

}