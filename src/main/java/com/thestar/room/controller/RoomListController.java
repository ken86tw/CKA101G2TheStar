package com.thestar.room.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.thestar.room.entity.RoomTypePhotoVO;
import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.service.RoomService;
import com.thestar.room.service.RoomTypePhotoService;
import com.thestar.room.service.RoomTypeService;

@Controller
@RequestMapping("/roomList") // 統一網址前綴
public class RoomListController {
	
	@Autowired
	private RoomService roomService;
	
	@Autowired
	private RoomTypeService roomTypeService;

	@Autowired
	private RoomTypePhotoService photoService;


	    // 負責顯示列表頁 (roomList.html)
	    @GetMapping("/list")
	    public String showList(Model model) {
	        // 1. 取得所有房型清單
	        List<RoomTypeVO> list = roomTypeService.getAllRoomTypes();
	        
	        // 2. 透過迴圈，將每個房型的第一張圖塞進 VO 物件裡
	        if (list != null) {
	            for (RoomTypeVO roomType : list) {
	                // 呼叫你的照片 Service 取得第一張圖
	                RoomTypePhotoVO photo = photoService.findFirstByRoomTypeId(roomType.getRoomTypeId());
	                
	                // 將圖片物件設定進去 (因為有 @Transient，這不會影響資料庫，只存在於當下這個 List 中)
	                roomType.setFirstPhoto(photo);
	            }
	        }
	        
	        // 3. 傳遞已經帶有圖片資訊的清單給前端
	        model.addAttribute("activeRoomList", list);
	        
	        return "user/room/roomList";
	    }
	    
	    @GetMapping("/api/photos/{roomId}")
	    @ResponseBody // 將結果轉為 JSON 並返回，而不是找頁面
	    public List<RoomTypePhotoVO> getPhotos(@PathVariable Integer roomId) {
	        // 假設你有一個 service 可以根據房型 ID 取得所有照片
	        return photoService.getPhotosByRoomTypeId(roomId); 
	    }
	    
}
	

