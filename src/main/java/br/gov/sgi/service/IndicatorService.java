package br.gov.sgi.service;

import br.gov.sgi.dto.*;
import br.gov.sgi.entity.*;
import br.gov.sgi.repository.IndicatorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndicatorService {

    private final IndicatorRepository indicatorRepo;
    private final TeamMemberService   teamMemberService;

    // ── Listagem ──────────────────────────────────────────────────────────────

    public Page<IndicatorSummaryDTO> findAll(
            Integer year, String range, String label,
            String creationStatus, String progressStatus,
            Pageable pageable) {

        return indicatorRepo
                .findWithFilters(year, range, label, creationStatus, progressStatus, pageable)
                .map(this::toSummary);
    }

    public List<IndicatorSummaryDTO> findRoots() {
        return indicatorRepo.findByParentIsNull()
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    public List<IndicatorSummaryDTO> findChildren(String parentId) {
        return indicatorRepo.findByParentId(parentId)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    public IndicatorDetailDTO findById(String id) {
        return toDetail(findOrThrow(id));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Transactional
    public IndicatorDetailDTO create(CreateIndicatorDTO dto) {
        Indicator indicator = new Indicator();
        indicator.setId(UUID.randomUUID().toString());
        applyCreateDTO(indicator, dto);
        return toDetail(indicatorRepo.save(indicator));
    }

    @Transactional
    public IndicatorDetailDTO update(String id, UpdateIndicatorDTO dto) {
        Indicator indicator = findOrThrow(id);
        applyUpdateDTO(indicator, dto);
        return toDetail(indicatorRepo.save(indicator));
    }

    @Transactional
    public void delete(String id) {
        Indicator indicator = findOrThrow(id);
        // Remove referência nos filhos antes de deletar
        indicator.getChildren().forEach(child -> child.setParent(null));
        indicatorRepo.delete(indicator);
    }

    // ── Check-ins ─────────────────────────────────────────────────────────────

    @Transactional
    public CheckInDTO addCheckIn(String indicatorId, CreateCheckInDTO dto) {
        Indicator indicator = findOrThrow(indicatorId);
        TeamMember author   = teamMemberService.findEntityById(dto.getAuthorId());

        CheckIn checkIn = new CheckIn();
        checkIn.setId(UUID.randomUUID().toString());
        checkIn.setIndicator(indicator);
        checkIn.setCheckDate(dto.getCheckDate());
        checkIn.setProgress(dto.getProgress());
        checkIn.setNotes(dto.getNotes());
        checkIn.setAuthor(author);

        if (dto.getCriteriaUpdates() != null) {
            List<CheckInCriteriaUpdate> updates = dto.getCriteriaUpdates().stream().map(u -> {
                CheckInCriteriaUpdate upd = new CheckInCriteriaUpdate();
                upd.setId(UUID.randomUUID().toString());
                upd.setCheckIn(checkIn);
                upd.setCriteriaId(u.getCriteriaId());
                upd.setValue(u.getValue());

                // Atualiza valor atual do critério no indicador
                indicator.getCriteria().stream()
                        .filter(c -> c.getId().equals(u.getCriteriaId()))
                        .findFirst()
                        .ifPresent(c -> c.setCurrentValue(u.getValue()));
                return upd;
            }).collect(Collectors.toList());
            checkIn.setCriteriaUpdates(updates);
        }

        indicator.getCheckIns().add(checkIn);
        indicator.setProgress(dto.getProgress());
        indicatorRepo.save(indicator);

        return toCheckInDTO(checkIn);
    }

    public List<CheckInDTO> listCheckIns(String indicatorId) {
        return findOrThrow(indicatorId).getCheckIns()
                .stream().map(this::toCheckInDTO).collect(Collectors.toList());
    }

    // ── Avaliações de Relevância ──────────────────────────────────────────────

    @Transactional
    public void updateRelevanceAssessments(String indicatorId, List<RelevanceAssessmentDTO> dtos) {
        Indicator indicator = findOrThrow(indicatorId);
        indicator.getRelevanceAssessments().clear();

        dtos.forEach(dto -> {
            RelevanceAssessment assessment = new RelevanceAssessment();
            RelevanceAssessmentId embId   = new RelevanceAssessmentId();
            embId.setIndicatorId(indicatorId);
            embId.setCriterionId(dto.getCriterionId());
            assessment.setId(embId);
            assessment.setIndicator(indicator);
            assessment.setScore(dto.getScore());
            indicator.getRelevanceAssessments().add(assessment);
        });

        indicatorRepo.save(indicator);
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private Indicator findOrThrow(String id) {
        return indicatorRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Indicador não encontrado: " + id));
    }

    private void applyCreateDTO(Indicator ind, CreateIndicatorDTO dto) {
        ind.setTitle(dto.getTitle());
        ind.setDescription(dto.getDescription());
        ind.setPdgId(dto.getPdgId());
        ind.setCreationStatus(dto.getCreationStatus() != null ? dto.getCreationStatus() : "Não iniciado");
        ind.setProgressStatus(dto.getProgressStatus() != null ? dto.getProgressStatus() : "Não iniciado");
        ind.setProgress(dto.getProgress() != null ? dto.getProgress() : 0);
        ind.setTargetDate(dto.getTargetDate());
        ind.setObservation(dto.getObservation());
        ind.setReferenceYear(dto.getReferenceYear());
        ind.setReferenceRange(dto.getReferenceRange());
        ind.setReferenceLabel(dto.getReferenceLabel());

        if (dto.getParentId() != null) {
            indicatorRepo.findById(dto.getParentId()).ifPresent(ind::setParent);
        }
        if (dto.getEditorId() != null) {
            teamMemberService.findEntityByIdOptional(dto.getEditorId()).ifPresent(ind::setEditor);
        }
        if (dto.getValidatorId() != null) {
            teamMemberService.findEntityByIdOptional(dto.getValidatorId()).ifPresent(ind::setValidator);
        }
        if (dto.getAssigneeIds() != null) {
            List<TeamMember> assignees = dto.getAssigneeIds().stream()
                    .map(teamMemberService::findEntityById).collect(Collectors.toList());
            ind.setAssignees(assignees);
        }
        if (dto.getCriteria() != null) {
            List<EvaluationCriteria> criteria = dto.getCriteria().stream().map(c -> {
                EvaluationCriteria ec = new EvaluationCriteria();
                ec.setId(UUID.randomUUID().toString());
                ec.setIndicator(ind);
                ec.setName(c.getName());
                ec.setWeight(c.getWeight());
                ec.setTargetValue(c.getTargetValue());
                ec.setCurrentValue(c.getCurrentValue() != null ? c.getCurrentValue() : java.math.BigDecimal.ZERO);
                ec.setUnit(c.getUnit());
                return ec;
            }).collect(Collectors.toList());
            ind.setCriteria(criteria);
        }
        if (dto.getAchievementScale() != null) {
            setAchievementScale(ind, dto.getAchievementScale());
        } else {
            setDefaultAchievementScale(ind);
        }
    }

    private void applyUpdateDTO(Indicator ind, UpdateIndicatorDTO dto) {
        if (dto.getTitle()          != null) ind.setTitle(dto.getTitle());
        if (dto.getDescription()    != null) ind.setDescription(dto.getDescription());
        if (dto.getPdgId()          != null) ind.setPdgId(dto.getPdgId());
        if (dto.getCreationStatus() != null) ind.setCreationStatus(dto.getCreationStatus());
        if (dto.getProgressStatus() != null) ind.setProgressStatus(dto.getProgressStatus());
        if (dto.getProgress()       != null) ind.setProgress(dto.getProgress());
        if (dto.getTargetDate()     != null) ind.setTargetDate(dto.getTargetDate());
        if (dto.getObservation()    != null) ind.setObservation(dto.getObservation());
        if (dto.getReferenceYear()  != null) ind.setReferenceYear(dto.getReferenceYear());
        if (dto.getReferenceRange() != null) ind.setReferenceRange(dto.getReferenceRange());
        if (dto.getReferenceLabel() != null) ind.setReferenceLabel(dto.getReferenceLabel());

        if (dto.getEditorId()    != null) teamMemberService.findEntityByIdOptional(dto.getEditorId()).ifPresent(ind::setEditor);
        if (dto.getValidatorId() != null) teamMemberService.findEntityByIdOptional(dto.getValidatorId()).ifPresent(ind::setValidator);

        if (dto.getAssigneeIds() != null) {
            ind.setAssignees(dto.getAssigneeIds().stream()
                    .map(teamMemberService::findEntityById).collect(Collectors.toList()));
        }
        if (dto.getCriteria() != null) {
            ind.getCriteria().clear();
            dto.getCriteria().forEach(c -> {
                EvaluationCriteria ec = new EvaluationCriteria();
                ec.setId(c.getId() != null ? c.getId() : UUID.randomUUID().toString());
                ec.setIndicator(ind);
                ec.setName(c.getName());
                ec.setWeight(c.getWeight());
                ec.setTargetValue(c.getTargetValue());
                ec.setCurrentValue(c.getCurrentValue() != null ? c.getCurrentValue() : java.math.BigDecimal.ZERO);
                ec.setUnit(c.getUnit());
                ind.getCriteria().add(ec);
            });
        }
        if (dto.getAchievementScale() != null) {
            setAchievementScale(ind, dto.getAchievementScale());
        }
    }

    private void setAchievementScale(Indicator ind, List<AchievementScaleLabelDTO> dtos) {
        ind.getAchievementScale().clear();
        dtos.forEach(s -> {
            AchievementScaleLabel label = new AchievementScaleLabel();
            label.setId(UUID.randomUUID().toString());
            label.setIndicator(ind);
            label.setScaleValue(s.getValue());
            label.setLabel(s.getLabel());
            ind.getAchievementScale().add(label);
        });
    }

    private void setDefaultAchievementScale(Indicator ind) {
        String[][] defaults = {
            {"1","Insuficiente"}, {"2","Abaixo do esperado"}, {"3","Dentro do esperado"},
            {"4","Acima do esperado"}, {"5","Excepcional"}
        };
        for (String[] d : defaults) {
            AchievementScaleLabel label = new AchievementScaleLabel();
            label.setId(UUID.randomUUID().toString());
            label.setIndicator(ind);
            label.setScaleValue(Integer.parseInt(d[0]));
            label.setLabel(d[1]);
            ind.getAchievementScale().add(label);
        }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private IndicatorSummaryDTO toSummary(Indicator i) {
        return IndicatorSummaryDTO.builder()
                .id(i.getId())
                .seqId(i.getSeqId())
                .title(i.getTitle())
                .pdgId(i.getPdgId())
                .creationStatus(i.getCreationStatus())
                .progressStatus(i.getProgressStatus())
                .progress(i.getProgress())
                .targetDate(i.getTargetDate())
                .parentId(i.getParent() != null ? i.getParent().getId() : null)
                .referenceYear(i.getReferenceYear())
                .referenceRange(i.getReferenceRange())
                .referenceLabel(i.getReferenceLabel())
                .assignees(i.getAssignees().stream().map(this::toMemberDTO).collect(Collectors.toList()))
                .checkInCount(i.getCheckIns().size())
                .updatedAt(i.getUpdatedAt())
                .build();
    }

    private IndicatorDetailDTO toDetail(Indicator i) {
        return IndicatorDetailDTO.builder()
                .id(i.getId())
                .seqId(i.getSeqId())
                .title(i.getTitle())
                .description(i.getDescription())
                .pdgId(i.getPdgId())
                .creationStatus(i.getCreationStatus())
                .progressStatus(i.getProgressStatus())
                .progress(i.getProgress())
                .targetDate(i.getTargetDate())
                .parentId(i.getParent() != null ? i.getParent().getId() : null)
                .childrenIds(i.getChildren().stream().map(Indicator::getId).collect(Collectors.toList()))
                .editor(i.getEditor() != null ? toMemberDTO(i.getEditor()) : null)
                .validator(i.getValidator() != null ? toMemberDTO(i.getValidator()) : null)
                .observation(i.getObservation())
                .referenceYear(i.getReferenceYear())
                .referenceRange(i.getReferenceRange())
                .referenceLabel(i.getReferenceLabel())
                .criteria(i.getCriteria().stream().map(this::toCriteriaDTO).collect(Collectors.toList()))
                .achievementScale(i.getAchievementScale().stream()
                        .map(s -> new AchievementScaleLabelDTO(s.getScaleValue(), s.getLabel()))
                        .collect(Collectors.toList()))
                .assignees(i.getAssignees().stream().map(this::toMemberDTO).collect(Collectors.toList()))
                .checkIns(i.getCheckIns().stream().map(this::toCheckInDTO).collect(Collectors.toList()))
                .relevanceAssessments(i.getRelevanceAssessments().stream()
                        .map(r -> new RelevanceAssessmentDTO(r.getId().getCriterionId(), r.getScore()))
                        .collect(Collectors.toList()))
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }

    private TeamMemberDTO toMemberDTO(TeamMember m) {
        return TeamMemberDTO.builder()
                .id(m.getId()).name(m.getName()).role(m.getRole())
                .email(m.getEmail()).avatarUrl(m.getAvatarUrl()).active(m.getActive())
                .build();
    }

    private EvaluationCriteriaDTO toCriteriaDTO(EvaluationCriteria c) {
        return new EvaluationCriteriaDTO(
                c.getId(), c.getName(), c.getWeight(),
                c.getTargetValue(), c.getCurrentValue(), c.getUnit());
    }

    private CheckInDTO toCheckInDTO(CheckIn ck) {
        return CheckInDTO.builder()
                .id(ck.getId())
                .indicatorId(ck.getIndicator().getId())
                .checkDate(ck.getCheckDate())
                .progress(ck.getProgress())
                .notes(ck.getNotes())
                .author(toMemberDTO(ck.getAuthor()))
                .criteriaUpdates(ck.getCriteriaUpdates().stream()
                        .map(u -> new CriteriaUpdateDTO(u.getCriteriaId(), u.getValue()))
                        .collect(Collectors.toList()))
                .createdAt(ck.getCreatedAt())
                .build();
    }
}
