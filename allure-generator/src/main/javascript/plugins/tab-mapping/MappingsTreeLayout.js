import TreeLayout from '../../layouts/tree/TreeLayout';
import MappingsTestResultTreeView from './MappingsTestResultTreeView';

export default class MappingsTreeLayout extends TreeLayout {

    createTestResultTreeView(tree, routeState, tabName, baseUrl) {
        return new MappingsTestResultTreeView({tree: tree, routeState: routeState, tabName, baseUrl});
    }
}
