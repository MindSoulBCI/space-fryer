package com.example.spacefryerdiner.p91d27a30.domain.usecase
import org.junit.Test
import org.junit.Assert.*
import com.example.spacefryerdiner.p91d27a30.data.repository.*
class AccountUseCaseTest {
 private class Memory:AccountStorage {val values=mutableMapOf<String,String>();override fun read(key:String)=values[key];override fun write(key:String,value:String){values[key]=value}}
 @Test fun registerPersistsHashAndNewRepositoryCanLogin(){val storage=Memory();val a=AccountUseCase(AccountRepository(storage));assertEquals("chef",a.submit(" chef ","secret123","secret123",true));assertFalse(storage.values.values.single().contains("secret123"));assertEquals("chef",AccountUseCase(AccountRepository(storage)).submit("chef","secret123","",false))}
 @Test fun wrongPasswordAndDuplicateAndConfirmationAreRejected(){val a=AccountUseCase(AccountRepository(Memory()));a.submit("chef","secret123","secret123",true);for(action in listOf<()->Unit>({a.submit("chef","wrong1234","",false)},{a.submit("chef","secret123","secret123",true)},{a.submit("newchef","secret123","different",true)},{a.submit("missing","secret123","",false)})){try{action();fail("Must reject")}catch(_:IllegalArgumentException){}}}
 @Test fun identicalPasswordsUseDifferentSalts(){val s=Memory();val a=AccountUseCase(AccountRepository(s));a.submit("one","secret123","secret123",true);a.submit("two","secret123","secret123",true);assertEquals(2,s.values.values.toSet().size)}
}
