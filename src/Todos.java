public class Todos {

  // TODO - Remove src/ from every logs creator

  // TODO - Authorization access issue: Even if one owner shares an itinerary, the other user cannot see it
  // TODO - If a user has access to an itinerary then only he can edit or share or delete

  // TODO - Objects are always changing: user?
  // TODO - List Collab shows two times the same Itinerary

  // TODO - Implement Profile Options: LIST CREATED throws multiple same values, and =[null] one time created has list size 3
  // TODO - Change Unique ID generated format

  // TODO - When we pass currentClientEmailId, learn() interferes with it due to executeOperation() recall

  // GET
  // TODO - If user have access to It i.e. he's an Owner or Collaborator, only then he can see it

  // EDIT
  // TODO - EDIT functionality. Only Owner and Shared users can edit the itinerary


  // FUTURE
  // To show who updated the last itinerary

  // In learn, we have a keyStore.executeOperation() where we pass the user.
  // What happens if it is invoked by a server instance which does not have a user; user == null
}
