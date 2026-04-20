package stud.ntnu.krisefikser.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import stud.ntnu.krisefikser.media.dto.ImageUploadResponse;

@Service
public class CloudinaryService {

  private final Cloudinary cloudinary;

  public CloudinaryService(@Value("${cloudinary.url:${CLOUDINARY_URL:}}") String cloudinaryUrl) {
    this.cloudinary = cloudinaryUrl == null || cloudinaryUrl.isBlank()
        ? null
        : new Cloudinary(cloudinaryUrl);
  }

  public ImageUploadResponse uploadImage(MultipartFile file, String folder) {
    return uploadImage(file, folder, null, null, null, null);
  }

  public ImageUploadResponse uploadImage(
      MultipartFile file,
      String folder,
      String tags,
      String context,
      String metadata,
      String uploadPreset
  ) {
    if (cloudinary == null) {
      throw new IllegalStateException(
          "Cloudinary is not configured. Set cloudinary.url or CLOUDINARY_URL.");
    }
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Image file is required.");
    }

    String resolvedFolder = (folder == null || folder.isBlank()) ? "krisefikser" : folder;
    try {
      Map<String, Object> uploadOptions = new HashMap<>();
      uploadOptions.put("folder", resolvedFolder);
      uploadOptions.put("resource_type", "image");
      if (tags != null && !tags.isBlank()) {
        uploadOptions.put("tags", tags);
      }
      if (context != null && !context.isBlank()) {
        uploadOptions.put("context", context);
      }
      if (metadata != null && !metadata.isBlank()) {
        uploadOptions.put("metadata", metadata);
      }
      if (uploadPreset != null && !uploadPreset.isBlank()) {
        uploadOptions.put("upload_preset", uploadPreset);
      }

      Map<?, ?> result = cloudinary.uploader().upload(
          file.getBytes(),
          ObjectUtils.asMap(uploadOptions));

      return ImageUploadResponse.builder()
          .url((String) result.get("secure_url"))
          .publicId((String) result.get("public_id"))
          .build();
    } catch (IOException e) {
      throw new IllegalStateException("Could not upload image to Cloudinary.", e);
    }
  }
}
