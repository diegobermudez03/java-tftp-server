package server;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;

public class TFTPServer 
{
	
	public static LinkedList<Request> requests = new LinkedList<>();
	
	
	public static void main(String[] args)
	{
		try
		{
			DatagramSocket serverSocket = new DatagramSocket(69);
			
			Runtime.getRuntime().addShutdownHook(new Thread(  () -> { 
				if(serverSocket.isClosed())  serverSocket.close(); }   )   );	
			
			byte[] receivedData;
			DatagramPacket packet;
			
			while(true)
			{
				receivedData = new byte[516];									
				packet = new DatagramPacket(receivedData, 516);
				serverSocket.receive(packet);
				byte[] received = receivedData;
				DatagramPacket pack = packet;	
													
				Thread thread = new Thread(   ()   -> {  
					forwardPacket(pack, received, serverSocket); 
					
				});
				thread.start();
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public static void forwardPacket(DatagramPacket packet, byte[] receivedData, DatagramSocket serverSocket)
	{
		switch(receivedData[1])
		{
		case 1: { byte mode = checkMode(packet, receivedData);
			switch(mode)
			{
			case 1: {new ReadOctet(packet, receivedData, serverSocket, false);};break;
			case 2: {new ReadNetascii(packet, receivedData, serverSocket, false);};break;
			default: {new ReadOctet(packet, receivedData, serverSocket, true);};break;
			}
		};break;
		case 2: {byte mode = checkMode(packet, receivedData);
			switch(mode)
			{
			case 1:{ new WriteOctet(packet, receivedData, serverSocket, false);};break;
			case 2: {new WriteNetascii(packet, receivedData, serverSocket, false);};break;
			default: {new WriteOctet(packet, receivedData, serverSocket, true);};break;
			}
		};break;
		case 3:
		case 4:
			synchronized(requests) {
				for(Request req: requests) {
					if(req.getAddress().toString().equals(packet.getAddress().toString()))
					{
						req.processPacket(receivedData, packet.getLength());
						break;
					}
				}
			}
			;break;
		default:		
		}
	}
	
	
	public static byte checkMode(DatagramPacket packet, byte[] data) 
	{
		boolean start = false;
		String mode = "";
		for(int i = 2; i < packet.getLength(); i++)
		{
			if(!start)
			{
				if(data[i] == 0)
					start = true;
			}
			else
			{
				if(data[i] != 0) 
					mode += (char)data[i];
				else
					break;
			}
		}
		if(mode.toUpperCase().equals("OCTET") )
			return 1;
		else if(mode.toUpperCase().equals("NETASCII") )
			return 2;
		else
			return 3;
	}
	
}
