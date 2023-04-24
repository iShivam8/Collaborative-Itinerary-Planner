package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import server.itinerary.Itinerary;
import server.user.User;

/**
 * Helper class for client to continuously fetch the user inputs.
 */
public class ClientInputHelper {

  static String fetchSignupOrLoginInput() {

    // TODO - If invalid input, then keep asking user until valid inputs

    System.out.println("\nChoose from the following Requests: ");
    System.out.println("1] Sign Up for New Account: Enter - Signup");
    System.out.println("2] Log In your Account: Enter - Login");
    System.out.println("\nPlease enter your intended action: ");

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    try {
      String userInput = bufferedReader.readLine().trim();

      if (userInput.equalsIgnoreCase("Signup") || userInput.equals("1")) {
        return "SignUp";
      } else if (userInput.equalsIgnoreCase("Login") || userInput.equals("2")) {
        return "LogIn";
      }

    } catch (Exception e) {
      System.out.println("Exception Occurred! Incorrect Sign In Inputs! Please try again!");
    }

    return null;
  }

  /**
   * Method to fetch user sign up information: Name, Email, Password
   *
   * @return
   */
  static String fetchSignUpInput() {

    // TODO - If invalid input, then keep asking user until valid inputs

    System.out.println("\nEnter the following Details to Register a New Account: ");
    System.out.println("1] Enter Your First Name only");
    System.out.println("2] Enter Your Email ID");
    System.out.println("3] Enter Your Password");
    System.out.println("\nPlease enter the above asked details: ");
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    try {
      String userInput = bufferedReader.readLine();

      String[] array = userInput.split("\\s");
      StringBuilder stringBuilder = new StringBuilder();

      for (String str: array) {
        if (str != null && str.length() != 0) {
          stringBuilder.append(str).append("|");
        }
      }

      return stringBuilder.substring(0, stringBuilder.length() - 1);
    } catch (Exception e) {
      System.out.println("Exception Occurred while taking Sign Up input details! Please try again!");
    }

    return null;
  }

  /**
   * Method to take Login user input: Email & password.
   *
   * @return
   */
  static String fetchLoginInput() {
    // TODO - If invalid input, then keep asking user until valid inputs

    System.out.println("\nEnter the following Credentials to Login: ");
    System.out.println("1] Enter Your Email ID");
    System.out.println("2] Enter Your Password");
    System.out.println("\nPlease enter the above asked details: ");

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    try {
      String userInput = bufferedReader.readLine();

      String[] array = userInput.split("\\s");
      StringBuilder stringBuilder = new StringBuilder();

      for (String str: array) {
        if (str != null && str.length() != 0) {
          stringBuilder.append(str).append("|");
        }
      }

      return stringBuilder.substring(0, stringBuilder.length() - 1);
    } catch (Exception e) {
      System.out.println("Exception Occurred while taking Login input details! Please try again!");
    }

    return null;
  }

