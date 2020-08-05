package com.ptps.microservices.taxamountDataloadService.util.environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import com.ptps.microservices.taxamountDataloadService.resource.TaxPaymentOrder;

//import com.bezkoder.spring.files.csv.model.Tutorial;

public class CSVHelper {
  public static String TYPE = "text/csv";
  static String[] HEADERs = { "loanNumber", "customerName", "agency", "amountToBePaid", "dueDate" };

  public static boolean hasCSVFormat(MultipartFile file) {

    if (!TYPE.equals(file.getContentType())) {
      return false;
    }

    return true;
  }

  public static List<TaxPaymentOrder> csvToOrder(InputStream is) throws NumberFormatException, ParseException {
    try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        CSVParser csvParser = new CSVParser(fileReader,
            CSVFormat.INFORMIX_UNLOAD.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

      List<TaxPaymentOrder> taxPaymentOrders = new ArrayList<TaxPaymentOrder>();

      Iterable<CSVRecord> csvRecords = csvParser.getRecords();

      for (CSVRecord csvRecord : csvRecords) {
    	  TaxPaymentOrder taxPaymentOrder = new TaxPaymentOrder(
              Long.parseLong(csvRecord.get("loanNumber")),
              csvRecord.get("customerName"),
              csvRecord.get("agency"),
              Double.parseDouble(csvRecord.get("amountToBePaid")),
              new SimpleDateFormat("MM-dd-yyyy").parse(csvRecord.get("dueDate"))
            );

    	  taxPaymentOrders.add(taxPaymentOrder);
      }

      return taxPaymentOrders;
    } catch (IOException e) {
      throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
    }
  }

}