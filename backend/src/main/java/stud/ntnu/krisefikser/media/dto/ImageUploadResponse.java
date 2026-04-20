package stud.ntnu.krisefikser.media.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ImageUploadResponse {

  String url;
  String publicId;
}
