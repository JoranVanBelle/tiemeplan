package io.joranvanbelle.tiemeplan.service;

import io.joranvanbelle.tiemeplan.model.ScenarioDto;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScenarioPersistenceService {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new YAMLMapper();

    public void saveJson(ScenarioViewModel viewModel, File file) throws IOException {
        ScenarioDto dto = viewModel.toDto();
        ensureSchema(dto);
        String json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
        Files.writeString(file.toPath(), json);
    }

    public void saveYaml(ScenarioViewModel viewModel, File file) throws IOException {
        ScenarioDto dto = viewModel.toDto();
        ensureSchema(dto);
        String yaml = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
        Files.writeString(file.toPath(), yaml);
    }

    public void loadJson(ScenarioViewModel viewModel, File file) throws IOException {
        String json = Files.readString(file.toPath());
        ScenarioDto dto = jsonMapper.readValue(json, ScenarioDto.class);
        validateSchema(dto);
        viewModel.loadFrom(dto);
    }

    public void loadYaml(ScenarioViewModel viewModel, File file) throws IOException {
        String yaml = Files.readString(file.toPath());
        ScenarioDto dto = yamlMapper.readValue(yaml, ScenarioDto.class);
        validateSchema(dto);
        viewModel.loadFrom(dto);
    }

    private void ensureSchema(ScenarioDto dto) {
        // Schema version is set by ScenarioViewModel.toDto() to CURRENT_SCHEMA_VERSION
    }

    private void validateSchema(ScenarioDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Invalid scenario file");
        }
        if (dto.schemaVersion() > ScenarioDto.CURRENT_SCHEMA_VERSION) {
            throw new IllegalArgumentException(
                    "Saved with newer schema version " + dto.schemaVersion() + ". Update the app.");
        }
        if (dto.schemaVersion() < ScenarioDto.CURRENT_SCHEMA_VERSION) {
            throw new IllegalArgumentException(
                    "Saved with older schema version " + dto.schemaVersion() + ". Migration not supported.");
        }
    }
}