  /**
   * Method to fetch the user input and pass it to the intended methods as per the input
   * specified by the user - PUT / GET / DELETE / EDIT / SHARE / X.
   *
   * @param takeUserInput - True, if we need to continue taking user inputs, false otherwise
   * @return - Input string entered by the user
   */
  static String fetchUserOperationInput(boolean takeUserInput) {

    // Keep taking the user input unless the boolean value turns false
    if (takeUserInput) {
      System.out.println("\nChoose from the following Requests: ");
      System.out.println("1] Create a new Itinerary: PUT");
      System.out.println("2] Get an Itinerary:       GET  (Itinerary-ID)");
      System.out.println("3] Delete an Itinerary:    DELETE  (Itinerary-ID)");
      System.out.println("4] Edit an Itinerary:      EDIT (Itinerary-ID)");
      System.out.println("5] Share an Itinerary:     SHARE (Itinerary-ID) (Email id)\n");
      // TODO - Implement User Profile Operations
      System.out.println("User Profile Operations:");
      // This will print all the itineraries that are created by this user,
      // and with whom he has shared it with
      System.out.println("a] List of Created and Shared Itineraries:        LIST CREATED");
      // This will print out the itineraries that you have been invited to collaborate
      // And you're not the owner but a collaborator for that Itinerary.
      System.out.println("b] List of Collaborated Itineraries (Non-Owner):  LIST COLLAB");
      System.out.println("Enter X to exit the Application and Logout!");
    }

    System.out.println("\nPlease enter your intended action: ");

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    try {
      String userInput = bufferedReader.readLine();

      // If the user decides to quit the application
      if (userInput.equalsIgnoreCase("X")) {
        return userInput;
      }

      String[] array = userInput.split("\\s");
      StringBuilder stringBuilder = new StringBuilder();

      for (String str: array) {
        if (str != null && str.length() != 0) {
          stringBuilder.append(str).append("|");
        }
      }

      return stringBuilder.substring(0, stringBuilder.length() - 1);
    } catch (Exception e) {
      System.out.println("Exception Occurred! Incorrect User Input! Please try again!");
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Method to fetch user inputs for creating a New itinerary.
   *
   * @param user - current user who's creating the itinerary
   * @return - Itineirary object with all the trip details
   */
  static Itinerary fetchItineraryInput(User user) {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    String name = null, location = null;
    Date startDate = null, endDate = null;
    String description = null;

    // Take Trip Name
    try {
      System.out.println("1] Enter Itinerary Name: ");
      name = br.readLine();
    } catch (IOException e) {
      System.out.println("Error reading Name Input!");
      return null;
    }

    // Take Trip Location
    try {
      System.out.println("2] Enter Location: ");
      location = br.readLine();
    } catch (IOException e) {
      System.out.println("Error reading Location Input!");
      return null;
    }

    // Taking startDate input
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    sdf.setLenient(false); // to prevent parsing invalid dates like Feb 30

    boolean validStartDate = false;
    while (!validStartDate) {
      try {
        System.out.print("3] Enter Start Date (MM/dd/yyyy): ");
        startDate = sdf.parse(br.readLine());

        validateStartDate(startDate);

        validStartDate = true;
      } catch (ParseException e) {
        System.out.println("Invalid date format! Enter date in MM/dd/yyyy format.");
      } catch (IOException e) {
        System.out.println("Error reading Start Date input! Please try again.");
      } catch (DateTimeException de) {
        // Throw exception
      }
    }

    // Taking endDate input
    boolean validEndDate = false;
    while (!validEndDate) {
      try {
        System.out.print("4] Enter End Date (MM/dd/yyyy): ");
        endDate = sdf.parse(br.readLine());

        validateEndDate(startDate, endDate);

        validEndDate = true;
      } catch (ParseException e) {
        System.out.println("Invalid date format! Enter date in MM/dd/yyyy format.");
      } catch (IOException e) {
        System.out.println("Error reading End Date input! Please try again.");
      } catch (DateTimeException de) {
        // Throw exception
      }
    }

    // Taking Itinerary Description
    try {
      System.out.println("5] Enter Itinerary Description: ");
      description = br.readLine();

      /*
      //For taking continuous input

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
        String line;
        while ((line = reader.readLine()) != null && line.length() > 0) {
          description.append(line).append("\n");
        }
      } catch (IOException e) {
        System.out.println("Error occurred while taking Itinerary Description input!");
      }
      */

    } catch (Exception e) {
      System.out.println("Exception Occurred while taking Itinerary Description details! " +
          "Please try again!");
    }

    try {
      return new Itinerary(name, location, startDate, endDate, description, user);
    } catch (Exception e) {
      System.out.println("Error while creating a new Itinerary! Please try Again!");
      e.printStackTrace();
    }

    return null;
  }

  private static void validateStartDate(Date startDate) {
    // Get today's date
    LocalDate today = LocalDate.now();
    LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

    if(startLocalDate.isBefore(today)) {
      System.out.println("Start date cannot be before today's date.");
      throw new DateTimeException("Start date cannot be before today's date.");
    }
  }

  // Helper method to validate End date
  private static void validateEndDate(Date startDate, Date endDate) {
     if (endDate.compareTo(startDate) < 0) {
      // end date is before start date
      System.out.println("End date cannot be before start date");
      throw new DateTimeException("End date cannot be before start date");
    } else if (startDate.compareTo(endDate) > 0) {
       // start date is after end date
       System.out.println("Start date cannot be after end date");
       throw new DateTimeException("tart date cannot be after end date");
     }
  }
}
