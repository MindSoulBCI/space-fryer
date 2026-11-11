package com.example.spacefryerdiner.p91d27a30.domain.usecase
import com.example.spacefryerdiner.p91d27a30.data.repository.AccountRepository
class AccountUseCase(private val accounts:AccountRepository) {
 fun submit(name:String,password:String,confirm:String,register:Boolean):String {
  require(!register || password==confirm) { "两次输入的密码不一致" }
  return accounts.authenticate(name,password,register)
 }
}
