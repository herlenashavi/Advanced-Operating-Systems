import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Method to Generate the test data
 * 
 * @author Avinash
 *
 */
public class MessageGenerator {
	static ArrayList<String> bcastMessages = new ArrayList<>();
	static String CONFIG_PATH = "//home//004//a///ax//axh132830//CS6378//Project1//config.txt";
	static String MSGS_PATH = "//home//004//a///ax//axh132830//CS6378//Project1//messages.txt";
	static int noOfNodes;
	static int requestsPerNode;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PrintWriter messageWriter = null;

		FileReader frObject;
		BufferedReader brObject = null;
		try {
			frObject = new FileReader(CONFIG_PATH);

			brObject = new BufferedReader(frObject);
			int lineNum = 0;
			String lineRead;

			while ((lineRead = brObject.readLine()) != null) {
				if (lineRead.contains("# Testing_server")) {
					lineRead = brObject.readLine().trim();

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
			System.out.println(noOfNodes);
			System.out.println(requestsPerNode);
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

		try {
			messageWriter = new PrintWriter(new FileOutputStream(MSGS_PATH,
					false));
			String nodeNumber;
			int nodeCounter = 0;
			int count = 1;

			for (int messageCounter = 1; messageCounter <= noOfNodes
					* requestsPerNode; messageCounter++) {
				++nodeCounter;
				nodeNumber = ((Integer) nodeCounter).toString();
				messageWriter.println(nodeNumber + " " + "Message Number "
						+ count + " from " + " " + nodeNumber);
				if (nodeCounter == noOfNodes) {
					nodeCounter = 0;
					count++;
				}

			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			messageWriter.close();
		}

	}

}
