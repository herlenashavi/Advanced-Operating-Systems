import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import java.net.*;
import com.sun.nio.sctp.*;
import java.nio.*;

public class SctpServer implements Runnable {

	static String filePath;
	private Thread thread;
	private String threadName;
	public int portNum;
	ArrayList<Integer> nodeNeighbours = new ArrayList<>();
	static Node serverNode;
	Node parentNode = null;
	Node neighbourNode;
	Node bcastParent = null;
	SctpClient clientObject;
	static HashMap<Integer, Node> nodeMap;
	int ackCount = 0, nackCount = 0;
	boolean treeJoined = false, joinSent = false, treeReady = false;
	int bcastAckSeen = 0;

	// Variables for Project 2 changes
	static volatile Queue<Integer> requestQueue = new LinkedList<Integer>();
	static volatile boolean requestSent = false;
	static volatile boolean haveToken = false;
	static volatile boolean csNonIdle = false;
	static volatile Node requestParent;
	static volatile boolean readyForBcast = false, appReady = false;
	static volatile int noOfMsgsRecvd = 0, noOfMsgsSent = 0;
	static ArrayList<String> bcastMessages = new ArrayList<>(
			Project1.bcastMessages);
	static ArrayList<Integer> treeNeighbours = new ArrayList<Integer>();
	static volatile int noOfCSEntries = 0;
	String logData;

	public SctpServer(Node node, HashMap<Integer, Node> map, int x) {
		serverNode = node;
		nodeMap = new HashMap<>(map);
		filePath = Constants.LOG_DIR + "//Node_" + node.nodeNumber + ".log";
	}

	public SctpServer(Node node, HashMap<Integer, Node> topologyMap) {

		// node = topologyMap.get(nodeNumber);
		serverNode = node;
		threadName = node.getNodeName();
		portNum = node.getPortNumber();
		nodeNeighbours = node.neighbours;
		nodeMap = new HashMap<>(topologyMap);

		// Writing to logs
		logData = "\nInitializing Node " + serverNode.nodeName + " on port "
				+ serverNode.portNumber;
		LoggerClass.printToLogs(logData, filePath, true);
		logData = "\n*************Starting Spanning Tree"
				+ " Neighbour discovery*************\n";
		LoggerClass.printToLogs(logData, filePath, false);

	}

