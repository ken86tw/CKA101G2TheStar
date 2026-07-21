package com.thestar.shop.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thestar.shop.entity.ProductsVO;
import com.thestar.shop.service.ProductOrderItemService;
import com.thestar.shop.service.ProductsService;

@Controller
@RequestMapping("/admin/shop/stock/listStock")
public class AdminShopStockController extends AdminShopBaseController {

    @Autowired
    ProductsService productsSvc;

    @Autowired
    ProductOrderItemService productOrderItemSvc;

    @GetMapping
    public String stockList(ModelMap model) {
        List<ProductsVO> allProducts = productsSvc.getAll();

        // 計算每個商品已賣出數量
        Map<Integer, Integer> soldMap = new HashMap<>();
        for (ProductsVO product : allProducts) {
        	int sold = productOrderItemSvc.getByProductId(product.getProductId())
        	        .stream()
        	        .filter(i -> i.getShopOrder() != null
        	                  && i.getShopOrder().getShopOrderStatus() != null
        	                  && i.getShopOrder().getShopOrderStatus() != 3)   // 排除已取消
        	        .mapToInt(item -> item.getProdOrderItemQty())
        	        .sum();
            soldMap.put(product.getProductId(), sold);
        }

        model.addAttribute("productsListData", allProducts);
        model.addAttribute("soldMap", soldMap);
        return "admin/shop/stock/listStock";
    }
    
    @PostMapping("updateStock")
    public String updateStock(
            @RequestParam("productId") Integer productId,
            @RequestParam("productQuantity") Integer productQuantity) {
        ProductsVO product = productsSvc.getOneProduct(productId);
        if (product != null) {
            product.setProductQuantity(productQuantity);
            productsSvc.updateProduct(product);
        }
        return "redirect:/admin/shop/stock/listStock";
    }
}