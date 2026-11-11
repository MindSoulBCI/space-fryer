package com.example.spacefryerdiner.p91d27a30.domain.usecase
import org.junit.Test
import org.junit.Assert.*
import kotlin.random.Random
import com.example.spacefryerdiner.p91d27a30.domain.model.*
import com.example.spacefryerdiner.p91d27a30.data.repository.LocalFoodRepository
class GuestDemoUseCaseTest {
 @Test fun seededSelectionMatchesOrderAndPicksMoreThanOneRecipe(){val demo=GuestDemoUseCase(Random(17));val cooker=CookingUseCase();val s=GameState(LocalFoodRepository().all(),autoDemo=true);val choices=mutableSetOf<Int>();repeat(20){val next=demo.advance(s,.7f,cooker);assertEquals(next.order,next.selected);assertEquals(FoodPhase.RAW,next.phase);choices.add(next.selected)};assertTrue(choices.size>1)}
 @Test fun readyFoodLiftedAndManualStateNeverAdvanced(){val demo=GuestDemoUseCase(Random(17));val cooker=CookingUseCase();val s=GameState(LocalFoodRepository().all(),autoDemo=true,phase=FoodPhase.READY,progress=1f);assertEquals(FoodPhase.PLATED,demo.advance(s,.7f,cooker).phase);val manual=s.copy(autoDemo=false);assertEquals(manual,demo.advance(manual,10f,cooker));assertEquals(s,demo.advance(s,Float.NaN,cooker))}
}
