package stud.ntnu.krisefikser.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import stud.ntnu.krisefikser.media.dto.ImageUploadResponse;

@Service
public class CloudinaryService {

  private final Cloudinary cloudinary;
  private final String cloudinaryUrl;

  public CloudinaryService(@Value("${cloudinary.url:${CLOUDINARY_URL:}}") String cloudinaryUrl) {
    this.cloudinaryUrl = cloudinaryUrl;
    this.cloudinary = cloudinaryUrl == null || cloudinaryUrl.isBlank()
        ? null
        : new Cloudinary(cloudinaryUrl);
  }

  public ImageUploadResponse uploadImage(MultipartFile file, String folder) {
    if (cloudinary == null) {
      throw new IllegalStateException(
          "Cloudinary is not configured. Set cloudinary.url or CLOUDINARY_URL.");
    }
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Image file is required.");
    }

    String resolvedFolder = (folder == null || folder.isBlank()) ? "krisefikser" : folder;
    try {
      Map<?, ?> result = cloudinary.uploader().upload(
          file.getBytes(),
          ObjectUtils.asMap(
              "folder", resolvedFolder,
              "resource_type", "image"));

      return ImageUploadResponse.builder()
          .url((String) result.get("secure_url"))
          .publicId((String) result.get("public_id"))
          .build();
    } catch (IOException e) {
      throw new IllegalStateException("Could not upload image to Cloudinary.", e);
    }
  }
}
