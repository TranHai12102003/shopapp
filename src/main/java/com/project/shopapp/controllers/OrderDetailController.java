package com.project.shopapp.controllers;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.responses.OrderDetailResponse;
import com.project.shopapp.services.OrderDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailController {
    private final OrderDetailService orderDetailService;
    //them moi 1 order_detail
    @PostMapping("")
    public ResponseEntity<?> createOrderDetail(@Valid @RequestBody OrderDetailDTO orderDetailDTO){
        try {
            OrderDetail newOrderDetail=orderDetailService.createOrderDetail(orderDetailDTO);
            //Trả về OrderDetail theo cách mình muốn trong OrderDetaiResponse
            return ResponseEntity.ok().body(OrderDetailResponse.fromOderDetail(newOrderDetail));
//            return ResponseEntity.ok().body(newOrderDetail); trả về chi tiết orderDetail
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(@Valid @PathVariable("id") Long id) throws DataNotFoundException {
        OrderDetail orderDetail= orderDetailService.getOrderDetail(id);
        //Trả về OrderDetail theo cách mình muốn trong OrderDetaiResponse
        return ResponseEntity.ok().body(OrderDetailResponse.fromOderDetail(orderDetail));
//        return ResponseEntity.ok(orderDetail);
    }

    //lay ra danh sach cac orderdetail tu 1 cai order nao do
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(@Valid @PathVariable("orderId") Long orderId){
       List<OrderDetail> orderDetails= orderDetailService.findByOrderId(orderId);
       List<OrderDetailResponse>orderDetailResponses=orderDetails
               .stream()
               .map(OrderDetailResponse::fromOderDetail)
               .toList();
        return ResponseEntity.ok(orderDetailResponses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail
            (@Valid @PathVariable("id") Long id,
             @RequestBody OrderDetailDTO orderDetailDTO)
    {
        try {
           OrderDetail orderDetail= orderDetailService.updateOrderDetail(id,orderDetailDTO);
            return ResponseEntity.ok().body(orderDetail);
        } catch (DataNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrderDeatail(@Valid @PathVariable("id") Long id){
        orderDetailService.deleteById(id);
        return ResponseEntity.ok("OrderDetail deleted successfully");
    }
}
