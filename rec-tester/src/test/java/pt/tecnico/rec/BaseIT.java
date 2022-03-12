package pt.tecnico.rec;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.*;

import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class BaseIT {

	private static final String PROP_FILE = "/test.properties";
	protected static Properties properties;
	static RecordFrontend frontend;
	
	@BeforeAll
	public static void oneTimeSetup () throws IOException, ZKNamingException {
		properties = new Properties();
		
		try {
			properties.load(BaseIT.class.getResourceAsStream(PROP_FILE));
			System.out.println("Test properties:");
			System.out.println(properties);
		}catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", PROP_FILE);
			System.out.println(msg);
			throw e;
		}
		final String host = properties.getProperty("zoo.host");
		final int port = Integer.parseInt(properties.getProperty("zoo.port"));
		final int cid = Integer.parseInt(properties.getProperty("i"));
		try {
			frontend = new RecordFrontend(cid, host, port, 1000);
		} catch (ZKNamingException e) {
			System.out.println("Error connecting to record: " + e.getMessage());
			throw e;
		}
	}
}