	public void run() {
		go();
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this, threadName);
			thread.start();
		}
	}

	public synchronized void go() {
		// Buffer to hold messages in byte format
		ByteBuffer byteBuffer;
		String message;
		int neighbour;
		boolean bcastDone = false;

		Constants.MessageType bcastReady = Constants.MessageType.BCAST_READY;
		Constants.MessageType joinMsg = Constants.MessageType.JOIN;

		try {

			if (!serverNode.neighbours.isEmpty()
					&& serverNode.isDesignatedNode()) {
				System.out.println();
				Thread.sleep(4000);
				for (int neighbourNodeNum : serverNode.neighbours) {
					neighbourNode = nodeMap.get(neighbourNodeNum);

					logData = "Message sent to " + neighbourNodeNum + " "
							+ joinMsg + " " + serverNode.nodeNumber;
					LoggerClass.printToLogs(logData, filePath, false);

					clientObject = new SctpClient(serverNode, neighbourNode,
							joinMsg);
					clientObject.start();
				}
			}

			SctpMultiChannel smc;
			InetSocketAddress serverAddr = new InetSocketAddress(portNum);
			smc = SctpMultiChannel.open().bind(serverAddr);

			while (true) {
				byteBuffer = ByteBuffer.allocate(Constants.MESSAGE_SIZE);
				smc.receive(byteBuffer, null, null);
				message = byteToString(byteBuffer);

				if (message != null)
					processMessageReceived(message);

				// Runs only during spanning tree construction
				if ((ackCount + nackCount) == serverNode.neighbours.size()
						&& !treeReady) {

					if (treeNeighbours.size() - 1 == 0
							&& !serverNode.designatedNode) {
						neighbour = treeNeighbours.get(0);
						neighbourNode = nodeMap.get(neighbour);

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						clientObject = new SctpClient(serverNode,
								neighbourNode, bcastReady);
						clientObject.start();
						treeReady = true;
						logData = "\nMessage sent to " + neighbour + " "
								+ bcastReady + " " + serverNode.nodeNumber
								+ "\n";
						LoggerClass.printToLogs(logData, filePath, false);
						readyForBcast = true;
						for (int neighbours : treeNeighbours) {
							System.out.print(neighbours + " ");
						}
						System.out.println("\n\n");

						logData = "\n*************Spanning Tree"
								+ " Neighbour discovery Complete*************\n";
						LoggerClass.printToLogs(logData, filePath, false);

						logData = "\n\nNode " + serverNode.nodeNumber
								+ " Tree Neighbours: ";
						LoggerClass.printToLogs(logData, filePath, false);

						for (int neighbours : treeNeighbours) {
							logData = neighbours + " ";
							LoggerClass.printToLogs(logData, filePath, false);
						}

						// Project 2 changes
						requestParent = neighbourNode;
						haveToken = false;
						requestSent = false;
						logData = "Node to request for token "
								+ requestParent.nodeNumber;
						LoggerClass.printToLogs(logData, filePath, false);
					}
				}

				if (noOfMsgsSent == bcastMessages.size()
						&& bcastMessages.size() != 0 && !bcastDone) {
					System.out.println("\n\nNode " + serverNode.getNodeNumber()
							+ " BROADCAST COMPLETE!!!\n\n");
					bcastDone = true;
				}

				message = null;
				byteBuffer.clear();

			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processMessageReceived(String messageRcvd) {
		String[] msgRcvdArr;
		String messageReceived;
		msgRcvdArr = messageRcvd.split(" ");
		Constants.MessageType sendMessage = null;
		Node sendingNode = null;
		int sender;

		if (msgRcvdArr != null) {
			sender = Integer.parseInt(msgRcvdArr[1].trim());
			sendingNode = nodeMap.get(sender);
			logData = "Message recevied from " + sender + ": " + messageRcvd
					+ "";
			LoggerClass.printToLogs(logData, filePath, false);
			if ((msgRcvdArr[0].trim()).equals(Constants.MessageType.JOIN
					.toString())) {
				if (!treeJoined) {
					sendMessage = Constants.MessageType.ACK;
					sendingNode = nodeMap.get(sender);
					treeNeighbours.add(sender);
					parentNode = sendingNode;
					treeJoined = true;
					ackCount++;
				} else {
					sendMessage = Constants.MessageType.NACK;
					sendingNode = nodeMap.get(sender);
				}
			} else if ((msgRcvdArr[0].trim()).equals(Constants.MessageType.ACK
					.toString())) {

				sendingNode = nodeMap.get(sender);

				treeNeighbours.add(sender);
				sendingNode = null;
				sendMessage = null;
				ackCount++;

			} else if ((msgRcvdArr[0].trim()).equals(Constants.MessageType.NACK
					.toString())) {
				sendingNode = nodeMap.get(sender);
				sendingNode = null;
				sendMessage = null;
				nackCount++;
			} else if ((msgRcvdArr[0].trim())
					.equals(Constants.MessageType.BCAST_READY.toString())) {
				bcastAckSeen++;
				// System.out.println("BCAST_READY Acknowledgements seen: " +
				// bcastAckSeen);
				if (bcastAckSeen == (treeNeighbours.size() - 1)
						&& !serverNode.designatedNode) {

					sendingNode = parentNode;
					sendMessage = Constants.MessageType.BCAST_READY;

					bcastAckSeen = 0;
					treeReady = true;
					readyForBcast = true;

					logData = "\n*************Spanning Tree"
							+ " Neighbour discovery Complete*************\n";
					LoggerClass.printToLogs(logData, filePath, false);
					logData = "\nNode " + serverNode.nodeNumber
							+ " Tree Neighbours: ";
					LoggerClass.printToLogs(logData, filePath, false);
					for (int neighbours : treeNeighbours) {
						logData = neighbours + " ";
						LoggerClass.printToLogs(logData, filePath, false);
					}

					for (int neighbours : treeNeighbours) {
						System.out.print(neighbours + " ");
					}
					System.out.println("\n");

					// Project 2 changes

					requestParent = parentNode;
					logData = "Node to request for token "
							+ requestParent.nodeNumber;
					LoggerClass.printToLogs(logData, filePath, false);
					haveToken = false;
					requestSent = false;

				}

				if (serverNode.designatedNode
						&& bcastAckSeen == treeNeighbours.size()) {
					sendingNode = null;
					sendMessage = null;
					logData = "Designated Node ready";
					LoggerClass.printToLogs(logData, filePath, false);
					treeReady = true;
					readyForBcast = true;
					bcastAckSeen = 0;

					logData = "\n*************Spanning Tree"
							+ " Neighbour discovery Complete*************\n";
					LoggerClass.printToLogs(logData, filePath, false);

					for (int neighbours : treeNeighbours) {
						System.out.print(neighbours + " ");
					}
					System.out.println("\n\n");
					serverNode.setDesignatedNode(false);

					// Project 2 changes

					requestParent = serverNode;

					haveToken = true;
					requestSent = false;

					// Send APP_READY message
					appReady = true;
					logData = "Sending APP_READY message to neighbours ";
					LoggerClass.printToLogs(logData, filePath, false);

					for (int child : SctpServer.treeNeighbours) {
						neighbourNode = SctpServer.nodeMap.get(child);
						clientObject = new SctpClient(serverNode,
								neighbourNode, Constants.MessageType.APP_READY);
						clientObject.start();
					}

					sendingNode = null;
					sendMessage = null;
				}

			} else if ((msgRcvdArr[0].trim())
					.equals(Constants.MessageType.APP_READY.toString())) {

				if (treeNeighbours.size() - 1 == 0 && !haveToken
						&& readyForBcast) {
					// readyForBcast = false;

					logData = "\n\nNode is Application Ready!! Can request for token!!\n\n";
					LoggerClass.printToLogs(logData, filePath, false);
					appReady = true;

					sendingNode = null;
					sendMessage = null;

				}
				if (treeNeighbours.size() - 1 > 0 && !haveToken
						&& readyForBcast) {

					logData = "\n\nNode is Application Ready!! Can request for token!!\n\n";
					LoggerClass.printToLogs(logData, filePath, false);
					appReady = true;

					logData = "\n\nSending APP_READY message to neighbours ";
					LoggerClass.printToLogs(logData, filePath, false);

					sendMessage = Constants.MessageType.APP_READY;

					for (int neighbourNodeNum : treeNeighbours) {
						if (neighbourNodeNum != sender) {
							neighbourNode = nodeMap.get(neighbourNodeNum);
							clientObject = new SctpClient(serverNode,
									neighbourNode, sendMessage);
							clientObject.start();
						}
					}
					sendingNode = null;
					sendMessage = null;
				}

			} else if ((msgRcvdArr[0].trim())
					.equals(Constants.MessageType.BROADCAST.toString())) {

				noOfMsgsRecvd++;
				StringBuilder builder = new StringBuilder();
				// sendingNode = nodeMap.get(sender);

				for (int i = 3; i < msgRcvdArr.length; i++) {
					builder.append(msgRcvdArr[i]);
					builder.append(" ");
				}
				messageReceived = builder.toString().trim();
				int hop = Integer.parseInt(msgRcvdArr[2].trim());
				bcastParent = nodeMap.get(hop);

				logData = "\n\n*************BROADCAST MESSAGE RECEIVED"
						+ "*************\n";
				LoggerClass.printToLogs(logData, filePath, false);
				logData = "Message Source: " + sendingNode.nodeName
						+ ", Recevied from: " + msgRcvdArr[2] + ", Message: "
						+ messageReceived;
				LoggerClass.printToLogs(logData, filePath, false);

				if (treeNeighbours.size() - 1 == 0 && !haveToken
						&& readyForBcast) {
					// readyForBcast = false;

					neighbourNode = nodeMap
							.get(Integer.parseInt(msgRcvdArr[2]));
					sendMessage = Constants.MessageType.BCAST_RECEIVED;
					clientObject = new SctpClient(serverNode, neighbourNode,
							sendMessage);
					clientObject.start();
					logData = "SENDING BROADCAST ACK TO "
							+ Integer.parseInt(msgRcvdArr[2]) + ": "
							+ sendMessage + " " + serverNode.nodeNumber;
					LoggerClass.printToLogs(logData, filePath, false);
					sendingNode = null;
					sendMessage = null;

				}
				if (treeNeighbours.size() - 1 > 0 && !haveToken
						&& readyForBcast) {

					builder = null;
					msgRcvdArr[2] = ((Integer) serverNode.nodeNumber)
							.toString();

					builder = new StringBuilder();
					for (String arrElement : msgRcvdArr) {
						builder.append(arrElement);
						builder.append(" ");
					}
					messageRcvd = builder.toString().trim();
					logData = "\n\nMessage sent to tree neighbours: "
							+ messageRcvd;
					LoggerClass.printToLogs(logData, filePath, false);

					for (int neighbourNodeNum : treeNeighbours) {
						if (neighbourNodeNum != hop) {
							serverNode.bcastMessage = messageRcvd;
							sendMessage = Constants.MessageType.BROADCAST;
							neighbourNode = nodeMap.get(neighbourNodeNum);
							clientObject = new SctpClient(serverNode,
									neighbourNode, sendMessage);
							clientObject.start();
						}
					}
					readyForBcast = false;
					sendingNode = null;
					sendMessage = null;
				}

			} else if ((msgRcvdArr[0].trim())
					.equals(Constants.MessageType.BCAST_RECEIVED.toString())) {
				bcastAckSeen++;
				if (bcastAckSeen == (treeNeighbours.size() - 1) && !haveToken) {
					logData = "Broadcast Acknowledgements received from all broadcast receving neighbours";
					LoggerClass.printToLogs(logData, filePath, false);
					logData = "SENDING BROADCAST ACK TO "
							+ bcastParent.nodeNumber + ": " + "BCAST_RECEIVED"
							+ " " + serverNode.nodeNumber;
					LoggerClass.printToLogs(logData, filePath, false);
					sendingNode = bcastParent;
					sendMessage = Constants.MessageType.BCAST_RECEIVED;

					bcastAckSeen = 0;
					readyForBcast = true;
				}
				if (haveToken && bcastAckSeen == treeNeighbours.size()) {
					noOfMsgsSent++;
					serverNode.setDesignatedNode(false);
					sendingNode = null;
					sendMessage = null;
					logData = "\n\nDesignated Node ready to broadcast";
					LoggerClass.printToLogs(logData, filePath, false);
					readyForBcast = true;
					bcastAckSeen = 0;
				}
			} else if ((msgRcvdArr[0].trim())
					.equals(Constants.MessageType.TOKEN_REQUEST.toString())) {

				if (!haveToken) {
					logData = "No token, Added to queue " + sender;
					LoggerClass.printToLogs(logData, filePath, false);
					requestQueue.add(sender);
					if (!requestQueue.isEmpty() && !requestSent) {
						logData = "No token, No request sent!!! Added to Queue "
								+ sender;
						LoggerClass.printToLogs(logData, filePath, false);
						sendMessage = Constants.MessageType.TOKEN_REQUEST;
						sendingNode = requestParent;
						requestSent = true;
					}
				} else if (haveToken && !csNonIdle) {
					// Concurrent reception of token and request

					if (requestQueue.isEmpty()) {
						logData = "Have token, not cs nonidle, Queue empty !!! token sent to "
								+ sender;
						LoggerClass.printToLogs(logData, filePath, false);
						sendMessage = Constants.MessageType.TOKEN;
						sendingNode = nodeMap.get(sender);
						requestParent = sendingNode;
						haveToken = false;
					} else if (!requestQueue.isEmpty()
							&& (requestQueue.peek() != null)) {
						if (!(requestQueue.peek() == serverNode.nodeNumber)) {
							logData = "Have token, not cs non idle!!! token sent to "
									+ sender;
							LoggerClass.printToLogs(logData, filePath, false);
							sendMessage = Constants.MessageType.TOKEN;
							sendingNode = nodeMap.get(sender);
							requestParent = sendingNode;
							haveToken = false;
						} else if ((requestQueue.peek() == serverNode.nodeNumber)) {

							requestQueue.add(sender);
							logData = "have token, not cs non idle, head is server node!! Added to queue "
									+ sender;
							LoggerClass.printToLogs(logData, filePath, false);
							sendMessage = null;
							sendingNode = null;
						}

					} else {
						requestQueue.add(sender);
						logData = "have token, not cs non idle!! Added to queue "
								+ sender;
						LoggerClass.printToLogs(logData, filePath, false);
						sendMessage = null;
						sendingNode = null;
					}

				} else if (haveToken && csNonIdle) {
					requestQueue.add(sender);
					logData = "have token, cs non idle!! Added to queue "
							+ sender;
					sendMessage = null;
					sendingNode = null;
				}

			} else if ((msgRcvdArr[0].trim())
					.equals(Constants.MessageType.TOKEN.toString())) {

				logData = "TOKEN RECEIVED FROM " + sender;
				LoggerClass.printToLogs(logData, filePath, false);
				if (!requestQueue.isEmpty() && requestQueue.peek() != null) {

					logData = "Queue head " + requestQueue.peek();
					LoggerClass.printToLogs(logData, filePath, false);
					int requestingNodeNo = (int) requestQueue.peek();
					Node requestingNode = nodeMap.get(requestingNodeNo);
					requestSent = false;

					if (serverNode == requestingNode) {
						haveToken = true;
						requestParent = serverNode;
						logData = "Node received TOKEN, Setting haveToken flag to true";
						LoggerClass.printToLogs(logData, filePath, false);
						sendingNode = null;
						sendMessage = null;

					} else {
						requestQueue.remove();
						sendMessage = Constants.MessageType.TOKEN;
						sendingNode = requestingNode;
						requestParent = requestingNode;
						haveToken = false;

						clientObject = new SctpClient(serverNode, sendingNode,
								sendMessage);
						clientObject.start();
						logData = "Message sent to " + sendingNode.nodeNumber
								+ " " + sendMessage + " "
								+ serverNode.nodeNumber;
						LoggerClass.printToLogs(logData, filePath, false);

						if (!requestQueue.isEmpty() && !requestSent) {
							logData = "Queue head " + requestQueue.peek();
							LoggerClass.printToLogs(logData, filePath, false);
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							sendMessage = Constants.MessageType.TOKEN_REQUEST;
							clientObject = new SctpClient(serverNode,
									requestParent, sendMessage);
							clientObject.start();
							logData = "Message sent to "
									+ requestParent.nodeNumber + " "
									+ sendMessage + " " + serverNode.nodeNumber
									+ "";
							LoggerClass.printToLogs(logData, filePath, false);
							requestSent = true;

						}

						sendMessage = null;
						sendingNode = null;

					}
				} else {
					haveToken = true;
				}

			}

			if (sendingNode != null && sendMessage != null) {

				clientObject = new SctpClient(serverNode, sendingNode,
						sendMessage);
				clientObject.start();
				logData = "Message sent to " + sendingNode.nodeNumber + " "
						+ sendMessage + " " + serverNode.nodeNumber + "";
				LoggerClass.printToLogs(logData, filePath, false);

				if (treeJoined && !joinSent) {
					sendMessage = Constants.MessageType.JOIN;
					logData = "Sending Join message to neighbours";
					LoggerClass.printToLogs(logData, filePath, false);
					for (int neighbourNodeNum : nodeNeighbours) {
						if (neighbourNodeNum != sender) {
							neighbourNode = nodeMap.get(neighbourNodeNum);
							clientObject = new SctpClient(serverNode,
									neighbourNode, sendMessage);
							clientObject.start();
							logData = "Message sent to " + neighbourNodeNum
									+ " " + sendMessage + " "
									+ serverNode.nodeNumber;
							LoggerClass.printToLogs(logData, filePath, false);
						}
					}
					joinSent = true;
				}

			}
		}
	}

	public String byteToString(ByteBuffer byteBuffer) {
		byteBuffer.position(0);
		byteBuffer.limit(Constants.MESSAGE_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return new String(bufArr);
	}

}
