import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Utilities {

	/**
	 * Method to extract all messages from configs file for particular node
	 * 
	 * @param nodeNumber
	 * @return
	 */
	public static ArrayList<String> extractBroadcastMessages(int nodeNumber) {
		// TODO Auto-generated method stub
		String bcastMessage;
		String[] splitMessage;
		StringBuilder builder;
		ArrayList<String> bcastMessages = new ArrayList<>();

		FileReader frObject;
		BufferedReader brObject;
		try {
			frObject = new FileReader(Constants.MSGS_PATH);
			brObject = new BufferedReader(frObject);

			while ((bcastMessage = brObject.readLine()) != null) {

				splitMessage = bcastMessage.split(" ");
				if (Integer.parseInt(splitMessage[0]) == nodeNumber) {
					builder = new StringBuilder();

					for (int i = 1; i < splitMessage.length; i++) {
						builder.append(splitMessage[i]);
						builder.append(" ");
					}

					bcastMessages.add(builder.toString());
				}

			}

			brObject.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bcastMessages;

	}

	/**
	 * Method to save entire network topology to a Map
	 * 
	 * @return
	 */
	public static HashMap<Integer, Node> extractConfigurationFromFile() {
		String lineRead;
		String[] strArr;
		int lineNum = 0;
		int designatedNodeNo = 0;

		// Code for Testserver
		String[] testServer;

		HashMap<Integer, Node> nodeMap = new HashMap<>();
		Node nodeObject = null;

		try {
			FileReader frObject = new FileReader(Constants.CONFIG_PATH);
			BufferedReader brObject = new BufferedReader(frObject);

			while ((lineRead = brObject.readLine()) != null) {
				if (lineRead.contains(Constants.CS_REQ)) {
					Project1.requestsPerNode = Integer.parseInt((brObject
							.readLine().trim()));
					continue;
				} else if (lineRead.contains(Constants.DESGN_NODE)) {
					designatedNodeNo = Integer.parseInt((brObject.readLine()
							.trim()));
					continue;
				} else if (lineRead.contains(Constants.CS_REQ_DELAY)) {
					Project1.meanDelay = Integer.parseInt((brObject.readLine()
							.trim()));
					continue;
				} else if (lineRead.contains(Constants.TESTING_SERVER)) {
					lineRead = brObject.readLine().trim();
					testServer = lineRead.split(" ");
					Project1.testServerName = testServer[0];
					Project1.testServerPortNum = Integer
							.parseInt(testServer[1]);

				} else if (lineRead.contains(Constants.ESC)) {
					continue;
				} else if (lineRead.trim().length() > 0) {
					lineNum++;
					nodeObject = new Node();
					lineRead = lineRead.replaceAll("\\t", " ");
					lineRead = lineRead.replaceAll("\\s+", " ").trim();
					strArr = lineRead.split(" ");
					nodeObject.setNodeNumber(lineNum);
					nodeObject.setNodeName(strArr[0]);
					nodeObject.setPortNumber(Integer.parseInt(strArr[1]));
					if (lineNum == designatedNodeNo) {
						nodeObject.setDesignatedNode(true);
					} else {
						nodeObject.setDesignatedNode(false);
					}

					for (int i = 2; i < strArr.length; i++) {
						nodeObject.neighbours.add(Integer.parseInt(strArr[i]));
					}

					nodeMap.put(lineNum, nodeObject);
					nodeObject = null;
					continue;
				}
			}
			Project1.noOfNodes = lineNum;
			brObject.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nodeMap;
	}

	public static long getExpRandomNumber() {
		Random random = new Random();
		long val = (long) (-Project1.meanDelay * Math.log(random.nextDouble()));
		return val;
	}

}
