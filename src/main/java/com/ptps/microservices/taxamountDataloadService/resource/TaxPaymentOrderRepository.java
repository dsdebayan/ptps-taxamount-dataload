package com.ptps.microservices.taxamountDataloadService.resource;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxPaymentOrderRepository extends JpaRepository<TaxPaymentOrder, Long> {
	//TaxPaymentOrder findByFromAndTo(String from, String to);
}
