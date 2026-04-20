package stud.ntnu.krisefikser.media.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import stud.ntnu.krisefikser.media.dto.ImageUploadResponse;
import stud.ntnu.krisefikser.media.service.CloudinaryService;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Image upload APIs")
public class ImageController {

  private final CloudinaryService cloudinaryService;

  @Operation(summary = "Upload image", description = "Uploads an image file to Cloudinary.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Image uploaded successfully",
          content = @Content(schema = @Schema(implementation = ImageUploadResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid upload request"),
      @ApiResponse(responseCode = "401", description = "Not authenticated"),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @PostMapping("/upload")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ImageUploadResponse> uploadImage(
      @Parameter(description = "Image file")
      @RequestParam("file") MultipartFile file,
      @Parameter(description = "Optional Cloudinary folder")
      @RequestParam(value = "folder", required = false) String folder,
      @Parameter(description = "Optional comma-separated tags")
      @RequestParam(value = "tags", required = false) String tags,
      @Parameter(description = "Optional Cloudinary context string (key=value|key2=value2)")
      @RequestParam(value = "context", required = false) String context,
      @Parameter(description = "Optional Cloudinary metadata string (field=value|field2=value2)")
      @RequestParam(value = "metadata", required = false) String metadata,
      @Parameter(description = "Optional upload preset name")
      @RequestParam(value = "uploadPreset", required = false) String uploadPreset
  ) {
    return ResponseEntity.ok(
        cloudinaryService.uploadImage(file, folder, tags, context, metadata, uploadPreset));
  }
}
