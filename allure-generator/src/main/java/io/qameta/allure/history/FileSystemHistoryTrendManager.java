package io.qameta.allure.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.entity.Statistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

public class FileSystemHistoryTrendManager implements IHistoryTrendManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemHistoryTrendManager.class);
    private static final String HISTORY_TREND_JSON = "history-trend.json";

    private Path historyDirectory;

    @Override
    public List<HistoryTrendItem> load(final Configuration configuration) throws IoHistoryTrendException {
        final Path historyFile = historyDirectory.resolve(HISTORY_TREND_JSON);
        if (historyFile.toFile().exists()) {
            try (InputStream is = Files.newInputStream(historyFile)) {
                final ObjectMapper mapper = configuration.requireContext(JacksonContext.class).getValue();
                final JsonNode jsonNode = mapper.readTree(is);
                return getStream(jsonNode).map(child -> parseItem(historyFile, mapper, child))
                        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

            } catch (IOException e) {
                throw new IoHistoryTrendException("Could not read history-trend file " + historyFile, e);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void save(final Configuration configuration, final List<HistoryTrendItem> historyTrendItems)
            throws IoHistoryTrendException {
        try (OutputStream os = Files
                .newOutputStream(Files.createDirectories(historyDirectory).resolve(HISTORY_TREND_JSON))) {
            configuration.requireContext(JacksonContext.class).getValue().writeValue(os, historyTrendItems);
        } catch (IOException e) {
            throw new IoHistoryTrendException(e);
        }
    }

    private Stream<JsonNode> getStream(final JsonNode jsonNode) {
        return stream(spliteratorUnknownSize(jsonNode.elements(), Spliterator.ORDERED), false);
    }

    private Optional<HistoryTrendItem> parseItem(final Path historyFile, final ObjectMapper mapper,
                                                 final JsonNode child) {
        try {
            if (Objects.nonNull(child.get("total"))) {
                final Statistic statistic = mapper.treeToValue(child, Statistic.class);
                return Optional.of(new HistoryTrendItem().setStatistic(statistic));
            }
            return Optional.ofNullable(mapper.treeToValue(child, HistoryTrendItem.class));
        } catch (JsonProcessingException e) {
            LOGGER.warn("Could not read {}", historyFile, e);
            return Optional.empty();
        }
    }

    public void setHistoryDirectory(final Path historyDirectory) {
        this.historyDirectory = historyDirectory;
    }
}
