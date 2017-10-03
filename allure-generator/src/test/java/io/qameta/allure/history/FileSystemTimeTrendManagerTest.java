package io.qameta.allure.history;

import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.entity.GroupTime;
import io.qameta.allure.testdata.TestData;
import org.assertj.core.groups.Tuple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
public class FileSystemTimeTrendManagerTest {

    private static final String TIME_TREND_JSON = "time-trend.json";
    private static final String HISTORY_DIRECTORY = "history";
    private static final long START = 1506951379940L;
    private static final long STOP = 1506951411833L;
    private static final long DURATION = 31893L;
    private static final long MIN_DURATION = 31893L;
    private static final long MAX_DURATION = 31893L;
    private static final long SUM_DURATION = 31893L;

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    private final ExpectedException expectedException = ExpectedException.none();

    private final FileSystemTimeTrendManager timeTrendManager = new FileSystemTimeTrendManager();

    @Test
    public void shouldLoadNewData() throws Exception {
        final Path resultsDirectory = temporaryFolder.newFolder().toPath();
        final Path history = Files.createDirectories(resultsDirectory.resolve(HISTORY_DIRECTORY));
        final Path trend = history.resolve(TIME_TREND_JSON);
        TestData.unpackFile(TIME_TREND_JSON, trend);

        timeTrendManager.setHistoryDirectory(history);
        List<GroupTime> result = timeTrendManager.load(mockConfiguration());

        assertThat(result)
                .hasSize(2)
                .extracting(GroupTime::getStart, GroupTime::getStop, GroupTime::getDuration,
                        GroupTime::getMinDuration, GroupTime::getMaxDuration, GroupTime::getSumDuration)
                .containsExactly(
                        Tuple.tuple(START, STOP, DURATION, MIN_DURATION, MAX_DURATION, SUM_DURATION),
                        Tuple.tuple(1506951080235L, 1506951110851L, 30616L, 30616L, 30616L, 30616L)
                );
    }

    @Test
    public void testLoadFileNotExists() throws Exception {
        timeTrendManager.setHistoryDirectory(Paths.get("not/exists"));
        assertTrue("Time tend items are absent", timeTrendManager.load(mock(Configuration.class)).isEmpty());
    }

    @Test
    @PrepareForTest({Files.class, FileSystemHistoryTrendManager.class})
    public void testLoadIOException() throws Exception {
        final Path history = Files.createDirectories(temporaryFolder.newFolder().toPath().resolve(HISTORY_DIRECTORY));
        final Path trend = history.resolve(TIME_TREND_JSON);
        TestData.unpackFile(TIME_TREND_JSON, trend);
        expectedException.expect(IoTrendException.class);
        expectedException.expectMessage("Could not read trend file " + trend);
        timeTrendManager.setHistoryDirectory(history);
        mockStatic(Files.class);
        when(Files.newInputStream(trend)).thenThrow(new IOException());
        timeTrendManager.load(mock(Configuration.class));
    }

    @Test
    public void testSave() throws Exception {
        timeTrendManager.setHistoryDirectory(temporaryFolder.newFolder().toPath());
        Configuration configuration = mockConfiguration();
        GroupTime groupTime = new GroupTime();
        groupTime.setStart(START);
        groupTime.setStop(STOP);
        groupTime.setDuration(DURATION);
        groupTime.setMinDuration(MIN_DURATION);
        groupTime.setMaxDuration(MAX_DURATION);
        groupTime.setSumDuration(SUM_DURATION);
        List<GroupTime> saved = Collections.singletonList(groupTime);
        timeTrendManager.save(configuration, saved);
        assertEquals("Loaded time trend items equal to saved", timeTrendManager.load(configuration), saved);
    }

    @Test
    @PrepareForTest({Files.class, FileSystemHistoryTrendManager.class})
    public void testSaveIOException() throws Exception {
        expectedException.expect(IoTrendException.class);
        Path historyDirectory = temporaryFolder.newFolder().toPath();
        timeTrendManager.setHistoryDirectory(historyDirectory);
        mockStatic(Files.class);
        when(Files.createDirectories(historyDirectory)).thenThrow(IOException.class);
        timeTrendManager.save(mock(Configuration.class), Collections.emptyList());
    }

    private Configuration mockConfiguration() {
        final Configuration configuration = mock(Configuration.class);
        when(configuration.requireContext(JacksonContext.class)).thenReturn(new JacksonContext());
        return configuration;
    }
}
