package ui.screens.database

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.entity.DocumentEntity

@Composable
fun DatabaseScreen(component: DatabaseComponent) {
  val state by component.state
  LaunchedEffect(Unit) {
    component.getCollections()
  }

  Scaffold(
    topBar = {
      Column {
        TopAppBar(title = {
          Text(
            "База данных: ${state.databaseEntity.name}", fontSize = 20.sp
          )
        }, navigationIcon = {
          IconButton(component.goBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
          }
        }, actions = {
          Text("Размер: ${state.databaseEntity.sizeOnDisk / 8} - Байт")
        })
        AnimatedVisibility(visible = state.isLoading) {
          LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
      }
    },
  ) {
    Row(
      modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxHeight().background(MaterialTheme.colors.surface),
        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 5.dp)
      ) {
        items(state.collectionList) {
          Card(
            modifier = Modifier.size(width = 170.dp, height = 40.dp).padding(vertical = 5.dp).clip(
              shape = RoundedCornerShape(35)
            ).border(
              width = 1.dp, color = MaterialTheme.colors.onBackground, shape = RoundedCornerShape(35)
            ).clickable { component.selectCollection(it) },
            backgroundColor = if (state.selectableCollection == it) MaterialTheme.colors.primary
            else MaterialTheme.colors.background,
          ) {
            Text(
              modifier = Modifier.fillMaxWidth().padding(5.dp),
              text = it.name,
              fontSize = 18.sp,
              textAlign = TextAlign.Center
            )
          }
        }
      }
      Column {
        Divider(Modifier.width(1.dp).fillMaxHeight(), color = MaterialTheme.colors.onBackground)
      }
      AnimatedVisibility(
        modifier = Modifier.weight(3f).background(Color.LightGray), visible = state.selectableCollection.name != ""
      ) {
        Row {
          Card(modifier = Modifier.padding(10.dp).fillMaxHeight()) {
            Column(
              modifier = Modifier.background(MaterialTheme.colors.background),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                  modifier = Modifier.padding(5.dp),
                  value = state.requestKeys,
                  onValueChange = component::changeRequestKeys,
                  singleLine = true,
                  label = { Text(text = "Ключи") },
                  placeholder = { Text(text = "id;name;..") },
                )
                IconButton(
                  modifier = Modifier.padding(5.dp).padding(top = 10.dp), onClick = {
                    component.getDocuments(collectionsEntity = state.selectableCollection)
                  }, enabled = !state.isLoading
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow, contentDescription = null
                  )
                }
              }
              LazyColumn(modifier = Modifier) {
                state.requestConditions.forEachIndexed { index, s ->
                  item {
                    OutlinedTextField(
                      modifier = Modifier.height(57.dp),
                      value = state.requestConditions[index].condition,
                      onValueChange = {
                        component.changeRequestCondition(
                          value = it, index = index
                        )
                      },
                      singleLine = true,
                      label = { Text(text = "Условие") },
                      trailingIcon = {
                        IconButton(onClick = { component.deleteRequestCondition(index) }) {
                          Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                      },
                      leadingIcon = {
                        IconButton(onClick = { component.changeRequestConditionType(!s.isString, index) }) {
                          if (s.isString) Text("Txt") else Text("123")
                        }
                      }
                    )
                  }
                }
                item {
                  AnimatedVisibility(state.requestConditions.size != 2) {
                    Button(onClick = { component.addRequestCondition() }) {
                      Text(
                        text = "Добавить условие"
                      )
                    }
                  }
                }
              }
              AnimatedVisibility(state.requestConditions.size == 2) {
                LazyRow {
                  items(component.operators) {
                    IconButton(modifier = Modifier,
                      onClick = { component.changeSelectableOperator(it) }) {
                      Text(
                        text = it.name,
                        fontSize = if (it == state.selectableOperator) 18.sp else 10.sp,
                        color = if (it == state.selectableOperator) MaterialTheme.colors.primary else MaterialTheme.colors.onSecondary
                      )
                    }
                  }
                }
              }
              AnimatedVisibility(
                modifier = Modifier, visible = state.selectableDocument != DocumentEntity()
              ) {
                LazyColumn {
                  item {
                    Card(
                      modifier = Modifier.padding(vertical = 5.dp).width(330.dp).clip(
                        shape = RoundedCornerShape(5)
                      ).clickable { component.getFullDocument(state.selectableDocument._id) },
                      backgroundColor = MaterialTheme.colors.primary
                    ) {
                      Text(
                        modifier = Modifier.padding(10.dp),
                        text = state.selectableDocument.content,
                        fontSize = 20.sp
                      )
                    }

                  }
                }
              }
            }
          }
          Card(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            LazyColumn(modifier = Modifier.weight(1f)) {
              state.result.forEach {
                item {
                  Card(
                    modifier = Modifier.padding(vertical = 5.dp).width(250.dp).clip(
                      shape = RoundedCornerShape(10)
                    ).clickable { component.getFullDocument(it._id) },
                    backgroundColor = if (state.selectableDocument._id == it._id) MaterialTheme.colors.primary
                    else MaterialTheme.colors.background,
                  ) {
                    Text(
                      text = it.content,
                      modifier = Modifier.padding(20.dp),
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
