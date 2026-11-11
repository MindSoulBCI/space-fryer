package com.example.spacefryerdiner.p91d27a30.account

import android.content.Context
import com.example.spacefryerdiner.p91d27a30.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class AuthSession(val token:String, val userId:String, val email:String, val nickname:String)
class AuthFailure(message:String):Exception(message)

class ForumAuthClient(private val baseUrl:String, private val appId:String) {
 private val base=baseUrl.trimEnd('/')
 init {
  val url=URL(base)
  require(url.protocol=="https" && url.userInfo==null)
 }

 private suspend fun post(path:String, body:JSONObject):JSONObject=withContext(Dispatchers.IO){
  val connection=URL(base+path).openConnection() as HttpsURLConnection
  try {
   connection.requestMethod="POST"
   connection.connectTimeout=12000
   connection.readTimeout=20000
   connection.doOutput=true
   connection.setRequestProperty("Content-Type","application/json; charset=utf-8")
   connection.setRequestProperty("Accept","application/json")
   connection.outputStream.use{it.write(body.toString().toByteArray(Charsets.UTF_8))}
   val status=connection.responseCode
   val stream=if(status in 200..299) connection.inputStream else connection.errorStream
   val raw=stream?.use{it.readBytes()} ?: ByteArray(0)
   val json=if(raw.isEmpty()) JSONObject() else JSONObject(String(raw,Charsets.UTF_8))
   val code=json.optInt("code", if(status in 200..299) 200 else status)
   if(code!=200) throw AuthFailure(json.optString("msg").ifBlank{"账号服务暂时不可用"})
   json.optJSONObject("data") ?: JSONObject()
  } catch(e:AuthFailure){throw e}
  catch(_:Exception){throw AuthFailure("无法连接账号服务，请检查网络")}
  finally{connection.disconnect()}
 }

 private fun payload(vararg fields:Pair<String,String>)=JSONObject().apply{
  put("appId", appId)
  fields.forEach{(k,v)->put(k,v)}
 }

 private fun session(data:JSONObject):AuthSession {
  val token=data.optString("token")
  if(token.isBlank()) throw AuthFailure("登录返回无效")
  return AuthSession(
   token=token,
   userId=data.opt("userId")?.toString().orEmpty(),
   email=data.optString("email"),
   nickname=data.optString("nickname").ifBlank{data.optString("email").substringBefore('@')}
  )
 }

 suspend fun sendCode(email:String){ post("/auth/sendVerifyCode", payload("email" to email)) }
 suspend fun register(email:String,password:String,code:String)=session(post("/auth/signup", payload(
  "email" to email,"password" to password,"rePassword" to password,"verifyCode" to code
 )))
 suspend fun login(email:String,password:String)=session(post("/auth/login", payload("email" to email,"password" to password)))
 suspend fun loginByCode(email:String,code:String)=session(post("/auth/loginByCode", payload("email" to email,"verifyCode" to code)))

 companion object {
  fun from(context:Context):ForumAuthClient? {
   val base=context.getString(R.string.mindsoul_account_api).trim()
   val appId=context.getString(R.string.mindsoul_app_id).trim()
   if(base.isEmpty()||appId.isEmpty()) return null
   return try{ForumAuthClient(base,appId)}catch(_:Exception){null}
  }
  fun validEmail(v:String)=v.length<=254 && Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(v.trim())
  fun validPassword(v:String)=v.length in 6..20 && v.all{it.isLetterOrDigit()}
  fun validCode(v:String)=Regex("^[0-9]{6}$").matches(v)
 }
}
