import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EmailClient extends Application {

    private DataService dataService;
    private User currentUser = null;

    // UI Listeleri
    private ObservableList<Email> inboxEmails = FXCollections.observableArrayList();
    private ObservableList<Email> outboxEmails = FXCollections.observableArrayList();
    private ObservableList<Email> trashEmails = FXCollections.observableArrayList(); // Çöp Kutusu Listesi

    // Varsayılan Tema: Kırmızı-Beyaz
    private Theme currentTheme = Theme.RED_WHITE;

    // 4 farklı canvas (scene)
    private Scene loginScene;
    private Scene signupScene;
    private Scene mainScene;
    private Scene rememberedAccountsScene;
    private Stage primaryStage;

    // UI bileşenleri - Ana Ekran
    private ImageView profileImageView;      // Signup ekranındaki
    private ImageView mainProfileImageView;  // Main ekrandaki
    private ListView<Email> emailListView;
    private TextArea emailContentArea;
    private BorderPane mainScreen;

    // UI Bileşenleri - Giriş/Kayıt/Menü referansları (Tema değişimi için global tanımlı)
    private Label loginBoosLabel, loginTLabel, loginMailLabel, loginSubtitle, inboxCount;
    private TextField loginUsernameField, loginPasswordTextField, signupUsernameField, signupPasswordTextField;
    private PasswordField loginPasswordField, signupPasswordField;
    private Button loginLoginButton, loginToggleButton, signupSignupButton, signupSelectImgButton, signupToggleButton;
    private CheckBox loginRememberMeCheckbox, signupRememberMeCheckbox;
    private Hyperlink loginSignUpLink, loginRememberedLink, loginForgotPasswordLink, signupLoginLink;
    private Label loginDividerLabel, loginDividerLabel2, signupTitleLabel, signupSubtitleLabel;
    private Label remTitleLabel, remSubtitleLabel, remNoAccountsLabel;
    private VBox accountsBox, loginScreen, signinScreen, rememberedScreen;
    private Button remBackButton, inboxButton, outboxButton, composeButton, starredButton, trashButton, settingsButton;
    private HBox topBar;
    private VBox menuBox;
    private Label welcomeLabel;

    @Override
    public void start(Stage primaryStage) {
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            // İkon bulunamazsa devam et
        }

        this.primaryStage = primaryStage;

        // Veri Servisini Başlat (Eski 'verileriYukle' metodlarının yerini aldı)
        this.dataService = new DataService();

        createLoginScene();
        createSignupScene();
        createRememberedAccountsScene();

        primaryStage.setTitle("BoosTMail");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    // --- YARDIMCI GÖRSEL METODLAR ---

    private VBox createTurkishFlag() {
        VBox flag = new VBox();
        flag.setPrefSize(60, 40);
        flag.setStyle("-fx-background-color: #8B0000;"); // Koyu Kırmızı

        Label symbol = new Label("☾ ★");
        symbol.setTextFill(Color.WHITE);
        symbol.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        symbol.setStyle("-fx-padding: 5;");

        flag.getChildren().add(symbol);
        flag.setAlignment(Pos.CENTER);
        return flag;
    }

    private void setDefaultIcon(ImageView imageView) {
        try {
            var res = getClass().getResourceAsStream("/images/default-profile.png");
            if (res != null) {
                imageView.setImage(new Image(res));
            }
        } catch (Exception e) {
            // System.err.println("Varsayılan ikon yüklenirken hata");
        }
    }

    // --- SAHNE OLUŞTURMA: LOGIN (GİRİŞ) ---

    private void createLoginScene() {
        loginScreen = new VBox(20);
        loginScreen.setPadding(new Insets(40));
        loginScreen.setAlignment(Pos.CENTER);

        VBox flag = createTurkishFlag();

        HBox titleBox = new HBox(0);
        titleBox.setAlignment(Pos.CENTER);

        loginBoosLabel = new Label("Boos");
        loginBoosLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        loginTLabel = new Label("T");
        loginTLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        loginMailLabel = new Label("Mail");
        loginMailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleBox.getChildren().addAll(loginBoosLabel, loginTLabel, loginMailLabel);

        loginSubtitle = new Label("BoosTMail - Giriş");
        loginSubtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        loginUsernameField = new TextField();
        loginUsernameField.setPromptText("E-mail adresi");
        loginUsernameField.setMaxWidth(300);

        HBox passwordBox = new HBox(10);
        passwordBox.setAlignment(Pos.CENTER);
        passwordBox.setMaxWidth(300);

        loginPasswordField = new PasswordField();
        loginPasswordField.setPromptText("Şifre");
        loginPasswordField.setPrefWidth(250);

        loginPasswordTextField = new TextField();
        loginPasswordTextField.setPromptText("Şifre");
        loginPasswordTextField.setPrefWidth(250);
        loginPasswordTextField.setVisible(false);
        loginPasswordTextField.setManaged(false);

        loginToggleButton = new Button("👁");
        loginToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px;");

        final boolean[] passwordVisible = {false};
        loginToggleButton.setOnAction(e -> {
            passwordVisible[0] = !passwordVisible[0];
            if (passwordVisible[0]) {
                loginPasswordTextField.setText(loginPasswordField.getText());
                loginPasswordField.setVisible(false);
                loginPasswordField.setManaged(false);
                loginPasswordTextField.setVisible(true);
                loginPasswordTextField.setManaged(true);
            } else {
                loginPasswordField.setText(loginPasswordTextField.getText());
                loginPasswordTextField.setVisible(false);
                loginPasswordTextField.setManaged(false);
                loginPasswordField.setVisible(true);
                loginPasswordField.setManaged(true);
            }
        });

        StackPane passwordStack = new StackPane(loginPasswordField, loginPasswordTextField);
        passwordBox.getChildren().addAll(passwordStack, loginToggleButton);

        loginRememberMeCheckbox = new CheckBox("Beni Hatırla");
        loginRememberMeCheckbox.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        HBox linksBox = new HBox(15);
        linksBox.setAlignment(Pos.CENTER);

        loginSignUpLink = new Hyperlink("Kayıt Ol");
        loginSignUpLink.setOnAction(e -> primaryStage.setScene(signupScene));

        loginRememberedLink = new Hyperlink("Kayıtlı Hesaplar");
        loginRememberedLink.setOnAction(e -> {
            createRememberedAccountsScene();
            primaryStage.setScene(rememberedAccountsScene);
        });

        loginDividerLabel = new Label("|");
        loginDividerLabel2 = new Label("|");

        loginForgotPasswordLink = new Hyperlink("Şifremi Unuttum");
        loginForgotPasswordLink.setOnAction(e -> handleForgotPassword());

        linksBox.getChildren().addAll(loginSignUpLink, loginDividerLabel, loginRememberedLink, loginDividerLabel2, loginForgotPasswordLink);

        loginLoginButton = new Button("Giriş Yap");
        loginLoginButton.setPrefWidth(150);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        loginLoginButton.setOnAction(e -> {
            String username = loginUsernameField.getText();
            String password = passwordVisible[0] ? loginPasswordTextField.getText() : loginPasswordField.getText();

            User user = dataService.authenticateUser(username, password);
            if (user != null) {
                currentUser = user;
                // Listeleri doldur
                inboxEmails.setAll(user.getInbox());
                outboxEmails.setAll(user.getOutbox());
                trashEmails.setAll(user.getTrash());

                if (loginRememberMeCheckbox.isSelected() && !dataService.getRememberedUsers().contains(username)) {
                    dataService.getRememberedUsers().add(username);
                    dataService.saveRememberedUsers();
                }
                createMainScene();
                primaryStage.setScene(mainScene);
                loginUsernameField.clear();
                loginPasswordField.clear();
            } else {
                errorLabel.setText("Hatalı kullanıcı adı veya şifre!");
            }
        });

        loginScreen.getChildren().addAll(flag, titleBox, loginSubtitle, loginUsernameField, passwordBox, loginRememberMeCheckbox, linksBox, loginLoginButton, errorLabel);
        loginScene = new Scene(loginScreen, 1000, 650);
        applyTheme();
    }

    // --- SAHNE OLUŞTURMA: SIGNUP (KAYIT) ---

    private void createSignupScene() {
        signinScreen = new VBox(20);
        signinScreen.setPadding(new Insets(40));
        signinScreen.setAlignment(Pos.CENTER);

        VBox flag = createTurkishFlag();

        signupTitleLabel = new Label("BoosTMail");
        signupTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        signupSubtitleLabel = new Label("BoosTMail - Kayıt");
        signupSubtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        profileImageView = new ImageView();
        profileImageView.setFitWidth(120);
        profileImageView.setFitHeight(90);
        profileImageView.setPreserveRatio(false);

        Circle clip = new Circle(60, 45, 45);
        profileImageView.setClip(clip);
        setDefaultIcon(profileImageView);

        signupSelectImgButton = new Button("Profil Fotoğrafı Seç");
        signupSelectImgButton.setOnAction(e -> selectProfileImage(profileImageView));

        signupUsernameField = new TextField();
        signupUsernameField.setPromptText("E-mail adresi");
        signupUsernameField.setMaxWidth(300);

        HBox passwordBox = new HBox(10);
        passwordBox.setAlignment(Pos.CENTER);
        passwordBox.setMaxWidth(300);

        signupPasswordField = new PasswordField();
        signupPasswordField.setPromptText("Şifre");
        signupPasswordField.setPrefWidth(250);

        signupPasswordTextField = new TextField();
        signupPasswordTextField.setPromptText("Şifre");
        signupPasswordTextField.setPrefWidth(250);
        signupPasswordTextField.setVisible(false);
        signupPasswordTextField.setManaged(false);

        signupToggleButton = new Button("👁");
        signupToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px;");

        final boolean[] passwordVisible = {false};
        signupToggleButton.setOnAction(e -> {
            passwordVisible[0] = !passwordVisible[0];
            if (passwordVisible[0]) {
                signupPasswordTextField.setText(signupPasswordField.getText());
                signupPasswordField.setVisible(false);
                signupPasswordField.setManaged(false);
                signupPasswordTextField.setVisible(true);
                signupPasswordTextField.setManaged(true);
            } else {
                signupPasswordField.setText(signupPasswordTextField.getText());
                signupPasswordTextField.setVisible(false);
                signupPasswordTextField.setManaged(false);
                signupPasswordField.setVisible(true);
                signupPasswordField.setManaged(true);
            }
        });

        StackPane passwordStack = new StackPane(signupPasswordField, signupPasswordTextField);
        passwordBox.getChildren().addAll(passwordStack, signupToggleButton);

        signupRememberMeCheckbox = new CheckBox("Beni Hatırla");
        signupRememberMeCheckbox.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        signupLoginLink = new Hyperlink("Giriş Yap");
        signupLoginLink.setOnAction(e -> primaryStage.setScene(loginScene));

        signupSignupButton = new Button("Kayıt Ol");
        signupSignupButton.setPrefWidth(150);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        signupSignupButton.setOnAction(event -> {
            String username = signupUsernameField.getText().trim();
            String password = passwordVisible[0] ? signupPasswordTextField.getText() : signupPasswordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Lütfen tüm alanları doldurun!");
                return;
            }

            if (dataService.findUserByEmail(username) != null) {
                errorLabel.setText("Bu e-mail adresi zaten kayıtlı!");
                return;
            }

            User user = new User(username, password);

            if (profileImageView.getImage() != null) {
                try {
                    String base64Image = ImageHelper.imageToBase64(profileImageView.getImage());
                    user.setProfileImage(base64Image);
                } catch (Exception e) {
                    System.err.println("Profil fotoğrafı kaydedilemedi: " + e.getMessage());
                }
            }

            user.addEmail(new Email("admin@boostmail.tr", user.getUsername(), "Hoş Geldiniz", "BoosTMail - hoş geldiniz!"));
            dataService.getUsers().add(user);
            dataService.saveUsers();

            if (signupRememberMeCheckbox.isSelected() && !dataService.getRememberedUsers().contains(username)) {
                dataService.getRememberedUsers().add(username);
                dataService.saveRememberedUsers();
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Kayıt Başarılı");
            alert.setHeaderText("Başarıyla kayıt oldunuz!");
            alert.setContentText("Bu hesap ile giriş yapmak ister misiniz?");

            ButtonType yesButton = new ButtonType("Evet", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("Hayır", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                    currentUser = user;
                    inboxEmails.setAll(user.getInbox());
                    outboxEmails.setAll(user.getOutbox());
                    trashEmails.setAll(user.getTrash());
                    createMainScene();
                    primaryStage.setScene(mainScene);
                } else {
                    primaryStage.setScene(loginScene);
                }
                signupUsernameField.clear();
                signupPasswordField.clear();
            });
        });

        signinScreen.getChildren().addAll(flag, signupTitleLabel, signupSubtitleLabel, profileImageView, signupSelectImgButton, signupUsernameField, passwordBox, signupRememberMeCheckbox, signupLoginLink, signupSignupButton, errorLabel);
        signupScene = new Scene(signinScreen, 1000, 650);
        applyTheme();
    }

    // --- SAHNE OLUŞTURMA: REMEMBERED ACCOUNTS (HATIRLANANLAR) ---

    private void createRememberedAccountsScene() {
        rememberedScreen = new VBox(20);
        rememberedScreen.setPadding(new Insets(40));
        rememberedScreen.setAlignment(Pos.CENTER);

        VBox flag = createTurkishFlag();

        remTitleLabel = new Label("Kayıtlı Hesaplar");
        remTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        remSubtitleLabel = new Label("Bir hesap seçin");
        remSubtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPrefHeight(350);

        accountsBox = new VBox(15);
        accountsBox.setAlignment(Pos.CENTER);
        accountsBox.setPadding(new Insets(20));

        if (dataService.getRememberedUsers().isEmpty()) {
            remNoAccountsLabel = new Label("Kayıtlı hesap bulunamadı.");
            remNoAccountsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            accountsBox.getChildren().add(remNoAccountsLabel);
        } else {
            for (String email : dataService.getRememberedUsers()) {
                HBox accountBox = new HBox(15);
                accountBox.setAlignment(Pos.CENTER_LEFT);
                accountBox.setPrefWidth(450);

                ImageView accountImageView = new ImageView();
                accountImageView.setFitWidth(50);
                accountImageView.setFitHeight(37.5);
                accountImageView.setPreserveRatio(false);

                Circle accountClip = new Circle(25, 18.75, 18.75);
                accountImageView.setClip(accountClip);

                User u = dataService.findUserByEmail(email);
                if (u != null && u.getProfileImage() != null && !u.getProfileImage().isEmpty()) {
                    Image profileImg = ImageHelper.base64ToImage(u.getProfileImage());
                    if (profileImg != null) {
                        accountImageView.setImage(profileImg);
                    } else {
                        setDefaultIcon(accountImageView);
                    }
                } else {
                    setDefaultIcon(accountImageView);
                }

                Label emailLabel = new Label(email);
                emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                emailLabel.setPrefWidth(250);

                Button loginBtn = new Button("Giriş Yap");
                loginBtn.setPrefWidth(100);

                Button removeBtn = new Button("🗑");
                removeBtn.setTooltip(new Tooltip("Hesabı kaldır"));

                loginBtn.setOnAction(e -> {
                    Dialog<String> dialog = new Dialog<>();
                    dialog.setTitle("Şifre Gerekli");
                    dialog.setHeaderText(email + " için şifre girin:");
                    ButtonType loginButtonType = new ButtonType("Giriş", ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    PasswordField passwordField = new PasswordField();
                    passwordField.setPromptText("Şifre");

                    // Şifre göster/gizle toggle
                    TextField textField = new TextField();
                    textField.setPromptText("Şifre");
                    textField.setVisible(false);
                    textField.setManaged(false);
                    Button toggleButton = new Button("👁");
                    toggleButton.setOnAction(ev -> {
                        if (textField.isVisible()) {
                            passwordField.setText(textField.getText());
                            textField.setVisible(false); textField.setManaged(false);
                            passwordField.setVisible(true); passwordField.setManaged(true);
                        } else {
                            textField.setText(passwordField.getText());
                            passwordField.setVisible(false); passwordField.setManaged(false);
                            textField.setVisible(true); textField.setManaged(true);
                        }
                    });

                    StackPane fieldStack = new StackPane(passwordField, textField);
                    hbox.getChildren().addAll(new Label("Şifre:"), fieldStack, toggleButton);
                    dialog.getDialogPane().setContent(hbox);

                    dialog.setResultConverter(dialogButton -> {
                        if (dialogButton == loginButtonType) {
                            return textField.isVisible() ? textField.getText() : passwordField.getText();
                        }
                        return null;
                    });

                    dialog.showAndWait().ifPresent(password -> {
                        User authenticated = dataService.authenticateUser(email, password);
                        if (authenticated != null) {
                            currentUser = authenticated;
                            inboxEmails.setAll(currentUser.getInbox());
                            outboxEmails.setAll(currentUser.getOutbox());
                            trashEmails.setAll(currentUser.getTrash());
                            createMainScene();
                            primaryStage.setScene(mainScene);
                        } else {
                            showAlert("Hata", "Hatalı şifre!");
                        }
                    });
                });

                removeBtn.setOnAction(e -> {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Hesap Kaldır");
                    confirmAlert.setHeaderText("Bu hesabı kayıtlı hesaplardan kaldırmak istediğinize emin misiniz?");
                    confirmAlert.setContentText(email);

                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            dataService.getRememberedUsers().remove(email);
                            dataService.saveRememberedUsers();
                            createRememberedAccountsScene();
                            primaryStage.setScene(rememberedAccountsScene);
                        }
                    });
                });

                accountBox.getChildren().addAll(accountImageView, emailLabel, loginBtn, removeBtn);
                accountsBox.getChildren().add(accountBox);
            }
        }

        scrollPane.setContent(accountsBox);

        remBackButton = new Button("← Geri");
        remBackButton.setPrefWidth(150);
        remBackButton.setOnAction(e -> primaryStage.setScene(loginScene));

        rememberedScreen.getChildren().addAll(flag, remTitleLabel, remSubtitleLabel, scrollPane, remBackButton);
        rememberedAccountsScene = new Scene(rememberedScreen, 1000, 650);
        applyTheme();
    }

    // --- SAHNE OLUŞTURMA: MAIN SCREEN (ANA EKRAN) ---

    private void createMainScene() {
        mainScreen = new BorderPane();

        FXCollections.reverse(inboxEmails);
        FXCollections.reverse(outboxEmails);
        FXCollections.reverse(trashEmails);
        topBar = new HBox(15);
        topBar.setPadding(new Insets(7));
        topBar.setAlignment(Pos.CENTER_LEFT);

        mainProfileImageView = new ImageView();
        mainProfileImageView.setFitWidth(60);
        mainProfileImageView.setFitHeight(45);
        mainProfileImageView.setPreserveRatio(false);

        Circle clip = new Circle(30, 22.5, 22.5);
        mainProfileImageView.setClip(clip);

        if (currentUser != null) {
            if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
                Image profileImage = ImageHelper.base64ToImage(currentUser.getProfileImage());
                if (profileImage != null) {
                    mainProfileImageView.setImage(profileImage);
                } else {
                    setDefaultIcon(mainProfileImageView);
                }
            } else {
                setDefaultIcon(mainProfileImageView);
            }
        }

        mainProfileImageView.setOnMouseClicked(e -> showSettingsScreen()); // Profil resmine basınca ayarlara git
        mainProfileImageView.setStyle("-fx-cursor: hand;");

        welcomeLabel = new Label("Hoş Geldiniz: " + currentUser.getUsername());
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button themeButton = new Button("Tema: Kırmızı-Beyaz");
        themeButton.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
        themeButton.setOnAction(e -> cycleTheme(themeButton));

        Button logoutButton = new Button("Çıkış Yap");
        logoutButton.setStyle("-fx-background-color: white; -fx-text-fill: #E30A17; -fx-font-weight: bold;");
        logoutButton.setOnAction(e -> primaryStage.setScene(loginScene));

        topBar.getChildren().addAll(mainProfileImageView, welcomeLabel, spacer, themeButton, logoutButton);
        mainScreen.setTop(topBar);

        menuBox = new VBox(10);
        menuBox.setPadding(new Insets(20));
        menuBox.setPrefWidth(200);

        inboxButton = new Button("Gelen Kutusu");
        inboxButton.setPrefWidth(180);
        inboxButton.setOnAction(e -> showInbox());

        outboxButton = new Button("Gönderilenler Kutusu");
        outboxButton.setPrefWidth(180);
        outboxButton.setOnAction(e -> showOutbox());

        composeButton = new Button("Yeni E-mail");
        composeButton.setPrefWidth(180);
        composeButton.setOnAction(e -> showComposeScreen("", "", ""));

        starredButton = new Button("⭐ Yıldızlı mailler");
        starredButton.setPrefWidth(180);
        starredButton.setOnAction(e -> showStarredEmails());

        // Çöp Kutusu Butonu
        trashButton = new Button("🗑 Çöp Kutusu");
        trashButton.setPrefWidth(180);
        trashButton.setOnAction(e -> showTrash());

        settingsButton = new Button("⚙ Profil Düzenle");
        settingsButton.setPrefWidth(180);
        settingsButton.setOnAction(e -> showSettingsScreen());

        inboxCount = new Label("Gelen Kutusu: " + inboxEmails.size() + " e-mail");
        inboxCount.setTextFill(Color.WHITE);

        menuBox.getChildren().addAll(inboxButton, outboxButton, composeButton, starredButton, trashButton, settingsButton, inboxCount);
        mainScreen.setLeft(menuBox);

        showInbox();
        applyTheme();

        // Temayı düzelt ve buton metnini güncelle
        switch (currentTheme) {
            case RED_WHITE: themeButton.setText("Tema: Kırmızı-Beyaz"); break;
            case DARK: themeButton.setText("Tema: Karanlık"); break;
            case LIGHT: themeButton.setText("Tema: Aydınlık"); break;
        }

        mainScene = new Scene(mainScreen, 1000, 650);
    }

    private void showInbox() {
        VBox centerBox = new VBox();
        Label inboxTitle = new Label("Gelen Kutusu");
        inboxTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        inboxTitle.setPadding(new Insets(10));
        inboxTitle.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);

        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px;");
        refreshBtn.setTooltip(new Tooltip("Yenile"));
        refreshBtn.setOnAction(e -> {
            refreshData();
            emailListView.refresh();
            applyTheme();
            showInbox();
        });
        inboxTitle.setGraphic(refreshBtn);
        inboxTitle.setContentDisplay(ContentDisplay.RIGHT);

        emailListView = new ListView<>(inboxEmails);
        setupEmailListView(emailListView, false, false);

        setupEmailContentArea(centerBox, inboxTitle, emailListView);
        mainScreen.setCenter(centerBox);
    }

    private void showOutbox() {
        VBox centerBox = new VBox();
        Label outboxTitle = new Label("Gönderilenler Kutusu");
        outboxTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        outboxTitle.setPadding(new Insets(10));
        outboxTitle.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);

        emailListView = new ListView<>(outboxEmails);
        setupEmailListView(emailListView, true, false);

        setupEmailContentArea(centerBox, outboxTitle, emailListView);
        mainScreen.setCenter(centerBox);
    }

    private void showTrash() {
        VBox centerBox = new VBox();
        Label trashTitle = new Label("Çöp Kutusu");
        trashTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        trashTitle.setPadding(new Insets(10));
        trashTitle.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);

        Button emptyTrashBtn = new Button("Çöpü Boşalt");
        emptyTrashBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;");
        emptyTrashBtn.setOnAction(e -> {
            currentUser.getTrash().clear();
            trashEmails.clear();
            dataService.saveUsers();
            if(emailContentArea != null) emailContentArea.clear();
        });
        trashTitle.setGraphic(emptyTrashBtn);
        trashTitle.setContentDisplay(ContentDisplay.RIGHT);

        emailListView = new ListView<>(trashEmails);
        setupEmailListView(emailListView, false, true); // isTrash = true

        // Özel içerik görüntüleme (zaman damgası ile)
        emailContentArea = new TextArea();
        emailContentArea.setEditable(false);
        emailContentArea.setWrapText(true);
        emailContentArea.setPrefHeight(300);
        styleContentArea(emailContentArea);

        emailListView.getSelectionModel().selectedItemProperty().addListener((obs, oldEmail, newEmail) -> {
            if (newEmail != null) {
                // Zaman hesaplama ve silme kontrolü
                long now = System.currentTimeMillis();
                long deletedTime = newEmail.getDeletionTimestamp();
                long remaining = (30L * 24 * 60 * 60 * 1000) - (now - deletedTime);

                if (remaining <= 0) {
                    currentUser.getTrash().remove(newEmail);
                    trashEmails.remove(newEmail);
                    dataService.saveUsers();
                    emailContentArea.clear();
                    showAlert("Bilgi", "Süresi dolan mesaj otomatik olarak silindi.");
                    return;
                }

                String timeInfo = calculateTimeInfo(remaining);
                String content = "--- SİLİNMİŞ MESAJ ---\n" + timeInfo +
                        "Kimden: " + newEmail.getSender() + "\n" +
                        "Konu: " + newEmail.getSubject() + "\n\n" +
                        newEmail.getContent();
                emailContentArea.setText(content);
            }
        });

        centerBox.getChildren().addAll(trashTitle, emailListView, emailContentArea);
        mainScreen.setCenter(centerBox);
    }

    private void showStarredEmails() {
        VBox centerBox = new VBox();
        Label starredTitle = new Label("🌟 Yıldızlı Mesajlar");
        starredTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        starredTitle.setPadding(new Insets(10));
        starredTitle.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);

        // Filtrele
        ObservableList<Email> starredList = FXCollections.observableArrayList();
        starredList.addAll(currentUser.getInbox().stream().filter(Email::isStarred).collect(Collectors.toList()));
        starredList.addAll(currentUser.getOutbox().stream().filter(Email::isStarred).collect(Collectors.toList()));
        FXCollections.reverse(starredList);
        ListView<Email> starredListView = new ListView<>(starredList);
        setupEmailListView(starredListView, false, false); // Basit görünüm

        setupEmailContentArea(centerBox, starredTitle, starredListView);
        mainScreen.setCenter(centerBox);
    }

    private void showComposeScreen(String to, String subject, String body) {
        VBox composeBox = new VBox(15);
        composeBox.setPadding(new Insets(20));

        Label composeTitle = new Label("Yeni E-mail");
        composeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        composeTitle.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);

        TextField toField = new TextField(to);
        toField.setPromptText("Kime (E-mail adresi)");
        TextField subjectField = new TextField(subject);
        subjectField.setPromptText("Konu");
        TextArea messageArea = new TextArea(body);
        messageArea.setPromptText("E-mail içeriğinizi buraya yazın...");
        messageArea.setPrefHeight(300);
        messageArea.setWrapText(true);

        if (currentTheme == Theme.DARK) {
            String darkInput = "-fx-control-inner-background: #333333; -fx-text-fill: white; -fx-prompt-text-fill: gray;";
            toField.setStyle(darkInput);
            subjectField.setStyle(darkInput);
            messageArea.setStyle(darkInput);
        }

        Button sendButton = new Button("Gönder");
        sendButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        Button cancelButton = new Button("İptal");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");

        sendButton.setOnAction(e -> {
            String recipient = toField.getText();
            String subj = subjectField.getText();
            String content = messageArea.getText();

            if (recipient.isEmpty() || subj.isEmpty() || content.isEmpty()) {
                showAlert("Hata", "Lütfen tüm alanları doldurun!");
                return;
            }

            Email newEmail = new Email(currentUser.getUsername(), recipient, subj, content);
            currentUser.addEmailtoOutbox(newEmail);

            // Alıcıyı bul ve ona da ekle (Simülasyon)
            User receiver = dataService.findUserByEmail(recipient);
            if (receiver != null) {
                receiver.addEmail(newEmail);
                showAlert("Başarılı", "E-mail başarıyla gönderildi!");
            } else {
                showAlert("Bilgi", "E-mail gönderildi (alıcı sistemde kayıtlı değil, simülasyon amaçlı)");
            }
            dataService.saveUsers();
            showInbox();
        });

        cancelButton.setOnAction(e -> showInbox());

        HBox btns = new HBox(10, sendButton, cancelButton);
        composeBox.getChildren().addAll(composeTitle, toField, subjectField, messageArea, btns);
        mainScreen.setCenter(composeBox);
    }

    private void showSettingsScreen() {
        VBox settingsBox = new VBox(20);
        settingsBox.setPadding(new Insets(20));
        settingsBox.setAlignment(Pos.TOP_CENTER);

        Label settingsTitle = new Label("Profil Düzenle");
        settingsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        settingsTitle.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);

        ImageView profileView = new ImageView();
        profileView.setFitWidth(120);
        profileView.setFitHeight(90);
        profileView.setPreserveRatio(false);
        Circle clip = new Circle(60, 45, 45);
        profileView.setClip(clip);

        if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            profileView.setImage(ImageHelper.base64ToImage(currentUser.getProfileImage()));
        } else {
            setDefaultIcon(profileView);
        }

        Button changeImgBtn = new Button("Fotoğrafı Değiştir");
        changeImgBtn.setOnAction(e -> {
            selectProfileImage(mainProfileImageView);
            // Main'i güncelledik, buradaki view'ı da güncelle
            if (currentUser.getProfileImage() != null) {
                profileView.setImage(ImageHelper.base64ToImage(currentUser.getProfileImage()));
            }
        });

        Label userLabel = new Label("Kullanıcı Adı: " + currentUser.getUsername());
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        userLabel.setTextFill(currentTheme == Theme.DARK ? Color.LIGHTGRAY : Color.DARKGRAY);

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Yeni Şifre (Değiştirmek istemiyorsanız boş bırakın)");
        newPassField.setMaxWidth(300);
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Yeni Şifre (Tekrar)");
        confirmPassField.setMaxWidth(300);

        if (currentTheme == Theme.DARK) {
            String darkInput = "-fx-control-inner-background: #333333; -fx-text-fill: white;";
            newPassField.setStyle(darkInput);
            confirmPassField.setStyle(darkInput);
        }

        Button saveButton = new Button("Kaydet");
        saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        saveButton.setPrefWidth(150);
        saveButton.setOnAction(e -> {
            String newPass = newPassField.getText();
            String confirmPass = confirmPassField.getText();
            if (!newPass.isEmpty()) {
                if (newPass.equals(confirmPass)) {
                    currentUser.setPassword(newPass);
                    dataService.saveUsers();
                    showAlert("Başarılı", "Profil ve şifre güncellendi!");
                    newPassField.clear(); confirmPassField.clear();
                } else {
                    showAlert("Hata", "Şifreler eşleşmiyor!");
                }
            } else {
                showAlert("Bilgi", "Değişiklikler kaydedildi.");
            }
        });

        settingsBox.getChildren().addAll(settingsTitle, profileView, changeImgBtn, userLabel, newPassField, confirmPassField, saveButton);
        mainScreen.setCenter(settingsBox);
    }

    // --- ORTAK LİSTE VE İÇERİK MANTIĞI ---

    private void setupEmailListView(ListView<Email> listView, boolean isOutbox, boolean isTrash) {
        listView.setCellFactory(param -> new ListCell<Email>() {
            @Override
            protected void updateItem(Email email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) {
                    setText(null); setGraphic(null);
                    setStyle(currentTheme == Theme.DARK ? "-fx-background-color: #333333;" : "");
                } else {
                    HBox cellBox = new HBox(10);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(cellBox, Priority.ALWAYS);

                    // Yıldız butonu (Çöp kutusunda gösterme)
                    if (!isTrash) {
                        Button starButton = new Button(email.isStarred() ? "🌟" : "☆");
                        starButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: " + (currentTheme == Theme.DARK ? "white" : "black") + "; -fx-font-size: 15px; -fx-padding: 5; -fx-border-color: transparent;");
                        starButton.setOnAction(e -> {
                            email.toggleStar();
                            listView.refresh();
                            dataService.saveUsers();
                        });
                        cellBox.getChildren().add(starButton);
                    }

                    // E-posta önizlemesi
                    Label textLabel = new Label(email.getPreview());
                    textLabel.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);
                    if (!email.isRead() && !isOutbox && !isTrash) {
                        textLabel.setStyle("-fx-font-weight: bold;");
                    }
                    if (isTrash) {
                        textLabel.setText(email.getSubject() + " (" + email.getSender() + ")");
                        textLabel.setTextFill(currentTheme == Theme.DARK ? Color.LIGHTGRAY : Color.DARKGRAY);
                        textLabel.setStyle("-fx-font-style: italic;");
                    }
                    cellBox.getChildren().add(textLabel);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    cellBox.getChildren().add(spacer);

                    if (isTrash) {
                        // Çöp Kutusu Butonları: Geri Yükle ve Kalıcı Sil
                        Button restoreBtn = new Button("♻");
                        restoreBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: green; -fx-cursor: hand; -fx-font-size: 16px;");
                        restoreBtn.setTooltip(new Tooltip("Geri Yükle"));
                        restoreBtn.setOnAction(e -> {
                            currentUser.restoreEmailFromTrash(email);
                            inboxEmails.add(email);
                            trashEmails.remove(email);
                            dataService.saveUsers();
                        });

                        Button deleteForever = new Button("❌");
                        deleteForever.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-cursor: hand; -fx-font-size: 16px;");
                        deleteForever.setTooltip(new Tooltip("Kalıcı Olarak Sil"));
                        deleteForever.setOnAction(e -> {
                            currentUser.getTrash().remove(email);
                            trashEmails.remove(email);
                            dataService.saveUsers();
                        });
                        cellBox.getChildren().addAll(restoreBtn, deleteForever);

                    } else {
                        // Normal Silme Butonu
                        Button deleteButton = new Button("🗑");
                        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + (currentTheme == Theme.DARK ? "#ff6b6b" : "red") + "; -fx-cursor: hand; -fx-font-size:20px;");
                        deleteButton.setTooltip(new Tooltip("Çöp Kutusuna Taşı"));
                        deleteButton.setOnAction(e -> {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("E-mail Sil");
                            alert.setHeaderText("Bu e-maili çöp kutusuna taşımak istediğinize emin misiniz?");
                            alert.setContentText(email.getSubject());

                            alert.showAndWait().ifPresent(resp -> {
                                if (resp == ButtonType.OK) {
                                    if (isOutbox) {
                                        currentUser.getOutbox().remove(email);
                                        outboxEmails.remove(email);
                                    } else {
                                        currentUser.getInbox().remove(email);
                                        inboxEmails.remove(email);
                                    }
                                    currentUser.addEmailToTrash(email);
                                    trashEmails.add(email);
                                    dataService.saveUsers();
                                    if(emailContentArea != null) emailContentArea.clear();
                                }
                            });
                        });
                        cellBox.getChildren().add(deleteButton);
                    }
                    setGraphic(cellBox);

                    // Okunmamış Arka Planı
                    if (!email.isRead() && !isTrash) {
                        setStyle("-fx-background-color: " + (currentTheme == Theme.DARK ? "#4a4a4a" : "#e8f4fc") + ";");
                    } else {
                        setStyle(currentTheme == Theme.DARK ? "-fx-background-color: #333333; -fx-text-fill: white;" : "");
                    }
                }
            }
        });
    }

    private void setupEmailContentArea(VBox container, Node title, ListView<Email> list) {
        Button openNewWindowBtn = new Button("Yeni Pencerede Aç ↗");
        Button replyBtn = new Button("Yanıtla ↩");
        styleActionButtons(openNewWindowBtn, replyBtn);

        emailContentArea = new TextArea();
        emailContentArea.setEditable(false);
        emailContentArea.setWrapText(true);
        emailContentArea.setPrefHeight(300);
        styleContentArea(emailContentArea);

        HBox actions = new HBox(10, replyBtn, openNewWindowBtn);
        actions.setAlignment(Pos.TOP_RIGHT);
        actions.setPadding(new Insets(10));

        StackPane stack = new StackPane(emailContentArea, actions);
        StackPane.setAlignment(actions, Pos.TOP_RIGHT);

        list.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                newVal.markAsRead();
                list.refresh();
                dataService.saveUsers();
                String content = "Kimden: " + newVal.getSender() + "\n" +
                        "Kime: " + newVal.getRecipient() + "\n" +
                        "Tarih: " + newVal.getTimestamp() + "\n" +
                        "Konu: " + newVal.getSubject() + "\n\n" +
                        newVal.getContent();
                emailContentArea.setText(content);
                openNewWindowBtn.setDisable(false);
                replyBtn.setDisable(false);
            } else {
                openNewWindowBtn.setDisable(true);
                replyBtn.setDisable(true);
            }
        });

        openNewWindowBtn.setOnAction(e -> openEmailInNewWindow(list.getSelectionModel().getSelectedItem()));
        replyBtn.setOnAction(e -> {
            Email sel = list.getSelectionModel().getSelectedItem();
            if (sel != null) {
                String reSub = "RE: " + sel.getSubject();
                String reBody = "\n\n--- Orijinal Mesaj ---\nKimden: " + sel.getSender() + "\nTarih: " + sel.getTimestamp() + "\n\n" + sel.getContent();
                showComposeScreen(sel.getSender(), reSub, reBody);
            }
        });

        container.getChildren().addAll(title, list, stack);
    }

    private void openEmailInNewWindow(Email email) {
        if (email == null) return;
        Stage stage = new Stage();
        stage.setTitle("BoosTMail - " + email.getSubject());
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Temaya göre arka plan
        String bgColor = (currentTheme == Theme.DARK) ? "#1e1e1e" : "#ffffff";
        root.setStyle("-fx-background-color: " + bgColor + ";");

        Label subjectLabel = new Label("Konu: " + email.getSubject());
        subjectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        Label senderLabel = new Label("Kimden: " + email.getSender());
        senderLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        Label dateLabel = new Label("Tarih: " + email.getTimestamp());
        dateLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 12));

        Color textColor = (currentTheme == Theme.DARK) ? Color.WHITE : Color.BLACK;
        subjectLabel.setTextFill(textColor);
        senderLabel.setTextFill(textColor);
        dateLabel.setTextFill(textColor);

        TextArea area = new TextArea(email.getContent());
        area.setWrapText(true);
        area.setEditable(false);
        styleContentArea(area);
        VBox.setVgrow(area, Priority.ALWAYS);

        // Yanıtla Butonu Ekleme
        Button replyBtnWin = new Button("Yanıtla ↩");
        replyBtnWin.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-cursor: hand;");
        replyBtnWin.setOnAction(e -> {
            stage.close();
            String reSub = "RE: " + email.getSubject();
            String reBody = "\n\n--- Orijinal Mesaj ---\nKimden: " + email.getSender() + "\nTarih: " + email.getTimestamp() + "\n\n" + email.getContent();
            showComposeScreen(email.getSender(), reSub, reBody);
        });

        root.getChildren().addAll(subjectLabel, senderLabel, dateLabel, area, replyBtnWin);
        stage.setScene(new Scene(root, 500, 400));

        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (Exception e) {}

        stage.show();
    }

    // --- TEMA VE GÖRÜNÜM ---

    private void styleContentArea(TextArea area) {
        if (currentTheme == Theme.DARK) {
            area.setStyle("-fx-control-inner-background: #333333; -fx-text-fill: white; -fx-font-size: 14px;");
        } else {
            area.setStyle("-fx-control-inner-background: white; -fx-text-fill: black; -fx-font-size: 14px;");
        }
    }

    private void styleActionButtons(Button... btns) {
        for(Button b : btns) {
            b.setDisable(true);
            String color = (currentTheme == Theme.LIGHT) ? "#3498db" : "#8B0000";
            b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-cursor: hand;");
        }
    }

    private void cycleTheme(Button themeButton) {
        if (currentTheme == Theme.RED_WHITE) {
            currentTheme = Theme.DARK;
            themeButton.setText("Tema: Karanlık");
        } else if (currentTheme == Theme.DARK) {
            currentTheme = Theme.LIGHT;
            themeButton.setText("Tema: Aydınlık");
        } else {
            currentTheme = Theme.RED_WHITE;
            themeButton.setText("Tema: Kırmızı-Beyaz");
        }
        applyTheme();

        // Ekranı yenile
        if (mainScreen.getCenter() instanceof VBox) {
            VBox center = (VBox) mainScreen.getCenter();
            if(!center.getChildren().isEmpty() && center.getChildren().get(0) instanceof Label) {
                Label title = (Label) center.getChildren().get(0);
                if(title.getText().contains("Gelen")) showInbox();
                else if(title.getText().contains("Gönderilen")) showOutbox();
                else if(title.getText().contains("Yıldız")) showStarredEmails();
                else if(title.getText().contains("Çöp")) showTrash();
            }
        }
    }

    private void applyTheme() {
        String mainBg, menuBg, topBarBg, loginBg, darkInput, lightInput, menuBtnStyle;
        darkInput = "-fx-control-inner-background: #333333; -fx-text-fill: white; -fx-prompt-text-fill: gray;";
        lightInput = "-fx-control-inner-background: white; -fx-text-fill: black; -fx-prompt-text-fill: gray;";
        String darkRedColor = "#8B0000";

        switch (currentTheme) {
            case DARK:
                mainBg = "#1e1e1e"; menuBg = "#1a252f"; topBarBg = "#2c3e50"; loginBg = "#121212";
                menuBtnStyle = "-fx-background-color: " + darkRedColor + "; -fx-text-fill: white;";
                updateLoginTheme(Color.WHITE, darkRedColor, Color.LIGHTGRAY, darkInput);
                break;
            case LIGHT:
                mainBg = "#f9f9f9"; menuBg = "#ecf0f1"; topBarBg = "#3498db"; loginBg = "#ffffff";
                menuBtnStyle = "-fx-background-color: #3498db; -fx-text-fill: white;";
                updateLoginTheme(Color.BLACK, "#3498db", Color.GRAY, lightInput);
                break;
            case RED_WHITE:
            default:
                mainBg = "#ffffff"; menuBg = "#34495e"; topBarBg = darkRedColor; loginBg = "#f0f0f0";
                menuBtnStyle = "-fx-background-color: " + darkRedColor + "; -fx-text-fill: white;";
                updateLoginTheme(Color.DARKBLUE, darkRedColor, Color.GRAY, lightInput);
                break;
        }

        if (mainScreen != null) mainScreen.setStyle("-fx-background-color: " + mainBg + ";");
        if (menuBox != null) menuBox.setStyle("-fx-background-color: " + menuBg + ";");
        if (topBar != null) topBar.setStyle("-fx-background-color: " + topBarBg + ";");
        if (loginScreen != null) loginScreen.setStyle("-fx-background-color: " + loginBg + ";");
        if (signinScreen != null) signinScreen.setStyle("-fx-background-color: " + loginBg + ";");
        if (rememberedScreen != null) rememberedScreen.setStyle("-fx-background-color: " + loginBg + ";");

        styleMenuButtons(menuBtnStyle, inboxButton, outboxButton, composeButton, starredButton, trashButton, settingsButton);
        if (inboxCount != null) inboxCount.setTextFill(currentTheme == Theme.LIGHT ? Color.BLACK : Color.WHITE);
        if (emailContentArea != null) styleContentArea(emailContentArea);
    }

    private void updateLoginTheme(Color textColor, String accentColor, Color subColor, String inputStyle) {
        if(loginBoosLabel != null) loginBoosLabel.setTextFill(textColor);
        if(loginTLabel != null) loginTLabel.setStyle("-fx-background-color: " + accentColor + "; -fx-text-fill: white; -fx-padding: 2 8 2 8; -fx-background-radius: 3;");
        if(loginMailLabel != null) loginMailLabel.setTextFill(textColor);
        if(loginSubtitle != null) loginSubtitle.setTextFill(subColor);
        if(loginUsernameField != null) loginUsernameField.setStyle(inputStyle);
        if(loginPasswordField != null) loginPasswordField.setStyle(inputStyle);
        if(loginPasswordTextField != null) loginPasswordTextField.setStyle(inputStyle);
        if(signupUsernameField != null) signupUsernameField.setStyle(inputStyle);
        if(signupPasswordField != null) signupPasswordField.setStyle(inputStyle);
        if(signupPasswordTextField != null) signupPasswordTextField.setStyle(inputStyle);

        if(loginLoginButton != null) loginLoginButton.setStyle("-fx-background-color: " + accentColor + "; -fx-text-fill: white; -fx-font-weight: bold;");
        if(signupSignupButton != null) signupSignupButton.setStyle("-fx-background-color: " + accentColor + "; -fx-text-fill: white; -fx-font-weight: bold;");

        if(loginSignUpLink != null) loginSignUpLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        if(signupLoginLink != null) signupLoginLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

        if(loginForgotPasswordLink != null) loginForgotPasswordLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: " + (currentTheme == Theme.LIGHT ? "#e74c3c" : "red") + ";");

        if(loginToggleButton != null) loginToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: " + (currentTheme == Theme.DARK ? "white" : "black") + "; -fx-background-color: transparent;");
        if(signupToggleButton != null) signupToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: " + (currentTheme == Theme.DARK ? "white" : "black") + "; -fx-background-color: transparent;");

        if(remTitleLabel != null) remTitleLabel.setTextFill(Color.web(currentTheme == Theme.LIGHT ? "#3498db" : (currentTheme == Theme.DARK ? "white" : "#3498db")));
        if(remSubtitleLabel != null) remSubtitleLabel.setTextFill(subColor);
        if(remNoAccountsLabel != null) remNoAccountsLabel.setTextFill(Color.GRAY);
    }

    private void styleMenuButtons(String style, Button... btns) {
        for(Button b : btns) if(b != null) b.setStyle(style + " -fx-font-weight: bold; -fx-cursor: hand;");
    }

    // --- DİĞER YARDIMCI METODLAR ---

    private void refreshData() {
        if (currentUser != null) {
            // Veriyi diskten tekrar çekebilmek için kullanıcıyı yeniden yükle
            User refreshedUser = dataService.findUserByEmail(currentUser.getUsername());
            if(refreshedUser != null) {
                currentUser = refreshedUser;
                inboxEmails.setAll(currentUser.getInbox());
                outboxEmails.setAll(currentUser.getOutbox());
                trashEmails.setAll(currentUser.getTrash());
                FXCollections.reverse(inboxEmails);
                FXCollections.reverse(outboxEmails);
                FXCollections.reverse(trashEmails);
                if (inboxCount != null) inboxCount.setText("Gelen Kutusu: " + inboxEmails.size() + " e-mail");
            }
        }
    }

    private void handleForgotPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Şifremi Unuttum");
        dialog.setHeaderText("Şifrenizi sıfırlamak için e-mail adresinizi girin.");
        dialog.setContentText("E-mail:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            User targetUser = dataService.findUserByEmail(email);
            if (targetUser != null) {
                Random rand = new Random();
                int verificationCode = 1000 + rand.nextInt(9000);

                Alert codeAlert = new Alert(Alert.AlertType.INFORMATION);
                codeAlert.setTitle("SMS Simülasyonu");
                codeAlert.setHeaderText("Telefonunuza gelen kod:");
                codeAlert.setContentText(String.valueOf(verificationCode));
                codeAlert.showAndWait();

                TextInputDialog codeDialog = new TextInputDialog();
                codeDialog.setTitle("Doğrulama Kodu");
                codeDialog.setHeaderText("Lütfen telefonunuza gelen 4 haneli kodu girin.");
                codeDialog.setContentText("Kod:");

                codeDialog.showAndWait().ifPresent(code -> {
                    if (code.equals(String.valueOf(verificationCode))) {
                        TextInputDialog passDialog = new TextInputDialog();
                        passDialog.setTitle("Yeni Şifre");
                        passDialog.setHeaderText("Kod doğrulandı! Lütfen yeni şifrenizi girin.");
                        passDialog.setContentText("Yeni Şifre:");
                        passDialog.showAndWait().ifPresent(newPass -> {
                            if(!newPass.trim().isEmpty()){
                                targetUser.setPassword(newPass);
                                dataService.saveUsers();
                                showAlert("Başarılı", "Şifreniz başarıyla güncellendi. Giriş yapabilirsiniz.");
                            } else {
                                showAlert("Hata", "Şifre boş olamaz.");
                            }
                        });
                    } else {
                        showAlert("Hata", "Hatalı kod girdiniz!");
                    }
                });
            } else {
                showAlert("Hata", "Bu e-mail adresiyle kayıtlı bir kullanıcı bulunamadı.");
            }
        });
    }

    private void selectProfileImage(ImageView target) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Profil Fotoğrafı Seç");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                Image original = new Image(file.toURI().toString());
                Image resized = ImageHelper.resizeImageTo4_3(original);
                target.setImage(resized);
                if (target == mainProfileImageView && currentUser != null) {
                    currentUser.setProfileImage(ImageHelper.imageToBase64(resized));
                    dataService.saveUsers();
                    showAlert("Başarılı", "Profil fotoğrafınız güncellendi!");
                }
            } catch (Exception e) {
                showAlert("Hata", "Resim yüklenirken hata oluştu: " + e.getMessage());
            }
        }
    }

    private String calculateTimeInfo(long remaining) {
        long days = TimeUnit.MILLISECONDS.toDays(remaining);
        long hours = TimeUnit.MILLISECONDS.toHours(remaining) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(remaining));

        if (days > 0) return String.format("\n[UYARI: Bu mesaj %d gün %d saat sonra kalıcı olarak silinecektir]\n", days, hours);
        return String.format("\n[UYARI: Bu mesaj %d saat %d dakika sonra kalıcı olarak silinecektir]\n", hours, minutes);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}