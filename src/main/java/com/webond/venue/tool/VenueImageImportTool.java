package com.webond.venue.tool;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VenueImageImportTool {

    private static final String URL = "jdbc:mysql://localhost:3306/webond_project?serverTimezone=Asia/Taipei";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    private static final String IMAGE_DIR = "src/main/resources/static/images/venue";
    private static final int[] VENUE_IDS = {2001, 2002, 2003, 2004, 2005};

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            for (int venueId : VENUE_IDS) {
                importOne(conn, venueId);
            }
        }
        System.out.println("全部處理完成。");
    }

    private static void importOne(Connection conn, int venueId) throws Exception {
        File file = new File(IMAGE_DIR, venueId + ".jpg");
        if (!file.exists()) {
            System.out.println("[跳過] 找不到檔案：" + file.getPath());
            return;
        }

        if (alreadyHasCover(conn, venueId)) {
            System.out.println("[跳過] VENUE_ID=" + venueId + " 已經有封面照片，避免重複匯入");
            return;
        }

        byte[] bytes = Files.readAllBytes(file.toPath());

        String sql = "INSERT INTO VENUE_IMAGES (VENUE_ID, VENUE_IMAGES, VENUE_COVER) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, venueId);
            ps.setBytes(2, bytes);
            ps.setByte(3, (byte) 1); // 設為封面照
            ps.executeUpdate();
        }

        System.out.println("[完成] VENUE_ID=" + venueId + " <- " + file.getName() + " (" + bytes.length + " bytes)");
    }

    private static boolean alreadyHasCover(Connection conn, int venueId) throws Exception {
        String sql = "SELECT COUNT(*) FROM VENUE_IMAGES WHERE VENUE_ID = ? AND VENUE_COVER = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, venueId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
