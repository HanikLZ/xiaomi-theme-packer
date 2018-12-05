package org.mdvsc.tools.xiaomi.skin

import org.mdvsc.tools.xiaomi.skin.controller.BaseController
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.mdvsc.tools.xiaomi.skin.utils.ResourceUtils
import java.awt.*
import java.awt.event.ActionListener
import java.io.File
import java.util.*
import javax.imageio.ImageIO

/**
 * @author HanikLZ
 * @since 2016/10/28
 */
class ApplicationStarter : Application(), Context {

    companion object {

        lateinit var appStage: Stage

        const val hasSystemTray = false

        val workPath = System.getProperty("user.home")?.run { File(this, "/.xiaomi_skin") } ?: File("data")
        val appProperties = Properties()
        val appPropertiesFile = File(workPath, "preferences.properties")

        @JvmStatic fun main(args: Array<String>) {
            appPropertiesFile.run {
                if (!exists()) {
                    parentFile?.mkdirs()
                    createNewFile()
                }
                inputStream().use { appProperties.load(it) }
            }
            launch(ApplicationStarter::class.java, *args)
        }

    }

    private fun <T : BaseController> Stage.loadLayout(path: String, args: Map<String, String> = emptyMap()): T {
        val loader = FXMLLoader(ResourceUtils.getResourceURL(path))
        scene = Scene(loader.load()).also {
            var sx = 0.0
            var sy = 0.0
            it.onMousePressed = EventHandler<MouseEvent> { event ->
                sx = event.sceneX
                sy = event.sceneY
            }
            it.onMouseDragged = EventHandler<MouseEvent> { event ->
                x = event.screenX - sx
                y = event.screenY - sy
            }
            it.stylesheets.add(ResourceUtils.getResourceURL("/layout/style.css").toExternalForm())
        }
        title = Lang.APP_NAME.lang()
        icons.add(Image(ResourceUtils.getResourceInputStream("/image/icon.png")))
        return loader.getController<T>().apply {
            context = this@ApplicationStarter
            stage = this@loadLayout
            onCreate(args)
            setOnCloseRequest { if (onCloseRequest()) it.consume() }
        }
    }

    override fun defaultProperties() = appProperties

    override fun <T : BaseController> startWindow(path: String, arguments: Map<String, String>, parentStage: Stage?) = Stage().let {
        it.loadLayout<T>(path, arguments).apply {
            it.initOwner(parentStage ?: appStage)
            it.initModality(Modality.WINDOW_MODAL)
            it.show()
        }
    }

    override fun workPath() = workPath

    fun startMainWindow(show: Boolean = false) {
        appStage.loadLayout<BaseController>("/layout/selector.fxml")
        if (if (hasSystemTray && SystemTray.isSupported()) {
            Platform.setImplicitExit(false)
            createSystemTray()
            show
        } else {
            true
        }) appStage.show()
    }

    override fun start(primaryStage: Stage) {
        appStage = primaryStage.apply { initStyle(StageStyle.UNDECORATED) }
        startMainWindow(true)
    }

    override fun stop() {
        super.stop()
        appPropertiesFile.run {
            if (!exists()) {
                parentFile?.mkdirs()
                createNewFile()
            }
            outputStream().use { appProperties.store(it, "app preferences") }
        }
    }

    override fun openUri(url: String) = if (url.contains("://")) hostServices.showDocument(url) else {
        try { File(url).let { if (it.exists() && Desktop.isDesktopSupported()) Desktop.getDesktop().open(it) } } catch (ignored : Exception) { println(ignored.message) }
    }

    fun trayIcon(name : String) = ImageIO.read(ResourceUtils.getResourceURL("/image/icon.png")).run {
        TrayIcon(getScaledInstance(TrayIcon(this).size.width, -1, java.awt.Image.SCALE_SMOOTH), name)
    }

    fun createSystemTray() = SystemTray.getSystemTray().apply {
        add(trayIcon(Lang.APP_NAME.lang()).also {
            val showAppListener = ActionListener { Platform.runLater { appStage.show() } }
            val icon = it
            it.addActionListener(showAppListener)
            it.popupMenu = PopupMenu().apply {
                add(MenuItem("theme".lang()).apply { addActionListener(showAppListener) })
                addSeparator()
                add(MenuItem("about".lang()))
                add(MenuItem("exit".lang()).apply {
                    addActionListener {
                        exit()
                        remove(icon)
                    }
                })
            }
        })
    }!!

    fun exit() = Platform.exit()
}

