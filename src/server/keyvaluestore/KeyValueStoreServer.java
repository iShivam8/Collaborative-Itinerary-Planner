package server.keyvaluestore;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import logs.Logger;
import server.PaxosServer;
import server.Promise;
import server.Server;
import server.itinerary.Itinerary;
import server.user.User;
import server.user.UserDB;

public class KeyValueStoreServer implements Server, PaxosServer {

  private final KeyValueStore keyValueStore;
  private final Map<String, PaxosServer> acceptors;
  private final Map<String, Promise> metadata;
  private static final int MAXIMUM_PAXOS_RETRIES = 10;
  private final String serverId;
  private final Logger logger;

  private User user;

  public KeyValueStoreServer(String serverId, Server userDb) {
    this.keyValueStore = new KeyValueStore("src/logs/server_" + serverId + ".log", serverId, userDb);
    this.acceptors = new HashMap<>();
    this.metadata = new HashMap<>();
    this.serverId = serverId;
    this.logger = new Logger("src/logs/server_" + serverId + ".log", serverId);
  }

  @Override
  public void connectWithAllServers(int numberOfServers, int portNumber) throws RemoteException {
    if (!acceptors.isEmpty()) {
      throw new IllegalStateException("Warning! Server has already connected with other servers. "
          + "Cannot connect again!");
    }

    Registry registry = null;

    try {
      registry = LocateRegistry.getRegistry(portNumber);
    } catch (RemoteException remoteException) {
      logger.error(true, "Error connecting to RMI registry and fetching server stub " +
          "while connecting with all servers!");
      return;
    }

    for (int i = 0; i < numberOfServers; i++) {
      String key = "KVS" + i;

      if (!key.equals(serverId)) {
        try {
          acceptors.put(key, (PaxosServer) registry.lookup(key));
        } catch (RemoteException | NotBoundException exception) {
          logger.error(true, "Error connecting to RMI registry and fetching server " +
              "stub while connecting with all servers!");
        }
      } else {
        acceptors.put(key, this);
      }
    }
  }

