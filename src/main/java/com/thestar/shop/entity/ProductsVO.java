package com.thestar.shop.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "PRODUCTS")
public class ProductsVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    private Integer productId;

    @NotNull(message = "請選擇商品類別")
    @Column(name = "PRODUCT_CATEGORY_ID", nullable = false)
    private Integer productCategoryId;
    
    @ManyToOne
    @JoinColumn(name = "PRODUCT_CATEGORY_ID", insertable = false, updatable = false)
    private ProductCategoryVO productCategory;

    public ProductCategoryVO getProductCategory() { return productCategory; }
    public void setProductCategory(ProductCategoryVO productCategory) { this.productCategory = productCategory; }

    @NotNull(message = "請輸入價格")
    @Min(value = 1, message = "價格必須大於 0")
    @Column(name = "PRODUCT_PRICE", nullable = false)
    private Integer productPrice;

    @NotBlank(message = "請輸入商品介紹")
    @Size(max = 1000, message = "商品介紹最多 1000 字")
    @Column(name = "PRODUCT_DESC", nullable = false)
    private String productDesc;

    @NotBlank(message = "商品名稱不可為空")
    @Size(max = 30, message = "商品名稱最多 30 字")
    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;

    @NotNull(message = "請輸入庫存數量")
    @Column(name = "PRODUCT_QUANTITY", nullable = false)
    private Integer productQuantity;

    @NotNull(message = "請選擇商品狀態")
    @Column(name = "PRODUCT_STATUS")
    private Byte productStatus;

    @Column(name = "PRODUCT_REVIEW_NUMBER")
    private Integer productReviewNumber = 0;

    @Column(name = "PRODUCT_TOTAL_STAR")
    private Integer productTotalStar = 0;

    // getters and setters
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getProductCategoryId() { return productCategoryId; }
    public void setProductCategoryId(Integer productCategoryId) { this.productCategoryId = productCategoryId; }

    public Integer getProductPrice() { return productPrice; }
    public void setProductPrice(Integer productPrice) { this.productPrice = productPrice; }

    public String getProductDesc() { return productDesc; }
    public void setProductDesc(String productDesc) { this.productDesc = productDesc; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getProductQuantity() { return productQuantity; }
    public void setProductQuantity(Integer productQuantity) { this.productQuantity = productQuantity; }

    public Byte getProductStatus() { return productStatus; }
    public void setProductStatus(Byte productStatus) { this.productStatus = productStatus; }

    public Integer getProductReviewNumber() { return productReviewNumber; }
    public void setProductReviewNumber(Integer productReviewNumber) { this.productReviewNumber = productReviewNumber; }

    public Integer getProductTotalStar() { return productTotalStar; }
    public void setProductTotalStar(Integer productTotalStar) { this.productTotalStar = productTotalStar; }
}
