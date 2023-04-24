package server.keyvaluestore;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Class that helps to generate Unique IDs everytime a new itinerary is created.
 * It ensures no id would be repeated by UUID, by using a Set in conjunction with recursion.
 */
public class UniqueIdGenerator {

  static Set<String> setOfUniqueIds = new HashSet<>();

  static String generateId() {
    String uniqueId = UUID.randomUUID().toString();

    if (setOfUniqueIds.contains(uniqueId)) {
      uniqueId = generateId();
    } else {
      setOfUniqueIds.add(uniqueId);
      return uniqueId;
    }

    return uniqueId;
  }
}
