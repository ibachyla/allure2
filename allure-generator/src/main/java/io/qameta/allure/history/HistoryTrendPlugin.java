package io.qameta.allure.history;

import io.qameta.allure.Aggregator;
import io.qameta.allure.Reader;
import io.qameta.allure.Widget;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.TestResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.qameta.allure.executor.ExecutorPlugin.EXECUTORS_BLOCK_NAME;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
 * Plugin that adds history trend widget.
 *
 * @since 2.0
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class HistoryTrendPlugin implements Reader, Aggregator, Widget {

    public static final String HISTORY_TREND_BLOCK_NAME = "history-trend";

    private IHistoryTrendManager historyTrendManager;

    private HistoryTrendPlugin() {}

    public HistoryTrendPlugin(final IHistoryTrendManager historyTrendManager) {
        this.historyTrendManager = historyTrendManager;
    }

    @Override
    public void readResults(final Configuration configuration,
                            final ResultsVisitor visitor,
                            final Path directory) {
        try {
            final List<HistoryTrendItem> history = historyTrendManager.load(configuration);
            if (!history.isEmpty()) {
                visitor.visitExtra(HISTORY_TREND_BLOCK_NAME, history);
            }
        } catch (IOException e) {
            visitor.error(e.getMessage(), e);
        }
    }

    @Override
    public void aggregate(final Configuration configuration,
                          final List<LaunchResults> launchesResults,
                          final Path outputDirectory) throws IOException {
        historyTrendManager.save(configuration, getHistoryTrendData(launchesResults));
    }

    @Override
    public List<HistoryTrendItem> getData(final Configuration configuration, final List<LaunchResults> launches) {
        return getHistoryTrendData(launches);
    }

    @Override
    public String getName() {
        return HISTORY_TREND_BLOCK_NAME;
    }

    private List<HistoryTrendItem> getHistoryTrendData(final List<LaunchResults> launchesResults) {
        final HistoryTrendItem item = createCurrent(launchesResults);
        final List<HistoryTrendItem> data = getHistoryItems(launchesResults);

        return Stream.concat(Stream.of(item), data.stream())
                .limit(20)
                .collect(Collectors.toList());
    }

    private HistoryTrendItem createCurrent(final List<LaunchResults> launchesResults) {
        final Statistic statistic = launchesResults.stream()
                .flatMap(results -> results.getResults().stream())
                .map(TestResult::getStatus)
                .collect(Statistic::new, Statistic::update, Statistic::merge);
        final HistoryTrendItem item = new HistoryTrendItem()
                .setStatistic(statistic);
        extractLatestExecutor(launchesResults).ifPresent(info -> {
            item.setBuildOrder(info.getBuildOrder());
            item.setReportName(info.getReportName());
            item.setReportUrl(info.getReportUrl());
        });
        return item;
    }

    private List<HistoryTrendItem> getHistoryItems(final List<LaunchResults> launchesResults) {
        return launchesResults.stream()
                .map(this::getPreviousTrendData)
                .reduce(new ArrayList<>(), (first, second) -> {
                    first.addAll(second);
                    return first;
                });
    }

    private List<HistoryTrendItem> getPreviousTrendData(final LaunchResults results) {
        return results.getExtra(HISTORY_TREND_BLOCK_NAME, ArrayList::new);
    }

    private static Optional<ExecutorInfo> extractLatestExecutor(final List<LaunchResults> launches) {
        final Comparator<ExecutorInfo> comparator = comparing(ExecutorInfo::getBuildOrder, nullsFirst(naturalOrder()));
        return launches.stream()
                .map(launch -> launch.getExtra(EXECUTORS_BLOCK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ExecutorInfo.class::isInstance)
                .map(ExecutorInfo.class::cast)
                .sorted(comparator.reversed())
                .findFirst();
    }
}
