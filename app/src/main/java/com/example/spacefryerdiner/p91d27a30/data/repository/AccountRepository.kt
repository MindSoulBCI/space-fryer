package com.example.spacefryerdiner.p91d27a30.data.repository

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

interface AccountStorage {
 fun read(key:String):String?
 fun write(key:String,value:String)
}
/** Device-local accounts only. No plaintext password or automatic login is persisted. */
class AccountRepository(private val storage:AccountStorage) {
 private fun key(name:String)="account."+Base64.getUrlEncoder().withoutPadding().encodeToString(name.toByteArray(Charsets.UTF_8))
 private fun hash(password:String,salt:ByteArray):ByteArray {
  val spec=PBEKeySpec(password.toCharArray(),salt,210000,256)
  return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded } finally { spec.clearPassword() }
 }
 @Synchronized fun authenticate(name:String,password:String,register:Boolean):String {
  val user=name.trim()
  require(user.length in 2..32 && user.none { it.isISOControl() }) { "账号需为 2–32 个字符" }
  require(password.length in 8..128) { "密码需为 8–128 个字符" }
  val record=storage.read(key(user))
  if(register) {
   require(record==null) { "这个本机账号已存在，请登录" }
   val salt=ByteArray(16).also { SecureRandom().nextBytes(it) }
   val encoder=Base64.getEncoder()
   storage.write(key(user),encoder.encodeToString(salt)+":"+encoder.encodeToString(hash(password,salt)))
  } else {
   val parts=record?.split(":")
   val valid=try { parts?.size==2 && MessageDigest.isEqual(Base64.getDecoder().decode(parts[1]),hash(password,Base64.getDecoder().decode(parts[0]))) } catch(_:IllegalArgumentException) { false }
   require(valid) { "账号或密码不正确，请先注册本机账号" }
  }
  return user
 }
}
