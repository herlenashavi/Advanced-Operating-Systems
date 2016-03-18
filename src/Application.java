import java.util.HashMap;

public class Application implements Runnable {

	private Thread thread;
	private String threadName;
	SctpServer serverObject;
	Node serverNode;

	public Application(Node node, HashMap<Integer, Node> nodeMap) {
		this.threadName = node.getNodeName();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		serverObject = new SctpServer(node, nodeMap, 0);

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

	// Method that broadcasts messages and
	// makes Critical section requests
	public void go() {

		int size = Project1.bcastMessages.size();
		try {
			for (int i = 1; i <= size; i++) {

				BroadcastServiceAPI.cs_enter();
				BroadcastServiceAPI.sendBroadcastMessage();
				if (SctpServer.csNonIdle) {
					Thread.sleep(500);

					BroadcastServiceAPI.cs_leave();
				}

				Thread.sleep((long) Utilities.getExpRandomNumber());
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
