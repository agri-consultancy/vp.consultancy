import com.example.vp.consultancy.entity.User;
import com.example.vp.consultancy.entity.UserRole;
import com.example.vp.consultancy.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRunnerDataLoader implements CommandLineRunner {

    private final UserService userService;

    public ApplicationRunnerDataLoader(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create default admin if not present
        if (!userService.findByMobile("7875513279")) {
            User admin = User.builder()
                    .mobile("7875513279")
                    .password("admin")
                    .role(UserRole.ADMIN)
                    .build();
            userService.createUser(admin);
            System.out.println("Default admin user created with mobile 7875513279 and password 'admin'");
        }
    }
}
