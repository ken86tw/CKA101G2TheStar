package com.thestar.shop.util;

import java.sql.*;
import java.io.*;

public class PhotoWrite {

    public static void main(String[] argv) {
        Connection con = null;
        PreparedStatement pstmt = null;
        InputStream fin = null;

        String url = "jdbc:mysql://localhost:3306/thestar?serverTimezone=Asia/Taipei";
        String userid = "root";
        String passwd = "123456";

        // 圖片資料夾路徑，每個子資料夾以商品ID命名
        // 例如：DB_photos/1/image1.jpg → 商品ID=1
        String photosRoot = "src/main/resources/static/images/products";

        String insert = "INSERT INTO PRODUCT_IMAGE (PRODUCT_ID, PRODUCT_IMAGE, IS_COVER) VALUES (?, ?, ?)";

        try {
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(insert);

            File rootDir = new File(photosRoot);
            File[] productFolders = rootDir.listFiles(File::isDirectory);

            for (File folder : productFolders) {
                int productId = Integer.parseInt(folder.getName());
                File[] imageFiles = folder.listFiles();
                boolean isFirst = true;

                for (File f : imageFiles) {
                    fin = new FileInputStream(f);
                    pstmt.setInt(1, productId);
                    pstmt.setBinaryStream(2, fin);
                    pstmt.setInt(3, isFirst ? 1 : 0); // 第一張設為封面
                    pstmt.executeUpdate();
                    isFirst = false;
                    System.out.println("已插入商品 " + productId + " 的圖片：" + f.getName());
                }
            }

            System.out.println("所有圖片插入完成！");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}