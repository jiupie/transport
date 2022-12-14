package com.wl.controller;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.wl.fp.Try;
import com.wl.listener.ServerListener;
import com.wl.utils.Asserts;
import com.wl.utils.StringUtil;
import com.wl.validator.NotNullValidator;
import com.wl.validator.PortValidator;
import com.wl.validator.StringNotBlankValidator;
import com.wl.validator.StringNotEmptyValidator;
import com.wl.view.toast.VToast;
import com.wl.vo.PrettyZooFacade;
import com.wl.vo.ServerConfigurationVO;
import com.wl.vo.ServerStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class ServerViewController {

    @FXML
    private AnchorPane serverInfoPane;


    @FXML
    private AnchorPane sshTunnelPane;

    @FXML
    private ProgressBar sshTunnelProgressBarTo;

    @FXML
    private JFXTextField zkHost;

    @FXML
    private JFXTextField zkPort;

    @FXML
    private JFXTextField zkAlias;

    @FXML
    private JFXTextField sshServer;

    @FXML
    private JFXTextField sshServerPort;

    @FXML
    private JFXTextField sshUsername;

    @FXML
    private JFXPasswordField sshPassword;

    @FXML
    private JFXButton sshPasswordVisibleButton;

    @FXML
    private JFXTextField remoteServer;

    @FXML
    private JFXTextField remoteServerPort;

    @FXML
    private Button closeButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button connectButton;

    @FXML
    private HBox buttonHBox;

    private Runnable closeHook;

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    public void show(StackPane stackPane) {
        show(stackPane, null);
    }

    public void show(StackPane stackPane, ServerConfigurationVO config) {
        resetValidate();
        if (config == null) {
            showNewServerView(stackPane);
        } else {
            showServerInfoView(stackPane, config);
        }
    }

    @FXML
    private void initialize() {
        closeButton.setOnMouseClicked(e -> onClose());
        saveButton.setOnMouseClicked(e -> onSave());
        deleteButton.setOnMouseClicked(e -> onDelete());
        serverInfoPane.setEffect(new DropShadow(15, 1, 1, Color.valueOf("#DDD")));

        initPasswordComponent();
        initValidator();
    }


    private void initPasswordComponent() {
        final String originPromptKey = "originPromptText";
        final String originTextKey = "originText";
        sshPasswordVisibleButton.setOnMousePressed(e -> {
            sshPassword.getProperties().put(originPromptKey, sshPassword.getPromptText());
            if (sshPassword.getText() != null && !sshPassword.getText().isEmpty()) {
                sshPassword.getProperties().put(originTextKey, sshPassword.getText());
                sshPassword.promptTextProperty().set(sshPassword.getText());
                sshPassword.setText("");
            }
        });
        sshPasswordVisibleButton.setOnMouseReleased(e -> {
            String originPromptText = ((String) sshPassword.getProperties()
                    .getOrDefault(originPromptKey, ""));
            String originText = ((String) sshPassword.getProperties()
                    .getOrDefault(originTextKey, ""));
            sshPassword.promptTextProperty().set(originPromptText);
            sshPassword.textProperty().set(originText);

            sshPassword.getProperties().remove(originTextKey);
            sshPassword.getProperties().remove(originPromptKey);
        });
    }

    private void initValidator() {
        zkHost.setValidators(new StringNotBlankValidator());
        zkPort.setValidators(new PortValidator());
        zkPort.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals("")) {
                zkPort.validate();
            }
        }));
        zkAlias.setValidators(new StringNotEmptyValidator());

        remoteServer.setValidators(new StringNotBlankValidator());
        remoteServerPort.setValidators(new PortValidator());

        sshServer.setValidators(new StringNotBlankValidator());
        sshServerPort.setValidators(new PortValidator());
        sshUsername.setValidators(new NotNullValidator());
        sshPassword.setValidators(new NotNullValidator());
    }

    public void onClose() {
        final StackPane parent = (StackPane) serverInfoPane.getParent();
        if (parent != null) {
            parent.getChildren().remove(serverInfoPane);
            if (closeHook != null) {
                closeHook.run();
            }
        }
    }


    private void onSave() {
        resetValidate();

        boolean passed = baseValidateBeforeSave();
        if (passed) {
            String serverUrl = zkHost.getText() + ":" + zkPort.getText();
            if (zkHost.isEditable() && prettyZooFacade.hasServerConfiguration(serverUrl)) {
                // new server must be unique
                VToast.error(serverUrl + " already exists");
            } else {
                ServerConfigurationVO serverConfigVO = new ServerConfigurationVO();
                // zookeeper server config
                serverConfigVO.setZkAlias(zkAlias.textProperty().get());
                serverConfigVO.setZkHost(zkHost.textProperty().get());
                serverConfigVO.setZkPort(Integer.parseInt(zkPort.getText()));
                serverConfigVO.setZkUrl(zkHost.getText() + ":" + zkPort.getText());
                // ssh-tunnel config
                serverConfigVO.setRemoteServer(remoteServer.textProperty().get());
                if (!StringUtil.hasText(remoteServerPort.getText())) {
                    serverConfigVO.setRemoteServerPort(null);
                } else {
                    serverConfigVO.setRemoteServerPort(Integer.parseInt(remoteServerPort.getText()));
                }
                serverConfigVO.setSshUsername(sshUsername.textProperty().get());
                serverConfigVO.setSshPassword(sshPassword.textProperty().get());
                serverConfigVO.setSshServer(sshServer.textProperty().get());
                if (!StringUtil.hasText(sshServerPort.getText())) {
                    serverConfigVO.setSshServerPort(null);
                } else {
                    serverConfigVO.setSshServerPort(Integer.parseInt(sshServerPort.getText()));
                }

                Try.of(() -> prettyZooFacade.saveServerConfiguration(serverConfigVO))
                        .onSuccess(e -> {
                            if (zkHost.isEditable()) {
                                onClose();
                            }
                            VToast.info("save success");
                        })
                        .onFailure(e -> {
                            VToast.error(e.getMessage());
                        });
            }
        }
    }

    private boolean baseValidateBeforeSave() {
        return Stream.of(
                zkHost.validate(),
                zkPort.validate(),
                zkAlias.validate(),
                remoteServer.validate(),
                remoteServerPort.validate(),
                sshUsername.validate(),
                sshPassword.validate(),
                sshServer.validate(),
                sshServerPort.validate()
        ).allMatch(t -> t);
    }


    private void onDelete() {
        Asserts.notBlank(zkHost.getText(), "server can not be null");
        Asserts.notBlank(zkPort.getText(), "port can not be null");
        String url = zkHost.getText() + ":" + zkPort.getText();
        prettyZooFacade.deleteServerConfiguration(url);
        if (prettyZooFacade.getServerConfigurations().isEmpty()) {
            onClose();
        }
        VToast.info("Delete success");
    }

    public void delete(String zkServer) {
        prettyZooFacade.deleteServerConfiguration(zkServer);
        if (prettyZooFacade.getServerConfigurations().isEmpty()) {
            onClose();
        }
        VToast.info("Delete success");
    }


    public void disconnect(String server, ServerConfigurationVO serverConfigurationVO) {
        prettyZooFacade.disconnect(server);
        hideAndThen(() -> {
            if (serverConfigurationVO.getStatus() == ServerStatus.CONNECTED) {
                serverConfigurationVO.setStatus(ServerStatus.DISCONNECTED);
            }
            VToast.info("disconnect " + server + " success");
        });
    }

    public void hideAndThen(Runnable runnable) {
        runnable.run();
    }

    private void showNewServerView(StackPane stackPane) {
        zkHost.setEditable(true);
        zkPort.setEditable(true);
        buttonHBox.getChildren().remove(deleteButton);
        buttonHBox.getChildren().remove(connectButton);
        propertyReset();
        switchIfNecessary(stackPane);
        zkHost.requestFocus();
    }

    private void switchIfNecessary(StackPane stackPane) {
        if (!stackPane.getChildren().contains(serverInfoPane)) {
            stackPane.getChildren().add(serverInfoPane);
        } else {
            stackPane.getChildren().remove(serverInfoPane);
            stackPane.getChildren().add(serverInfoPane);
        }
    }


    private void showServerInfoView(StackPane stackPane, ServerConfigurationVO config) {
        if (config.getStatus() == ServerStatus.CONNECTING) {
            buttonHBox.setDisable(true);
        } else if (config.getStatus() == ServerStatus.DISCONNECTED) {
            buttonHBox.setDisable(false);
        }
        zkHost.setEditable(false);
        zkPort.setEditable(false);
        connectButton.setOnMouseClicked(e -> onConnect(stackPane, config));
        propertyBind(config);
        showConnectAndSaveButton();
        switchIfNecessary(stackPane);
    }

    public void connect(StackPane stackPane, ServerConfigurationVO configurationVO) {
        onConnect(stackPane, configurationVO);
    }

    private void onConnect(StackPane parent, ServerConfigurationVO serverConfigurationVO) {
        if (serverConfigurationVO.getStatus() == ServerStatus.CONNECTING) {
            return;
        }
        Try.of(() -> {
            Asserts.notNull(serverConfigurationVO, "save config first");
        }).onSuccess(o -> {
            if (serverConfigurationVO.getStatus() == ServerStatus.DISCONNECTED) {
                serverConfigurationVO.setStatus(ServerStatus.CONNECTING);
            }
            buttonHBox.setDisable(true);
            prettyZooFacade.connect(serverConfigurationVO.getZkUrl(),
                    Arrays.asList(new ServerListener() {
                        @Override
                        public void onClose(String serverUrl) {
                            if (serverUrl.equals(serverConfigurationVO.getZkUrl())) {
                                Platform.runLater(() -> {
                                    serverConfigurationVO.setStatus(ServerStatus.DISCONNECTED);
                                    if (closeHook != null) {
                                        closeHook.run();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onReconnecting(String serverHost) {
                            if (serverHost.equals(serverConfigurationVO.getZkUrl())) {
                                Platform.runLater(() -> {
                                    serverConfigurationVO.setStatus(ServerStatus.RECONNECTING);
                                    VToast.error(serverHost + " lost connection");
                                });
                            }
                        }

                        @Override
                        public void onConnected(String serverHost) {
                            if (serverHost.equals(serverConfigurationVO.getZkUrl())) {
                                Platform.runLater(() -> {
                                    if (serverConfigurationVO.getStatus() == ServerStatus.RECONNECTING) {
                                        VToast.info("reconnect " + serverHost + " success");
                                    }
                                    serverConfigurationVO.setStatus(ServerStatus.CONNECTED);
                                });
                            }
                        }
                    }))
                    .thenAccept(v -> connectSuccessCallback(parent, serverConfigurationVO))
                    .exceptionally(e -> connectErrorCallback(e, serverConfigurationVO));
        }).onFailure(e -> {
            log.error("connect server error", e);
            VToast.error(e.getMessage());
        });
    }


    private void connectSuccessCallback(StackPane parent,
                                        ServerConfigurationVO serverConfigurationVO) {
        Platform.runLater(() -> {
            parent.getChildren().remove(serverInfoPane);

            if (serverConfigurationVO.getStatus() == ServerStatus.CONNECTING) {
                serverConfigurationVO.setStatus(ServerStatus.CONNECTED);
            }
            buttonHBox.setDisable(false);
        });
    }


    private Void connectErrorCallback(Throwable e, ServerConfigurationVO serverConfigurationVO) {
        log.error("connect server error", e);
        Platform.runLater(() -> {
            buttonHBox.setDisable(false);
            serverConfigurationVO.setStatus(ServerStatus.DISCONNECTED);
            VToast.error(e.getCause().getMessage());
        });
        return null;
    }


    private void showConnectAndSaveButton() {
        if (!buttonHBox.getChildren().contains(connectButton)) {
            buttonHBox.getChildren().add(connectButton);
        }
        if (!buttonHBox.getChildren().contains(deleteButton)) {
            buttonHBox.getChildren().add(deleteButton);
        }
    }

    private void propertyReset() {
        zkHost.textProperty().unbind();
        zkHost.textProperty().setValue("");
        zkPort.textProperty().setValue("");
        zkAlias.textProperty().setValue("");
        sshServer.textProperty().setValue("");
        sshUsername.textProperty().setValue("");
        sshPassword.textProperty().setValue("");
        remoteServer.textProperty().setValue("");
    }

    private void propertyBind(ServerConfigurationVO config) {
        zkHost.textProperty().setValue(config.getZkHost());
        zkPort.textProperty().setValue(config.getZkPort() + "");
        zkAlias.textProperty().setValue(config.getZkAlias());
        sshServer.textProperty().setValue(config.getSshServer());
        sshServerPort.textProperty().setValue(Objects.toString(config.getSshServerPort(), ""));
        sshUsername.textProperty().setValue(config.getSshUsername());
        sshPassword.textProperty().setValue(config.getSshPassword());
        remoteServer.textProperty().setValue(config.getRemoteServer());
        remoteServerPort.textProperty().setValue(Objects.toString(config.getRemoteServerPort(), ""));
    }

    private void resetValidate() {
        zkHost.resetValidation();
        zkPort.resetValidation();
        zkAlias.resetValidation();
        remoteServer.resetValidation();
        remoteServerPort.resetValidation();
        sshUsername.resetValidation();
        sshPassword.resetValidation();
        sshServer.resetValidation();
        sshServerPort.resetValidation();
    }

    public void setOnClose(Runnable runnable) {
        this.closeHook = runnable;
    }

}