  @Override
  public String startPaxos(String[] inputTokens) throws RemoteException {

    String key = inputTokens[1], value = null;

    if (inputTokens.length == 3) {
      value = inputTokens[2];
    }

    int minMajority = acceptors.size() / 2 + 1;
    ExecutorService executorService = Executors.newCachedThreadPool();

    for (int i = 0; i < MAXIMUM_PAXOS_RETRIES; i++) {

      // Phase 1 of PAXOS begins:

      /*
      Proposer sends prepare(sequenceNumber) requests to all the Acceptors.
      - It then waits for responses from all the acceptors

      - If the majority of acceptors responds with Promises, it proceeds ahead.

      - However, if all acceptors respond with only promises, Proposer's value is Accepted.

      - And if some acceptors have already accepted values,
        then the value with the highest sequence id number becomes the Proposed value.
      */

      logger.debug(true, "Initiating PAXOS Phase 1");
      long sequenceNumber = generateCurrentSequenceNumber();
      logger.debug(true, "Sequence Number: ", String.valueOf(sequenceNumber));

      List<Callable<Promise>> prepareTasks = new ArrayList<>();

      for (PaxosServer paxosServer: acceptors.values()) {
        prepareTasks.add(() -> paxosServer.prepare(sequenceNumber, key));
      }

      int promises = 0;
      long maxSequenceNumber = 0;

      try {
        List<Future<Promise>> prepareResponses = executorService
            .invokeAll(prepareTasks, 30, TimeUnit.SECONDS);

        for (Future<Promise> result: prepareResponses) {
          Promise promise = result.get();

          if (promise != null) {

            // If result of the prepare request is Accepted or Promised,
            // then increment the promises

            if (promise.getStatus().equalsIgnoreCase("Accepted")
                || promise.getStatus().equalsIgnoreCase("Promised")) {
              promises++;

              // If the response contains accepted value,
              // choose the proposed value with the highest accepted sequence number

              if (promise.getAccepted()
                  && promise.getAcceptedSequenceNumber() > maxSequenceNumber) {
                maxSequenceNumber = promise.getAcceptedSequenceNumber();
                value = promise.getAcceptedValue();
              }
            }
          }
        }
      } catch (ExecutionException | InterruptedException exception) {
        logger.error(true, "Error in PAXOS Phase 1!");
      }

      logger.debug(true, "Number of received Promises: ", String.valueOf(promises));

      if (minMajority > promises) {
        logger.debug(true, "Consensus cannot be reached! Number of Promises = ",
            String.valueOf(promises), ", minimum majority = ", String.valueOf(minMajority));
        logger.debug(true, "PAXOS Try", String.valueOf(i + 1), " failed!");
        continue;
      }


      /*
      Now we know that Majority of Acceptors have responded with Promises.
      Proposer will now begin the Phase 2 of PAXOS.

      - Proposer sends propose(sequence, proposedValue) request to every acceptor
      - And if majority acceptors responds with Positive responses, Consensus would have been achieved
       */

      List<Callable<Boolean>> proposeTasks = new ArrayList<>();
      logger.debug(true, "Initiating PAXOS Phase 2 with proposed value: ", value);

      for (PaxosServer acceptor: acceptors.values()) {
        String finalValue = value;
        proposeTasks.add(() -> acceptor.propose(sequenceNumber, key, finalValue));
      }

      int totalAcceptedResponses = 0;

      try {
        List<Future<Boolean>> proposeResponses = executorService
            .invokeAll(proposeTasks, 30, TimeUnit.SECONDS);

        for (Future<Boolean> result: proposeResponses) {
          Boolean isAccepted = result.get();

          // To track how many Acceptors have accepted the Proposal
          System.out.println("isAccepted: " + isAccepted);
          if (isAccepted != null && isAccepted) {
            totalAcceptedResponses++;
          }
        }

      } catch (ExecutionException | InterruptedException exception) {
        logger.error(true, "Error in PAXOS Phase 2!");
      }

      logger.debug(true, "Number of Accepted Responses received: ",
          String.valueOf(totalAcceptedResponses));

      if (minMajority > totalAcceptedResponses) {
        logger.debug(true, "Consensus cannot be reached! Number of accepted responses = ",
            String.valueOf(totalAcceptedResponses), ", and minimum majority = ",
            String.valueOf(minMajority));
        logger.debug(true, "PAXOS Try", String.valueOf(i + 1), " failed!");
      }


      /*
      Now after the consensus have been achieved, the Value can be Learned.
      Here, the learned value is committed.
       */

      logger.debug(true, "Consensus has been reached! Learning and Committing the value: ",
          value, " for Key: ", key);

      String response = learn(key, value);

      for (Map.Entry<String, PaxosServer> entry: acceptors.entrySet()) {
        if (!serverId.equals(entry.getKey())) {
          String finalValue = value;
          executorService.submit(() -> entry.getValue().learn(key, finalValue));
        }
      }

      return response;
    }

    logger.error(true, "Error! Consensus could Not be reached even after " +
        "Maximum number of tries!");
    return "Failed";
  }

  // Helper method to generate current system time as the sequence id number
  private static long generateCurrentSequenceNumber() {
    return System.currentTimeMillis();
  }

  @Override
  public Promise prepare(long sequenceId, String key) throws RemoteException {

    if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
      logger.error(true, "Acceptor", serverId, " has failed!");
      return null;
    }

    logger.debug(true, "Prepare() request received with sequence id: ",
        String.valueOf(sequenceId), ", for Key: ", key);

    if (!metadata.containsKey(key)) {
      metadata.put(key, new Promise());
    }

    long currentSequenceId = metadata.get(key).getSequenceNumber();

    // Reject the current prepare request if a larger sequence number was promised / accepted
    if (sequenceId <= currentSequenceId) {
      logger.debug(true, "Rejecting the prepare() request because sequence id: ",
          String.valueOf(sequenceId), ", and current sequence id: ",
          String.valueOf(currentSequenceId));
      Promise promise = new Promise();
      promise.setStatus("Rejected");
      return promise;
    }

    // Setting sequence id to the larger sequence number from the prepare request
    // But, return the proposal first, if there's already an accepted proposal
    metadata.get(key).setSequenceNumber(sequenceId);

