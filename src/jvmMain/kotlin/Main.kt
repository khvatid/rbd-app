// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.


import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import data.Client
import ui.MainComponent

@OptIn(ExperimentalDecomposeApi::class)
fun main() = application {
  val root = MainComponent(
    applicationScope = this,
    client = Client(),
    componentContext = DefaultComponentContext(LifecycleRegistry())
  )
  Window(onCloseRequest = ::exitApplication) {

    Children(
      routerState = root.routerState,
    ) {
      it.instance()
    }
  }
}