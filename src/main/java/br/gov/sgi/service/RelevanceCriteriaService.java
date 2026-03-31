package br.gov.sgi.service;

import br.gov.sgi.dto.RelevanceCriteriaDTO;
import br.gov.sgi.dto.ScaleLabelDTO;
import br.gov.sgi.entity.RelevanceCriteria;
import br.gov.sgi.entity.RelevanceScaleLabel;
import br.gov.sgi.repository.RelevanceCriteriaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelevanceCriteriaService {

    private final RelevanceCriteriaRepository repo;

    public List<RelevanceCriteriaDTO> findAll() {
        return repo.findByActiveTrue().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public RelevanceCriteriaDTO findById(String id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public RelevanceCriteriaDTO create(RelevanceCriteriaDTO dto) {
        RelevanceCriteria entity = new RelevanceCriteria();
        entity.setId(UUID.randomUUID().toString());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setActive(true);
        setScaleLabels(entity, dto);
        return toDTO(repo.save(entity));
    }

    @Transactional
    public RelevanceCriteriaDTO update(String id, RelevanceCriteriaDTO dto) {
        RelevanceCriteria entity = findOrThrow(id);
        if (dto.getName()        != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getScaleLabels() != null) {
            entity.getScaleLabels().clear();
            setScaleLabels(entity, dto);
        }
        return toDTO(repo.save(entity));
    }

    @Transactional
    public void delete(String id) {
        RelevanceCriteria entity = findOrThrow(id);
        entity.setActive(false);
        repo.save(entity);
    }

    private RelevanceCriteria findOrThrow(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Critério não encontrado: " + id));
    }

    private void setScaleLabels(RelevanceCriteria entity, RelevanceCriteriaDTO dto) {
        if (dto.getScaleLabels() == null) return;
        dto.getScaleLabels().forEach(sl -> {
            RelevanceScaleLabel label = new RelevanceScaleLabel();
            label.setId(UUID.randomUUID().toString());
            label.setCriterion(entity);
            label.setScaleValue(sl.getValue());
            label.setLabel(sl.getLabel());
            entity.getScaleLabels().add(label);
        });
    }

    private RelevanceCriteriaDTO toDTO(RelevanceCriteria c) {
        return RelevanceCriteriaDTO.builder()
                .id(c.getId()).name(c.getName())
                .description(c.getDescription()).active(c.getActive())
                .scaleLabels(c.getScaleLabels().stream()
                        .map(s -> new ScaleLabelDTO(s.getScaleValue(), s.getLabel()))
                        .collect(Collectors.toList()))
                .build();
    }
}
