package examples;

import org.opennebula.client.Client;

public class SessionInit {

	public static void main(String[] args) {
		System.out.print("Creating a new OpenNebula session...");

		try {
			Client oneClient = new Client();
			System.out.println(" ok");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}

		System.out.println("Forcing a wrong user/password initialization...");
		try {
			// The secret string should be user:password. The url is null, so it
			// will be set to default.
			Client oneClient = new Client("wrong_password_token", null);
		}
		catch (Exception e) {
			System.out.println("\t" + e.getMessage());
		}

		System.out.println("Forcing a wrong url initialization...");
		try {
			// The HTTP is misspelled
			Client oneClient = new Client(null, "HTP://localhost:2633/RPC2");
		}
		catch (Exception e) {
			System.out.println("\t" + e.getMessage());
		}
	}
}
