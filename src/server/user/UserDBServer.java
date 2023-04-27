package server.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import logs.Logger;
import server.Server;
import server.TwoPCServer;
import server.itinerary.Itinerary;

/**
 * UserDBServer that implements the Server interface and implements the user sign-in methods
 * such as Signup, Login, Logout.
 */
public class UserDBServer implements Server, TwoPCServer {

  private final UserDB userDB;
  private final String serverId;

  private final Map<String, TwoPCServer> participants;
  private final Map<String, Integer> transactionCommits;
  private final Map<String, String[]> transactionLog;
  private final Logger logger, logger2PC, transactionLogger;

  public UserDBServer(String serverId) {
    this.userDB = new UserDB("src/logs/user_db/userDB.log", serverId);
    this.serverId = serverId;
    this.logger = new Logger("src/logs/userDatabase_" + serverId + ".log", serverId);

    this.participants = new HashMap<>();
    this.transactionCommits = new HashMap<>();
    this.transactionLog = new HashMap<>();
    this.logger2PC = new Logger("src/logs/user_db/userDB2PC_" + serverId + ".log", serverId);
    this.transactionLogger
        = new Logger("src/logs/transactions/transaction_log_" + serverId + ".log", serverId);
    populateTransactionLog(transactionLog, "src/logs/transaction_log_" + serverId + ".log");
  }

  /**
   * Helper method that constructs the transaction log map by reading the transaction log file.
   *
   * @param transactionLog - map that needs to be populated
   * @param fileName - filename for the transaction log file
   */
  private void populateTransactionLog(Map<String, String[]> transactionLog, String fileName) {
    File file = new File(fileName);

    try (Scanner reader = new Scanner(file)) {

      while (reader.hasNextLine()) {
        String line = reader.nextLine();
        String log = line.split("]")[1].trim();
        String[] logTokens = log.split("\\|");

        if (logTokens[1].equals("Committed") || logTokens[1].equals("Aborted")) {
          transactionLog.remove(logTokens[0]);
        } else {
          String[] txnLog = new String[6];
          System.arraycopy(logTokens, 1, txnLog, 0, 6);
          transactionLog.put(logTokens[0], txnLog);
        }
      }
    } catch (FileNotFoundException fileNotFoundException) {
      //
    }
  }

  @Override
  public String signUp(String signupInfo) throws RemoteException {
    logger.debug(true, "SignUp Info received from the Client: ", signupInfo);
    logger2PC.debug(true, "SignUp Info received from the Client: ", signupInfo);

    String result = null;
    String[] tokens = this.userDB.parseMessage(signupInfo);
    String validatedResponse = this.userDB.validateTokens(tokens);

    if (validatedResponse.startsWith("Invalid")) {
      result = validatedResponse;
    } else if (validatedResponse.contains("2PC")) {
      result = start2PC(tokens);
    }

    logger.debug(true, "Sending response message to the Client: ", result);
    logger2PC.debug(true, "Sending response message to the Client: ", result);

    return result;
  }

  @Override
  public String login(String loginInfo) throws RemoteException {
    logger.debug(true, "Login Info received from the Client: ", loginInfo);

    String result = null;
    String[] tokens = this.userDB.parseMessage(loginInfo);
    String validatedResponse = this.userDB.validateTokens(tokens);

    if (validatedResponse.startsWith("Invalid")) {
      result = validatedResponse;
    } else if (validatedResponse.contains("2PC")) {
      result = start2PC(tokens);
    }

    logger.debug(true, "Sending response message to the Client: ", result);
    return result;
  }

  @Override
  public String logout(String emailId) throws RemoteException {
    logger.debug(true, "Logout Info received from the Client: ", emailId);
    return start2PC(new String[] {"LOGOUT", emailId});
  }

  @Override
  public UserDB getUserDB() throws RemoteException {
    return this.userDB;
  }

