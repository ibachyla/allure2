package io.qameta.allure.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.entity.GroupTime;

import java.nio.file.Path;

public class FileSystemTimeTrendManager extends AbstractFileSystemTrendManager<GroupTime> {

    @Override
    protected GroupTime parseItem(final Path trendFile, final ObjectMapper mapper, final JsonNode child)
            throws JsonProcessingException {
        return mapper.treeToValue(child, GroupTime.class);
    }

    @Override
    protected String getTrendFileName() {
        return "time-trend.json";
    }
}
