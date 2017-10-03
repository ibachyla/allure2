package io.qameta.allure.history;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Statistic;
import io.qameta.allure.entity.Status;
import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.qameta.allure.executor.ExecutorPlugin.EXECUTORS_BLOCK_NAME;
import static io.qameta.allure.testdata.TestData.createLaunchResults;
import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static io.qameta.allure.testdata.TestData.randomHistoryTrendItems;
import static io.qameta.allure.testdata.TestData.randomTestResult;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author charlie (Dmitry Baev).
 */
@RunWith(MockitoJUnitRunner.class)
public class HistoryTrendPluginTest {

    private static final String HISTORY_TREND_BLOCK_NAME = "history-trend";

    @Mock
    private ITrendManager<HistoryTrendItem> historyTrendManager;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReadData() throws Exception {
        final Configuration configuration = mock(Configuration.class);
        final ResultsVisitor visitor = mock(ResultsVisitor.class);

        HistoryTrendItem historyTrendItem = new HistoryTrendItem();
        Statistic statistic = new Statistic();
        statistic.setPassed(1);
        historyTrendItem.setStatistic(statistic);
        List<HistoryTrendItem> historyTrendItems = Collections.singletonList(historyTrendItem);
        when(historyTrendManager.load(configuration)).thenReturn(historyTrendItems);
        final HistoryTrendPlugin plugin = new HistoryTrendPlugin(historyTrendManager);
        plugin.readResults(configuration, visitor, mock(Path.class));

        verify(visitor, times(1)).visitExtra(HISTORY_TREND_BLOCK_NAME, historyTrendItems);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAggregateForEmptyReport() throws Exception {
        final Configuration configuration = mock(Configuration.class);

        final HistoryTrendPlugin plugin = new HistoryTrendPlugin(historyTrendManager);
        plugin.aggregate(configuration, Collections.emptyList(), mock(Path.class));

        final ArgumentCaptor<List<HistoryTrendItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(historyTrendManager, times(1)).save(eq(configuration), captor.capture());

        assertThat(captor.getValue())
                .hasSize(1)
                .extracting(HistoryTrendItem::getStatistic)
                .extracting(Statistic::getTotal)
                .containsExactly(0L);

        assertThat(captor.getValue())
                .hasSize(1)
                .extracting(HistoryTrendItem::getBuildOrder,
                        HistoryTrendItem::getReportName, HistoryTrendItem::getReportUrl)
                .containsExactly(Tuple.tuple(null, null, null));

    }

    @Test
    public void shouldGetData() throws Exception {
        final Configuration configuration = mock(Configuration.class);

        final List<HistoryTrendItem> history = randomHistoryTrendItems();
        final List<HistoryTrendItem> data = new HistoryTrendPlugin(historyTrendManager).getData(configuration, createSingleLaunchResults(
                singletonMap(HISTORY_TREND_BLOCK_NAME, history),
                randomTestResult().setStatus(Status.PASSED),
                randomTestResult().setStatus(Status.FAILED),
                randomTestResult().setStatus(Status.FAILED)
        ));

        assertThat(data)
                .hasSize(1 + history.size())
                .extracting(HistoryTrendItem::getStatistic)
                .extracting(Statistic::getTotal, Statistic::getFailed, Statistic::getPassed)
                .first()
                .isEqualTo(Tuple.tuple(3L, 2L, 1L));

        final List<HistoryTrendItem> next = data.subList(1, data.size());

        assertThat(next)
                .containsExactlyElementsOf(history);

    }

    @Test
    public void shouldFindLatestExecutor() throws Exception {
        final Configuration configuration = mock(Configuration.class);

        final Map<String, Object> extra1 = new HashMap<>();
        final List<HistoryTrendItem> history1 = randomHistoryTrendItems();
        extra1.put(HISTORY_TREND_BLOCK_NAME, history1);
        extra1.put(EXECUTORS_BLOCK_NAME, new ExecutorInfo().setBuildOrder(1L));
        final Map<String, Object> extra2 = new HashMap<>();
        final List<HistoryTrendItem> history2 = randomHistoryTrendItems();
        extra2.put(HISTORY_TREND_BLOCK_NAME, history2);
        extra2.put(EXECUTORS_BLOCK_NAME, new ExecutorInfo().setBuildOrder(7L));

        final List<LaunchResults> launchResults = Arrays.asList(
                createLaunchResults(extra1,
                        randomTestResult().setStatus(Status.PASSED),
                        randomTestResult().setStatus(Status.FAILED),
                        randomTestResult().setStatus(Status.FAILED)
                ),
                createLaunchResults(extra2,
                        randomTestResult().setStatus(Status.PASSED),
                        randomTestResult().setStatus(Status.FAILED),
                        randomTestResult().setStatus(Status.FAILED)
                )
        );

        final List<HistoryTrendItem> data = new HistoryTrendPlugin(historyTrendManager).getData(configuration, launchResults);

        assertThat(data)
                .hasSize(1 + history1.size() + history2.size());

        final HistoryTrendItem historyTrendItem = data.get(0);

        assertThat(historyTrendItem)
                .hasFieldOrPropertyWithValue("buildOrder", 7L);
    }

    @Test
    public void shouldProcessNullBuildOrder() throws Exception {
        final Configuration configuration = mock(Configuration.class);

        final List<HistoryTrendItem> history = randomHistoryTrendItems();
        final Map<String, Object> extra = new HashMap<>();
        extra.put(HISTORY_TREND_BLOCK_NAME, history);
        extra.put(EXECUTORS_BLOCK_NAME, new ExecutorInfo().setBuildOrder(null));

        final List<LaunchResults> launchResults = Arrays.asList(
                createLaunchResults(extra,
                        randomTestResult().setStatus(Status.PASSED),
                        randomTestResult().setStatus(Status.FAILED),
                        randomTestResult().setStatus(Status.FAILED)
                ),
                createLaunchResults(extra,
                        randomTestResult().setStatus(Status.PASSED),
                        randomTestResult().setStatus(Status.FAILED),
                        randomTestResult().setStatus(Status.FAILED)
                )
        );
        final List<HistoryTrendItem> data = new HistoryTrendPlugin(historyTrendManager).getData(configuration, launchResults);

        assertThat(data)
                .hasSize(1 + 2 * history.size());
    }
}
