package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReadOctet extends ReadRequest{
	
	private FileInputStream file;
	
	public ReadOctet(DatagramPacket packet, byte[] received, DatagramSocket serverSocket, boolean modeError)
	{
		super(packet, received, serverSocket, modeError);
		File name = new File(super.fileName);
		if(!name.exists())
			super.sendError(1);
		else {
			try {
				this.file = new FileInputStream(name);
				sendPacket();
			}
			catch(FileNotFoundException e)
			{
				super.sendError(2);
			}
		}
	}
	

	@Override
	public void sendPacket() 
	{
		try
		{
			readed = file.read(sending, 4, 512); 
			if(readed != -1)	
			{
				super.blockN++;	
				getBlockNumber();
				super.sendingPacket = new DatagramPacket(sending, readed+4, super.client.getAddress(), super.client.getPort());	
			}
			else
				super.sendingPacket = new DatagramPacket(sending, 4, super.client.getAddress(), super.client.getPort());	
			super.serverSocket.send(super.sendingPacket); 
		
			if(readed < 512)	
			{
				ended = true;
				file.close();
			}
		}
		catch(Exception e)
		{
			try{ file.close(); } catch(Exception ex) {}
			super.sendError(0);
		}
	}
	

}
