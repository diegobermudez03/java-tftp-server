package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReadNetascii extends ReadRequest{
	
	private BufferedReader fileReader;
	private byte[] lineBytes;
	private int lastOne;
	private boolean preEnded = false;
	private boolean full = false;
	
	public ReadNetascii(DatagramPacket packet, byte[] received, DatagramSocket serverSocket, boolean modeError)
	{
		super(packet, received, serverSocket, modeError);
		File name = new File(super.fileName);
		if(!name.exists())
			super.sendError(1);
		else {
			try {
				this.fileReader = new BufferedReader(new FileReader(fileName));
				sendPacket();
			}
			catch(FileNotFoundException e)
			{
				super.sendError(2);
			}
			catch(Exception e)
			{
				super.sendError(0);
			}
		}
	}

	
	//Overload
	public void sendPacket() throws Exception
	{
		try {
			super.readed = 0;
			if(this.full)
			{
				int aux = lineBytes.length - lastOne;
				if(aux > 512)
					aux = 512;
				for(int i = 0; i < aux; i++)
				{
					super.sending[i+4] = lineBytes[lastOne];
					lastOne++;
					super.readed++;
					if(i == 511)
					{
						this.full = true;
						break;
					}
				}
			}
			this.full = false;
			String line;
			while(!this.full && !preEnded)
			{
				line = fileReader.readLine();
				if(line == null)
					this.preEnded = true;
				else
				{
					line += "\r\n";
					lineBytes = line.getBytes("US-ASCII");
					lastOne = 0;
					int aux = 512 - super.readed;
					if(lineBytes.length < aux)
						aux = lineBytes.length;
					else
						this.full = true;
					for(int i = 0; i < aux; i++)
					{
						super.sending[super.readed + 4] = lineBytes[i];
						lastOne++;
						super.readed++;
					}
				}
				
			}
			super.blockN++;
			super.getBlockNumber();
			super.sendingPacket = new DatagramPacket(sending, super.readed+4, super.client.getAddress(), super.client.getPort());	
			super.serverSocket.send(super.sendingPacket); 
			if(preEnded && lastOne==lineBytes.length)
			{
			super.ended = true;
				this.fileReader.close();
			}
		}
		catch(Exception e)
		{
			this.fileReader.close();
			super.sendError(0);
		}
	}

}
