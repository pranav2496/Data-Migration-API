package com.example.demo;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

@RestController
public class Operations {
	/**
     * Prints the names and majors of students in a sample spreadsheet:
     * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
     */
	@RequestMapping("/create")
    public static void ReadData() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        Authorization auth = new Authorization();
        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties()
                        .setTitle("My Sheet"));
        spreadsheet = Authorization.getSheetsService().spreadsheets().create(spreadsheet)
                .setFields("spreadsheetId")
                .execute();
        System.out.println("Spreadsheet ID: " + spreadsheet.getSpreadsheetId());
        //return auth.getSpreadSheetRecords(spreadsheetId, range);
        
    }
	
	@RequestMapping("/demo")
	public void whenWriteSheet_thenReadSheetOk() throws IOException, GeneralSecurityException {
		  final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		  final String spreadsheetId = "1kS74KCIvz9w1-EuEYSHwoe6YvCwK1kNHjPvpCrArjkA";
		  final String range = "Sheet1!A1:D";
	      
		ValueRange body = new ValueRange()
          .setValues(Arrays.asList(
            Arrays.asList("Expenses January"), 
            Arrays.asList("books", "30"), 
            Arrays.asList("pens", "10"),
            Arrays.asList("Expenses February"), 
            Arrays.asList("clothes", "20"),
            Arrays.asList("shoes", "5")));
        UpdateValuesResponse result = Authorization.getSheetsService()
        		.spreadsheets().values()
        		.update(spreadsheetId, "A1", body)
        		.setValueInputOption("RAW")
        		.execute();
    }

}
