import createSettingsModel from '../data/settings/createSettingsModel';

const GlobalSettingsModel = createSettingsModel(null, {
  language: 'en',
  testResultSorting: {
    field: 'index',
    order: 'asc'
  },
  sidebarCollapsed: false,
  visibleStatuses: {
    failed: true,
    broken: true,
    skipped: true,
    pending: true,
    passed: true,
    knownissuesonly: true,
    notcovered: true
  },
  showGroupInfo: false,
  totalResultSelectOption: 'scenariosWithoutExamplesStatistic',
  stepDisplayParams: {
    showStepStartTime: true,
    showDebugLogs: false
  },
  performanceShowPercentage: false,
  isCoverageChecked: false
});
const settings = new GlobalSettingsModel();
settings.fetch();

export default settings;
