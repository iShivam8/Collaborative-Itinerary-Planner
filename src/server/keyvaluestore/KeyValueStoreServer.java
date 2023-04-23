package server.keyvaluestore;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import logs.Logger;
import server.PaxosServer;
import server.Promise;
import server.Server;

public class KeyValueStoreServer implements Server, PaxosServer {

  private final KeyValueStore keyValueStore;
  private final Map<String, PaxosServer> acceptors;
  private final Map<String, Promise> metadata;
  private static final int MAXIMUM_PAXOS_RETRIES = 10;
  private final String serverId;
  private final Logger logger;

  public KeyValueStoreServer(String serverId) {
    this.keyValueStore = new KeyValueStore("src/logs/server_" + serverId + ".log", serverId);
    this.acceptors = new HashMap<>();
    this.metadata = new HashMap<>();
    this.serverId = serverId;
    this.logger = new Logger("src/logs/server_" + serverId + ".log", serverId);
  }

  @Override
  public void connectWithAllServers(int numberOfServers, int portNumber) throws RemoteException {

  }

  @Override
  public String startPaxos(String[] inputTokens) throws RemoteException {
    return null;
  }

  @Override
  public Promise prepare(long sequenceId, String key) throws RemoteException {
    return null;
  }

  @Override
  public Boolean propose(long sequenceId, String key, String value) throws RemoteException {
    return null;
  }

  @Override
  public String learn(String key, String value) throws RemoteException {
    return null;
  }

  @Override
  public String executeOperation(String inputMessage) throws RemoteException {
    return null;
  }



  // We don't need implementation of these methods, as they're already implemented in UserDBServer
  @Override
  public String signUp(String signupInfo) throws RemoteException {
    return null;
  }

  @Override
  public String login(String loginInfo) throws RemoteException {
    return null;
  }
}
