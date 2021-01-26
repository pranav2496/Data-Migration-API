package com.example.demo;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Data;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.Lists;

import org.apache.commons.collections4.ListUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.RequestMapping;

public class Authorization {
	    /** Application name. */
	    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";

	    /** Directory to store user credentials for this application. */
	    private static final java.io.File DATA_STORE_DIR = new java.io.File(
	        System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");

	    /** Global instance of the {@link FileDataStoreFactory}. */
	    private static FileDataStoreFactory DATA_STORE_FACTORY;

	    /** Global instance of the JSON factory. */
	    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	    			
	    /** Global instance of the HTTP transport. */
	    private static HttpTransport HTTP_TRANSPORT;

	    /** Global instance of the scopes required by this quickstart.
	     *
	     * If modifying these scopes, delete your previously saved credentials
	     * at ~/.credentials/sheets.googleapis.com-java-quickstart
	     */
	    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS);

	    static {
	        try {
	            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
	        } catch (Throwable t) {
	            t.printStackTrace();
	            System.exit(1);
	        }
	    }

	    /**
	     * Creates an authorized Credential object.
	     * @return an authorized Credential object.
	     * @throws IOException
	     */
	    public static Credential authorize() throws IOException {
	        // Load client secrets.
	        InputStream in =
	        Authorization.class.getResourceAsStream("/credentials.json");		//Store credentials file into the resource folder
	        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
	        
	        // Build flow and trigger user authorization request.
	        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
	                .setDataStoreFactory(DATA_STORE_FACTORY)
	                .setAccessType("offline")
	                .build();
	        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	        //System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
	        return credential;
	    }

	    /**
	     * Build and return an authorized Sheets API client service.
	     * @return an authorized Sheets API client service
	     * @throws IOException
	     */
	    public static Sheets getSheetsService() throws IOException {
	        Credential credential = authorize();
	        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
	                .setApplicationName(APPLICATION_NAME)
	                .build();
	    }
	    //Function to Read the records present in the Google sheet
		public List<List<Object>> getSpreadSheetRecords(String spreadsheetId, String range) throws IOException {
			Sheets service = getSheetsService();		
	        ValueRange response = service.spreadsheets().values()
	                .get(spreadsheetId, range)
	                .execute();
	        List<List<Object>> values = response.getValues();
	        if (values != null && values.size() != 0) {
	        	return values;
	        } else {
	            System.out.println("No data found.");
	            return null;
	        }
		}
		
	   
		//Funtion used to find the size of List in bytes
		public static long getBytesFromList(List list) throws IOException {
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    ObjectOutputStream out = new ObjectOutputStream(baos);
		    out.writeObject(list);
		    out.close();
		    return baos.toByteArray().length;
		}
		
		//Funtion to clear all the data present in the Google Sheet
		public static void clearSheet(final String spreadsheetId, final String range) throws IOException {
		ClearValuesRequest requestBody = new ClearValuesRequest();

	    Sheets sheetsService = getSheetsService();
	    Sheets.Spreadsheets.Values.Clear request =
	        sheetsService.spreadsheets().values().clear(spreadsheetId, range, requestBody);
	    ClearValuesResponse response = request.execute();

	    System.out.println(response);
		}

