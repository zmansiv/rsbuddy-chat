import java.io.File;
import java.io.IOException;

public class RSBuddy {

	public static void main(String[] args) throws IOException {
		Runtime.getRuntime().exec("java -jar " + new File(System.getProperty("user.home")).toPath().resolve("RSBuddy").resolve("loader.jar").toString());
	}

}