    if (!metadata.get(key).getAccepted()) {
      metadata.get(key).setStatus("Promised");
    }

    logger.debug(true, "Responding back to the prepare() request with: ",
        metadata.get(key).toString());

    return new Promise(metadata.get(key));
  }

  @Override
  public Boolean propose(long sequenceId, String key, String value) throws RemoteException {

    if (ThreadLocalRandom.current().nextInt(0, 10) == 0) {
      logger.error(true, "Acceptor", serverId, " has failed!");
      return null;
    }

    logger.debug(true, "Prepare() request received with sequence id: ",
        String.valueOf(sequenceId), ", for Key: ", key, ", and Proposed value: ", value);

    if (!metadata.containsKey(key)) {
      logger.error(true, "Cannot execute propose() Key does not have the correct metadata");
      return false;
    }

    // Do not accept - if the sequence id in propose request != highest sequence id seen until now

    if (sequenceId != metadata.get(key).getSequenceNumber()) {
      logger.debug(true, "Rejecting propose() request, since the Sequence Id: ",
          String.valueOf(sequenceId), " does not match with maximum promised sequence number: ",
          String.valueOf(metadata.get(key).getSequenceNumber()));

      return false;
    }

    // If everything is fine, accept the current proposal
    // Store its value and the sequence id number as the accepted value, and accepted sequence id

    logger.debug(true, "Accepting proposed value: ", value, ", for Key: ", key,
        ", and setting the accepted sequence id number to: ", String.valueOf(sequenceId));

    metadata.get(key).setStatus("Accepted");
    metadata.get(key).setAccepted(true);
    metadata.get(key).setAcceptedValue(value);
    metadata.get(key).setAcceptedSequenceNumber(sequenceId);

    return true;
  }

  @Override
  public String learn(String key, String value) throws RemoteException {
    // Need to commit the accepted value
    logger.debug(true, "Learning and Committing the Value: ", value, " for Key: ", key);

    String[] operation;

    if (value == null) {
      operation = new String[2];
      operation[0] = "DELETE";
      operation[1] = key;
    } else {
      operation = new String[3];
      operation[0] = "PUT";
      operation[1] = key;
      operation[2] = value;
    }

    String result;

    if (operation[0].equals("PUT")) {
      result = "Itinerary Created";
    } else {
      result = keyValueStore.executeOperation(operation, user);
    }

    metadata.remove(key);
    return result;
  }

  @Override
  public String executeOperation(String inputMessage, User currentUser) throws RemoteException {

    // InputMessage:  Put;   EDIT|123;    Get|123;    Delete|123;   Share|123|a@a.com;
    logger.debug(true, "Message received from the Client: ", inputMessage);
    this.user = currentUser;

    String result;
    String[] tokens = keyValueStore.parseMessage(inputMessage);
    String validatedResponse = keyValueStore.validateTokens(tokens);

    if (validatedResponse.startsWith("Invalid")) {
      result = validatedResponse;
    } else if (validatedResponse.contains("PAXOS")) {
      result = startPaxos(tokens);
    } else {
      result = keyValueStore.executeOperation(tokens, currentUser);
    }

    logger.debug(true, "Sending response message to the Client: ", result);
    return result;
  }

  @Override
  public String putItinerary(Itinerary itinerary, User currentUser) throws RemoteException {
    logger.debug(true, "Itinerary received from the Client: ", itinerary.getName());

    String itineraryId = keyValueStore.addItinerary(itinerary, currentUser);
    String[] tokens = {"PUT", itineraryId, itinerary.getName()};
    String result = startPaxos(tokens);

    // TODO - Not receiving Itinerary ID on Client side

    logger.debug(true, "Sending response message to the Client: ", itineraryId);
    logger.debug(true, "Client Access the Created Itinerary of: ",
        itinerary.getName(), ", with Unique Key ID: ", itineraryId);

    return itineraryId;
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

  @Override
  public User getUser() {
    logger.debug(true, "Is it running this server?");
    return null;
  }

  @Override
  public UserDB getUserDB() {
    return null;
  }
}
