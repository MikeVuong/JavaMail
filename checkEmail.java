import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Multipart;
import javax.mail.Part;

import java.util.Scanner;

/* 
   The program is compiled with :
   javac -cp activation.jar:javax.mail.jar: checkEmail.java
   The program is run with the following,
   java -cp activation.jar:javax.mail.jar: checkEmail pop.gmail.com user password %d
   for %d input a number for a specific email, empty otherwise to display all unread emails:
*/
public class checkEmail {

   public static void check(String host, String storeType, String user,
      String password, String emailSelection) 
   {
      try {

      //create properties field
      Properties properties = new Properties();

      properties.put("mail.pop3.host", host);
      properties.put("mail.pop3.port", "995");
      properties.put("mail.pop3.starttls.enable", "true");
      Session emailSession = Session.getDefaultInstance(properties);
  
      //create the POP3 store object and connect with the pop server
      Store store = emailSession.getStore("pop3s");
      // Connect to the store
      store.connect(host, user, password);

      //create the folder object and open it
      Folder emailFolder = store.getFolder("INBOX");
      emailFolder.open(Folder.READ_ONLY);
      // Create a buffered reader to read the emails
      BufferedReader reader = new BufferedReader(new InputStreamReader(
         System.in));

      // retrieve the messages from the folder in an array and print it
      Message[] messages = emailFolder.getMessages();
      System.out.println("Unread Mail : " + messages.length);

      // Int variable used to check whether the user selected a specific email
      int checkLength  = Integer.parseInt(emailSelection);

      // Displays the emails content when the user specifies an email number
      // And when the email number is valid
      if(emailSelection != "999" && checkLength < messages.length)
      {
         // If the number is valid, displays the user specified email
         Message message = messages[checkLength];
         System.out.println("---------------------------------");
         writePart(message);
         String line = reader.readLine();
         if ("YES".equals(line)) {
            message.writeTo(System.out);
            emailFolder.close(false);
            store.close();
         } 
      } 
      // Called when the user chooses to display an email but the number
      // They have chosen is invalid or greater than the amount of unread 
      // Emails available
      else if(emailSelection != "999" && checkLength > messages.length - 1)
      {
         System.out.println("Invalid Number for email selection, Emails start at the 0 Index");
      }
      // If no number was specified display all emails without displaying contents
      else 
      {
         for (int i = 0, n = messages.length; i < n; i++) {
            Message message = messages[i];
            System.out.println("---------------------------------");
            System.out.println("Email Number " + (i));
            System.out.println("Subject: " + message.getSubject());
            System.out.println("From: " + message.getFrom()[0]);
            System.out.println("Text: " + message.getContent().toString());

         }
      }

      //close the store and folder objects
      emailFolder.close(false);
      store.close();

      } catch (NoSuchProviderException e) {
         e.printStackTrace();
      } catch (MessagingException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void main(String[] args) {

         // Email server client, in this case it is pop.gmail.com
         String host;
         // Type of server, usually either POP or IMEI. In this case it is pop3
         String mailStoreType;
         // Username for the email client
         String username;
         // Password for the email client
         String password;
         // Used when the user wishes to view the contents for a specified email
         String emailSelection;

         try{
         // Assigning host based on argument 0
         host = args[0];
         mailStoreType = "pop3";
         // Assigning user based on argument 1
         username = args[1];
         // Assigning user based on argument 2
         password = args[2];
         // Default value for email selection for if the user does not wish to 
         // Choose a number and view all emails
         emailSelection = "999";
         // Empty case for if the user does not specify a number
         if(args.length == 4)
         {
            emailSelection = args[3];
         }

         check(host, mailStoreType, username, password, emailSelection);

         }catch(ArrayIndexOutOfBoundsException ex)
         {

         }

   }

   /*
   * This method checks for content-type 
   * based on which, it processes and
   * fetches the content of the message
   * Taken directly from the example "Fetching Email"
   */
   public static void writePart(Part p) throws Exception {
      if (p instanceof Message)
         //Call methos writeEnvelope
         writeEnvelope((Message) p);

      System.out.println("----------------------------");
      System.out.println("CONTENT-TYPE: " + p.getContentType());

      //check if the content is plain text
      if (p.isMimeType("text/plain")) {
         System.out.println("This is plain text");
         System.out.println("---------------------------");
         System.out.println((String) p.getContent());
      } 
      //check if the content has attachment
      else if (p.isMimeType("multipart/*")) {
         System.out.println("This is a Multipart");
         System.out.println("---------------------------");
         Multipart mp = (Multipart) p.getContent();
         int count = mp.getCount();
         for (int i = 0; i < count; i++)
            writePart(mp.getBodyPart(i));
      } 
      //check if the content is a nested message
      else if (p.isMimeType("message/rfc822")) {
         System.out.println("This is a Nested Message");
         System.out.println("---------------------------");
         writePart((Part) p.getContent());
      } 
      else if (p.getContentType().contains("image/")) {
         System.out.println("content type" + p.getContentType());
         File f = new File("image" + new Date().getTime() + ".jpg");
         DataOutputStream output = new DataOutputStream(
            new BufferedOutputStream(new FileOutputStream(f)));
            com.sun.mail.util.BASE64DecoderStream test = 
                 (com.sun.mail.util.BASE64DecoderStream) p
                  .getContent();
         byte[] buffer = new byte[1024];
         int bytesRead;
         while ((bytesRead = test.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
         }
      } 
      else {
         Object o = p.getContent();
         if (o instanceof String) {
            System.out.println("This is a string");
            System.out.println("---------------------------");
            System.out.println((String) o);
         } 
         else if (o instanceof InputStream) {
            System.out.println("This is just an input stream");
            System.out.println("---------------------------");
            InputStream is = (InputStream) o;
            is = (InputStream) o;
            int c;
            while ((c = is.read()) != -1)
               System.out.write(c);
         } 
         else {
            System.out.println("This is an unknown type");
            System.out.println("---------------------------");
            System.out.println(o.toString());
         }
      }

   }
   /*
   * This method would print FROM,TO and SUBJECT of the message
   */
   public static void writeEnvelope(Message m) throws Exception {
      System.out.println("This is the message envelope");
      System.out.println("---------------------------");
      Address[] a;

      // FROM
      if ((a = m.getFrom()) != null) {
         for (int j = 0; j < a.length; j++)
         System.out.println("FROM: " + a[j].toString());
      }

      // TO
      if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
         for (int j = 0; j < a.length; j++)
         System.out.println("TO: " + a[j].toString());
      }

      // SUBJECT
      if (m.getSubject() != null)
         System.out.println("SUBJECT: " + m.getSubject());

   }

}