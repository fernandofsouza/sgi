package br.gov.sgi.service;

import br.gov.sgi.dto.TeamMemberDTO;
import br.gov.sgi.entity.TeamMember;
import br.gov.sgi.repository.TeamMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamMemberService {

    private final TeamMemberRepository repo;

    public List<TeamMemberDTO> findAll() {
        return repo.findByActiveTrue().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TeamMemberDTO findById(String id) {
        return toDTO(findEntityById(id));
    }

    public TeamMember findEntityById(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membro não encontrado: " + id));
    }

    public Optional<TeamMember> findEntityByIdOptional(String id) {
        return repo.findById(id);
    }

    @Transactional
    public TeamMemberDTO create(TeamMemberDTO dto) {
        TeamMember member = new TeamMember();
        member.setId(UUID.randomUUID().toString());
        member.setName(dto.getName());
        member.setRole(dto.getRole());
        member.setEmail(dto.getEmail());
        member.setAvatarUrl(dto.getAvatarUrl());
        member.setActive(true);
        return toDTO(repo.save(member));
    }

    @Transactional
    public TeamMemberDTO update(String id, TeamMemberDTO dto) {
        TeamMember member = findEntityById(id);
        if (dto.getName()      != null) member.setName(dto.getName());
        if (dto.getRole()      != null) member.setRole(dto.getRole());
        if (dto.getEmail()     != null) member.setEmail(dto.getEmail());
        if (dto.getAvatarUrl() != null) member.setAvatarUrl(dto.getAvatarUrl());
        if (dto.getActive()    != null) member.setActive(dto.getActive());
        return toDTO(repo.save(member));
    }

    @Transactional
    public void delete(String id) {
        TeamMember member = findEntityById(id);
        member.setActive(false); // soft delete
        repo.save(member);
    }

    private TeamMemberDTO toDTO(TeamMember m) {
        return TeamMemberDTO.builder()
                .id(m.getId()).name(m.getName()).role(m.getRole())
                .email(m.getEmail()).avatarUrl(m.getAvatarUrl()).active(m.getActive())
                .build();
    }
}
