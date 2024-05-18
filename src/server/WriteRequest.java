package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public abstract class WriteRequest extends Request {
	
	protected byte[] ack = new byte[4];
	
	
	public WriteRequest(DatagramPacket packet, byte[] received, DatagramSocket serverSocket, boolean modeError)
	{
		super(packet, received, serverSocket, modeError);
		this.ack[0] = 0;
		this.ack[1] = 4;
	}
	
	
	
	@Override
	public void sendPacket() throws Exception
	{
		System.out.println("ACK ENVIADO: " + super.blockN + " " + super.getAddress());
		super.sendingPacket = new DatagramPacket(ack, 4, super.client.getAddress(), super.client.getPort());
		super.serverSocket.send(sendingPacket);
		super.blockN++;
	}
	
	
	protected int getBlockNumberInt(byte[] data)
	{
		int n2 = data[3] & 0xFF; //el 0xFF es para leer el byte que tiene signo, como su correspondiente unsigned
		int n1 = data[2] & 0xFF;
		
		byte[] byt1 = {0,0,0,0,0,0,0,0};
		byte[] byt2 = {0,0,0,0,0,0,0,0};
		
		for(int i = 0; i < 8; i++)
		{
			if(n1 != 0) {
				byt1[i] = (byte) (n1%2);
				n1 = n1/2;
			}
			if(n2 != 0)
			{
				byt2[i] = (byte) (n2%2);
				n2 = n2/2;
			}
		}
		
		int number = byt2[0];
		for(int i = 1; i < 16; i++)
		{
			if(i<8)
				number += (int)Math.pow( 2* byt2[i], i );
			else
				number += (int)Math.pow( 2* byt1[i-8], i );
		}
		return number;
	}
}
