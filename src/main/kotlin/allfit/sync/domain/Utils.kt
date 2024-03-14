package allfit.sync.domain

import allfit.sync.core.SyncListenerManager

fun <ENTITY> SyncListenerManager.onSyncDetailReport(report: DiffReport<ENTITY, ENTITY>, label: String) {
    if (report.somethingHappened) {
        onSyncDetail("Inserted ${report.toInsert.size} and deleted ${report.toDelete.size} $label.")
    } else {
        onSyncDetail("No changes for $label.")
    }
}
