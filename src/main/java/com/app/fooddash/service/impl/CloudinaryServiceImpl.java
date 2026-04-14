package com.app.fooddash.service.impl;

import com.app.fooddash.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file, String folder, String publicId) {

        log.info("Uploading file to Cloudinary. folder={}, publicId={}", folder, publicId);

        try {
            Map<String, Object> options = new HashMap<>();
            options.put("folder", folder);
            options.put("public_id", publicId);
            options.put("overwrite", true);

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            String imageUrl = uploadResult.get("secure_url").toString();

            log.info("File uploaded successfully to Cloudinary. publicId={}, url={}", publicId, imageUrl);

            return imageUrl;

        } catch (IOException e) {

            log.error("Cloudinary upload failed. folder={}, publicId={}, error={}", folder, publicId, e.getMessage(), e);

            throw new RuntimeException("Image upload failed", e);
        }
    }
}