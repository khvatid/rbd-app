package ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


@Composable
fun HomeScreen(component: HomeComponent) {
  val state by component.state
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {


    Column {
      Button(onClick = component::getDatabase)
      {
        Text("Получить базы данных")
      }
      AnimatedVisibility(!state.databaseList.isEmpty()) {
        LazyColumn {
          state.databaseList.forEach {
            item {
              Card(
                modifier = Modifier.padding(vertical = 5.dp).width(200.dp).clip(
                  shape = RoundedCornerShape(20)
                ).border(
                  width = 1.dp, color = MaterialTheme.colors.onBackground, shape = RoundedCornerShape(20)
                ).clickable { component.navigateToDatabase(it) },
              ) {
                Column(modifier = Modifier.padding(20.dp)
                ) {
                  Text(it.name)
                  Text("Размер на диске: ${it.sizeOnDisk / 8}")
                  Text("Пустой : ${it.empty}")
                }
              }
            }
          }
        }
      }
    }
  }
}