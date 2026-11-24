package io.joranvanbelle.tiemeplan.service;

import io.joranvanbelle.tiemeplan.model.ScenarioDto;
import io.joranvanbelle.tiemeplan.util.AppInfo;
import io.joranvanbelle.tiemeplan.viewmodel.ScenarioViewModel;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ScenarioPersistenceService {
    private final JsonMapper jsonMapper = new JsonMapper();
    private final ObjectMapper yamlMapper = new YAMLMapper();

    public void saveJson(ScenarioViewModel viewModel, File file) throws IOException {
        saveJson(viewModel.toDto(), file);
    }

    public void saveJson(ScenarioDto dto, File file) throws IOException {
        String json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
        Files.writeString(file.toPath(), json);
    }

    public void saveYaml(ScenarioViewModel viewModel, File file) throws IOException {
        saveYaml(viewModel.toDto(), file);
    }

    public void saveYaml(ScenarioDto dto, File file) throws IOException {
        String yaml = yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
        Files.writeString(file.toPath(), yaml);
    }

    public void loadJson(ScenarioViewModel viewModel, File file) throws IOException {
        viewModel.loadFrom(readJson(file));
    }

    public ScenarioDto readJson(File file) throws IOException {
        String json = Files.readString(file.toPath());
        ScenarioDto dto = jsonMapper.readValue(json, ScenarioDto.class);
        validateSchema(dto);
        return dto;
    }

    public void loadYaml(ScenarioViewModel viewModel, File file) throws IOException {
        viewModel.loadFrom(readYaml(file));
    }

    public ScenarioDto readYaml(File file) throws IOException {
        String yaml = Files.readString(file.toPath());
        ScenarioDto dto = yamlMapper.readValue(yaml, ScenarioDto.class);
        validateSchema(dto);
        return dto;
    }

    private void validateSchema(ScenarioDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Invalid scenario file");
        }
        if (dto.schemaVersion() > ScenarioDto.CURRENT_SCHEMA_VERSION) {
            throw new IllegalArgumentException(
                    "Saved with newer schema version " + dto.schemaVersion() + ". Update the app.");
        }
        if (dto.schemaVersion() < ScenarioDto.CURRENT_SCHEMA_VERSION - 2) {
            throw new IllegalArgumentException(
                """
                %s is no longer supported by Tiemeplan version %s.
                Tiemeplan supports versions %s until %s.
                Use an older version of Tiemeplan to open this file."""
                    .formatted(dto.schemaVersion(),
                        AppInfo.getVersion(),
                        ScenarioDto.CURRENT_SCHEMA_VERSION - 2,
                        ScenarioDto.CURRENT_SCHEMA_VERSION));
        }
    }
}
