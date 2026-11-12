package lab.neurotrino.app

import android.app.Application
import android.content.Context
import lab.neurotrino.app.data.db.NeurotrinoDatabase
import lab.neurotrino.app.data.repo.LabRepository

class AppContainer(context: Context) {
    val database: NeurotrinoDatabase = NeurotrinoDatabase.create(context.applicationContext)
    val repository: LabRepository = LabRepository(database, context.applicationContext)
}

class NeurotrinoApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
