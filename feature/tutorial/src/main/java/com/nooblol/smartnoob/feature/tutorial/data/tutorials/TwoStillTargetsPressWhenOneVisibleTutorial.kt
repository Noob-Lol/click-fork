/*
 * Copyright (C) 2023 Kevin Buzeau
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
package com.nooblol.smartnoob.feature.tutorial.data.tutorials

import com.nooblol.smartnoob.feature.tutorial.R
import com.nooblol.smartnoob.feature.tutorial.data.StepEndCondition
import com.nooblol.smartnoob.feature.tutorial.data.StepStartCondition
import com.nooblol.smartnoob.feature.tutorial.data.TutorialData
import com.nooblol.smartnoob.feature.tutorial.data.TutorialInfo
import com.nooblol.smartnoob.feature.tutorial.data.TutorialStepData
import com.nooblol.smartnoob.feature.tutorial.data.game.TutorialGameData
import com.nooblol.smartnoob.feature.tutorial.data.game.rules.TwoStillTargetsPressWhenOneVisibleRules

internal val twoStillTargetsPressWhenOneVisibleTutorialInfo: TutorialInfo =
    TutorialInfo(
        nameResId = R.string.item_title_tutorial_4,
        descResId = R.string.item_desc_tutorial_4,
    )

internal fun newTwoStillTargetsPressWhenOneVisibleTutorial(): TutorialData =
    TutorialData(
        info = twoStillTargetsPressWhenOneVisibleTutorialInfo,
        game = TutorialGameData(
            instructionsResId = R.string.message_tutorial_1_step_1,
            gameRules = TwoStillTargetsPressWhenOneVisibleRules(10),
        ),
        steps = listOf(
            TutorialStepData.TutorialOverlay(
                contentTextResId = R.string.message_tutorial_1_step_1,
                stepStartCondition = StepStartCondition.Immediate,
                stepEndCondition = StepEndCondition.NextButton,
            ),
        )
    )