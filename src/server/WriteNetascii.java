package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class WriteNetascii extends WriteRequest{
	
	private BufferedWriter fileWriter;
	
	public WriteNetascii(DatagramPacket packet, byte[] received, DatagramSocket serverSocket, boolean modeError)
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
				this.fileWriter = new BufferedWriter(new FileWriter(super.fileName));
				super.ack[2] = 0;
				super.ack[3] = 0;
				super.sendPacket();
			}
		}
		catch(Exception e)
		{
			try { this.fileWriter.close(); } catch(Exception ex) {}
			super.sendError(0);
		}
	}

	//Overload
	public void processPacket(byte[] data, int lenght) 
	{	
		try
		{
			int n = super.getBlockNumberInt(data);
			if(super.blockN == n)
			{
				int aux = lenght-4;
				int size = aux;
				for(int i = 4; i < lenght; i++) {
					if(data[i] == 13)
						aux--;
				}
				byte[] onlyData = new byte[aux];
				int cont = 0;
				for(int i = 0; i < size; i++)
				{
					if(data[i+4] != 13) {
						onlyData[cont] = data[i+4];
						cont++;
					}
				}	
				String toWrite = new String(onlyData, StandardCharsets.US_ASCII);
				this.fileWriter.write(toWrite);
				super.ack[2] = data[2];
				super.ack[3] = data[3];
				super.sendPacket();
				if(lenght < 516)
				{
					System.out.println("Se termina request " + super.client.getAddress());
					fileWriter.close();
					TFTPServer.requests.remove(this);
				}
			}
			else if(blockN < n)
			{
				fileWriter.close();
				sendError(0);
			}			
		}
		catch(Exception e)
		{
			try { this.fileWriter.close(); } catch(Exception ex) {}
			super.sendError(0);
		}
	
	}

}
