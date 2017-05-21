package org.mdvsc.tools.xiaomi.skin

import javafx.stage.Stage
import org.mdvsc.tools.xiaomi.skin.controller.BaseController
import java.io.File
import java.util.*

/**
 * @author HanikLZ
 * @since 2016/11/21
 */
interface Context {

    fun <T : BaseController> startWindow(path: String, arguments: Map<String, String> = emptyMap(), parentStage: Stage? = null): T
    fun defaultProperties(): Properties
    fun workPath(): File
    fun openUri(url: String)

}
