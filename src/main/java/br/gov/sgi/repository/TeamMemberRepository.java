package br.gov.sgi.repository;

import br.gov.sgi.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {
    List<TeamMember> findByActiveTrue();
}
