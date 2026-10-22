package com.shohankhan.apexcalculator.core.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shohankhan.apexcalculator.ApexApplication
import com.shohankhan.apexcalculator.core.domain.model.CalculationSource
import com.shohankhan.apexcalculator.core.domain.model.HistoryRecord
import kotlinx.coroutines.launch

/** Writes solver, formula and tool results into the shared calculation history. */
class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ApexApplication).container

    fun record(
        source: CalculationSource,
        expression: String,
        result: String,
        details: String? = null,
    ) {
        viewModelScope.launch {
            runCatching {
                container.historyRepository.add(
                    HistoryRecord(
                        expression = expression,
                        result = result,
                        source = source,
                        createdAt = System.currentTimeMillis(),
                        details = details,
                    ),
                )
            }
        }
    }
}
