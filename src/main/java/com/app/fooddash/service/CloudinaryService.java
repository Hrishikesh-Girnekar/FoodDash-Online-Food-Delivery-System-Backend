package com.app.fooddash.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
	String uploadFile(MultipartFile file, String folder, String publicId);
}