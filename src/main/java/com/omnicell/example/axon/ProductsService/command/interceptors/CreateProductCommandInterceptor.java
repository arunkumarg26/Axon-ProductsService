package com.omnicell.example.axon.ProductsService.command.interceptors;

import java.util.List;
import java.util.function.BiFunction;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.omnicell.example.axon.ProductsService.command.CreateProductCommand;
import com.omnicell.example.axon.ProductsService.core.data.ProductLookupEntity;
import com.omnicell.example.axon.ProductsService.core.data.ProductLookupRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateProductCommandInterceptor.class);
	private final ProductLookupRepository productLookupRepository;
	
	public CreateProductCommandInterceptor(ProductLookupRepository productLookupRepository) {
		this.productLookupRepository = productLookupRepository;
	}
 
	@Override
	public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(
			List<? extends CommandMessage<?>> messages) {
		 
		return (index, command) -> {
			
			log.info("Intercepted command: " + command.getPayloadType());
			
			if(CreateProductCommand.class.equals(command.getPayloadType())) {
				
				CreateProductCommand createProductCommand = (CreateProductCommand)command.getPayload();
				
				ProductLookupEntity productLookupEntity =  productLookupRepository.findByProductIdOrTitle(createProductCommand.getProductId(),
						createProductCommand.getTitle());
				
				if(productLookupEntity != null) {
					throw new IllegalStateException(
							String.format("Product with productId %s or title %s already exist", 
									createProductCommand.getProductId(), createProductCommand.getTitle())
							);
				}

			}
			
			return command;
		};
	}

}
