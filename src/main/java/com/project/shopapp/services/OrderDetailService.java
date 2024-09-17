package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.models.Product;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class OrderDetailService implements IOrderDetailService{
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        //tim xem orderId co ton tai khong
        Order order=orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(()->new DataNotFoundException
                        ("Cannot find Order with id : "+orderDetailDTO.getOrderId()));
        //tim Product theo id
        Product product=productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(()->new DataNotFoundException
                        ("Cannot find Product with id : "+ orderDetailDTO.getProductId()));
        OrderDetail orderDetail=OrderDetail.builder()
                .order(order)
                .product(product)
                .price(orderDetailDTO.getPrice())
                .numberOfProduct(orderDetailDTO.getNumberOfProducts())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .color(orderDetailDTO.getColor())
                .build();
        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public OrderDetail getOrderDetail(Long id) throws DataNotFoundException {
        return orderDetailRepository.findById(id).orElseThrow(()->new DataNotFoundException("Cannot find OrderDetail with id : "+id));
    }

    @Override
    @Transactional
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        OrderDetail existingOrderDetail=orderDetailRepository
                .findById(id)
                .orElseThrow(()->new DataNotFoundException("Cannot find OrderDetail with id: "+id));
        Order existingOrder = orderRepository
                .findById(orderDetailDTO.getOrderId())
                .orElseThrow(()->new DataNotFoundException("Cannot find Order with id: "+id));
        Product existingProduct = productRepository
                .findById(orderDetailDTO.getProductId())
                .orElseThrow(()->new DataNotFoundException("Cannot find Product with id: "+id));
        existingOrderDetail.setProduct(existingProduct);
        existingOrderDetail.setOrder(existingOrder);
        existingOrderDetail.setPrice(orderDetailDTO.getPrice());
        existingOrderDetail.setColor(orderDetailDTO.getColor());
        existingOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
        existingOrderDetail.setNumberOfProduct(orderDetailDTO.getNumberOfProducts());
        return orderDetailRepository.save(existingOrderDetail);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        orderDetailRepository.deleteById(id);
    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
}
