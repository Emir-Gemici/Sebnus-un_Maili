import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Bu sınıf JSON dosyalarını okuma, yazma ve veri tutarlılığını sağlama işlerini yürütür.
 * EmailClient sınıfını veri yükünden kurtarır.
 */
public class DataService {
    private final Gson gson;
    private List<User> users;
    private List<String> rememberedUsers;

    public DataService() {
        // PrettyPrinting sayesinde JSON okunabilir formatta kaydedilir
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.users = new ArrayList<>();
        this.rememberedUsers = new ArrayList<>();
        loadUsers();
        loadRememberedUsers();
    }

    public List<User> getUsers() {
        return users;
    }

    public List<String> getRememberedUsers() {
        return rememberedUsers;
    }

    // user.json dosyasından kullanıcıları yükler
    public void loadUsers() {
        File file = new File("user.json");

        if (!file.exists() || file.length() == 0) {
            this.users = new ArrayList<>();
            return;
        }

        try {
            String jsonContent = new String(Files.readAllBytes(file.toPath()));

            if (jsonContent == null || jsonContent.trim().isEmpty()) {
                this.users = new ArrayList<>();
                return;
            }

            String trimmedContent = jsonContent.trim();
            // Basit JSON format kontrolü
            if (!trimmedContent.startsWith("[") || !trimmedContent.endsWith("]")) {
                this.users = new ArrayList<>();
                saveUsers(); // Dosyayı sıfırla/düzelt
                return;
            }

            Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
            ArrayList<User> loadedData = gson.fromJson(jsonContent, userListType);

            if (loadedData != null) {
                this.users = loadedData;
                // Veri bütünlüğü kontrolü ve Oto Temizlik
                for (User u : this.users) {
                    u.getInbox();  // Null listeleri initialize et
                    u.getOutbox();
                    u.getTrash();
                    u.cleanupTrash(); // 30 günü geçenleri sil
                }
            } else {
                this.users = new ArrayList<>();
            }

        } catch (Exception e) {
            this.users = new ArrayList<>();
            saveUsers();
        }
    }

    // Kullanıcıları JSON dosyasına kaydeder
    public void saveUsers() {
        try (FileWriter writer = new FileWriter("user.json")) {
            gson.toJson(users, writer);
            // System.out.println("Kullanıcılar kaydedildi."); // Log kirliliği olmaması için kapalı
        } catch (IOException e) {
            System.err.println("Kullanıcı kaydetme hatası: " + e.getMessage());
        }
    }

    // remembered.json dosyasından hatırlanan kullanıcı adlarını yükler
    public void loadRememberedUsers() {
        File file = new File("remembered.json");
        if (!file.exists() || file.length() == 0) {
            this.rememberedUsers = new ArrayList<>();
            return;
        }
        try {
            String jsonContent = new String(Files.readAllBytes(file.toPath()));
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> loaded = gson.fromJson(jsonContent, listType);
            if (loaded != null) {
                this.rememberedUsers = loaded;
            } else {
                this.rememberedUsers = new ArrayList<>();
            }
        } catch (Exception e) {
            this.rememberedUsers = new ArrayList<>();
        }
    }

    // Hatırlanan kullanıcıları kaydeder
    public void saveRememberedUsers() {
        try (FileWriter writer = new FileWriter("remembered.json")) {
            gson.toJson(rememberedUsers, writer);
        } catch (IOException e) {
            System.err.println("Hatırlanan kullanıcılar kaydedilemedi: " + e.getMessage());
        }
    }

    // Giriş doğrulama
    public User authenticateUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.authenticate(password)) {
                return user;
            }
        }
        return null;
    }

    // E-mail adresine göre kullanıcı bulma (Örn: E-mail gönderirken alıcı kontrolü)
    public User findUserByEmail(String email) {
        for (User user : users) {
            if (user.getUsername().equals(email)) {
                return user;
            }
        }
        return null;
    }
}