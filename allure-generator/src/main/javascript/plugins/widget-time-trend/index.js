import TimeTrendWidgetView from './TimeTrendWidgetView';

allure.api.addWidget('widgets', 'time-trend', TimeTrendWidgetView);

allure.api.addTranslation('en', {
    widget: {
        timeTrend: {
            name: 'Test Execution Time Trend'
        }
    }
});