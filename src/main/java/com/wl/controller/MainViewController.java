package com.wl.controller;

import com.jfoenix.controls.JFXSlider;
import com.wl.TransportApplication;
import com.wl.config.model.ConfigData;
import com.wl.context.PrimaryStageContext;
import com.wl.context.RootPaneContext;
import com.wl.fp.Try;
import com.wl.listener.DefaultConfigurationListener;
import com.wl.utils.FXMLs;
import com.wl.utils.ResourceBundleUtils;
import com.wl.view.cell.ZkServerListCell;
import com.wl.view.dialog.Dialog;
import com.wl.view.toast.VToast;
import com.wl.vo.ConfigurationVO;
import com.wl.vo.PrettyZooFacade;
import com.wl.vo.ServerConfigurationVO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ResourceBundle;

public class MainViewController {

    @FXML
    private StackPane rootStackPane;

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private AnchorPane mainLeftPane;

    @FXML
    private StackPane mainRightPane;

    @FXML
    private ListView<ServerConfigurationVO> serverListView;

    @FXML
    private HBox serverButtons;

    @FXML
    private Button serverAddButton;

    @FXML
    private MenuItem exportMenuItem;

    @FXML
    private MenuItem importMenuItem;

    @FXML
    private Menu langMenu;

    @FXML
    private MenuButton fontMenuButton;

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private ServerViewController serverViewController = FXMLs.getController("fxml/ServerView.fxml");

    @FXML
    private void initialize() {
        initServerListView();
        RootPaneContext.set(rootStackPane);
        mainRightPane.setPadding(new Insets(30, 30, 30, 30));
        serverAddButton.setOnMouseClicked(event -> {
            serverListView.getSelectionModel().clearSelection();
            serverViewController.show(mainRightPane);
        });
        exportMenuItem.setOnAction(e -> onExportAction());
        importMenuItem.setOnAction(e -> onImportAction());

        serverViewController.setOnClose(() -> this.serverListView.selectionModelProperty().get().clearSelection());
        initFontChangeButton();
    }

    private void initFontChangeButton() {
        Integer fontSize = prettyZooFacade.getFontSize();
        rootStackPane.setStyle("-fx-font-size: " + fontSize);
        JFXSlider jfxSlider = new JFXSlider(8, 25, fontSize);
        jfxSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                rootStackPane.setStyle("-fx-font-size: " + newValue);
                prettyZooFacade.changeFontSize(newValue.intValue());
            }
        }));
        MenuItem jfxSliderItem = new MenuItem("", jfxSlider);
        StringBinding  valueTextBinding  = Bindings.createStringBinding(() -> jfxSlider.valueProperty().intValue() + "",
                jfxSlider.valueProperty());
        jfxSliderItem.textProperty().bind(valueTextBinding);
        fontMenuButton.getItems().add(jfxSliderItem);

        ToggleGroup langToggleGroup = new ToggleGroup();
        for (ConfigData.Lang value : ConfigData.Lang.values()) {
            RadioMenuItem radioMenuItem = new RadioMenuItem(value.getLocale().toLanguageTag());
            radioMenuItem.setId(value.name());
            radioMenuItem.setToggleGroup(langToggleGroup);
            if (prettyZooFacade.getLocale().equals(value.getLocale())) {
                radioMenuItem.setSelected(true);
            }
            langMenu.getItems().add(radioMenuItem);
        }
        langToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioMenuItem item = (RadioMenuItem) newValue;
                final ConfigData.Lang newLang = ConfigData.Lang.valueOf(item.getId());
                prettyZooFacade.updateLocale(newLang);
                ResourceBundle rb = ResourceBundleUtils.get(newLang.getLocale());
                String title = rb.getString("lang.change.confirm.title");
                String content = rb.getString("lang.change.confirm.content");
                Dialog.confirm(title, content, () -> {
                    PrimaryStageContext.get().close();
                    Platform.runLater(() -> {
                        try {
                            new TransportApplication().start(new Stage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                });
            }
        });
    }

    private void onExportAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your target directory");
        fileChooser.setInitialFileName("prettyZoo-config");
        File file = fileChooser.showSaveDialog(PrimaryStageContext.get());
        // configFile is null means click cancel
        if (file == null) {
            return;
        }
        Platform.runLater(() -> Try.of(() -> prettyZooFacade.exportConfig(file))
                .onFailure(e -> VToast.error(e.getMessage())));
    }

    private void onImportAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose config file");
        File configFile = fileChooser.showOpenDialog(PrimaryStageContext.get());
        // configFile is null means click cancel
        if (configFile == null) {
            return;
        }
    }

    private void initServerListView() {
        final ConfigurationVO configurationVO = new ConfigurationVO();
        prettyZooFacade.loadServerConfigurations(new DefaultConfigurationListener(configurationVO));
        serverListView.itemsProperty().set(configurationVO.getServers());
        serverListView.setCellFactory(cellCallback -> {
            ZkServerListCell cell = new ZkServerListCell(
                    server -> serverViewController.connect(mainRightPane, server),
                    server -> serverViewController.delete(server.getZkUrl()),
                    server -> serverViewController.disconnect(server.getZkUrl(),server)
            );
            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    serverViewController.connect(mainRightPane, cell.getItem());
                }
            });
            return cell;
        });
        ReadOnlyObjectProperty<ServerConfigurationVO> selectedItemProperty = serverListView.getSelectionModel().selectedItemProperty();
        selectedItemProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.unbind();
            }
            if (newValue != null) {
                serverViewController.show(mainRightPane, newValue);
            }
        });
    }

    public StackPane getRootStackPane() {
        return rootStackPane;
    }

}
