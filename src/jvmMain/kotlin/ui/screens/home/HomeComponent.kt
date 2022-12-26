package ui.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import data.Client
import data.entity.DatabaseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class HomeScreenState(
    val databaseList: List<DatabaseEntity> = listOf()
)

class HomeComponent(
    private val client: Client,
    componentContext: ComponentContext,
    val navigateToDatabase: (DatabaseEntity) -> Unit
) :
    ComponentContext by componentContext {

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    val state: MutableState<HomeScreenState> = mutableStateOf(HomeScreenState())

    fun getDatabase() {
        coroutineScope.launch(Dispatchers.IO) {
            state.value = state.value.copy(databaseList = client.getDataBases())
        }
    }
}