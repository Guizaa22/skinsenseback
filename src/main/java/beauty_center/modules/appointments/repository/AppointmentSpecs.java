package beauty_center.modules.appointments.repository;

import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.entity.AppointmentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Specifications for Appointment filtering.
 * Avoids PostgreSQL "could not determine data type of parameter" when using nullable params in JPQL.
 */
public final class AppointmentSpecs {

    private AppointmentSpecs() {}

    public static Specification<Appointment> withFilters(
            UUID clientId,
            UUID employeeId,
            AppointmentStatus status,
            OffsetDateTime startAt,
            OffsetDateTime endAt) {
        Specification<Appointment> spec = (root, q, cb) -> null;
        if (clientId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("clientId"), clientId));
        }
        if (employeeId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("employeeId"), employeeId));
        }
        if (status != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        }
        if (startAt != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("startAt"), startAt));
        }
        if (endAt != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("endAt"), endAt));
        }
        return spec;
    }
}
