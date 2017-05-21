package org.mdvsc.tools.xiaomi.skin.controller

import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.TextField
import javafx.stage.StageStyle
import org.mdvsc.tools.xiaomi.skin.preference.AppPreferences
import org.mdvsc.tools.xiaomi.skin.preference.AppPreferences.deviceIp
import org.mdvsc.tools.xiaomi.skin.preference.AppPreferences.devicePort
import org.mdvsc.tools.xiaomi.skin.utils.DeviceUtils

/**
 * @author HanikLZ
 * @since 2017/5/9
 */
class PreferenceController : BaseController() {

    @FXML lateinit var textFieldIp : TextField
    @FXML lateinit var textFieldPort : TextField

    override fun onCreate(arguments: Map<String, String>) {
        super.onCreate(arguments)
        stage.initStyle(StageStyle.UNDECORATED)
        context.defaultProperties().also {
            textFieldIp.text = AppPreferences.deviceIp.get(it)
            textFieldPort.text = AppPreferences.devicePort.get(it).toString()
        }
    }

    @FXML
    fun actionClose() {
        stage.close()
    }

    @FXML
    fun actionOk() {
        val port =context.defaultProperties().let {
            deviceIp.set(it, textFieldIp.text)
            devicePort.set(it, textFieldPort.text.toIntOrNull() ?: devicePort.defaultValue)
            devicePort.get(it)
        }
        try {DeviceUtils.openTcpip(port) } catch (ignored : Exception) { ignored.printStackTrace() }
        stage.close()
    }

    @FXML
    fun actionFix() {
        Alert(Alert.AlertType.INFORMATION).apply {
            initOwner(stage)
            title = "修复ADB"
            headerText = "修复ADB"
            contentText = "你好"
        }.showAndWait()
    }

    @FXML
    fun actionPurchase() {
        Alert(Alert.AlertType.INFORMATION).apply {
            initOwner(stage)
            title = "支付"
            headerText = "支付"
            contentText = "你好"
        }.showAndWait()
    }

    @FXML
    fun actionContact() {
        Alert(Alert.AlertType.INFORMATION).apply {
            initOwner(stage)
            title = "联系作者"
            headerText = "联系作者"
            contentText = "你好"
        }.showAndWait()
    }

}