  @Override
  public Set<String> getSetOfLoggedInUsers() throws RemoteException {
    return this.userDB.getListOfLoggedInUsers();
  }

  @Override
  public User getUser(String emailId) throws RemoteException {
    User user = this.userDB.fetchUser(emailId);

    if (user == null) {
      logger.error(true, "Can't find logged in user with Email: ", emailId);
      return null;
    }

    logger.debug(true, "Found the Logged in user: ", user.getName());
    return this.userDB.fetchUser(emailId);
  }

  // This is for 2PC
  @Override
  public void connectWithAllServers(int numberOfServerInstances, int port) throws RemoteException {
    if (!participants.isEmpty()) {
      throw new IllegalStateException("The Server has already been connected with other servers. "
          + "Cannot connect again.");
    }

    Registry registry = null;
    try {
      registry = LocateRegistry.getRegistry(port);
    } catch (RemoteException e) {
      logger2PC.error(true, "Error connecting to RMI registry and while fetching the "
          + "server stub.");
    }

    for (int i = 0; i < numberOfServerInstances; i++) {
      String key = "UserDB2PC" + i;

      if (!key.equals(serverId)) {
        try {
          participants.put(key, (TwoPCServer) registry.lookup(key));
        } catch (RemoteException | NotBoundException e) {
          logger2PC.error(true, "Error connecting to RMI registry and while fetching the"
              + " server stub.");
        }
      } else {
        participants.put(key, this);
      }
    }
  }

  @Override
  public String start2PC(String[] operation) throws RemoteException {
    String transactionId = UUID.randomUUID().toString();
    logger2PC.debug(true, "Transaction ", transactionId, ": Initiating 2PC with " +
        "coordinator as ", serverId);

    // Phase 1 (voting phase) begins

    List<Boolean> votes = Collections.synchronizedList(new ArrayList<>());

    for (TwoPCServer participant : participants.values()) {
      new Thread(() -> {
        try {
          votes.add(participant.canCommit(transactionId, operation, serverId));
        } catch (RemoteException e) {
          logger2PC.error(true, "Error connecting to RMI registry and while fetching the"
              + " server stub.");
        }
      }).start();
    }

    long startTime = System.currentTimeMillis();
    while (true) {
      if (votes.size() == participants.size()) {
        break;
      }
      if (System.currentTimeMillis()-startTime > 15000) {
        votes.add(false);
        break;
      }
    }

    logger2PC.debug(true, "Transaction ", transactionId, ": Voting phase completed");
    logger2PC.debug(true, "Transaction ", transactionId, ": Votes: ", votes.toString());

    // Phase 1 (voting phase) completed

    // Phase 2 (completion as per the outcome of vote) begins

    if (votes.contains(false)) {
      for (TwoPCServer participant : participants.values()) {
        new Thread(() -> {
          try {
            participant.abortTransaction(transactionId);
          } catch (RemoteException e) {
            logger2PC.error(true, "Error connecting to RMI registry and while fetching the"
                + " server stub.");
          }
        }).start();
      }

      logger2PC.debug(true, "Transaction ", transactionId, " is being aborted.");
      System.out.println(votes);

      return "Failed";
    } else {
      logger2PC.debug(true, "Transaction ", transactionId, ": Starting commits.");
      String response = doCommit(transactionId);

      for (Map.Entry<String, TwoPCServer> entry : participants.entrySet()) {
        if (!serverId.equals(entry.getKey())) {
          new Thread(() -> {
            try {
              entry.getValue().doCommit(transactionId);
            } catch (RemoteException e) {
              logger2PC.error(true, "Error connecting to RMI registry and while fetching the"
                  + " server stub.");
            }
          }).start();
        }
      }

      new Thread(() -> waitForCommitResponses(transactionId)).start();
      return response;
    }

    // Phase 2 (completion as per the outcome of vote) completed
  }

