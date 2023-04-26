package client;

import java.rmi.RemoteException;

/**
 * Entry gateway for the client application.
 */
public class ClientMainApplication {

  public static void main(String[] args) {
    int port = -1;
    String serverIPAddress = null;
    Client client = null;

    if (args.length != 3) {
      System.out.println("The Application requires 3 Arguments: 1] RMI Registry IP/hostname  " +
          "2] Port Number  3] Server ID Number (Instance)");
      return;
    } else {
      try {
        serverIPAddress = args[0];
        port = Integer.parseInt(args[1]);

        if (port < 0 || port > 65536) {
          throw new NumberFormatException();
        }

        int serverId = Integer.parseInt(args[2]);
        client = new Client(serverIPAddress, port, serverId);
      } catch (NumberFormatException numberFormatException) {
        System.out.println("Incorrect Port Number! It should be an integer between 0 and 65536.");
        return;
      }
    }

    client.signIn();

    /*
    // If the application gets closed via Option + c
    Client finalClient = client;
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // your code to logout the user
        try {
          finalClient.logout();
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      }
    });
    */
  }
}
