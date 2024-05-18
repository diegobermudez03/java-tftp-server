package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public abstract class ReadRequest extends Request{
	
	protected boolean ended = false;
	protected byte[] sending = new byte[516];
	protected int readed;
	
	public ReadRequest(DatagramPacket packet, byte[] received, DatagramSocket serverSocket, boolean modeError)
	{
		super(packet, received, serverSocket, modeError);
		this.sending[0] = 0;
		this.sending[1] = 3;
	}

	@Override
	public void processPacket(byte[] data, int lenght) 
	{
		if(this.sending[2] == data[2] && this.sending[3] == data[3] && data[1] == 4) 
		{
			try
			{
				System.out.println("ACK RECIBIDO: " + (data[2]&0xFF) + "-" + (data[3]&0xFF) + "  " + super.client.getAddress());
				if(!ended)
					sendPacket();
				else
				{
					System.out.println("Se termina request " + super.client.getAddress());
					TFTPServer.requests.remove(this);
				}
			}
			catch(Exception e) {
				super.sendError(0);
			}
		}
	}
	
	
	protected  void getBlockNumber()
	{
		int[] n1 = new int[8];
		int[] n2 = new int[8];
		
		int res;
		int block = super.blockN;
		for(int j = 0; j < 16; j++)
		{
			res = block%2;
			
			if(j <8)
			{
				n1[j] = res;
			}
			else
			{
				n2[j-8] = res;
			}
			block = block/2;
			if(super.blockN == 0) break;
		}
		
		sending[2] = binToInt(n2);
		sending[3] = binToInt(n1);
	}
	
	
	public static byte binToInt(int[] n)
	{
		int number = n[0];
		for(int i = 1; i < 8; i++)
		{
			number += (int)Math.pow( 2* n[i], i );
		}
		return (byte)number;
	}

}
