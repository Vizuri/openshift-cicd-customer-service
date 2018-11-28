package com.vizuri.customer.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vizuri.customer.entity.Customer;
import com.vizuri.customer.repository.CustomerRepository;

@RestController
@RequestMapping("/customer")
public class CustomerController {
	private final CustomerRepository repository;
    private static Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    public CustomerController(CustomerRepository repository) {
            this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public Iterable<?> findAll() {
            logger.info(">>>>>> Find All Customers");
            return repository.findAll();
    }
    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value="/{id}")
    public Customer findById(@PathVariable("id") String id) {
            logger.info(">>>>>> Find Customer:"  + id);
            return repository.findById(id).get();
    }
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public Customer create(@RequestBody Customer customer) {
            logger.info(">>>>> Creating Customer:" + customer);
            return repository.save((Customer)customer);
    }
    @RequestMapping(method = RequestMethod.PUT, value="/{id}")
      public Customer update(@PathVariable("id") String id, @RequestBody Customer customer) {
        Customer update = repository.findById(id).get();

        update.setFirstName(customer.getFirstName());
        update.setLastName(customer.getLastName());
        logger.info(">>>>> Updating Customer:" + update);
        return repository.save((Customer) update);
    }
    @RequestMapping(method = RequestMethod.DELETE, value="/{id}")
    public void delete(@PathVariable("id") String id) {
            logger.info(">>>>> Deleting Customer:" + id);
            Customer customer = repository.findById(id).get();
            repository.delete(customer);
    }

}
