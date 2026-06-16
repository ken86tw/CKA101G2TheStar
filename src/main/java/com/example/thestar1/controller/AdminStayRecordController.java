package com.example.thestar1.controller;


import com.example.thestar1.dto.CheckInDTO;
import com.example.thestar1.entity.RoomVO;
import com.example.thestar1.entity.StayRecordVO;
import com.example.thestar1.service.StayRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/thestar/stayrecord")
public class AdminStayRecordController {

    private final StayRecordService stayRecordService;

    public AdminStayRecordController(StayRecordService stayRecordService) {
        this.stayRecordService = stayRecordService;
    }

    @GetMapping("/rooms/{roomTypeId}")
    public List<RoomVO> availableRoom(@PathVariable Integer roomTypeId) {
        return stayRecordService.findAvailableRoom(roomTypeId);
    }

    @PostMapping("/checkin")
    public ResponseEntity<String> checkIn(@RequestBody CheckInDTO dto, HttpSession session) {
        Integer employeeId = (Integer) session.getAttribute("loginEmployee");
        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        stayRecordService.checkIn(employeeId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("check in OK");
    }

    @PostMapping("/checkout/{roomId}")
    public ResponseEntity<String> checkOut(@PathVariable Integer roomId, HttpSession session) {

        Integer employeeId = (Integer) session.getAttribute("loginEmployee");

        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        stayRecordService.checkOut(roomId, employeeId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("房號" + roomId + "退房成功");
    }

    @GetMapping("/find")
    public ResponseEntity<List> searchStayRecord(@RequestParam(required = false) Integer roomId,
                                                 @RequestParam(required = false) String stayCustomer,
                                                 @RequestParam(required = false) LocalDate checkInTime,
                                                 @RequestParam(required = false) LocalDate checkOutTime,
                                                 HttpSession session) {
        Integer empolyeeId = (Integer) session.getAttribute("loginEmployee");

        if (empolyeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<StayRecordVO> list = stayRecordService.findStayRecord(roomId, stayCustomer, checkInTime, checkOutTime);
        return ResponseEntity.ok(list);
    }
}
