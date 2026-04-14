package de.billory.backend.customer;

import org.springframework.stereotype.Service;
import de.billory.backend.common.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponse> getAllCustomers(String search) {
        List<Customer> customers;

        if (search == null || search.isBlank()) {
            customers = customerRepository.findAll();
        } else {
            customers = customerRepository.findByNameContainingIgnoreCase(search);
        }

        return customers.stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request){
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

    return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse getCustomerById(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        return toResponse(customer);
    }

    public void deleteCustomer(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        customerRepository.delete(customer);
    }

    public CustomerResponse updateCustomer(Integer id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        customer.setName(request.getName());
        customer.setStreet(request.getStreet());
        customer.setZip(request.getZip());
        customer.setCity(request.getCity());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setNotes(request.getNotes());
        customer.setUpdatedAt(LocalDateTime.now().toString());

        return toResponse(customerRepository.save(customer));
    }

    private CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();

        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setStreet(customer.getStreet());
        response.setZip(customer.getZip());
        response.setCity(customer.getCity());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setNotes(customer.getNotes());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());

        return response;
    }
}