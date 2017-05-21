package org.mdvsc.tools.xiaomi.skin.controller

import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ChoiceDialog
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.ToggleButton
import javafx.scene.input.TransferMode
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.StageStyle
import org.mdvsc.tools.xiaomi.skin.model.MIUITheme
import org.mdvsc.tools.xiaomi.skin.preference.AppPreferences
import org.mdvsc.tools.xiaomi.skin.preference.AppPreferences.deviceIp
import org.mdvsc.tools.xiaomi.skin.preference.AppPreferences.devicePort
import org.mdvsc.tools.xiaomi.skin.preference.AppPreferences.themeHistory
import org.mdvsc.tools.xiaomi.skin.preference.AppPreferences.themeHistorySize
import org.mdvsc.tools.xiaomi.skin.utils.DeviceUtils
import org.mdvsc.tools.xiaomi.skin.utils.MtzUtils
import org.mdvsc.tools.xiaomi.skin.utils.fadeOut
import org.mdvsc.tools.xiaomi.skin.utils.fadeIn
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author HanikLZ
 * @since 2017/5/9
 */
class SelectorController : BaseController() {

    companion object {
        var selectedTheme: MIUITheme? = null
    }

    @FXML lateinit var comboBoxPath: ComboBox<String>
    @FXML lateinit var labelTheme: Label
    @FXML lateinit var labelNotification: Label
    @FXML lateinit var labelStatus: Label
    @FXML lateinit var buttonApply: Button
    @FXML lateinit var buttonExport: Button
    @FXML lateinit var buttonInfo: Button
    @FXML lateinit var toggleButtonTop: ToggleButton
    @FXML lateinit var progressBar: ProgressBar

    private val themeSelectHistory = mutableListOf<String>()
    private val defaultNotificationDuration = 2000L
    private var defaultThemeTitle = ""

    override fun onCreate(arguments: Map<String, String>) {
        super.onCreate(arguments)
        stage.initStyle(StageStyle.UNDECORATED)
        stage.focusedProperty().addListener { _, _, newValue -> if (newValue) refresh() }
        defaultThemeTitle = labelTheme.text
        labelStatus.textProperty().run { isNotNull.and(isNotEmpty) }.let {
            labelStatus.visibleProperty().bind(it)
            progressBar.visibleProperty().bind(it)
            comboBoxPath.disableProperty().bind(it)
            val binding = it.or(labelTheme.textProperty().isEqualTo(defaultThemeTitle))
            buttonApply.disableProperty().bind(binding)
            buttonExport.disableProperty().bind(binding)
            buttonInfo.disableProperty().bind(binding)
        }
        labelNotification.isVisible = false
        comboBoxPath.apply {
            items.addListener(ListChangeListener {
                if (it.next() && it.wasAdded()) {
                    selectionModel.clearSelection()
                    Platform.runLater {
                        val first = items.first()
                        val lastIndex = items.lastIndexOf(first)
                        if (lastIndex > 0) items.removeAt(lastIndex)
                        selectionModel.selectFirst()
                    }
                }
            })
            setOnDragOver {
                it.acceptTransferModes(*TransferMode.ANY)
                it.consume()
            }
            setOnDragDropped {
                if (it.dragboard.hasFiles()) {
                    comboBoxPath.items.add(0, it.dragboard.files.first().absolutePath)
                }
                it.isDropCompleted = true
                it.consume()
            }
            valueProperty().addListener { _, _, newValue ->
                if (newValue != null && newValue != selectedTheme?.themePath?.absolutePath) {
                    if (newValue.isNotBlank()) {
                        openTheme(newValue)
                        // selectionModel.clearSelection()
                        // items.add(0, newValue)
                        // val removeIndex = items.lastIndexOf(newValue)
                        // if (removeIndex > 0) items.removeAt(removeIndex)
                    } else {
                        selectedTheme = null
                        refresh()
                    }
                }
            }
        }
        themeSelectHistory.addAll(themeHistory.get(context.defaultProperties()).toStringList())
        if (themeSelectHistory.isNotEmpty()) {
            comboBoxPath.items.addAll(themeSelectHistory)
        }
        toggleButtonTop.selectedProperty().addListener { _, _, newValue ->
            stage.alwaysOnTopProperty()
            stage.isAlwaysOnTop = newValue
            AppPreferences.alwaysOnTop.set(context.defaultProperties(), newValue)
        }
        toggleButtonTop.isSelected = AppPreferences.alwaysOnTop.get(context.defaultProperties())
        refresh()
    }

    private fun refresh() {
        labelTheme.text = selectedTheme?.run { getThemeTitle() } ?: defaultThemeTitle
    }

    @FXML
    fun actionInfo() {
        if (selectedTheme == null) showNotification(Alert.AlertType.WARNING, "请先打开主题") else context.startWindow<BaseController>("/layout/information.fxml", parentStage = stage)
    }

    @FXML
    fun actionApply() {
        getThemeFile()?.let {
            val devices = try {
                DeviceUtils.devices()
            } catch (ignored: Exception) {
                showNotification(Alert.AlertType.WARNING, "ADB未配置，请在设置中尝试修复ADB配置")
                return
            }
            val themeFilePath = it.absolutePath
            if (devices.isEmpty()) {
                val ip = deviceIp.get(context.defaultProperties())
                if (ip.isNotBlank()) {
                    val port = devicePort.get(context.defaultProperties())
                    Single.fromCallable {
                        DeviceUtils.connect(ip, port)
                        DeviceUtils.devices()
                    }.defaultScheduler()
                            .doAfterTerminate { labelStatus.text = null }
                            .doOnSubscribe {
                                progressBar.progress = 1.0
                                labelStatus.text = "连接到 $ip:$port ..."
                            }.subscribe({
                        if (it.isEmpty()) showNotification(Alert.AlertType.WARNING, "没有连接的设备") else applyToDevice(it.first(), themeFilePath)
                    }, { println(it.message) })
                }
            } else if (devices.size > 1) {
                ChoiceDialog<String>(devices.first(), devices).apply {
                    title = "选择设备"
                    headerText = "选择需要应用主题的设备"
                    contentText = "传输主题到设备"
                }.showAndWait().ifPresent { applyToDevice(it, themeFilePath) }
            } else applyToDevice(devices.first(), themeFilePath)
        } ?: showNotification(Alert.AlertType.ERROR, "不是合法的主题路径")
    }

