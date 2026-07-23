package pl.salezjanie.most.communication.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.salezjanie.most.communication.domain.DeviceToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {
    List<DeviceToken> findByMemberIdIn(List<UUID> memberIds);
    Optional<DeviceToken> findByToken(String token);
}
