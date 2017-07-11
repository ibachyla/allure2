import template from './StatusWidgetView.hbs';
import {View} from 'backbone.marionette';
import {className, regions} from '../../decorators';
import {Model} from 'backbone';
import PieChartView from '../../components/graph-pie-chart/PieChartView';

@className('status-widget')
@regions({
    chart: '.status-widget__content'
})
export default class StatusWidgetView extends View {
    template = template;

    onRender() {
        this.showChildView('chart', new PieChartView({
            model: this.getStatusChartData(),
            showLegend: true
        }));
    }

    getStatusChartData() {
        const statistic =  this.model.reduce((stats, testResult) => {
            stats[testResult.get('status').toLowerCase()]++;
            return stats;
        }, {
            total: this.model.length,
            failed: 0,
            broken: 0,
            skipped: 0,
            pending: 0,
            passed: 0,
            knownissuesonly: 0,
            notcovered: 0,
            unknown: 0
        });
        statistic.total -= statistic.notcovered;
        delete statistic['notcovered'];
        return new Model({statistic});
    }
}
