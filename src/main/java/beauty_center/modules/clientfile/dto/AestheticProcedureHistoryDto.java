package beauty_center.modules.clientfile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for aesthetic procedure history section.
 * Free text field in MVP.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AestheticProcedureHistoryDto {

    private String procedures;
}

