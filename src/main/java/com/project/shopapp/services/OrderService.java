package com.project.shopapp.services;

import com.project.shopapp.dtos.CartItemDTO;
import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    @Transactional
    public Order createOrder(OrderDTO orderDTO) throws Exception {
        //tim user_id co ton tai khong
        User user= userRepository
                .findById(orderDTO.getUserId())
                .orElseThrow(()-> new DataNotFoundException("Cannot found user with id "+orderDTO.getUserId()));
        //convert OrderDTO => Order
        //dung thu vien Model Mapper
        //Tao 1 luong bang anh xa rieng biet de kiem soat viec anh xa
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper->mapper.skip(Order::setId));
        //cap nhat cac truong cua don hang tu orderDTO
        Order order=new Order();
        modelMapper.map(orderDTO,order);
        order.setUser(user);
        order.setOrderDate(LocalDate.now());//lay thoi diem hien tai
        order.setStatus(OrderStatus.PENDING);
        //kiem tra shipping date >= ngay hom nay
        LocalDate shippingDate=orderDTO.getShippingDate()==null ? LocalDate.now() : orderDTO.getShippingDate();
        if(shippingDate.isBefore(LocalDate.now())){
            throw new DataNotFoundException("Date must be at least today");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true);
        order.setTotalMoney(orderDTO.getTotalMoney());
        orderRepository.save(order);
        //Tao danh sach cac doi tuong OrderDetail tu CartItems
        List<OrderDetail> orderDetails=new ArrayList<>();
        for (CartItemDTO cartItemDTO : orderDTO.getCartItems()){
            //Tao 1 doi tuong OrderDetail tu CartItemDTO
            OrderDetail orderDetail=new OrderDetail();
            orderDetail.setOrder(order);
            //Lay thong tin san pham tu cartItemDTO
            Long productId= cartItemDTO.getProductId();
            int quantity= cartItemDTO.getQuantity();

            //tim thong tin san pham tu csdl(hoac su dung cache neu can) // co the kiem tra trong kho con hang k
            Product product = productRepository.findById(productId)
                    .orElseThrow(()->new DataNotFoundException("Product not found with id: "+ productId));
            //Dat thong tin cho OrderDetail
            orderDetail.setProduct(product);
            orderDetail.setNumberOfProduct(quantity);
            //Cac truong khac cua OrderDetail neu can
            orderDetail.setPrice(product.getPrice());
            orderDetail.setTotalMoney(product.getPrice()*quantity);
            //them OrderDetail vao danh sach
            orderDetails.add(orderDetail);
        }
        //luu danh sach OrderDetail vao csdl
        orderDetailRepository.saveAll(orderDetails);
        return order;
    }

    @Override
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException {
        Order order=orderRepository
                .findById(id)
                .orElseThrow(()->new DataNotFoundException("Cannot find order with id: "+id));
        User existingUser=userRepository
                .findById(orderDTO.getUserId())
                .orElseThrow(()->new DataNotFoundException("Cannot find user with id: "+id));
        //Tao 1 luong bang anh xa rieng biet de kiem soat viec anh xa
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper->mapper.skip(Order::setId));
        //cap nhat cac truong cua don hang tu orderDTO
        modelMapper.map(orderDTO,order);
        order.setUser(existingUser);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        //xóa mềm
        Order order=orderRepository.findById(id).orElse(null);
        if(order != null){
            order.setActive(false);
            orderRepository.save(order);
        }
    }

    @Override
    public List<Order> findByUserId(Long userid) {
        return orderRepository.findByUserId(userid);
    }

    @Override
    public Page<Order> getOrdersByKeyword(String keyword, Pageable pageable){
        return orderRepository.findByKeyword(keyword,pageable);
    }

    @Override
    public void updateOrderSatus(Long orderId, String newStatus) {
        Order order=orderRepository.findById(orderId).
                orElseThrow(()-> new DataNotFoundException("Đơn hàng không tồn tại."));
        order.setStatus(newStatus);
        orderRepository.save(order);
    }
}
