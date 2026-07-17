package com.thestar.room.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thestar.room.entity.RoomTypePhotoVO;
import com.thestar.room.service.RoomTypePhotoService;
import com.thestar.room.service.RoomTypeService;

@Controller
@RequestMapping("/roomtypephoto")
public class RoomTypePhotoController {

	@Autowired
	private RoomTypePhotoService service;

	@Autowired
	private RoomTypeService roomTypeService;

	@Autowired
	private ResourceLoader resourceLoader;
	
	// 圖片顯示總覽用
	@GetMapping("/display/room/{roomTypeId}")
	public ResponseEntity<byte[]> displayRoomTypeFirstPhoto(@PathVariable("roomTypeId") Integer roomTypeId) {
        List<RoomTypePhotoVO> photos = service.getPhotosByRoomTypeId(roomTypeId);
        if (photos != null && !photos.isEmpty() && photos.get(0).getRoomTypePic() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(photos.get(0).getRoomTypePic());
        }
        return getNoImage();
	}
	
	// 給編輯頁面預覽使用 (對應 HTML 中的 /display/photo/ 路徑)
    @GetMapping("/display/photo/{photoId}")
    public ResponseEntity<byte[]> displaySpecificPhoto(@PathVariable("photoId") Integer photoId) {
        RoomTypePhotoVO photo = service.getPhotoById(photoId); // 假設 Service 有此方法
        if (photo != null && photo.getRoomTypePic() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(photo.getRoomTypePic());
        }
        return getNoImage();
    }

    
	

	// 共用方法：處理沒圖的情況
    private ResponseEntity<byte[]> getNoImage() {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/images/noimage.jpg");
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource.getInputStream().readAllBytes());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
	}

	// 刪除圖片
	@PostMapping("/delete/{id}")
	// 接收圖片ID與房型ID，刪除指定圖片後重導向回該房型的圖片管理頁面
	public String delete(@PathVariable("id") Integer id, @RequestParam("roomTypeId") Integer roomTypeId) {
        service.deletePhotoById(id);
        return "redirect:/roomtype/edit/" + roomTypeId;
	}

}
