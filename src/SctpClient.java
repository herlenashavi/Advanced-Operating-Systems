import java.io.*;
import java.net.*;

import com.sun.nio.sctp.*;

import java.nio.*;

public class SctpClient implements Runnable {

	private Thread thread;
	private String threadName;
	private int nodeNumber;
	private String hostName;
	private int portNumber;
	Constants.MessageType sendMessage;
	String broadcastMessage;

	public SctpClient(Node node, Node neighbourNode,
			Constants.MessageType message) {

		threadName = neighbourNode.getNodeName();
		nodeNumber = node.getNodeNumber();
		hostName = neighbourNode.getNodeName() + ".utdallas.edu";
		portNumber = neighbourNode.getPortNumber();
		sendMessage = message;
		broadcastMessage = node.getBcastMessage();
	}

	public SctpClient(Node node, Constants.MessageType message) {

		threadName = node.getNodeName();
		hostName = Project1.testServerName + ".utdallas.edu";
		portNumber = Project1.testServerPortNum;
		sendMessage = message;
		nodeNumber = node.getNodeNumber();

	}

	public void run() {
		go();
	}

	private synchronized void go() {
		// TODO Auto-generated method stub

		// Buffer to hold messages in byte format
		ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.MESSAGE_SIZE);
		String msgString;
		
		try {
			
			SocketAddress socketAddress = new InetSocketAddress(hostName,
					portNumber);
			
			SctpChannel sctpChannel = SctpChannel.open(socketAddress, 0, 0);
			MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
			
			if (sendMessage == Constants.MessageType.BCAST_READY) {
				msgString = sendMessage.toString().concat(" " + nodeNumber);
				byteBuffer.put(msgString.getBytes());
			} else if (sendMessage == Constants.MessageType.ENTERING
					|| sendMessage == Constants.MessageType.EXITING) {
				msgString = sendMessage.toString().concat(" " + nodeNumber);
				byteBuffer.put(msgString.getBytes());
			} else if (sendMessage == Constants.MessageType.APP_READY) {
				msgString = sendMessage.toString().concat(" " + nodeNumber);
				byteBuffer.put(msgString.getBytes());
			} else if (sendMessage != Constants.MessageType.BROADCAST) {
				msgString = sendMessage.toString().concat(" " + nodeNumber);
				byteBuffer.put(msgString.getBytes());
			} else if (sendMessage == Constants.MessageType.BROADCAST) {
				byteBuffer.put(broadcastMessage.getBytes());
			}

			byteBuffer.flip();
			sctpChannel.send(byteBuffer, messageInfo);
			byteBuffer.clear();
			sctpChannel.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this, threadName);
			thread.start();
		}
	}
}
