package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.MetinexDatabase
import com.example.data.repository.InventoryRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {

    private lateinit var database: MetinexDatabase
    private lateinit var repository: InventoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = MetinexDatabase.getDatabase(this, lifecycleScope)
        repository = InventoryRepository(database)

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return InventoryViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
            }
        }

        setContent {
            MyApplicationTheme {
                val inventoryViewModel: InventoryViewModel = viewModel(factory = viewModelFactory)
                MainScreen(viewModel = inventoryViewModel)
            }
        }
    }
}
