/*
 * Copyright (C) 2024 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.nooblol.smartnoob.feature.revenue.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.nooblol.smartnoob.feature.revenue.domain.InternalRevenueRepository
import com.nooblol.smartnoob.feature.revenue.domain.model.PurchaseState

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn

import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

import kotlinx.coroutines.flow.transformLatest

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class BillingActivityViewModel@Inject constructor(
    private val revenueRepository: InternalRevenueRepository,
) : ViewModel() {

    init {
        revenueRepository.purchaseState
            .transformLatest<PurchaseState, Unit> { purchaseState ->
                if (purchaseState != PurchaseState.PENDING) return@transformLatest

                while (true) {
                    revenueRepository.refreshPurchases()
                    delay(PENDING_PURCHASE_POLLING_DURATION)
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshPurchaseState() {
        revenueRepository.refreshPurchases()
    }

    fun setBillingActivityDestroyed() {
        revenueRepository.setBillingActivityDestroyed()
    }
}

private val PENDING_PURCHASE_POLLING_DURATION = 3.seconds