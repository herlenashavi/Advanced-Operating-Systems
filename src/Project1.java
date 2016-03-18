import java.util.ArrayList;
import java.util.HashMap;

public class Project1 {

	static ArrayList<String> bcastMessages = new ArrayList<>();
	static int noOfNodes;
	static int requestsPerNode;
	static int nodeNumber;
	static int meanDelay;

	// Code for TestServer
	static int testServerPortNum;
	static String testServerName;

	public static void main(String[] args) {

		HashMap<Integer, Node> nodeMap = new HashMap<>();
		Node nodeInfo;

		nodeNumber = Integer.parseInt(args[0]);
		
		// Extract configs from Configuration files
		bcastMessages = Utilities.extractBroadcastMessages(nodeNumber);
		nodeMap = Utilities.extractConfigurationFromFile();

		nodeInfo = nodeMap.get(nodeNumber);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Kick start the server thread for the node
		SctpServer serverObject = new SctpServer(nodeInfo, nodeMap);
		serverObject.start();

		while (!SctpServer.appReady)
			continue;
		
		// Kick start application thread for the node
		Application appObject = new Application(nodeInfo, nodeMap);
		appObject.start();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}