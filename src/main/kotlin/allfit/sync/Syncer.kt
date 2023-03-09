package allfit.sync

interface Syncer {
    fun sync()
}

object NoOpSyncer : Syncer {
    override fun sync() {
        println("NoOp sync")
    }
}
