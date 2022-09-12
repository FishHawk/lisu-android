package com.fishhawk.lisu.util

import kotlinx.coroutines.flow.*

inline fun <K, reified T> flatten(flows: Map<K, Flow<T>>): Flow<Map<K, T>> =
    combine(flows.mapValues { (key, value) -> value.map { key to it } }.values) { it.toMap() }

inline fun <reified T> flatten(flows: Iterable<Flow<T>>): Flow<List<T>> =
    combine(flows) { it.toList() }

inline fun <reified T> flatten(vararg flows: Flow<T>): Flow<List<T>> =
    combine(*flows) { it.toList() }

inline fun <reified T> flatten(flow: Result<Flow<T>>?): Flow<Result<T>?> =
    flow {
        flow
            ?.onSuccess { it.collect { emit(Result.success(it)) } }
            ?.onFailure { emit(Result.failure(it)) }
            ?: emit(null)
    }

inline fun <T1, T2, R> flatCombine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    crossinline transform: suspend (a: T1, b: T2) -> Flow<R>
): Flow<R> =
    combine(flow1, flow2) { v1, v2 -> v1 to v2 }
        .flatMapLatest { (v1, v2) -> transform(v1, v2) }

