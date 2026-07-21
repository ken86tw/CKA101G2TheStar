package com.thestar.shop.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.thestar.shop.entity.ProductCategoryVO;
import com.thestar.shop.service.ProductCategoryService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/shop/category")
public class ProductCategoryController extends AdminShopBaseController{

    @Autowired
    ProductCategoryService productCategorySvc;

    // 顯示所有類別
    @GetMapping("listAllCategories")
    public String listAllCategories(ModelMap model) {
        List<ProductCategoryVO> list = productCategorySvc.getAll();
        model.addAttribute("categoryListData", list);
        return "admin/shop/category/listAllCategories";
    }

    // 顯示新增頁面
    @GetMapping("addCategory")
    public String addCategory(ModelMap model) {
        model.addAttribute("productCategoryVO", new ProductCategoryVO());
        return "admin/shop/category/addCategory";
    }

    // 執行新增
    @PostMapping("insert")
    public String insert(@Valid ProductCategoryVO productCategoryVO, BindingResult result, ModelMap model) {
        if (result.hasErrors()) {
            return "admin/shop/category/addCategory";
        }
        productCategorySvc.addProductCategory(productCategoryVO);
        return "redirect:/admin/shop/category/listAllCategories";
    }

    // 準備修改
    @PostMapping("getOne_For_Update")
    public String getOne_For_Update(@RequestParam("productCategoryId") Integer productCategoryId, ModelMap model) {
        ProductCategoryVO productCategoryVO = productCategorySvc.getOneProductCategory(productCategoryId);
        model.addAttribute("productCategoryVO", productCategoryVO);
        return "admin/shop/category/update_category_input";
    }

    // 執行修改
    @PostMapping("update")
    public String update(@Valid ProductCategoryVO productCategoryVO, BindingResult result, ModelMap model) {
        if (result.hasErrors()) {
            return "admin/shop/category/update_category_input";
        }
        productCategorySvc.updateProductCategory(productCategoryVO);
        return "redirect:/admin/shop/category/listAllCategories";
    }

    // 執行刪除
    @PostMapping("delete")
    public String delete(@RequestParam("productCategoryId") Integer productCategoryId,
                         RedirectAttributes ra) {                  // ← 加參數
        try {
            productCategorySvc.deleteProductCategory(productCategoryId);
            ra.addFlashAttribute("successMsg", "類別已刪除");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/shop/category/listAllCategories";
    }
}