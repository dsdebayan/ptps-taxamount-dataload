package com.ptps.microservices.taxamountDataloadService;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.storage.GoogleStorageResource;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ptps.microservices.taxamountDataloadService.resource.TaxPaymentOrder;

import com.ptps.microservices.taxamountDataloadService.resource.TaxPaymentOrderRepository;
import com.ptps.microservices.taxamountDataloadService.util.environment.CSVHelper;

@SpringBootApplication
public class TaxamountDataloadServiceApplication {


	private static final Log LOGGER = LogFactory.getLog(TaxamountDataloadServiceApplication.class);

	@Autowired
	private TaxPaymentOrderRepository repository;

	public static void main(String[] args) {
		SpringApplication.run(TaxamountDataloadServiceApplication.class, args);
	}


	@Bean 
	public PubSubInboundChannelAdapter messageChannelAdapter(@Qualifier("ptpsInputChannel") MessageChannel inputChannel,
			PubSubTemplate pubSubTemplate) {

		PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "ptpsSub");
		adapter.setOutputChannel(inputChannel);
		return adapter; 

	}

	@Bean public 
	MessageChannel myInputChannel() {
		return new DirectChannel(); }

	@ServiceActivator(inputChannel = "ptpsInputChannel")
	public void	messageReceiver(String payload) { 
		JsonObject jsonObject = new JsonParser().parse(payload).getAsJsonObject(); 
		String file = jsonObject.get("name").getAsString();
		Storage storage =
				StorageOptions.getDefaultInstance().getService();
		GoogleStorageResource
		gcsFile = new GoogleStorageResource(storage, "gs://ptps/" + file, false);
		String message;
		try { 
			try { List<TaxPaymentOrder> taxPaymentOrders = CSVHelper.csvToOrder(gcsFile.getInputStream());
			repository.saveAll(taxPaymentOrders); 
			} catch (IOException e) { 
				throw new
				RuntimeException("fail to store csv data: " + e.getMessage()); 
			} message =	"Uploaded the file successfully: " + gcsFile.getFilename();
			LOGGER.info(message); 
		} catch (Exception e) {
			message = "Could not upload the file: " + gcsFile.getFilename() + "!";
			LOGGER.error(message);
		} LOGGER.info("Message arrived! Payload: " + payload);
	}

}
