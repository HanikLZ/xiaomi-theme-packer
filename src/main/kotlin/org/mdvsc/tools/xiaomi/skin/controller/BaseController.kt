package org.mdvsc.tools.xiaomi.skin.controller

import org.mdvsc.tools.xiaomi.skin.Context
import javafx.stage.Stage

/**
 * @author HanikLZ
 * @since 2016/11/21
 */
abstract class BaseController {

    lateinit var context: Context
    lateinit var stage: Stage

    open fun onCreate(arguments: Map<String, String>) {}
    open fun onCloseRequest() = false

}

