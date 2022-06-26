package com.omnicell.example.axon.ProductsService.query;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import com.omnicell.example.axon.ProductsService.core.data.ProductEntity;
import com.omnicell.example.axon.ProductsService.core.data.ProductsRepository;
import com.omnicell.example.axon.ProductsService.core.events.ProductCreatedEvent;
import com.omnicell.example.axon.core.events.ProductReservationCancelledEvent;
import com.omnicell.example.axon.core.events.ProductReservedEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@ProcessingGroup("product-group")
@Slf4j
public class ProductEventsHandler {

	private final ProductsRepository productsRepository;

	public ProductEventsHandler(ProductsRepository productsRepository) {
		this.productsRepository = productsRepository;
	}
	
	@ExceptionHandler(resultType=Exception.class)
	public void handle(Exception exception) throws Exception {
		throw exception;
	}
	
	@ExceptionHandler(resultType=IllegalArgumentException.class)
	public void handle(IllegalArgumentException exception) {
		// Log error message
	}
	

	@EventHandler
	public void on(ProductCreatedEvent event) {

		ProductEntity productEntity = new ProductEntity();
		BeanUtils.copyProperties(event, productEntity);

		try {
			productsRepository.save(productEntity);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		}

	}
	
	@EventHandler
	public void on(ProductReservedEvent productReservedEvent) {
		ProductEntity productEntity = productsRepository.findByProductId(productReservedEvent.getProductId());
		
		log.debug("ProductReservedEvent: Current product quantity " + productEntity.getQuantity());
		
		productEntity.setQuantity(productEntity.getQuantity() - productReservedEvent.getQuantity());
		
		
		productsRepository.save(productEntity);
		
		log.debug("ProductReservedEvent: New product quantity " + productEntity.getQuantity());
 	
		log.info("ProductReservedEvent is called for productId:" + productReservedEvent.getProductId() +
				" and orderId: " + productReservedEvent.getOrderId());
	}
	
	@EventHandler
	public void on(ProductReservationCancelledEvent productReservationCancelledEvent) {
		ProductEntity currentlyStoredProduct =  productsRepository.findByProductId(productReservationCancelledEvent.getProductId());
	
		log.debug("ProductReservationCancelledEvent: Current product quantity " 
		+ currentlyStoredProduct.getQuantity() );
		
		int newQuantity = currentlyStoredProduct.getQuantity() + productReservationCancelledEvent.getQuantity();
		currentlyStoredProduct.setQuantity(newQuantity);
		
		productsRepository.save(currentlyStoredProduct);
		
		log.debug("ProductReservationCancelledEvent: New product quantity " 
		+ currentlyStoredProduct.getQuantity() );
	
	}
	
	@ResetHandler
	public void reset() {
		productsRepository.deleteAll();
	}

}
