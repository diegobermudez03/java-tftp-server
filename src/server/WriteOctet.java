package server;

import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class WriteOctet extends WriteRequest{
	
	private FileOutputStream file;
	
	public WriteOctet(DatagramPacket packet, byte[] received, DatagramSocket serverSocket, boolean modeError)
	{
		super(packet, received, serverSocket, modeError);
		try {
			File name = new File(super.fileName);
			if(name.exists())
			{
				super.sendError(6);
			}
			else
			{
				this.file = new FileOutputStream(name, true);
				this.ack[2] = 0;
				this.ack[3] = 0;
				this.sendPacket();
			}
		}
		catch(Exception e)
		{
			try { file.close(); } catch(Exception ex) {}
			super.sendError(0);
		}
	}
	
	
	@Override
	public void processPacket(byte[] data, int lenght)
	{
		try
		{
			int n = getBlockNumberInt(data);
			if(super.blockN == n)
			{
				this.file.write(data, 4, lenght - 4);
				super.ack[2] = data[2];
				super.ack[3] = data[3];
				sendPacket();
				if(lenght < 516)
				{
					System.out.println("Se termina request " + super.client.getAddress());
					file.close();
					TFTPServer.requests.remove(this);
				}
			}
			else if(blockN < n)
			{
				sendError(0);
			}			
		}
		catch(Exception e)
		{
			try { file.close(); } catch(Exception ex) {}
			super.sendError(0);
		}
	}

}
