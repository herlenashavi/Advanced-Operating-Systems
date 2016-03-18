public class BroadcastServiceAPI {

	/**
	 * Method to enter critical section and perform broadcast
	 */
	public static void cs_enter() {
		String logData;
		SctpClient clientObject;

		logData = "CS request from Node "
				+ SctpServer.serverNode.getNodeNumber() + "'s application";
		LoggerClass.printToLogs(logData, SctpServer.filePath, false);

		// If node has the token
		if (SctpServer.haveToken) {
			// Critical Section
			SctpServer.csNonIdle = true;
			SctpServer.noOfCSEntries++;

			sendMessageToTestServer(Constants.MessageType.ENTERING);

			logData = "Node " + SctpServer.serverNode.getNodeNumber()
					+ " Inside Critical Section\n\n";
			LoggerClass.printToLogs(logData, SctpServer.filePath, false);
			logData = "Request " + SctpServer.noOfCSEntries;
			LoggerClass.printToLogs(logData, SctpServer.filePath, false);
		} else {
			logData = "Added to queue " + SctpServer.serverNode.getNodeNumber();
			LoggerClass.printToLogs(logData, SctpServer.filePath, false);
			SctpServer.requestQueue.add(SctpServer.serverNode.getNodeNumber());

			if (!SctpServer.requestSent) {
				Constants.MessageType sendMessage = Constants.MessageType.TOKEN_REQUEST;
				clientObject = new SctpClient(SctpServer.serverNode,
						SctpServer.requestParent, sendMessage);
				clientObject.start();
				logData = "Application at node "
						+ SctpServer.serverNode.nodeNumber
						+ " requesting for TOKEN!!";
				LoggerClass.printToLogs(logData, SctpServer.filePath, false);
				logData = "Message sent to "
						+ SctpServer.requestParent.nodeNumber + " "
						+ sendMessage + " " + SctpServer.serverNode.nodeNumber;
				LoggerClass.printToLogs(logData, SctpServer.filePath, false);
				SctpServer.requestSent = true;
			}

		}

		while (!SctpServer.haveToken) {
			System.out.print("");
			if (SctpServer.noOfCSEntries == (Project1.requestsPerNode - 1)
					&& SctpServer.serverNode.nodeNumber == 10) {
				SctpServer.haveToken = true;
			}
			continue;
		}

		logData = "Node " + SctpServer.serverNode.nodeNumber
				+ " SctpServer.haveToken " + SctpServer.haveToken
				+ " SctpServer.csNonIdle " + SctpServer.csNonIdle;

		if (SctpServer.haveToken
				&& !SctpServer.csNonIdle
				&& (SctpServer.requestQueue.peek() == SctpServer.serverNode.nodeNumber)) {
			// Critical Section
			SctpServer.requestQueue.remove();
			SctpServer.noOfCSEntries++;
			SctpServer.csNonIdle = true;
			logData = "Request " + SctpServer.noOfCSEntries;
			LoggerClass.printToLogs(logData, SctpServer.filePath, false);
			logData = "***********************************************\n";
			LoggerClass.printToLogs(logData, SctpServer.filePath, false);
			logData = "Node " + SctpServer.serverNode.getNodeNumber()
					+ " Inside Critical Section\n\n";
			LoggerClass.printToLogs(logData, SctpServer.filePath, false);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendMessageToTestServer(Constants.MessageType.ENTERING);
		}

	}

	private static void sendMessageToTestServer(Constants.MessageType message) {
		System.out.println("\n\nMessage sent to testserver "
				+ message.toString() + " " + SctpServer.serverNode.nodeNumber);
		SctpClient clientObject;
		clientObject = new SctpClient(SctpServer.serverNode, message);
		clientObject.start();
	}

	/**
	 * Method to broadcast messages
	 */
	public static void sendBroadcastMessage() {
		// TODO Auto-generated method stub
		String logData;

		SctpServer.readyForBcast = false;
		Node neighbourNode;
		SctpClient clientObject;
		Constants.MessageType bcastMsg = Constants.MessageType.BROADCAST;

		String messageBcast = SctpServer.bcastMessages
				.get(SctpServer.noOfMsgsSent);
		String bcastFinalMsg = "BROADCAST " + SctpServer.serverNode.nodeNumber
				+ " " + SctpServer.serverNode.nodeNumber + " " + messageBcast;
		SctpServer.serverNode.bcastMessage = bcastFinalMsg;
		logData = "Broadcast Message from designated node "
				+ SctpServer.serverNode.nodeNumber + " " + bcastFinalMsg;
		LoggerClass.printToLogs(logData, SctpServer.filePath, false);

		for (int child : SctpServer.treeNeighbours) {
			neighbourNode = SctpServer.nodeMap.get(child);
			clientObject = new SctpClient(SctpServer.serverNode, neighbourNode,
					bcastMsg);
			clientObject.start();
		}

		while (!SctpServer.readyForBcast) {
			System.out.print("");

			continue;
		}

	}

	/**
	 * Method to transfer token while exiting CS
	 */
	public static void cs_leave() {
		SctpClient clientObject;
		String logData;
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logData = "Node " + SctpServer.serverNode.getNodeNumber()
				+ " Exiting critical section\n\n ";
		LoggerClass.printToLogs(logData, SctpServer.filePath, false);
		logData = "Request " + SctpServer.noOfCSEntries;
		LoggerClass.printToLogs(logData, SctpServer.filePath, false);
		sendMessageToTestServer(Constants.MessageType.EXITING);

		SctpServer.csNonIdle = false;

		if (!SctpServer.requestQueue.isEmpty()) {
			int requestingNodeNo = (int) SctpServer.requestQueue.remove();
			logData = "Token to be given to " + requestingNodeNo;
			LoggerClass.printToLogs(logData, SctpServer.filePath, false);
			Node requestingNode = SctpServer.nodeMap.get(requestingNodeNo);
			Constants.MessageType sendMessage = Constants.MessageType.TOKEN;
			SctpServer.haveToken = false;
			try {
				Thread.sleep(750);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			clientObject = new SctpClient(SctpServer.serverNode,
					requestingNode, sendMessage);
			clientObject.start();
			logData = "Message sent to " + requestingNode.nodeNumber + " "
					+ sendMessage + " " + SctpServer.serverNode.nodeNumber + "";
			LoggerClass.printToLogs(logData, SctpServer.filePath, false);
			SctpServer.requestParent = requestingNode;

			if (!SctpServer.requestQueue.isEmpty()) {

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				sendMessage = Constants.MessageType.TOKEN_REQUEST;
				clientObject = new SctpClient(SctpServer.serverNode,
						SctpServer.requestParent, sendMessage);
				clientObject.start();
				logData = "Message sent to "
						+ SctpServer.requestParent.nodeNumber + " "
						+ sendMessage + " " + SctpServer.serverNode.nodeNumber
						+ "";
				LoggerClass.printToLogs(logData, SctpServer.filePath, false);
				SctpServer.requestSent = true;

			}
		}
	}

}
