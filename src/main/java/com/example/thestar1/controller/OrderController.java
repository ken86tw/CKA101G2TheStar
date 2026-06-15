package com.example.thestar1.controller;


import com.example.thestar1.dto.CreateRoomOrderDTO;
import com.example.thestar1.entity.OrderVO;
import com.example.thestar1.service.OrderQueryService;
import com.example.thestar1.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/thestar/order")
public class OrderController {

    private OrderService orderService;
    private OrderQueryService orderQueryService;

    public OrderController(OrderService orderService, OrderQueryService orderQueryService) {
        this.orderService = orderService;
        this.orderQueryService = orderQueryService;
    }

    @PostMapping
    public ResponseEntity<OrderVO> createOrder(@RequestParam Integer memberId,
                                               @RequestBody CreateRoomOrderDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).
                body(orderService.createOrder(memberId,dto));
    }


}