		//Driver Function
		public static void main(String[] args) throws IOException {
		        Authorization auth = new Authorization();
		        final String spreadsheetId = "----------------------------";	//Particular Id of the Spreadsheet present in the Goolge Sheets
		        final String range = "Sheet1!A1:F";   //Range of the spreadsheet
			      
				
		        //Reading the Data from the postgresSql database
		        String url = "jdbc:postgresql://localhost:5432/postgres";
		        String user = "-----------";     //Login crdentials for postgres database
		        String password = "--------";

		        try (Connection con = DriverManager.getConnection(url, user, password);
		                Statement st = con.createStatement();
		        		ResultSet rs = st.executeQuery("SELECT * FROM pin_code")) {
	        		
		        	List<List<Object>> myData = new ArrayList<>();
		        	myData.add(Arrays.asList("Name","District Name","Division Name","Pin Number","State","Taluk"));
	        		
		        	while ( rs.next() ) {		//All the Data is stored in the myData list object
		        		myData.add(Arrays.asList(rs.getString("city"),rs.getString("district_name"),
		        				rs.getString("division_name"),rs.getInt("pin_number"),rs.getString("state"),
		        				rs.getString("taluk")));
		        		
		        	}   
		        	//Getting the number of bytes from the getBytesfromList(List) function
		        	System.out.println("Got "+getBytesFromList(myData)+" bytes of data from the database");           
		            	
		            clearSheet(spreadsheetId,range);
		        	
		        	//Checking the Data whether it is less than the required limit i.e 10MB
		            if(getBytesFromList(myData) <= 10485760) {
		        		ValueRange body = new ValueRange()		//Writing the values in the spreadsheet using append option
		        		        .setValues(myData);
		        		
		        		AppendValuesResponse append =
		        		        getSheetsService().spreadsheets().values().append(spreadsheetId, range, body)
		        		                .setValueInputOption("RAW")
		        		                .execute();
		        		System.out.println(body+" \n"+append);
						
		        		System.out.println("Written Data to..\n https://docs.google.com/spreadsheets/d/"+spreadsheetId+"/edit");
					            
		        	}
		        	else {
		        		//If the data is larger than required limit of the Google spreadsheet then,
		        		//Dividing the myData list object into four parts.
		        		List<List<List<Object>>> parts = Lists.partition(myData, (myData.size()+1)/4); 
			        	List<List<Object>> data_1 = new ArrayList<>(parts.get(0));
			            List<List<Object>> data_2 = new ArrayList<>(parts.get(1));
			            List<List<Object>> data_3 = new ArrayList<>(parts.get(2));
			            List<List<Object>> data_4 = new ArrayList<>(parts.get(4));
			           
			            //After storing data in four parts then writing each data parts using append method.
		        		System.out.println("Request payload size exceeds the limit 10485760 bytes...\n"
		        				+ "Writing the data into parts...");
		        		
		        		System.out.println("Got "+getBytesFromList(data_1)+" bytes from data part 1");           
		        		ValueRange body_1 = new ValueRange()
			            		.setValues(data_1);		
				        AppendValuesResponse append_1 =
				        		getSheetsService().spreadsheets().values().append(spreadsheetId, range, body_1)
				        		.setValueInputOption("RAW")
				        		.execute();
				        
				        System.out.println("Got "+getBytesFromList(data_2)+" bytes from data part 2");           
				        ValueRange body_2 = new ValueRange()
			            		.setValues(data_2);
				        AppendValuesResponse append_2 =
				        		getSheetsService().spreadsheets().values().append(spreadsheetId, range, body_2)
				        		.setValueInputOption("RAW")
				        		.execute();
				        
				        System.out.println("Got "+getBytesFromList(data_3)+" bytes from data part 3");           
				        ValueRange body_3 = new ValueRange()
			            		.setValues(data_3);
				        AppendValuesResponse append_3 =
				        		getSheetsService().spreadsheets().values().append(spreadsheetId, range, body_3)
				        		.setValueInputOption("RAW")
				        		.execute();
				        
				        System.out.println("Got "+getBytesFromList(data_4)+" bytes from data part 4");           
				        ValueRange body_4 = new ValueRange()
			            		.setValues(data_4);
				        AppendValuesResponse append_4 =
				        		getSheetsService().spreadsheets().values().append(spreadsheetId, range, body_4)
				        		.setValueInputOption("RAW")
				        		.execute();
		            	
			        	System.out.println("Written Data to..\n https://docs.google.com/spreadsheets/d/"+spreadsheetId+"/edit");
					    
		        		
		        	}
		         
		        }
			        
		        catch (Exception e) {
		        	System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		            System.exit(0);
		        }
		    }

}


