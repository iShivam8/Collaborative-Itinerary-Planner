package server;

import java.rmi.RemoteException;

public interface Server {

  /**
   * Client can use this method as a part of the server stub. It allows the client to interact with
   * the KeyValueStore for the three operations of - PUT / GET / DELETE / EDIT / SHARE.
   *
   * @param inputMessage - Client's input message specifying the operation to execute.
   * @return - Response of the executed operation to the client.
   * @throws RemoteException
   */
  String executeOperation(String inputMessage) throws RemoteException;

  /**
   * Method used to allow the client to create a new account or sign in to an old account.
   *
   * @param inputMessage - SignUp / LogIn / X
   * @return - Response whether the account is created or the client is logged in
   * @throws RemoteException
   */
  String signIn(String inputMessage) throws RemoteException;
}
