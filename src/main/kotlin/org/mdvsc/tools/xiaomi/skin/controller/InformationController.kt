package org.mdvsc.tools.xiaomi.skin.controller

import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import javafx.stage.StageStyle

/**
 * @author HanikLZ
 * @since 2017/5/9
 */
class InformationController : BaseController() {

    @FXML lateinit var textFieldVersion : TextField
    @FXML lateinit var textFieldAuthor : TextField
    @FXML lateinit var textFieldDesigner : TextField
    @FXML lateinit var textFieldTitle : TextField

    @FXML lateinit var buttonUiVersionV7 : ToggleButton
    @FXML lateinit var buttonUiVersionV8 : ToggleButton
    @FXML lateinit var buttonUiVersionV9 : ToggleButton

    private val uiVersionButtonMap = mutableMapOf<Int, ToggleButton>()

    override fun onCreate(arguments: Map<String, String>) {
        super.onCreate(arguments)
        stage.initStyle(StageStyle.UNDECORATED)
        uiVersionButtonMap[5] = buttonUiVersionV7
        uiVersionButtonMap[6] = buttonUiVersionV8
        uiVersionButtonMap[7] = buttonUiVersionV9
        SelectorController.selectedTheme?.let {
            textFieldAuthor.text = it.getThemeAuthor()
            textFieldDesigner.text = it.getThemeDesigner()
            textFieldTitle.text = it.getThemeTitle()
            textFieldVersion.text = it.getThemeVersion()
            selectUiVersion(it.getThemeUiVersion().toIntOrNull() ?: 0)
        }
        uiVersionButtonMap.values.forEach {
            it.selectedProperty().addListener { _, _, newValue ->
                if (newValue) uiVersionButtonMap.values.forEach { action -> if (action != it) action.isSelected = false }
            }
        }
    }

    @FXML
    fun actionClose() {
        stage.close()
    }

    @FXML
    fun actionOk() {
        SelectorController.selectedTheme?.run {
            setThemeAuthor(textFieldAuthor.text)
            setThemeTitle(textFieldTitle.text)
            setThemeVersion(textFieldVersion.text)
            setThemeDesigner(textFieldDesigner.text)
            setThemeUiVersion(uiVersionButtonMap.filter { it.value.isSelected }.keys.firstOrNull() ?: getThemeVersion().toIntOrNull()?:0)
            save()
        }
        stage.close()
    }

    private fun selectUiVersion(version: Int) {
        uiVersionButtonMap.values.forEach { it.isSelected = false }
        uiVersionButtonMap[version]?.isSelected = true
    }

}

