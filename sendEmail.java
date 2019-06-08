import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.Files;
import java.util.concurrent.*;
import java.util.logging.*;
import java.lang.*;
import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/*
   The program is compiled with :
   javac -cp activation.jar:javax.mail.jar: sendEmail.java
   The program is run with: 
   java -cp activation.jar:javax.mail.jar: sendEmail thisfile.txt
*/

public class sendEmail {
   public static void main(String[] args){

   try{
      // Create a buffered reader to read the file
      BufferedReader temp = new BufferedReader(new InputStreamReader(
         new FileInputStream(args[0])));
    
      // Used to read the split information
      String[] parsedOutput;
      // Used to split the parsed output
      String[] split;
      // Used to contain the current CC reciever 
      String[] ccCopy;
      // Used to contain the current BCC reciever
      String[] bccCopy;
      // Allocating sizes for the strings
      parsedOutput = new String[20];
      ccCopy = new String[10];
      bccCopy = new String[10];
      // Making initial ints for later use
      // Used to keep index on which line the file is reading
      int i = 0;
      // Used for keeping index on the cc and bcc recievers
      int j = 0;
      // Used for keeping index of the amount of recievers
      int k = 0;

      // Parses through the file line by line until end of file
      for(String test = temp.readLine(); test != null; test = temp.readLine()){
                
         // Split the read line delimited by a colon       
         split = test.split(":");
         // Only save the information after the colon
         parsedOutput[i] = split[1];

         // Used for CC data
         if(i == 4)
         {
            // Split the information based on each seperate cc reciever
            split = parsedOutput[i].split(",");
            // Loop for every cc reciever
            for(String cpy : split)
            {
               // Create a copy of each cc reciever and save into an array
               ccCopy[j] = cpy;
               j++;
            }
         }

         // Used for BCC data
         if(i == 5)
         {
            // Reset counter
            j = 0;
            // Split the information based on each seperate bcc reciever
            split = parsedOutput[i].split(",");
            // Loop for every bcc reciever
            for(String cpy : split)
            {
               // Copy the bcc client names into an array
               bccCopy[j] = cpy;
               j++;
            }
         }

         i++;
      }

/*
The following indexes for parsed output are as follows: 
0 : Server: email server
1 : User: email account (also used as the From: in the email)
2 : Password: email account password
3 : To: primary recipient
4 : CC: comma separated list of secondary recipients
5 : BCC: comma separated list of tertiary recipients
6 : Subject: Email subject
7 : Body: multiple lies of text representing the body of the email
*/

      // Closing the reader after copying over the contents    
      temp.close();

      // Recipient's email ID needs to be mentioned.
      String to = parsedOutput[3];
      // Sender's email ID needs to be mentioned
      String from = parsedOutput[1];
      // Sender's username read from file
      final String username = parsedOutput[1];
      // Sender's password read from file
      final String password = parsedOutput[2];
      // Host name read from file, in this case it is smtp.gmail.com
      String host = parsedOutput[0];

      // Creating properties to be used with the session object
      Properties props = new Properties();
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.host", host);
      props.put("mail.smtp.port", "587");

      // Get the Session object.
      // Authenticates based on user/password, fails if invalid
      Session session = Session.getInstance(props,
      new javax.mail.Authenticator() {
         protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
         }
      });

      try {
         // Create a default MimeMessage object.
         Message message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.setRecipients(Message.RecipientType.TO,
         InternetAddress.parse(to));

         // Iterating through each cc and bcc client, if there are more
         // Adjust the max value of k accordingly
         for(k = 0; k < 3; k++)
         {
            // Add the cc recievers to the recipient list
            message.addRecipients(Message.RecipientType.CC, 
               InternetAddress.parse(ccCopy[k]));
            // Add the bcc recievers to the recipient list         
            message.addRecipients(Message.RecipientType.BCC, 
               InternetAddress.parse(bccCopy[k]));
         }

         // Set Subject: header field read from file
         message.setSubject(parsedOutput[6]);
         // Now set the actual message read from file
         message.setText(parsedOutput[7]);

         // Send message
         Transport.send(message);
         // Confirmation message
         System.out.println("Sent message successfully....");

      } catch (MessagingException e) {
            throw new RuntimeException(e);
      } catch (ArrayIndexOutOfBoundsException ex)
      {
         System.out.println("File not found");
      }
   }catch (IOException ex)
   {
      System.out.println("File not found");

   }
   }
}