    @FXML
    fun actionExport() {
        val themeFile = getThemeFile() ?: return
        FileChooser().apply {
            title = "导出主题包"
            initialDirectory = themeFile.parentFile
            initialFileName = "${selectedTheme?.getThemeTitle() ?: "未命名"}.mtz"
            extensionFilters.addAll(FileChooser.ExtensionFilter("MIUI主题文件", "*.mtz"))
        }.showSaveDialog(stage)?.let {
            saveMtz(themeFile.absolutePath, it.absolutePath)
        }
    }

    @FXML
    fun actionOpen() {
        selectedTheme?.let {
            context.openUri(it.themePath.absolutePath)
        } ?: DirectoryChooser().apply {
            title = "打开目录"
            try {
                File(comboBoxPath.value).let { if (it.exists()) initialDirectory = it }
            } catch (ignored: Exception) {
            }
        }.showDialog(stage)?.let {
            comboBoxPath.items.add(0, it.absolutePath)
        }
    }

    @FXML
    fun actionClose() {
        stage.close()
    }

    @FXML
    fun actionPreference() {
        context.startWindow<BaseController>("/layout/preference.fxml", parentStage = stage)
    }

    private fun getThemeFile() = try {
        File(comboBoxPath.value)
    } catch (ignored: Exception) {
        Alert(Alert.AlertType.ERROR, "错误").apply {
            title = "错误"
            contentText = "无效的主题目录"
            initOwner(stage)
        }.show()
        null
    }

    private fun applyToDevice(device: String, dirPath: String) {
        val tempMtzFile = File.createTempFile("created", ".mtz")
        saveMtz(dirPath, tempMtzFile.absolutePath, { Platform.runLater { progressBar.progress = it } }).map {
            Platform.runLater { labelStatus.text = "应用主题到手机中..." }
            DeviceUtils.applyTheme(device, tempMtzFile.absolutePath) {
                if (it >= 0) Platform.runLater { progressBar.progress = it }
                false
            }
        }.defaultScheduler().doOnSubscribe { labelStatus.text = "生成临时MTZ包中..." }
                .doAfterTerminate {
                    labelStatus.text = null
                    tempMtzFile.delete()
                }.subscribe({
            if (it) {
                showNotification(Alert.AlertType.INFORMATION, "已应用主题：${selectedTheme?.getThemeTitle()}")
            } else {
                showNotification(Alert.AlertType.ERROR, "应用主题：${selectedTheme?.getThemeTitle()} 失败！")
            }
        }, { println(it.message) })
    }

    private fun saveMtz(dirPath: String, targetFilePath: String, progress: ((Double) -> Unit)?) = Single.fromCallable {
        MtzUtils.compressFileToMtz(dirPath, targetFilePath, progress)
    }

    private fun saveMtz(dirPath: String, targetFilePath: String, showNotification: Boolean = true) {
        saveMtz(dirPath, targetFilePath, { progressBar.progress = it }).defaultScheduler()
                .doOnSubscribe { labelStatus.text = "导出MTZ包中..." }
                .doAfterTerminate { labelStatus.text = null }
                .subscribe({
                    if (showNotification) showNotification(Alert.AlertType.INFORMATION, "导出到：$targetFilePath")
                }, { println(it.message) })
    }

    private fun openTheme(path: String) {
        selectedTheme = MIUITheme.create(path)
        if (selectedTheme != null) {
            showNotification(Alert.AlertType.INFORMATION, "已打开主题")
            if (themeSelectHistory.size < themeHistorySize.get(context.defaultProperties())) {
                val p = selectedTheme?.themePath?.absolutePath ?: path
                themeSelectHistory.apply { removeIf { it == p } }.add(0, p)
                themeHistory.set(context.defaultProperties(), themeSelectHistory.toStringValue())
            }
        } else {
            showNotification(Alert.AlertType.ERROR, "无法找到 ${MIUITheme.THEME_DESCRIPTION_FILE_NAME}")
        }
        refresh()
    }

    private fun showNotification(type: Alert.AlertType, notification: String, duration: Long = defaultNotificationDuration) {
        labelNotification.run {
            text = notification
            id = when (type) {
                Alert.AlertType.ERROR -> "notificationError"
                Alert.AlertType.WARNING -> "notificationWarning"
                else -> "notificationInfo"
            }
            fadeIn()
        }
        if (duration > 0) {
            Single.just(notification).delay(duration, TimeUnit.MILLISECONDS)
                    .observeOn(JavaFxScheduler.platform())
                    .subscribe({ labelNotification.fadeOut() }, { it.printStackTrace() })
        }
    }

    private fun List<String>.toStringValue() = fold(StringBuilder(), { v, e -> v.append(e).append('|') }).run {
        if (length > 0) dropLast(1) else this
    }.toString()
    private fun String.toStringList() = split('|').filter { !it.isBlank() }
    private fun <T> Single<T>.defaultScheduler() = subscribeOn(Schedulers.io()).observeOn(JavaFxScheduler.platform())
}

