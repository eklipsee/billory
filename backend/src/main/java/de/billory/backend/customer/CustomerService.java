package de.billory.backend.customer;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer createCustomer(CreateCustomerRequest request) {
    Customer customer = new Customer();

    customer.setName(request.getName());
    customer.setStreet(request.getStreet());
    customer.setZip(request.getZip());
    customer.setCity(request.getCity());
    customer.setEmail(request.getEmail());
    customer.setPhone(request.getPhone());
    customer.setNotes(request.getNotes());

    String now = LocalDateTime.now().toString();
    customer.setCreatedAt(now);
    customer.setUpdatedAt(now);

    return customerRepository.save(customer);
}

    public Customer getCustomerById(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public void deleteCustomer(Integer id) {
        customerRepository.deleteById(id);
    }
}