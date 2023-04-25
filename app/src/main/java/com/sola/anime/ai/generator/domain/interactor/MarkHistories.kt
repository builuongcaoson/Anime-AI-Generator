package com.sola.anime.ai.generator.domain.interactor

import com.sola.anime.ai.generator.domain.model.history.ChildHistory
import com.sola.anime.ai.generator.domain.repo.HistoryRepository
import io.reactivex.Flowable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

//class MarkHistories @Inject constructor(
//    private val historyRepo: HistoryRepository
//) : Interactor<MarkHistories.Params>() {
//
//    class Params(val childHistories: List<ChildHistory>)
//
//    override fun buildObservable(params: Params): Flowable<*> {
//        return Flowable.just(System.currentTimeMillis())
//            .doOnNext { historyRepo.markHistories(childHistories = params.childHistories.toTypedArray()) }
//            .map { startTime -> System.currentTimeMillis() - startTime }
//            .map { elapsed -> TimeUnit.MILLISECONDS.toMillis(elapsed) }
//            .doOnNext { milliseconds -> Timber.i("Completed mark history in $milliseconds milliseconds") }
//    }
//
//}