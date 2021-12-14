package com.wl;

import com.wl.context.HostServiceContext;
import com.wl.context.PrimaryStageContext;
import com.wl.controller.MainViewController;
import com.wl.utils.FXMLs;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

public class TransportApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        PrimaryStageContext.set(primaryStage);
        HostServiceContext.set(getHostServices());


        //保证窗口关闭后，Stage对象仍然存活
        Platform.setImplicitExit(false);



        //构建系统托盘图标
        BufferedImage image = ImageIO.read(Objects.requireNonNull(TransportApplication.class.getClassLoader().getResourceAsStream("assets/img/prettyzoo-logo.png")));
        TrayIcon trayIcon = new TrayIcon(image, "Transport");
        trayIcon.setImageAutoSize(true);
        //获取系统托盘
        SystemTray tray = SystemTray.getSystemTray();
        //添加鼠标点击事件
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //双击左键
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    Platform.runLater(() -> {
                        if (primaryStage.isIconified()) {
                            primaryStage.setIconified(false);
                        }
                        if (!primaryStage.isShowing()) {
                            primaryStage.show();
                        }
                        primaryStage.toFront();
                    });
                }
                //鼠标右键,关闭应用
                else if (e.getButton() == MouseEvent.BUTTON3) {
                    Platform.setImplicitExit(true);
                    tray.remove(trayIcon);
                    Platform.runLater(primaryStage::close);
                }
            }
        });
        //添加托盘图标
        tray.add(trayIcon);

        MainViewController controller = FXMLs.getController("fxml/MainView.fxml");
        final StackPane stackPane = controller.getRootStackPane();

        primaryStage.setScene(new Scene(stackPane));
        primaryStage.setTitle("Transport");
        getIconStream().ifPresent(stream -> primaryStage.getIcons().add(new Image(stream)));

        primaryStage.show();
    }

    private static Optional<InputStream> getIconStream() {
        InputStream stream = TransportApplication.class.getClassLoader()
                .getSystemResourceAsStream("assets/img/prettyzoo-logo.png");
        return Optional.ofNullable(stream);
    }

}
