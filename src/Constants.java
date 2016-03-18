public class Constants {
	static String CONFIG_PATH = "//home//004//a///ax//axh132830//CS6378//Project1//config.txt";
	static String MSGS_PATH = "//home//004//a///ax//axh132830//CS6378//Project1//messages.txt";
	static String CSLOG_PATH = "//home//004//a///ax//axh132830//CS6378//Project1//logs//cs.log";
	static String VIOLATIONS_LOG = "//home//004//a///ax//axh132830//CS6378//Project1//logs//voilations.log";
	static String SUMMARY_LOG = "//home//004//a///ax//axh132830//CS6378//Project1//logs//summary.log";
	static String LOG_DIR = "//home//004//a///ax//axh132830//CS6378//Project1//logs";
	static String CS_REQ = "# CS_REQUEST_PER_NODE";
	static String DESGN_NODE = "# Designated_Node";
	static String CS_REQ_DELAY = "# CS_REQUEST_DELAY";
	static String TESTING_SERVER = "# Testing_server";
	static String ESC = "#";
	public static final int MESSAGE_SIZE = 1000;

	public static enum MessageType {
		JOIN, ACK, NACK, BCAST_READY, BROADCAST, BCAST_RECEIVED, TOKEN_REQUEST, TOKEN, ENTERING, EXITING, APP_READY
	};

}
