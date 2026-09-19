package com.thestar.restaurant.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.thestar.restaurant.entity.RestaurantMenuVO;
import com.thestar.restaurant.service.RestaurantMenuService;
import com.thestar.restaurant.service.MenuCategoryService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/restaurant/menu")
public class RestaurantMenuController {

    @Autowired
    private RestaurantMenuService menuService;

    @Autowired
    private MenuCategoryService categoryService;

    // 菜單與餐點列表
    @GetMapping("/list")
    public String list(Model model) {
        List<RestaurantMenuVO> list = menuService.getAll();
        model.addAttribute("menuList", list);
        return "admin/restaurant/menu/list";
    }

    // 前往新增餐點
    @GetMapping("/addPage")
    public String addPage(Model model) {
        model.addAttribute("restaurantMenuVO", new RestaurantMenuVO());
        model.addAttribute("categoryList", categoryService.getAll()); // 下拉選單供選擇分類
        return "admin/restaurant/menu/add";
    }
    
    @GetMapping("/DBGifReader")
    public void dBGifReader(@RequestParam("itemId") Integer itemId, HttpServletResponse response) throws IOException {
        
        // 1. 設定瀏覽器不要快取圖片，確保每次都拿到最新資料
        response.setContentType("image/gif"); // 瀏覽器對大多數圖片格式有相容性，這裡用 image/gif 或 image/jpeg 皆可
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // 2. 從資料庫取得該筆餐點資料
        RestaurantMenuVO menuVO = menuService.getOneRestaurantMenu(itemId);
        
        if (menuVO != null && menuVO.getItemImage() != null) {
            // 3. 取得圖片的 byte[] 並寫入 Response
            byte[] imageBytes = menuVO.getItemImage();
            try (ServletOutputStream out = response.getOutputStream()) {
                out.write(imageBytes);
                out.flush();
            }
        } else {
            // 4. 如果資料庫沒有圖片，你可以選擇不輸出，或是輸出一個預設的「無圖片」佔位圖
            // 這裡直接回傳 404 或者不做任何寫出即可
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // 儲存餐點
 // 儲存餐點
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("restaurantMenuVO") RestaurantMenuVO menuVO, 
                       BindingResult result, 
                       @RequestParam("itemImageFile") MultipartFile file, 
                       Model model) {
        
        // 1. 檢查同一分類下的排序是否重疊（改由呼叫 Service 方法）
        if (menuVO.getMenuCategoryVO() != null && menuVO.getMenuCategoryVO().getCategoryId() != null && menuVO.getSortOrder() != null) {
            
            RestaurantMenuVO existingMenu = menuService.getByCategoryIdAndSortOrder(
                menuVO.getMenuCategoryVO().getCategoryId(), 
                menuVO.getSortOrder()
            );
            
            // 如果有找到相同排序的餐點
            if (existingMenu != null) {
                // 情境一：新增時發現有人佔用排序 (menuVO.getItemId() == null)
                // 情境二：修改時發現「別人」佔用了這個排序 (itemId 不同)
                if (menuVO.getItemId() == null || !existingMenu.getItemId().equals(menuVO.getItemId())) {
                    result.rejectValue("sortOrder", "error.sortOrder", "此分類下已存在相同的排序項目：" + existingMenu.getItemName() + " (排序:" + existingMenu.getSortOrder() + ")");
                }
            }
        }
        
        // 2. 統一由原本的 Validation 檢查（包含我們剛才加進去的 sortOrder 錯誤）
        if (result.hasErrors()) {
            model.addAttribute("categoryList", categoryService.getAll());
            if (menuVO.getItemId() != null) {
                RestaurantMenuVO originalMenu = menuService.getOneRestaurantMenu(menuVO.getItemId());
                if (originalMenu != null) {
                    menuVO.setItemImage(originalMenu.getItemImage());
                }
            }
            return menuVO.getItemId() == null ? "admin/restaurant/menu/add" : "admin/restaurant/menu/edit";
        }
        
        try {
            if (file != null && !file.isEmpty()) {
                menuVO.setItemImage(file.getBytes());
            } else {
                if (menuVO.getItemId() != null) {
                    RestaurantMenuVO originalMenu = menuService.getOneRestaurantMenu(menuVO.getItemId());
                    if (originalMenu != null) {
                        menuVO.setItemImage(originalMenu.getItemImage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "圖片處理失敗");
            model.addAttribute("categoryList", categoryService.getAll());
            return menuVO.getItemId() == null ? "admin/restaurant/menu/add" : "admin/restaurant/menu/edit";
        }
        
        menuService.addRestaurantMenu(menuVO);
        return "redirect:/admin/restaurant/menu/list";
    }

    // 前往修改餐點
    @GetMapping("/editPage")
    public String editPage(@RequestParam("itemId") Integer itemId, Model model) {
        RestaurantMenuVO menuVO = menuService.getOneRestaurantMenu(itemId);
        model.addAttribute("restaurantMenuVO", menuVO);
        model.addAttribute("categoryList", categoryService.getAll());
        return "admin/restaurant/menu/edit";
    }

    // 刪除餐點
    @PostMapping("/delete")
    public String delete(@RequestParam("itemId") Integer itemId) {
        menuService.deleteRestaurantMenu(itemId);
        return "redirect:/admin/restaurant/menu/list";
    }
}