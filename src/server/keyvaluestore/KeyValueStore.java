package server.keyvaluestore;

import java.util.concurrent.ConcurrentHashMap;
import logs.Logger;
import server.itinerary.Itinerary;

public class KeyValueStore {

  // KeyValueStore that stores Unique ID as Key, and Itinerary Object as value
  private final ConcurrentHashMap<Integer, Itinerary> keyValueStore;
  private final Logger logger;

  /**
   * Constructor of KeyValueStore that initializes the Key-Value Store.
   *
   * @param fileName - Filename for logger
   */
  public KeyValueStore(String fileName, String serverId) {
    this.keyValueStore = new ConcurrentHashMap<>();
    this.logger = new Logger(fileName, serverId);
  }
}
