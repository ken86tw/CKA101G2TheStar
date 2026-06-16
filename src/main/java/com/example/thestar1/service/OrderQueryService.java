package com.example.thestar1.service;


import com.example.thestar1.dto.OrderDetailDTO;
import com.example.thestar1.entity.OrderListVO;
import com.example.thestar1.entity.OrderVO;
import com.example.thestar1.entity.RoomTypeVO;
import com.example.thestar1.repository.OrderRepository;
import com.example.thestar1.repository.RoomRepository;
import com.example.thestar1.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Autowired
    public OrderQueryService(OrderRepository orderRepository, RoomTypeRepository roomTypeRepository) {
        this.orderRepository = orderRepository;
        this.roomTypeRepository = roomTypeRepository;
    }

    //會員查詢依訂單狀態分類
    public Page<OrderVO> findMemberOrder(Integer memberId, Byte orderStaus, int page, int size) {

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "createdTime")
        );

        return orderRepository.findByMemberIdAndOrderStatus(memberId, orderStaus, pageable);

    }


    //查詢訂單明細
    @Transactional(readOnly = true)
    public List<OrderDetailDTO> findOrderDetail(Integer orderId) {

        List<OrderListVO> orderList = orderRepository.findById(orderId).orElseThrow().getOrderList();
        List<OrderDetailDTO> dtoList = new ArrayList<>();

        for (OrderListVO list : orderList) {
            OrderDetailDTO dto = new OrderDetailDTO();
            Integer roomTypeId = list.getRoomTypeId();
            RoomTypeVO room = roomTypeRepository.findById(roomTypeId).orElseThrow();
            String roomName = room.getRoomTypeName();
            dto.setRoomTypeName(roomName);
            dto.setQTY(list.getQuantity());
            dto.setSubtotal(list.getSubtotal());
            dto.setRoomPrice(list.getRoomPrice());

            dtoList.add(dto);
        }
        return dtoList;
    }

    //會員查詢自己的訂單
    @Transactional(readOnly = true)
    public List<OrderDetailDTO> getMemberOrderDetail(Integer memberId, Integer orderId) {

        if (!orderRepository.existsByMemberIdAndOrderId(memberId, orderId)) {
            throw new IllegalArgumentException("非本人訂單");
        }

        return findOrderDetail(orderId);
    }

    //後台查詢訂單用
    public Page<OrderVO> findAllOrders(Byte orderStatus, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        return orderRepository.findByOrderStatus(orderStatus, pageable);
    }

}
