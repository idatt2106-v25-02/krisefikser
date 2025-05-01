package stud.ntnu.krisefikser.household.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductTypeResponse {

  @NotNull
  private UUID id;
  @NotNull
  private String name;
  @NotNull
  private String unit;
}