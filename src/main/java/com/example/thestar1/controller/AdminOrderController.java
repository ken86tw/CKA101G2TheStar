package com.example.thestar1.controller;

import com.example.thestar1.dto.OrderDetailDTO;
import com.example.thestar1.entity.OrderVO;
import com.example.thestar1.service.OrderQueryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/thestar/admin/order")
public class AdminOrderController {

    private final OrderQueryService orderQueryService;

    public AdminOrderController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    // 後台訂單查詢
    @GetMapping
    public ResponseEntity<Page<OrderVO>> allOrders(@RequestParam Byte orderStatus,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   HttpSession session) {

        Integer employeeId = (Integer) session.getAttribute("loginEmployee");

        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(orderQueryService.findAllOrders(orderStatus, page, size));
    }


    // 後台訂單明細查詢
    @GetMapping("/detail/{orderId}")
    public ResponseEntity<List<OrderDetailDTO>> orderDetail(@PathVariable Integer orderId,
                                                            HttpSession session) {
        Integer employeeId = (Integer) session.getAttribute("loginEmployee");
        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(orderQueryService.findOrderDetail(orderId));
    }


}