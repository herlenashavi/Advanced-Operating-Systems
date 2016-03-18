import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Node {

	String nodeName;
	int nodeNumber;
	int portNumber;
	ArrayList<Integer> neighbours = new ArrayList<>();
	boolean designatedNode = false;
	String bcastMessage;
	volatile Queue<Integer> requestQueue = new LinkedList<>();
	volatile boolean requestSent = false;
	static volatile boolean haveToken = false;
	volatile boolean csNonIdle = false;
	volatile Node parent;

	public boolean isRequestSent() {
		return requestSent;
	}

	public void setRequestSent(boolean requestSent) {
		this.requestSent = requestSent;
	}

	public boolean isHaveToken() {
		return haveToken;
	}

	public Node getNodeToRequest() {
		return parent;
	}

	public void setNodeToRequest(Node parent) {
		this.parent = parent;
	}

	public String getBcastMessage() {
		return bcastMessage;
	}

	public void setBcastMessage(String bcastMessage) {
		this.bcastMessage = bcastMessage;
	}

	public int getNodeNumber() {
		return nodeNumber;
	}

	public void setNodeNumber(int nodeNumber) {
		this.nodeNumber = nodeNumber;
	}

	public boolean isDesignatedNode() {
		return designatedNode;
	}

	public void setDesignatedNode(boolean designatedNode) {
		this.designatedNode = designatedNode;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public ArrayList<Integer> getNeighbours() {
		return neighbours;
	}

}