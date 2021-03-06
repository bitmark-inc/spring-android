/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.util.callback

interface Action

interface Action0 : Action {
    fun invoke()
}

interface Action1<T> : Action {
    fun invoke(data: T)
}