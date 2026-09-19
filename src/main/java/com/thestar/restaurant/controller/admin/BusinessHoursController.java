package com.thestar.restaurant.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thestar.restaurant.entity.BusinessHoursVO;
import com.thestar.restaurant.service.BusinessHoursService;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin/restaurant/businesshours")
public class BusinessHoursController {

    @Autowired
    private BusinessHoursService bhService;

    // 時段列表
    @GetMapping("/list")
    public String list(Model model) {
        List<BusinessHoursVO> list = bhService.getAll();
        model.addAttribute("bhList", list);
        return "admin/restaurant/businesshours/list";
    }

    // 前往新增
    @GetMapping("/addPage")
    public String addPage(Model model) {
        BusinessHoursVO bhVO = new BusinessHoursVO();
        bhVO.setIsAvailable(true); // 核心關鍵：在這裡就先餵好預設值為上架（true）！
        
        model.addAttribute("businessHoursVO", bhVO);
        return "admin/restaurant/businesshours/add";
    }

    // 儲存時段
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("businessHoursVO") BusinessHoursVO bhVO, 
                       BindingResult result, 
                       RedirectAttributes redirectAttributes) {
        
        // 1. 基本 JSR303 欄位驗證（如：是否為空值）
        if (result.hasErrors()) {
            return "admin/restaurant/businesshours/add";
        }
        
        // 2. 商業邏輯檢查：防呆，避免重複增設一模一樣的時間區間
        // 註：這需要你的 bhService 裡有提供根據時間查詢或檢查的方法，可依需求決定是否實作
        boolean isDuplicate = bhService.isTimeSlotDuplicate(bhVO.getStartTime(), bhVO.getEndTime());
        if (isDuplicate) {
            result.rejectValue("startTime", "duplicate", "該營業時段已存在，請勿重複新增！");
            return "admin/restaurant/businesshours/add";
        }

        // 3. 核心修改：全新時段初始狀態一律預設為「上架中 (TRUE)」
        bhVO.setIsAvailable(true);
        
        // 4. 寫入資料庫
        bhService.addBusinessHours(bhVO);
        
        // 5. 帶回成功提示訊息到 list 頁面（搭配我們之前在 list.html 寫好的 successMsg 區塊）
        redirectAttributes.addFlashAttribute("successMsg", "全新營業時段已成功新增並自動上架！");
        
        return "redirect:/admin/restaurant/businesshours/list";
    }

    // === 新增：上架時段功能 ===
    @PostMapping("/onShelf")
    public String onShelf(@RequestParam("sessionId") Integer sessionId) {
        bhService.updateShelfStatus(sessionId, true);
        return "redirect:/admin/restaurant/businesshours/list";
    }

    // === 新增：下架時段功能 ===
    @PostMapping("/offShelf")
    public String offShelf(@RequestParam("sessionId") Integer sessionId) {
        bhService.updateShelfStatus(sessionId, false);
        return "redirect:/admin/restaurant/businesshours/list";
    }

    // 刪除時段
    @PostMapping("/delete")
    public String delete(@RequestParam("sessionId") Integer sessionId, RedirectAttributes redirectAttributes) {
        try {
            bhService.deleteBusinessHours(sessionId);
            // 成功訊息（選填，可加可不加）
            redirectAttributes.addFlashAttribute("successMsg", "營業時段已成功永久刪除！");
        } catch (DataIntegrityViolationException e) {
            // 核心邏輯：一旦引發外鍵約束異常（代表有訂位紀錄或每日控管表關聯），抓到後拋回錯誤訊息
            redirectAttributes.addFlashAttribute("errorMsg", "無法刪除：該時段已被客戶訂位或已有每日桌數控管紀錄，請改用「下架」功能！");
        }
        return "redirect:/admin/restaurant/businesshours/list";
    }
}