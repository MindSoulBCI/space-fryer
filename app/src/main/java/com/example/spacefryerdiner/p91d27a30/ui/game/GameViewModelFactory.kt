package com.example.spacefryerdiner.p91d27a30.ui.game
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spacefryerdiner.p91d27a30.account.ForumAuthClient
import com.example.spacefryerdiner.p91d27a30.data.repository.LocalFoodRepository

class GameViewModelFactory(private val context:Context) : ViewModelProvider.Factory {
 @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T {
  return GameViewModel(
   LocalFoodRepository().all(),
   forum=ForumAuthClient.from(context.applicationContext)
  ) as T
 }
}
