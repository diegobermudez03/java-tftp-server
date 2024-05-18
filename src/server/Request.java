package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public abstract class Request {
	
	protected int blockN = 0;
	protected String fileName;
	protected DatagramSocket serverSocket;
	protected Client client;
	protected DatagramPacket sendingPacket;
	
	public Request(DatagramPacket packet, byte[] received, DatagramSocket serverSocket, boolean modeError)
	{
		this.client = new Client(packet.getAddress(), packet.getPort());
		this.fileName = getFileName(received);
		this.serverSocket = serverSocket;
		
		if(!modeError)
			TFTPServer.requests.add(this);
		else
			sendError(4);
	}
	
	protected String getFileName(byte[] received)
	{
		String filename = "";
		int i = 2;
		while(true)
		{
			if(received[i] == 0)
				break;
			else
			{
				filename += (char)received[i];
				i++;
			}
		}
		return filename;
	}
	
	
	public InetAddress getAddress()
	{
		return client.getAddress();
	}
	
	
	public void sendError(int code)
	{
		try {
			String sentence = null;
			byte[] error = new byte[100];
			error[0] = 0;
			error[1] = 5;
			error[2] = 0;
			error[3] = (byte)code;
			int n = 0;
			switch(code)
			{
			case 0:
				sentence = "Unknown Issue";
				break;
			case 1:
				sentence = "File Not Found";
				break;
			case 2:
				sentence = "Access violation, file has permission restrictions";
				break;
			case 4:
				sentence = "Illegal TFTP action, no able mode";
				break;
			case 6:
				sentence = "File Already Exists";
				break;
			}
			for(int i = 0; i < sentence.length(); i++)
			{
				error[i+4] = (byte)sentence.charAt(i);
			}
			n = sentence.length() + 4 + 1;
			error[n-1] = 0;
			System.out.println("ERROR EN REQUEST DE: " + this.client.getAddress() + " TIPO " + sentence);
			DatagramPacket errorPacket = new DatagramPacket(error, n, client.getAddress(), client.getPort());
			serverSocket.send(errorPacket);
		
			TFTPServer.requests.remove(this);
		}
		catch(Exception e) {
			TFTPServer.requests.remove(this);
		}
	}
	
	public abstract void sendPacket() throws Exception;
	
	
	public abstract void processPacket(byte[] data, int lenght);

}
