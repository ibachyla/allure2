import AppLayout from '../../layouts/application/AppLayout';
import GraphCollection from './GraphCollection';
import WidgetsGridView from '../../components/widgets-grid/WidgetsGridView';
import WidgetsModel from '../../data/widgets/WidgetsModel';

export default class GraphLayout extends AppLayout {
    initialize() {
        this.collection = new GraphCollection();
        this.widgetsData = new WidgetsModel();
    }

    loadData() {
        this.widgetsData.fetch();
        return this.collection.fetch();
    }

    getContentView() {
        return new WidgetsGridView({
            model: this.collection,
            widgetsData: this.widgetsData,
            tabName: 'graph'
        });
    }
}
