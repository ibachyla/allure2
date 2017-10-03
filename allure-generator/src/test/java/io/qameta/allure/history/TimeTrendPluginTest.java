package io.qameta.allure.history;

import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.ResultsVisitor;
import io.qameta.allure.entity.GroupTime;
import io.qameta.allure.entity.Status;
import io.qameta.allure.entity.Time;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static io.qameta.allure.testdata.TestData.createSingleLaunchResults;
import static io.qameta.allure.testdata.TestData.randomTestResult;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimeTrendPluginTest {

    private static final String TIME_TREND_BLOCK_NAME = "time-trend";

    @Mock
    private ITrendManager<GroupTime> timeTrendManager;

    @Test
    public void shouldReadData() throws Exception {
        final Configuration configuration = mock(Configuration.class);
        final ResultsVisitor visitor = mock(ResultsVisitor.class);

        List<GroupTime> groupTimes = Collections.singletonList(new GroupTime());
        when(timeTrendManager.load(configuration)).thenReturn(groupTimes);
        final TimeTrendPlugin plugin = new TimeTrendPlugin(timeTrendManager);
        plugin.readResults(configuration, visitor, mock(Path.class));

        verify(visitor, times(1)).visitExtra(TIME_TREND_BLOCK_NAME, groupTimes);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAggregateForEmptyReport() throws Exception {
        final Configuration configuration = mock(Configuration.class);

        final TimeTrendPlugin plugin = new TimeTrendPlugin(timeTrendManager);
        plugin.aggregate(configuration, Collections.emptyList(), mock(Path.class));

        final ArgumentCaptor<List<GroupTime>> captor = ArgumentCaptor.forClass(List.class);
        verify(timeTrendManager, times(1)).save(eq(configuration), captor.capture());

        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    public void shouldGetData() throws Exception {
        final Configuration configuration = mock(Configuration.class);

        final List<GroupTime> history = Collections.singletonList(new GroupTime());
        final List<GroupTime> data = new TimeTrendPlugin(timeTrendManager).getData(configuration, createSingleLaunchResults(
                singletonMap(TIME_TREND_BLOCK_NAME, history),
                randomTestResult().setStatus(Status.PASSED).setTime(Time.create(1L, 2L)),
                randomTestResult().setStatus(Status.FAILED).setTime(Time.create(2L, 3L)),
                randomTestResult().setStatus(Status.FAILED).setTime(Time.create(3L, 4L))
        ));

        assertThat(data)
                .hasSize(1 + history.size())
                .extracting(GroupTime::getDuration)
                .first()
                .isEqualTo(3L);

        final List<GroupTime> next = data.subList(1, data.size());

        assertThat(next)
                .containsExactlyElementsOf(history);
    }
}
