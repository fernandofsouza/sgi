package br.gov.sgi.repository;

import br.gov.sgi.entity.RelevanceCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RelevanceCriteriaRepository extends JpaRepository<RelevanceCriteria, String> {
    List<RelevanceCriteria> findByActiveTrue();
}
