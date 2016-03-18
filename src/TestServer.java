import java.io.*;
import java.net.*;

import com.sun.nio.sctp.*;

import java.nio.*;

public class TestServer {

	PrintWriter testWriter;
	int portNumber;

	String testServerName;
	int testServerPortNum;
	static int prevNode, currentNode;
	static String[] messageArr;
	static volatile boolean inCS = false, violation = false;
	static boolean completed = false;
	static int fulfilled = 0;
	static int violated = 0;
	static int totalRequests = 0;
	static int noOfNodes;
	static int requestsPerNode;

	public TestServer() {

		FileReader frObject;
		BufferedReader brObject = null;
		try {
			frObject = new FileReader(Constants.CONFIG_PATH);

			brObject = new BufferedReader(frObject);
			String[] testServer;
			int lineNum = 0;
			String lineRead;

			while ((lineRead = brObject.readLine()) != null) {
				if (lineRead.contains("# Testing_server")) {
					lineRead = brObject.readLine().trim();
					testServer = lineRead.split(" ");
					testServerName = testServer[0];
					testServerPortNum = Integer.parseInt(testServer[1]);

				} else if (lineRead.contains("# CS_REQUEST_PER_NODE")) {
					requestsPerNode = Integer.parseInt((brObject.readLine()
							.trim()));
					continue;
				} else if (lineRead.contains("# CS_REQUEST_DELAY")) {
					brObject.readLine().trim();
					continue;
				} else if (lineRead.contains("# Designated_Node")) {
					brObject.readLine().trim();
					continue;
				} else if (lineRead.contains("#")) {
					continue;
				} else if (lineRead.trim().length() > 0) {
					lineNum++;
					continue;
				}

			}
			noOfNodes = lineNum;
			brObject.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void go() {
		// Buffer to hold messages in byte format

		String message;
		ByteBuffer byteBuffer;
		SctpChannel sctpChannel;
		try {
			// Open a server channel
			SctpServerChannel sctpServerChannel = SctpServerChannel.open();
			InetSocketAddress serverAddr = new InetSocketAddress(
					testServerPortNum);
			sctpServerChannel.bind(serverAddr);
			System.out.println("noOfNodes " + noOfNodes);
			System.out.println("requestsPerNode " + requestsPerNode);

			while (true) {
				sctpChannel = sctpServerChannel.accept();
				byteBuffer = ByteBuffer.allocate(Constants.MESSAGE_SIZE);
				sctpChannel.receive(byteBuffer, null, null);
				message = byteToString(byteBuffer);
				messageProcessing(message);
				writeLog(message, Constants.CSLOG_PATH);
				message = null;
				byteBuffer.clear();

				if (completed) {
					System.out
							.println("\n\nExecution completed!!\nCritical Section Execution logs: "
									+ Constants.CSLOG_PATH
									+ "\nExecution Summary logs: "
									+ Constants.SUMMARY_LOG);
					writeRunSummaryLog();
				}
				/**/
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Method to write summary of simulation
	 */
	private void writeRunSummaryLog() {
		writeLog("Total Critical Section requests fulfilled: " + fulfilled,
				Constants.SUMMARY_LOG);
		writeLog("Total Critical Section requests violated: " + violated,
				Constants.SUMMARY_LOG);
		writeLog("Total Critical Section requests: " + totalRequests + "\n\n",
				Constants.SUMMARY_LOG);
	}

	/**
	 * Process the message received by testing server
	 * 
	 * @param message
	 */
	private void messageProcessing(String message) {
		messageArr = message.split(" ");
		currentNode = Integer.parseInt(messageArr[1].trim());

		if (messageArr[0].trim().equals(
				Constants.MessageType.ENTERING.toString())) {
			totalRequests++;
			if (!inCS) {
				inCS = true;
				fulfilled++;
				prevNode = currentNode;
			} else {
				violation = true;
				violated++;
			}
		} else if (messageArr[0].trim().equals(
				Constants.MessageType.EXITING.toString())) {
			inCS = false;

			if (totalRequests == noOfNodes * requestsPerNode) {
				completed = true;
			}
		}

		if (violation) {
			System.out.println("VIOLATION!!!!!!--------------------------"
					+ totalRequests);

			writeLog("\n\n****************************************\n\n"
					+ totalRequests, Constants.VIOLATIONS_LOG);
			writeLog("Node " + prevNode + " in critical section",
					Constants.VIOLATIONS_LOG);
			writeLog("CRITICAL SECTION VIOLATION DETECTED!!!",
					Constants.VIOLATIONS_LOG);
			writeLog("Node " + currentNode
					+ " tried to concurrently execute critical section",
					Constants.VIOLATIONS_LOG);
			writeLog("\n\n****************************************\n\n",
					Constants.VIOLATIONS_LOG);
			writeRunSummaryLog();
			violation = false;
		}

	}

	/**
	 * Method to write to logs
	 */
	private void writeLog(String log, String fileName) {
		try {

			testWriter = new PrintWriter(new FileOutputStream(fileName, true));
			testWriter.println(log);
			testWriter.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			testWriter.close();
		}

	}

	public String byteToString(ByteBuffer byteBuffer) {
		byteBuffer.position(0);
		byteBuffer.limit(Constants.MESSAGE_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return new String(bufArr);
	}

	public static void main(String args[]) {
		File dir = new File(Constants.LOG_DIR);
		purgeDirectory(dir);
		TestServer testServerObj = new TestServer();
		testServerObj.go();
	}

	static void purgeDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				purgeDirectory(file);
			file.delete();
		}
	}

}