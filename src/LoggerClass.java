import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LoggerClass {

	public static void printToLogs(String logData, String filePath,
			boolean showConsole) {

		PrintWriter logWriter = null;

		try {
			logWriter = new PrintWriter(new BufferedWriter(new FileWriter(
					filePath, true)));
			logWriter.println(logData);

			if (showConsole == true) {
				System.out.println(logData);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (logWriter != null) {
				logWriter.close();
			}
		}

	}

}
