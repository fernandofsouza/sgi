package br.gov.sgi.repository;

import br.gov.sgi.entity.Indicator;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndicatorRepository extends JpaRepository<Indicator, String> {

    @Query(
        value = """
            SELECT i FROM Indicator i
            LEFT JOIN FETCH i.assignees
            WHERE (:year IS NULL OR i.referenceYear = :year)
              AND (:range IS NULL OR i.referenceRange = :range)
              AND (:label IS NULL OR i.referenceLabel = :label)
              AND (:creationStatus IS NULL OR i.creationStatus = :creationStatus)
              AND (:progressStatus IS NULL OR i.progressStatus = :progressStatus)
            """,
        countQuery = """
            SELECT COUNT(DISTINCT i) FROM Indicator i
            LEFT JOIN i.assignees
            WHERE (:year IS NULL OR i.referenceYear = :year)
              AND (:range IS NULL OR i.referenceRange = :range)
              AND (:label IS NULL OR i.referenceLabel = :label)
              AND (:creationStatus IS NULL OR i.creationStatus = :creationStatus)
              AND (:progressStatus IS NULL OR i.progressStatus = :progressStatus)
            """
    )
    Page<Indicator> findWithFilters(
        @Param("year")           Integer year,
        @Param("range")          String range,
        @Param("label")          String label,
        @Param("creationStatus") String creationStatus,
        @Param("progressStatus") String progressStatus,
        Pageable pageable
    );

    List<Indicator> findByParentIsNull();

    List<Indicator> findByParentId(String parentId);

    @Query("SELECT COUNT(i) FROM Indicator i WHERE i.parent.id = :parentId")
    long countByParentId(@Param("parentId") String parentId);
}
