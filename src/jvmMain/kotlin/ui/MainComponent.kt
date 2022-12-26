package ui

import androidx.compose.ui.window.ApplicationScope
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.bringToFront
import com.arkivanov.decompose.router.pop
import com.arkivanov.decompose.router.router
import com.arkivanov.essenty.parcelable.Parcelable
import data.Client
import data.entity.DatabaseEntity
import ui.screens.database.DatabaseComponent
import ui.screens.database.DatabaseScreen
import ui.screens.home.HomeComponent
import ui.screens.home.HomeScreen
import ui.util.Content
import ui.util.asContent

class MainComponent(
    private val applicationScope: ApplicationScope,
    private val client: Client,
    componentContext: ComponentContext
) : ComponentContext by componentContext {

    private val router = router<MainConfiguration, Content>(
        initialConfiguration = MainConfiguration.Home(client),
        handleBackButton = true,
        childFactory = ::createChild
    )
    val routerState = router.state

    private fun createChild(configuration: MainConfiguration, componentContext: ComponentContext): Content =
        when (configuration) {
            is MainConfiguration.Home -> home(this, client = client)
            is MainConfiguration.Database -> database(this, configuration)
        }

    private fun popBack() {
        router.pop()
    }

    private fun navigateToDatabase(databaseEntity: DatabaseEntity) {
        router.bringToFront(MainConfiguration.Database(client = client, databaseEntity = databaseEntity))
    }

    private fun home(componentContext: ComponentContext, client: Client): Content =
        HomeComponent(
            client = client,
            componentContext = componentContext,
            navigateToDatabase = ::navigateToDatabase
        ).asContent { HomeScreen(it) }

    private fun database(componentContext: ComponentContext, configuration: MainConfiguration.Database): Content =
        DatabaseComponent(
            client = configuration.client,
            componentContext = componentContext,
            goBack = ::popBack,
            databaseEntity = configuration.databaseEntity
        ).asContent { DatabaseScreen(it) }

    sealed class MainConfiguration() : Parcelable {
        data class Database(val client: Client, val databaseEntity: DatabaseEntity?) : MainConfiguration()
        data class Home(val client: Client) : MainConfiguration()
    }

}