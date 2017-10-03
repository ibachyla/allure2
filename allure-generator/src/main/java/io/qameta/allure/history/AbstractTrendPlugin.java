package io.qameta.allure.history;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Reader;
import io.qameta.allure.Widget;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractTrendPlugin<T extends Serializable> implements Reader, Aggregator, Widget {

    private final ITrendManager<T> trendManager;

    protected AbstractTrendPlugin(final ITrendManager<T> trendManager) {
        this.trendManager = trendManager;
    }

    @Override
    public void readResults(final Configuration configuration, final ResultsVisitor visitor, final Path directory) {
        try {
            final List<T> history = trendManager.load(configuration);
            if (!history.isEmpty()) {
                visitor.visitExtra(getName(), history);
            }
        } catch (IOException e) {
            visitor.error(e.getMessage(), e);
        }
    }

    @Override
    public void aggregate(final Configuration configuration, final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        trendManager.save(configuration, getTrendData(launchesResults));
    }

    @Override
    public List<T> getData(final Configuration configuration, final List<LaunchResults> launches) {
        return getTrendData(launches);
    }

    private List<T> getTrendData(final List<LaunchResults> launchesResults) {
        final T item = createCurrent(launchesResults);
        final List<T> data = getHistoryItems(launchesResults);

        return Stream.concat(Stream.of(item), data.stream())
                .limit(20)
                .collect(Collectors.toList());
    }

    private List<T> getHistoryItems(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(this::getPreviousTrendData)
                .reduce(new ArrayList<>(), (first, second) -> {
                    first.addAll(second);
                    return first;
                });
    }

    private List<T> getPreviousTrendData(final LaunchResults results) {
        return results.getExtra(getName(), ArrayList::new);
    }

    protected abstract T createCurrent(final List<LaunchResults> launchesResults);
}