  private void waitForCommitResponses(String transactionId) {
    long startTime = System.currentTimeMillis();
    logger2PC.debug(true, "Transaction ", transactionId, ": Waiting for commit responses");

    while (true) {

      if (transactionCommits.get(transactionId) == participants.size()) {
        logger2PC.debug(true, "Transaction ", transactionId, ": Received all commit " +
            "responses. Removing transaction.");
        transactionCommits.remove(transactionId);
        break;
      }

      if (System.currentTimeMillis()-startTime > 15000) {
        logger2PC.error(true, "Timed out while waiting for all commit responses!");
        break;
      }
    }
  }

  @Override
  public boolean canCommit(String transactionId, String[] operation, String coordinatorId)
      throws RemoteException {

    boolean canCommit = true;
    long startTime = System.currentTimeMillis();
    logger2PC.debug(true, "Transaction ", transactionId, ": canCommit() invoked");

    while(true) {
      String key = operation[1];

      for (String txId : transactionLog.keySet()) {
        String[] log = transactionLog.get(txId);

        if (key.equals(log[3]) && !log[0].equals("Committed") && !log[0].equals("Aborted")) {
          canCommit = false;
          break;
        }
      }
      if (canCommit) {
        break;
      }
      if (System.currentTimeMillis()-startTime > 15000) {
        break;
      }
    }

    if (canCommit) {
      String[] log = new String[6];
      log[0] = "Prepared to commit";
      log[1] = coordinatorId;
      int p = 2;

      for (String op : operation) {
        log[p] = op;
        p++;
      }

      log[5] = "0";
      transactionLog.put(transactionId, log);
      transactionLogger.logTransaction(transactionId, log);
      logger2PC.debug(true, "Transaction ", transactionId, ": Created transaction log",
          Arrays.asList(log).toString());

      return true;
    } else {
      return false;
    }
  }

  @Override
  public String doCommit(String transactionId) throws RemoteException {
    logger2PC.debug(true, "Transaction ", transactionId, ": doCommit() invoked");

    String[] log = transactionLog.get(transactionId);
    log[0] = "Started commit";
    transactionLogger.logTransaction(transactionId, log);

    int operationSize = (log[4] == null) ? 2 : 3;
    String[] operation = new String[operationSize];
    System.arraycopy(log, 2, operation, 0, operationSize);
    String response = this.userDB.executeOperation(operation);
    log[0] = "Committed";

    transactionLogger.logTransaction(transactionId, log);
    participants.get(log[1]).haveCommitted(transactionId);
    logger2PC.debug(true, "Transaction", transactionId, ": Transaction log",
        Arrays.asList(log).toString());
    transactionLog.remove(transactionId);

    return response;
  }

  @Override
  public void haveCommitted(String transactionId) throws RemoteException {
    logger2PC.debug(true, "Transaction ", transactionId, ": haveCommitted() invoked");
    transactionCommits.put(transactionId, transactionCommits.getOrDefault(transactionId, 0) + 1);
  }

  @Override
  public void abortTransaction(String transactionId) throws RemoteException {
    logger2PC.debug(true, "Transaction ", transactionId, ": doAbort() invoked");

    String[] log = transactionLog.get(transactionId);

    if (log != null) {
      log[0] = "Aborted";
      transactionLogger.logTransaction(transactionId, log);
      logger2PC.debug(true, "Transaction ", transactionId, ": Transaction log", Arrays.asList(log).toString());
      transactionLog.remove(transactionId);
    } else {
      String[] logEmpty = {"Aborted", "", "", "", "", ""};
      transactionLogger.logTransaction(transactionId, logEmpty);
    }
  }

  // Below methods are implemented in KeyValueStoreServer
  @Override
  public String executeOperation(String inputMessage, String clientEmailId) throws RemoteException {
    return null;
  }

  @Override
  public String putItinerary(Itinerary itinerary, String clientEmailId) throws RemoteException {
    return null;
  }
}