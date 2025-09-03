package ch.notenverwaltung.repository;

import ch.notenverwaltung.model.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestRepository extends JpaRepository<TestEntity, UUID> {
}
