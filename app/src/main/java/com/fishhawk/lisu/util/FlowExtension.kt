package com.fishhawk.lisu.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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