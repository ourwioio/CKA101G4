package com.webond.service.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class ServiceImageImportTool {

    // =========================================================
    // 資料庫連線設定
    // 請換成你 application.properties / application.yml 的帳密
    // =========================================================

    private static final String JDBC_URL =
            "jdbc:mysql://localhost:3306/webond_project"
            + "?serverTimezone=Asia/Taipei"
            + "&useUnicode=true"
            + "&characterEncoding=utf8";

    private static final String DB_USERNAME =
            "root";

    private static final String DB_PASSWORD =
            "123456";


    // =========================================================
    // 圖片設定
    // =========================================================

    private static final int FIRST_SERVICE_ID = 1;

    private static final int LAST_SERVICE_ID = 10;

    private static final String IMAGE_FOLDER =
            "static/images/service/";

    private static final String IMAGE_EXTENSION =
            ".png";

    private static final String IMAGE_CONTENT_TYPE =
            "image/png";


    public static void main(String[] args) {

        String updateSql = """
                UPDATE SERVICE
                SET
                    SERVICE_IMAGE = ?,
                    SERVICE_IMAGE_TYPE = ?
                WHERE SERVICE_ID = ?
                """;

        int successCount = 0;
        int failureCount = 0;

        System.out.println(
                "========================================"
        );

        System.out.println(
                "開始直接匯入 SERVICE_ID 1～10 的圖片"
        );

        System.out.println(
                "========================================"
        );

        try {
            /*
             * 不啟動 Spring。
             * 直接透過 JDBC 連線 MySQL。
             */
            try (Connection connection =
                         DriverManager.getConnection(
                                 JDBC_URL,
                                 DB_USERNAME,
                                 DB_PASSWORD
                         );

                 PreparedStatement statement =
                         connection.prepareStatement(updateSql)) {

                // 關閉自動提交，10 張視為同一批交易
                connection.setAutoCommit(false);

                try {
                    for (int serviceId = FIRST_SERVICE_ID;
                         serviceId <= LAST_SERVICE_ID;
                         serviceId++) {

                        String imagePath =
                                IMAGE_FOLDER
                                + serviceId
                                + IMAGE_EXTENSION;

                        try (InputStream inputStream =
                                     ServiceImageImportTool.class
                                             .getClassLoader()
                                             .getResourceAsStream(imagePath)) {

                            if (inputStream == null) {

                                System.out.println(
                                        "[失敗] 找不到圖片："
                                        + imagePath
                                );

                                failureCount++;
                                continue;
                            }

                            byte[] imageBytes =
                                    inputStream.readAllBytes();

                            if (imageBytes.length == 0) {

                                System.out.println(
                                        "[失敗] 圖片內容為空："
                                        + imagePath
                                );

                                failureCount++;
                                continue;
                            }

                            statement.setBytes(
                                    1,
                                    imageBytes
                            );

                            statement.setString(
                                    2,
                                    IMAGE_CONTENT_TYPE
                            );

                            statement.setInt(
                                    3,
                                    serviceId
                            );

                            int affectedRows =
                                    statement.executeUpdate();

                            if (affectedRows == 1) {

                                successCount++;

                                System.out.println(
                                        "[成功] SERVICE_ID = "
                                        + serviceId
                                        + "，圖片 = "
                                        + serviceId
                                        + ".png，大小 = "
                                        + imageBytes.length
                                        + " bytes"
                                );

                            } else {

                                failureCount++;

                                System.out.println(
                                        "[失敗] 找不到 SERVICE_ID = "
                                        + serviceId
                                );
                            }
                        }
                    }

                    // 全部執行完才正式寫入
                    connection.commit();

                } catch (Exception e) {

                    // 中途發生嚴重錯誤，整批復原
                    connection.rollback();

                    throw e;
                }
            }

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "服務圖片匯入完成"
            );

            System.out.println(
                    "成功：" + successCount + " 張"
            );

            System.out.println(
                    "失敗：" + failureCount + " 張"
            );

            System.out.println(
                    "========================================"
            );

        } catch (Exception e) {

            System.err.println(
                    "服務圖片匯入失敗："
                    + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}