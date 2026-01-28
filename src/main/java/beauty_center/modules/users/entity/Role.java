package beauty_center.modules.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Role enumeration for role-based access control
 */
public enum Role {
    ADMIN,
    EMPLOYEE,
    CLIENT
}
