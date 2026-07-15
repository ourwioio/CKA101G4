package com.webond.service.service;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.model.ServiceVO;
import com.webond.service.repository.ServiceRepository;

@Service
public class ServiceImageImportService {

    private static final int FIRST_SERVICE_ID = 1;
    private static final int LAST_SERVICE_ID = 10;

    private static final String IMAGE_FOLDER =
            "static/images/service/";

    private static final String IMAGE_EXTENSION =
            ".png";

    private static final String IMAGE_CONTENT_TYPE =
            "image/png";

    private final ServiceRepository serviceRepository;

    public ServiceImageImportService(
            ServiceRepository serviceRepository) {

        this.serviceRepository = serviceRepository;
    }

    // =========================================================
    // 匯入 SERVICE_ID 1～10 的服務圖片
    //
    // 圖片位置：
    // src/main/resources/static/images/service/1.png
    // ...
    // src/main/resources/static/images/service/10.png
    // =========================================================

    @Transactional
    public ImportResult importServiceImages() {

        int successCount = 0;
        int failureCount = 0;

        StringBuilder failureMessages =
                new StringBuilder();

        for (int serviceId = FIRST_SERVICE_ID;
             serviceId <= LAST_SERVICE_ID;
             serviceId++) {

            try {
                ServiceVO serviceVO =
                        serviceRepository
                                .findById(serviceId)
                                .orElse(null);

                if (serviceVO == null) {

                    failureCount++;

                    appendFailure(
                            failureMessages,
                            "找不到服務 #" + serviceId
                    );

                    continue;
                }

                String imagePath =
                        IMAGE_FOLDER
                        + serviceId
                        + IMAGE_EXTENSION;

                ClassPathResource imageResource =
                        new ClassPathResource(imagePath);

                if (!imageResource.exists()) {

                    failureCount++;

                    appendFailure(
                            failureMessages,
                            "找不到圖片 " + serviceId + ".png"
                    );

                    continue;
                }

                byte[] imageBytes;

                try (InputStream inputStream =
                             imageResource.getInputStream()) {

                    imageBytes =
                            inputStream.readAllBytes();
                }

                if (imageBytes.length == 0) {

                    failureCount++;

                    appendFailure(
                            failureMessages,
                            "圖片 " + serviceId + ".png 為空"
                    );

                    continue;
                }

                serviceVO.setServiceImage(imageBytes);

                serviceVO.setServiceImageType(
                        IMAGE_CONTENT_TYPE
                );

                serviceRepository.save(serviceVO);

                successCount++;

            } catch (IOException e) {

                failureCount++;

                appendFailure(
                        failureMessages,
                        "服務 #" + serviceId
                        + " 圖片讀取失敗"
                );
            }
        }

        serviceRepository.flush();

        return new ImportResult(
                successCount,
                failureCount,
                failureMessages.toString()
        );
    }

    private void appendFailure(
            StringBuilder builder,
            String message) {

        if (!builder.isEmpty()) {
            builder.append("；");
        }

        builder.append(message);
    }

    // =========================================================
    // 匯入結果
    // =========================================================

    public static class ImportResult {

        private final int successCount;
        private final int failureCount;
        private final String failureMessage;

        public ImportResult(
                int successCount,
                int failureCount,
                String failureMessage) {

            this.successCount = successCount;
            this.failureCount = failureCount;
            this.failureMessage = failureMessage;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public String getFailureMessage() {
            return failureMessage;
        }
    }
}