package com.ptps.microservices.taxamountDataloadService.resource;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.storage.GoogleStorageResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.ptps.microservices.taxamountDataloadService.util.environment.CSVHelper;
import com.ptps.microservices.taxamountDataloadService.util.environment.ResponseMessage;

@RestController
public class TxamountDataloadController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TxamountDataloadController.class);

	@Autowired
	private TaxPaymentOrderRepository repository;


	@GetMapping("/")
	public String imHealthy() {
		return "{healthy:true}";
	}

	@GetMapping("/gcs/{file}")
	public ResponseEntity<ResponseMessage> readGcsFile(@PathVariable String file) throws IOException {
		Storage storage = StorageOptions.getDefaultInstance().getService();
		GoogleStorageResource gcsFile = new GoogleStorageResource(storage, "gs://ptps/" + file, false);
		String message;

		try {
			try {
				List<TaxPaymentOrder> taxPaymentOrders = CSVHelper.csvToOrder(gcsFile.getInputStream());
				repository.saveAll(taxPaymentOrders);
			} catch (IOException e) {
				throw new RuntimeException("fail to store csv data: " + e.getMessage());
			}
			message = "Uploaded the file successfully: " + gcsFile.getFilename();
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
		} catch (Exception e) {
			message = "Could not upload the file: " + gcsFile.getFilename() + "!";
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
		}
	}


	@GetMapping("/taxamount-dataload/all")
	public List<TaxPaymentOrder> retrieveAllOrders() {

		List<TaxPaymentOrder> taxPaymentOrderList = repository.findAll();

		LOGGER.info("{}", taxPaymentOrderList);
		return taxPaymentOrderList;
	}

	@PostMapping("/taxamount-dataload/upload")
	public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
		String message = "";

		if (CSVHelper.hasCSVFormat(file)) {
			try {
				try {
					List<TaxPaymentOrder> taxPaymentOrders = CSVHelper.csvToOrder(file.getInputStream());
					repository.saveAll(taxPaymentOrders);
				} catch (IOException e) {
					throw new RuntimeException("fail to store csv data: " + e.getMessage());
				}
				message = "Uploaded the file successfully: " + file.getOriginalFilename();
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
			} catch (Exception e) {
				message = "Could not upload the file: " + file.getOriginalFilename() + "!";
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
			}
		}

		message = "Please upload a csv file!";
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
	}

}
