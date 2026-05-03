package com.example.microservices.order.service;

import com.example.microservices.order.dto.OrderRequest;
import com.example.microservices.order.dto.OrderResponse;
import com.example.microservices.order.entity.CustomerOrder;
import com.example.microservices.order.exception.ResourceNotFoundException;
import com.example.microservices.order.repository.CustomerOrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerOrderService {

    private final CustomerOrderRepository repository;
    private final UserLookupService userLookupService;

    public CustomerOrderService(CustomerOrderRepository repository, UserLookupService userLookupService) {
        this.repository = repository;
        this.userLookupService = userLookupService;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByUserId(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse create(OrderRequest request) {
        userLookupService.findWithFeign(request.userId());
        CustomerOrder order = new CustomerOrder();
        apply(request, order);
        return toResponse(repository.save(order));
    }

    public OrderResponse update(Long id, OrderRequest request) {
        CustomerOrder order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
        userLookupService.findWithRestTemplate(request.userId());
        apply(request, order);
        return toResponse(repository.save(order));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with id " + id);
        }
        repository.deleteById(id);
    }

    private void apply(OrderRequest request, CustomerOrder order) {
        order.setUserId(request.userId());
        order.setProductName(request.productName());
        order.setQuantity(request.quantity());
        order.setPrice(request.price());
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice()
        );
    }
}
