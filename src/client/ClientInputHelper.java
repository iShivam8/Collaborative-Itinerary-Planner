package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import server.itinerary.Itinerary;
import server.user.User;

/**
 * Helper class for client to continuously fetch the user inputs.
 */
public class ClientInputHelper {

  static String fetchSignupOrLoginInput() {

    System.out.println("\nChoose from the following Requests: ");
    System.out.println("1] Sign Up for New Account: Enter - Signup");
    System.out.println("2] Log In your Account: Enter - Login");
    System.out.println("\nPlease enter your intended action: ");

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    try {
      String userInput = bufferedReader.readLine().trim();

      if (userInput.equalsIgnoreCase("Signup")) {
        return "SignUp";
      } else if (userInput.equalsIgnoreCase("Login")) {
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
      System.out.println("5] Share an Itinerary:     SHARE (Itinerary-ID) (Email id)");
      System.out.println("Enter X to exit the Application");
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
        validStartDate = true;
      } catch (ParseException e) {
        System.out.println("Invalid date format! Enter date in MM/dd/yyyy format.");
      } catch (IOException e) {
        System.out.println("Error reading Start Date input! Please try again.");
      }
    }

    // Taking endDate input
    boolean validEndDate = false;
    while (!validEndDate) {
      try {
        System.out.print("Enter End Date (MM/dd/yyyy): ");
        endDate = sdf.parse(br.readLine());
        validEndDate = true;
      } catch (ParseException e) {
        System.out.println("Invalid date format! Enter date in MM/dd/yyyy format.");
      } catch (IOException e) {
        System.out.println("Error reading End Date input! Please try again.");
      }
    }

    // TODO - Check if end date is before start date or not


    // Taking Itinerary Description
    try {
      System.out.println("5] Enter Itinerary Description: ");
      description = br.readLine();

      /*
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
      Itinerary itinerary = new Itinerary(name, location, startDate, endDate, description, user);
      //user.setListOfCreatedItinerary(itinerary);
      return itinerary;
    } catch (Exception e) {
      System.out.println("Error while creating a new Itinerary! Please try Again!");
      e.printStackTrace();
    }

    return null;
  }
}
