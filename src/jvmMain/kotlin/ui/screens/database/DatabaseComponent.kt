package ui.screens.database

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.mongodb.client.model.Filters.empty
import data.Client
import data.ConditionEntity
import data.Operator
import data.entity.CollectionsEntity
import data.entity.DatabaseEntity
import data.entity.DocumentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.nor
import org.litote.kmongo.not
import org.litote.kmongo.or

data class DatabaseScreenState(
  val databaseEntity: DatabaseEntity,
  val collectionList: List<CollectionsEntity>,
  val selectableCollection: CollectionsEntity = CollectionsEntity(name = ""),
  val result: List<DocumentEntity> = emptyList(),
  val selectableDocument: DocumentEntity = DocumentEntity(),
  val requestKeys: String = "",
  val requestConditions: List<ConditionEntity> = emptyList(),
  val selectableOperator: Operator = Operator.AND,
  val isLoading: Boolean = false
)

class DatabaseComponent(
  componentContext: ComponentContext,
  private val client: Client,
  databaseEntity: DatabaseEntity?,
  val goBack: () -> Unit
) : ComponentContext by componentContext {

  private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

  val operators = Operator.toList()
  val state: MutableState<DatabaseScreenState> =
    mutableStateOf(
      DatabaseScreenState(
        databaseEntity = databaseEntity ?: DatabaseEntity("", 8, true), listOf()
      )
    )


  fun getCollections() {
    coroutineScope.launch(Dispatchers.IO) {
      state.value = state.value.copy(collectionList = client.getCollections(state.value.databaseEntity))
    }
  }

  fun selectCollection(collectionsEntity: CollectionsEntity) {
    state.value = state.value.copy(
      selectableCollection = collectionsEntity,
      result = emptyList(),
      requestKeys = "",
      selectableDocument = DocumentEntity()
    )
  }

  fun getDocuments(collectionsEntity: CollectionsEntity) {
    state.value = state.value.copy(isLoading = true)
    coroutineScope.launch(Dispatchers.IO) {
      val field: List<String>? =
        if (state.value.requestKeys != "") state.value.requestKeys.split(";") else null

      state.value = state.value.copy(
        result = client.getDocuments(
          collectionName = state.value.selectableCollection.name,
          databaseName = state.value.databaseEntity.name,
          condition = getCondition(),
          projection = field
        ),
        isLoading = false
      )
    }
  }

  fun getFullDocument(id: String) {
    if (state.value.selectableDocument._id != id) {
      coroutineScope.launch(Dispatchers.IO) {
        state.value = state.value.copy(
          selectableDocument = client.getOneDoc(
            state.value.selectableCollection,
            state.value.databaseEntity.name,
            id
          )
        )
      }
    } else
      state.value = state.value.copy(selectableDocument = DocumentEntity("", ""))
  }

  fun changeRequestKeys(value: String) {
    if (value != " ")
      state.value = state.value.copy(requestKeys = value)
  }

  fun changeRequestCondition(value: String, index: Int) {
    coroutineScope.launch(Dispatchers.IO) {
      val newList: MutableList<ConditionEntity> = state.value.requestConditions.toMutableList()
      newList[index] = newList[index].copy(condition = value).update()
      state.value = state.value.copy(
        requestConditions = newList
      )
    }
  }
  fun changeRequestConditionType(value: Boolean,index: Int){
    coroutineScope.launch(Dispatchers.IO){
      val newList: MutableList<ConditionEntity> = state.value.requestConditions.toMutableList()
      newList[index] = newList[index].copy(isString = value).update()
      state.value = state.value.copy(
        requestConditions = newList
      )
    }
  }
  fun changeSelectableOperator(index: Operator) {
    state.value = state.value.copy(selectableOperator = index)
  }

  fun addRequestCondition() {
    state.value = state.value.copy(
      requestConditions = state.value.requestConditions + listOf(ConditionEntity())
    )
  }

  fun deleteRequestCondition(index: Int) {
    coroutineScope.launch(Dispatchers.IO) {
      val newList: MutableList<ConditionEntity> = state.value.requestConditions.toMutableList()
      newList.removeAt(index)
      state.value = state.value.copy(
        requestConditions = newList
      )
    }
  }



  private fun getCondition(): Bson =
    when (state.value.requestConditions.size) {
      1 -> state.value.requestConditions[0].bsonCondition
      2 -> when (state.value.selectableOperator) {
        Operator.AND -> and(
          state.value.requestConditions[0].bsonCondition,
          state.value.requestConditions[1].bsonCondition
        )

        Operator.OR -> or(
          state.value.requestConditions[0].bsonCondition,
          state.value.requestConditions[1].bsonCondition
        )

        Operator.NOR -> nor(
          state.value.requestConditions[0].bsonCondition,
          state.value.requestConditions[1].bsonCondition
        )

        Operator.NOT -> and(
            not(state.value.requestConditions[0].bsonCondition),
            not(state.value.requestConditions[1].bsonCondition)
          )
      }

      else -> empty()
    }


}