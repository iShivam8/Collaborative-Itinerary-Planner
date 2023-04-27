#CS6650 - Final Project
##Collaborative Itinerary Planner

####Group 13:

- Shivam Shukla (shukla.shi@northeastern.edu)
- Mahvash Maghrabi (maghrabi.m@northeastern.edu)
- Ali Saremi (saremi.a@northeastern.edu)

##Introduction

This project is a Collaborative Itinerary Planner that enables multiple individuals to contribute and 
plan a travel itinerary together. The goal of our project is to provide a user-friendly tool that 
enables groups of people to plan and organize their travel itineraries together, no matter where 
they are located. Our planner is specifically designed for use in distributed systems, where users 
may be located in different parts of the world and need a tool that allows for seamless 
collaboration. People can share ideas, suggestions, and preferences for destinations, activities, 
and accommodations, making it easier to come up with a travel plan. It can be especially helpful 
for group trips, where multiple people need to agree on travel plans. Our project allows users to 
create, get, delete, edit, and share the itinerary.


#How to run this project?

This project runs on Java 11.

##1. Using JAR Files

1. Open 3 separate New Terminals at folder - 'CS6650 Final Project'


2. On Terminal 1, you'll need to start Server.jar - It requires two argument:

[i] Port Number for registry, and

[ii] Number of server instances (How many replicas you want)

> java -jar src/Server.jar 1099 5

This would start the RMI registry on port 1099, and simultaneously would create 5 instances of server.
These 5 instances have their ID number starting from 0 i.e. 0, 1, 2, 3, 4.
The client would then use these ID numbers to connect with a particular server instance.

After that, it'll bound all those instances of the server stubs to the RMI Registry.
The log would be saved in logs/server.log


3. On Terminal 2, you'll need to start the Client.jar - It requires three arguments:

[i] RMI Registry IP Address / hostname (localhost or 127.0.0.1)

[ii] Port number to connect with the server via RMI Registry

[iii] Server ID Number (which instance of the server it wants to connect with)

> java -jar src/Client.jar 127.0.0.1 1099 2

The log for the client would be saved in src/logs/client_yyyy_mm_dd_HH_mm_ss.log


- For PUT, DELETE, EDIT, SHARE operations:
- The UserDBServer instance with whom client is connected,
- would act the coordinator and the rest of the instances would act as participants for 2PC protocol.

- the KeyValueStoreServer instance with whom client is connected,
  would act as the proposer and will initiate the PAXOS algorithm with the other instances
  of the server that would act as acceptors.

- Different server instances would have different logs, marked under src/logs/ folder



4. On Terminal 3, start a new client, and connect with a different instance of the server:

> java -jar src/Client.jar 127.0.0.1 1099 4



# Steps to follow once the Client gets Connected with the Server

After the client gets connected to the Server, you can enter different commands to execute the following operations:

PUT             -   To Add a new itinerary entry in key-value store

GET KEY         -   To fetch the itinerary of the entered key

DELETE KEY      -   To remove the specified itinerary from the store

EDIT KEY        -   To edit or update an existing itinerary

SHARE KEY user@email.com    -       To share the specified Itinerary with the specified user

LIST CREATED    -   Shows all the created itineraries by the user

LIST COLLAB     -   Displays all the itineraries that the user was added as a collaborator (Non-Owner)


- After the client gets connected with the server, you'll need to signup for two separate users.
- You can track the data operations that was performed using 2PC and PAXOS in the src/logs folder.


##Syntax to follow for entering user input requests on client console:

- For PUT requests:

> PUT

Following the put operation, the server will ask to enter itinerary details.


- For GET requests:

> GET KeyId

> GET d4f9c7c1-341-45cc-8b60-e35767c9533a


- For DELETE requests:

> DELETE d4f9c7c1-341-45cc-8b60-e35767c9533a


- For EDIT requests:

> EDIT d4f9c7c1-341-45cc-8b60-e35767c9533a shivam@gmail.com

Similar to PUT operation, the server will ask itinerary details to update.
After each update operation, the version of the itinerary changes by 1, to keep a track of update.


- For SHARE requests with other user:

> SHARE mahavash@gmail.com



- For LIST CREATED requests:

> LIST CREATED

This method returns all the list of itineraries created by the user.



- For LIST COLLAB requests:

> LIST COLLAB

This method returns the list of itineraries of which the current user has access but as a collaborator.



##NOTE: In order the exit from the application as a client, Enter: 'X' and Use '^C' to close the server.

Since this project runs on localhost, do not close the client terminal using '^C', 
because by doing so the client won't be able to log out.