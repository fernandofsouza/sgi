package br.gov.sgi.service;

import br.gov.sgi.entity.SystemConfig;
import br.gov.sgi.repository.SystemConfigRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private static final String KEY_CREATION = "creation_statuses";
    private static final String KEY_PROGRESS  = "progress_statuses";

    private final SystemConfigRepository repo;
    private final ObjectMapper            mapper;

    public Map<String, List<String>> getConfig() {
        List<String> creation = readList(KEY_CREATION);
        List<String> progress = readList(KEY_PROGRESS);
        return Map.of("creationStatuses", creation, "progressStatuses", progress);
    }

    @Transactional
    public Map<String, List<String>> updateConfig(Map<String, List<String>> config) {
        if (config.containsKey("creationStatuses")) {
            saveList(KEY_CREATION, config.get("creationStatuses"));
        }
        if (config.containsKey("progressStatuses")) {
            saveList(KEY_PROGRESS, config.get("progressStatuses"));
        }
        return getConfig();
    }

    private List<String> readList(String key) {
        return repo.findById(key).map(c -> {
            try {
                return mapper.readValue(c.getConfigValue(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                return List.<String>of();
            }
        }).orElse(List.of());
    }

    private void saveList(String key, List<String> values) {
        try {
            String json = mapper.writeValueAsString(values);
            SystemConfig cfg = repo.findById(key).orElse(new SystemConfig());
            cfg.setConfigKey(key);
            cfg.setConfigValue(json);
            repo.save(cfg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao serializar config: " + key, e);
        }
    }
}
