package com.thestar.room.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thestar.room.dto.RoomTypePhotoDTO;
import com.thestar.room.entity.RoomTypePhotoVO;
import com.thestar.room.entity.RoomTypeVO;
import com.thestar.room.service.RoomTypePhotoService;
import com.thestar.room.service.RoomTypeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/roomtype") // 設定網址路徑
public class RoomTypeController {

	@Autowired
	private RoomTypeService service;

	@Autowired
	private RoomTypePhotoService photoService;

	// 所有房型總覽
	@GetMapping("/manage")
	public String showRoomTypesPage(Model model) {
		// 取得所有房型資料
		List<RoomTypeVO> list = service.getAllRoomTypes();
		// 計算總房型類別數
		int totalCount = list.size();
		// 計算所有房型的總庫存數量 (將每個房型的 roomTypeAmount 相加)
		int totalAmount = list.stream()
				.mapToInt(room -> room.getRoomTypeAmount() != null ? room.getRoomTypeAmount() : 0).sum();

		// roomTypeManage管理頁面使用，顯示庫存剩餘數量
		int remaining = 50 - totalAmount;

		// 將資料放入 Model 中
		model.addAttribute("rooms", list);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("totalAmount", totalAmount);
		model.addAttribute("remaining", remaining);
		return "admin/room/roomTypeManage";
	}

	// 檢視房型詳細資料
	@GetMapping("/details/{id}")
	public String getRoomTypeDetails(@PathVariable("id") Integer id, Model model) {
		// 1. 取得房型基本資料
		RoomTypeVO roomTypeVO = service.getOneRoomType(id);
		model.addAttribute("roomTypeVO", roomTypeVO);

		// 2. 【關鍵修正】取得該房型所有的照片列表，並傳入 model
		// 這樣 HTML 頁面才能透過 th:each="photo : ${photoList}" 讀到資料
		List<RoomTypePhotoVO> photos = photoService.getPhotosByRoomTypeId(id);
		model.addAttribute("photoList", photos);

		return "admin/room/roomTypeDetails"; // 對應到你的 HTML 檔名
	}

	// 進入新增頁面
	@GetMapping("/add")
	public String addRoomTypePage(Model model) {
		model.addAttribute("roomTypeVO", new RoomTypeVO());

		// 計算剩餘：總容量 50 - 目前已使用量
		int totalUsed = service.getSumOfAmounts();
		int remaining = 50 - totalUsed;
		model.addAttribute("remaining", remaining);

		return "admin/room/roomTypeForm";
	}

	// 執行新增
	@PostMapping("/insert")
	public String insert(@Valid RoomTypeVO roomTypeVO, BindingResult result,
			@RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles, Model model,
			RedirectAttributes redirectAttributes) { // 記得加入 Model

		if (result.hasErrors()) {
			model.addAttribute("remaining", 50 - service.getSumOfAmounts());
			return "admin/room/roomTypeForm";
		}

		try {
			service.addRoomType(roomTypeVO);

			// 使用迴圈處理多張圖片
			if (imageFiles != null) {
				for (MultipartFile file : imageFiles) {
					if (file != null && !file.isEmpty()) {
						RoomTypePhotoDTO photoDTO = new RoomTypePhotoDTO();
						photoDTO.setRoomTypeId(roomTypeVO.getRoomTypeId());
						photoDTO.setRoomTypePic(file);
						photoService.addRoomTypePhoto(photoDTO);
					}
				}
			}
			redirectAttributes.addFlashAttribute("successMessage", "房型新增成功！");
		} catch (ResponseStatusException e) {
			model.addAttribute("errorMessage", e.getReason());
			model.addAttribute("remaining", 50 - service.getSumOfAmounts());
			return "admin/room/roomTypeForm";
		}

		return "redirect:/roomtype/manage";
	}

	// 進入修改頁面 (帶入特定 ID)
	@GetMapping("/edit/{id}")
	public String editRoomTypePage(@PathVariable("id") Integer id, Model model) {
		RoomTypeVO roomTypeVO = service.getOneRoomType(id);
		model.addAttribute("roomTypeVO", roomTypeVO);

		// 【關鍵補強】：載入該房型所有的照片列表
		List<RoomTypePhotoVO> photos = photoService.getPhotosByRoomTypeId(id);
		model.addAttribute("photoList", photos);

		int totalUsed = service.getSumOfAmounts();
		int remaining = 50 - (totalUsed - roomTypeVO.getRoomTypeAmount());
		model.addAttribute("remaining", remaining);

		return "admin/room/roomTypeForm";
	}

	// 執行修改
	@PostMapping("/update")
	public String update(@Valid RoomTypeVO roomTypeVO, BindingResult result,
			@RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles, Model model,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			model.addAttribute("photoList", photoService.getPhotosByRoomTypeId(roomTypeVO.getRoomTypeId()));
			return "admin/room/roomTypeForm";
		}

		try {
			service.updateRoomType(roomTypeVO);

			// 【關鍵補強】：處理多張圖片上傳
			if (imageFiles != null) {
				for (MultipartFile file : imageFiles) {
					if (file != null && !file.isEmpty()) {
						RoomTypePhotoDTO photoDTO = new RoomTypePhotoDTO();
						photoDTO.setRoomTypeId(roomTypeVO.getRoomTypeId());
						photoDTO.setRoomTypePic(file);
						photoService.addRoomTypePhoto(photoDTO); // 新增圖片到資料庫
					}
				}
			}
			redirectAttributes.addFlashAttribute("successMessage", "房型更新成功！");
		} catch (ResponseStatusException e) {
			model.addAttribute("errorMessage", e.getReason());
			model.addAttribute("photoList", photoService.getPhotosByRoomTypeId(roomTypeVO.getRoomTypeId()));
			return "admin/room/roomTypeForm";
		}

		return "redirect:/roomtype/edit/" + roomTypeVO.getRoomTypeId();
	}

	// 執行刪除指定房型
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable("id") Integer id) {
		// 刪除房型 (根據你的 Service 的方法)
		service.deleteRoomType(id);

		// 刪除後跳轉回房型管理列表頁面
		return "redirect:/roomtype/manage";
	}
}
